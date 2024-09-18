/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.custom.authenticators.browser;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.ws.rs.core.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.custom.storage.user.SocialProviderLink;
import org.keycloak.custom.storage.user.UserEntity;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpResponse;

import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;

import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.util.JsonSerialization;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.keycloak.utils.StringUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CustomUsernamePasswordForm extends AbstractUsernameFormAuthenticator implements Authenticator {
    protected static ServicesLogger log = ServicesLogger.LOGGER;
    public static final String G_RECAPTCHA_RESPONSE = "g-recaptcha-response";
    public static final String RECAPTCHA_REFERENCE_CATEGORY = "recaptcha";
    public static final String SITE_KEY = "site.key";
    public static final String SITE_SECRET = "secret";
    public static final String USE_RECAPTCHA_NET = "useRecaptchaNet";
    public static final String START_RECAPTCHA_FROM = "startRecaptchaFrom";

    public static final String VERIFIED_EMPLOYEE_REF = "verifiedEmployeeRef";
    public static final int defaultRecaptchCnt = 5;


    private ObjectMapper objectMapper = new ObjectMapper();

    public static final String LOGIN_TIMEOUT = "loginTimeout";

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (!validateForm(context, formData)) {
            return;
        }

        context.success();
    }

    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validateUserAndPassword(context, formData);
    }



    protected void setCustomEventLog(AuthenticationFlowContext context, EventBuilder userEvent) {

        HttpHeaders headers = context.getHttpRequest().getHttpHeaders();
        String userAgentString = headers.getHeaderString(HttpHeaders.USER_AGENT);

        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        log.debug("=================================");
        log.debugf("userAgent : %s", userAgentString);
        log.debug("=================================");

        userEvent.detail("Browser", userAgent.getBrowser().getName() + " " + userAgent.getBrowserVersion());
        userEvent.detail("os", userAgent.getOperatingSystem().getName());
        userEvent.detail("DeviceType", userAgent.getOperatingSystem().getDeviceType().getName());
    }


    // TODO 한곳에서 만들어서 CustomUserForm에서도 적용해야 함
    // Spring에서 제공하는 암호화 기법을 사용했을때 적용
    private PasswordEncoder passwordEncoder() {
        String idForEncode = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(idForEncode, new BCryptPasswordEncoder());
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

    @Override
    public boolean validatePassword(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData, boolean clearUser) {
        log.debug("========== validatePassword started..... ");
        String password = (String)inputData.getFirst("password");
        String captcha  =  (String)inputData.getFirst(G_RECAPTCHA_RESPONSE);

        String secret   = null;
        Map<String, String> config = null;
        if(context.getAuthenticatorConfig() != null) {
            config = context.getAuthenticatorConfig().getConfig();
            secret = config.get(SITE_SECRET);
        }

        UserLoginFailureModel model = context.getSession().loginFailures().getUserLoginFailure(context.getRealm(), user.getId());
        boolean captchaSuccess = true;

        // 실패 횟수에 따른 리캡차 처리 여부
        if(model != null && config !=null) {
            log.debugf("========== model is not null..... ");
            int numFailures = model.getNumFailures();

            int recaptchaFailCnt = rechptchShowCount(config); // default : 3;

            if (numFailures >= recaptchaFailCnt) {
                captchaSuccess = validateRecaptcha(context, captcha, secret);
            }
        }

        // CI 매칭된 내용이 있어 ID선택을 이미 한 경우 화면에 버튼을 감춰야 함..
        String selectExistingAccountYn = (String)inputData.getFirst("selectExistingAccountYn");
        if (StringUtil.isNotBlank(selectExistingAccountYn) && "Y".equals(selectExistingAccountYn)) {
            context.form().setAttribute("selectExistingAccountYn", selectExistingAccountYn);
        }

        // 비밀번호 체크를 할것임
        if (password != null && !password.isEmpty() && captchaSuccess) {
            // 잠긴 회원은 항상 실패
            if (this.isDisabledByBruteForce(context, user)) {
                return false;
            } else {
                boolean result = user.credentialManager().isValid(new CredentialInput[]{UserCredentialModel.password(password)});
                if(result) {

                    return true;
                }

                return badPasswordHandler(context, user, clearUser, false, captchaSuccess);
            }
        } else {
          //  setCustomEventLog(context);
            // todo : 패스워드 없이 요청해도 항상 실패건수가 증가하도록
            return badPasswordHandler(context, user, clearUser, false, captchaSuccess);
        }
    }


    protected void setCookie(AuthenticationFlowContext context, String username) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        int maxCookieAge = 60 * 60 * 24 * 30; // 30 days

        URI uri = context.getUriInfo().getBaseUriBuilder().path("realms").path(context.getRealm().getName()).build();
        addCookie(context, "username", username,
                uri.getRawPath(),
                null, null,
                maxCookieAge,
                false, true);
    }

    public void addCookie(AuthenticationFlowContext context, String name, String value, String path, String domain, String comment, int maxAge, boolean secure, boolean httpOnly) {
        HttpResponse response = context.getSession().getContext().getHttpResponse();
//        response.setCookieIfAbsent(new NewCookie(name, value, path, domain, comment, maxAge, secure, httpOnly, null));
        response.setCookieIfAbsent(new NewCookie(name, value));
    }

    private String getRecaptchaDomain(AuthenticatorConfigModel config) {
        Boolean useRecaptcha = Optional.ofNullable(config)
                .map(configModel -> configModel.getConfig())
                .map(cfg -> Boolean.valueOf(cfg.get(USE_RECAPTCHA_NET)))
                .orElse(false);
        if (useRecaptcha) {
            return "recaptcha.net";
        }

        return "google.com";
    }

    private int rechptchShowCount(Map<String, String> config) {
        try{
            return Integer.parseInt(config.get(START_RECAPTCHA_FROM));
        }catch (Exception e) {
            log.error("recaptcha start count numberexception ", e);
        }
        return defaultRecaptchCnt;
    }


    @Override
    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        if (error != null) {
            if (field != null) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }

        makeForm(context, context.form());
        return createLoginForm(form);
    }

    private boolean badPasswordHandler(AuthenticationFlowContext context, UserModel user, boolean clearUser, boolean isEmptyPassword, boolean captchaSuccess) {
        EventBuilder userEvent = context.getEvent().user(user);
        context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);

        setCustomEventLog(context, userEvent);

        log.debugf("========== numFailures user-getId() %s", user.getId());

        UserLoginFailureModel model = context.getSession().loginFailures().getUserLoginFailure(context.getRealm(), user.getId());

        LoginFormsProvider form1 = context.form();
        form1.setAttribute("recaptchaRequired", false);

        AuthenticatorConfigModel captchaConfig = context.getAuthenticatorConfig();

        if(captchaConfig == null) {
            captchaConfig = context.getRealm().getAuthenticatorConfigByAlias("ndev-setting");
        }

        Map<String, String> config = null;
        if(context.getAuthenticatorConfig() != null) {
            config = context.getAuthenticatorConfig().getConfig();
        }

        String userLanguageTag = context.getSession().getContext().resolveLocale(context.getUser()).toLanguageTag();


        if (model !=null && config != null) {
            int numFailures = model.getNumFailures();
            int recaptchaFailCnt = rechptchShowCount(config); // default : 5;
            log.debug("========== recaptcha count..... "+recaptchaFailCnt);
            log.debugf("========== START_RECAPTCHA_FROM %s", config.get(START_RECAPTCHA_FROM));
            if (numFailures >= recaptchaFailCnt) {
                String siteKey = config.get(SITE_KEY);
                String recaptchaJsUrl = "https://www." + getRecaptchaDomain(captchaConfig) + "/recaptcha/api.js?hl=" + userLanguageTag;
                log.debugf("========== SITE_KEY %s", SITE_KEY);

                form1.setAttribute("recaptchaJsUrl", recaptchaJsUrl);
                form1.setAttribute("captchaSuccess", captchaSuccess);
                form1.setAttribute("recaptchaRequired", true);
                form1.setAttribute("recaptchaSiteKey", siteKey);


                form1.addScript("https://www." + getRecaptchaDomain(captchaConfig) + "/recaptcha/api.js?hl=" + userLanguageTag);
            }
        }

        if(model != null) {
            log.debugf("========== numFailures %s", model.getNumFailures());
            form1.setAttribute("numFailures", model.getNumFailures());
            form1.setAttribute("lastFailure", model.getLastFailure());

            userEvent.detail("numFailures", model.getNumFailures()+"");
            userEvent.detail("lastFailure", model.getLastFailure()+"");

            userEvent.success();

            int numFailures = model.getNumFailures();
            log.debug("========== numFailures..... "+numFailures);
            form1.setAttribute("numFailures", numFailures);
        }

        if (this.isUserAlreadySetBeforeUsernamePasswordAuth(context)) {
            LoginFormsProvider form = context.form();
            form.setAttribute("usernameHidden", true);
            form.setAttribute("registrationDisabled", true);
        }

        makeForm(context, context.form());
        Response challengeResponse = this.challenge(context, this.getDefaultChallengeMessage(context), "password");


        if (isEmptyPassword) {
            context.forceChallenge(challengeResponse);
        } else {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
        }

        if (clearUser) {
            context.clearUser();
        }

        return false;
    }


    /**
     * 구글 리캡차 입력값 검증 프로세스
     * @param context
     * @param captcha
     * @param secret
     * @return
     */

    protected boolean validateRecaptcha(AuthenticationFlowContext context, String captcha, String secret) {
        boolean success = false;

        CloseableHttpClient httpClient = context.getSession().getProvider(HttpClientProvider.class).getHttpClient();
        HttpPost post = new HttpPost("https://www." + getRecaptchaDomain(context.getAuthenticatorConfig()) + "/recaptcha/api/siteverify");
        List<NameValuePair> formparams = new LinkedList<>();
        formparams.add(new BasicNameValuePair("secret", secret));
        formparams.add(new BasicNameValuePair("response", captcha));
        formparams.add(new BasicNameValuePair("remoteip", context.getConnection().getRemoteAddr()));
        try {
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                InputStream content = response.getEntity().getContent();
                try {
                    Map json = JsonSerialization.readValue(content, Map.class);
                    Object val = json.get("success");
                    success = Boolean.TRUE.equals(val);
                } finally {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
            }
        } catch (Exception e) {
            ServicesLogger.LOGGER.recaptchaFailed(e);
        }
        return success;
    }


    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

        String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getSession());

        if (isLoginTimeout(context)) context.form().setAttribute("loginTimeoutYN", "Y");

        if (context.getUser() != null) {
            log.debugf("========== user is : %s", context.getUser().getId());
            LoginFormsProvider form = context.form();
            form.setAttribute(LoginFormsProvider.USERNAME_HIDDEN, true);
            form.setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true);

            context.getAuthenticationSession().setAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH, "true");
        } else {
            log.debug("========== user is null");
            context.getAuthenticationSession().removeAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);
            if (loginHint != null || rememberMeUsername != null) {
                if (loginHint != null) {
                    formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
                } else {
                    formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
                    formData.add("rememberMe", "on");
                }
            }
        }


        Response challengeResponse = challenge(context, formData);
        context.challenge(challengeResponse);
    }

    private boolean isLoginTimeout(AuthenticationFlowContext context) {
        String message = Optional.ofNullable(context.getForwardedErrorMessage())
                .map(FormMessage::getMessage)
                .orElse(null);

        return StringUtils.equals(LOGIN_TIMEOUT, message);
    }

    public void makeForm(AuthenticationFlowContext context, LoginFormsProvider form) {
        AuthenticatorConfigModel captchaConfig = context.getAuthenticatorConfig();

        if(captchaConfig == null) {
            captchaConfig = context.getRealm().getAuthenticatorConfigByAlias("ndev-setting");
        }
        SerializedBrokeredIdentityContext serializedCtx =
                SerializedBrokeredIdentityContext
                        .readFromAuthenticationSession(context.getAuthenticationSession(), AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);

        if (serializedCtx != null) {
            List<SocialProviderLink> idList = new ArrayList<SocialProviderLink>();
            try {
                log.debugf(" find email user ====== %s ", serializedCtx.getFirstAttribute("email"));

                if (getEmailUser(context.getSession(), serializedCtx.getFirstAttribute("email"))) {
                    form.setAttribute("existingMailUserYN", "Y");
                } else {
                    form.setAttribute("existingMailUserYN", "N");
                }

                if ( "kakao".equals(serializedCtx.getIdentityProviderId()) || "naver".equals(serializedCtx.getIdentityProviderId())) {
                    log.debugf(" find ci info ====== %s - %s", serializedCtx.getIdentityProviderId(), serializedCtx.getFirstAttribute("ci"));
                    idList = getMatchCiUserId(context.getSession(), serializedCtx.getFirstAttribute("ci"));

                    LocalDate birthDay = LocalDate.parse(serializedCtx.getFirstAttribute("birthday"));

                    log.debugf(" find birthDay user ====== %s ", birthDay.toString());

                    LocalDate underAge = LocalDate.now().minusYears(14);

                }
            } catch(Exception e) {
                e.printStackTrace();
            }


        }

        String verifiedEmployeeRef = captchaConfig.getConfig().get(VERIFIED_EMPLOYEE_REF);

        form.setAttribute("verifiedEmployeeRef", verifiedEmployeeRef);
    }

    private boolean getEmailUser(KeycloakSession session, String email) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByEmail", UserEntity.class);
        query.setParameter("email", email);

        List<UserEntity> result = query.getResultList();
        if (result.isEmpty()) return false;
        return true;
    }


    private void mmbrLinkSocial(SerializedBrokeredIdentityContext serializedCtx, UserModel user) {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost request = new HttpPost("http://localhost:8888");

            Map<String, String> jsonData = new HashMap<>();

            String[] brockerId = serializedCtx.getBrokerUserId().split("\\.");

            if(brockerId.length == 2) {
                String provider = brockerId[1];
                String providerLinkId = brockerId[1];
                jsonData.put("sns_api_key_wrth", providerLinkId);                  // SNSAPIKEY
                jsonData.put("sns_dvsn_code", provider);                  // SNS구분코드 001, 002, 003
            }

            String mmbrNum[] = user.getId().split(":");

            String gender = "101";

            if("F".equals(serializedCtx.getFirstAttribute("gender"))) gender = "102";
            else if("".equals(serializedCtx.getFirstAttribute("gender"))) gender = "103";

            JsonNode jsonNode = objectMapper.readTree(serializedCtx.getToken());

            // ObjectMapper를 사용하여 JSON 문자열로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(jsonData);

            // 요청에 JSON 데이터 추가
            StringEntity entity = new StringEntity(json);

            log.debugf("sns data : %s", json);

            request.setEntity(entity);
            request.setHeader("Content-type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String result = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<SocialProviderLink> getMatchCiUserId(KeycloakSession session, String ci) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
        TypedQuery<SocialProviderLink> query = em.createNamedQuery("getCiMatchIds", SocialProviderLink.class);
        query.setParameter("ci", ci);

        List<SocialProviderLink> result = query.getResultList();
        if (result.isEmpty()) return new ArrayList<SocialProviderLink>();
        return result;
    }


    @Override
    public boolean requiresUser() {
        return false;
    }

    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {

        makeForm(context, context.form());

        LoginFormsProvider forms = context.form();

        if (!formData.isEmpty()) forms.setFormData(formData);

        return forms.createLoginUsernamePassword();
    }


    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // never called
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // never called
    }

    @Override
    public void close() {

    }

}