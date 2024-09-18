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

package org.keycloak.custom.authenticators.broker;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.custom.authenticators.browser.CustomUsernamePasswordForm;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

import java.util.Optional;

import static org.keycloak.services.validation.Validation.FIELD_PASSWORD;

/**
 * Same like classic username+password form, but for use in IdP linking.
 *
 * User identity is optionally established by the preceding idp-create-user-if-unique execution.
 * In this case username field will be pre-filled (but still changeable).
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ReAuthUsernamePasswordForm extends CustomUsernamePasswordForm {

    private static final Logger logger = Logger.getLogger(ReAuthUsernamePasswordForm.class);

    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return setupForm(context, formData, getExistingUser(context))
                .setStatus(Response.Status.OK)
                .createLoginUsernamePassword();
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        Optional<UserModel> existingUser = getExistingUser(context);
        existingUser.ifPresent(context::setUser);

        boolean result = validateUserAndPassword(context, formData);

        // Restore formData for the case of error
        setupForm(context, formData, existingUser);

        return result;
    }

    protected LoginFormsProvider setupForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData, Optional<UserModel> existingUser) {
        SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(context.getAuthenticationSession(), AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
        if (serializedCtx == null) {
            throw new AuthenticationFlowException("Not found serialized context in clientSession", AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
        }

        existingUser.ifPresent(u -> formData.putSingle(AuthenticationManager.FORM_USERNAME, u.getUsername()));

        logger.debugf("+++++++++++++++ token : %s", serializedCtx.getToken());


        String[] idList = new String[]{};
        if ( "kakao".equals(serializedCtx.getIdentityProviderId()) || "naver".equals(serializedCtx.getIdentityProviderId())) {
            idList = getMatchCiUserId(context.getSession(), "11111");
        }

        // TODO
        AuthenticatorConfigModel authenticatorConfig = context.getRealm().getAuthenticatorConfigByAlias("ndev-setting");
        authenticatorConfig.getConfig().keySet().forEach(key -> System.out.println("Key :" + key));

        LoginFormsProvider form = context.form()
                .setFormData(formData)
                .setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true)
                .setAttribute("token", serializedCtx.getToken())
                .setAttribute("provider", serializedCtx.getIdentityProviderId())
                .setInfo(Messages.FEDERATED_IDENTITY_CONFIRM_REAUTHENTICATE_MESSAGE, serializedCtx.getIdentityProviderId());

        SerializedBrokeredIdentityContext serializedCtx0 = SerializedBrokeredIdentityContext.readFromAuthenticationSession(context.getAuthenticationSession(), AbstractIdpAuthenticator.NESTED_FIRST_BROKER_CONTEXT);

        if (serializedCtx0 != null) {
            BrokeredIdentityContext ctx0 = serializedCtx0.deserialize(context.getSession(), context.getAuthenticationSession());
            form.setError(Messages.NESTED_FIRST_BROKER_FLOW_MESSAGE, ctx0.getIdpConfig().getAlias(), ctx0.getUsername());
            context.getAuthenticationSession().setAuthNote(AbstractIdpAuthenticator.NESTED_FIRST_BROKER_CONTEXT, null);
        }

        return form;
    }


    // Set up AuthenticationFlowContext error.
    private boolean badPasswordHandler(AuthenticationFlowContext context, UserModel user, boolean clearUser,boolean isEmptyPassword) {
        context.getEvent().user(user);
        context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);

        SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(context.getAuthenticationSession(), AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
        if (serializedCtx == null) {
            throw new AuthenticationFlowException("Not found serialized context in clientSession", AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
        }

        String[] idList = new String[]{};
        if ( "kakao".equals(serializedCtx.getIdentityProviderId()) || "naver".equals(serializedCtx.getIdentityProviderId())) {
            idList = getMatchCiUserId(context.getSession(), "11111");
        }

        AuthenticatorConfigModel authenticatorConfig = context.getRealm().getAuthenticatorConfigByAlias("ndev-setting");

        UserLoginFailureModel model = context.getSession().loginFailures().getUserLoginFailure(context.getRealm(), user.getId());

        if (isUserAlreadySetBeforeUsernamePasswordAuth(context)) {
            LoginFormsProvider form = context.form();
            form.setAttribute(LoginFormsProvider.USERNAME_HIDDEN, true);
            form.setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true)
                 .setAttribute("token", serializedCtx.getToken())
                .setAttribute("provider", serializedCtx.getIdentityProviderId())
                .setAttribute("ciMatchIdList", idList)

                .setAttribute("numFailures", model == null ? 0 : model.getNumFailures())
                .setAttribute("lastFailure", model == null ? 0 : model.getLastFailure())

                .setAttribute("homeBaseUrl", authenticatorConfig.getConfig().get("homeBaseUrl"))
                .setAttribute("mmbrBaseUrl", authenticatorConfig.getConfig().get("mmbrBaseUrl"))
                .setAttribute("ordBaseUrl", authenticatorConfig.getConfig().get("ordBaseUrl"))
                .setAttribute("cloudgwBaseUrl", authenticatorConfig.getConfig().get("cloudgwBaseUrl"))
                .setInfo(Messages.FEDERATED_IDENTITY_CONFIRM_REAUTHENTICATE_MESSAGE, serializedCtx.getIdentityProviderId());;
        }

        Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_PASSWORD);
        if(isEmptyPassword) {
            context.forceChallenge(challengeResponse);
        }else{
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
        }

        if (clearUser) {
            context.clearUser();
        }
        return false;
    }

    @Override
    public boolean validatePassword(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData, boolean clearUser) {
        String password = inputData.getFirst(CredentialRepresentation.PASSWORD);
        if (password == null || password.isEmpty()) {
            return this.badPasswordHandler(context, user, clearUser,true);
        }

        if (isDisabledByBruteForce(context, user)) return false;

        if (password != null && !password.isEmpty() && user.credentialManager().isValid(UserCredentialModel.password(password))) {
            context.getAuthenticationSession().setAuthNote(AuthenticationManager.PASSWORD_VALIDATED, "true");
            return true;
        } else {
            return this.badPasswordHandler(context, user, clearUser,false);
        }
    }

    private String[] getMatchCiUserId(KeycloakSession session, String ci) {
          // TODO
//        EntityManager em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
//        TypedQuery<KbookSocialEntity> query = em.createNamedQuery("getCiMatchIds", KbookCrttLnkg.class);
//        query.setParameter("ci", ci);
//
//        List<KbookSocialEntity> result = query.getResultList();
//        if (result.isEmpty()) return new String[]{};
//        return (String[]) result.stream().map(KbookCrttLnkg::getUsername).toArray();


        return new String[]{"test1","test2","test3" };
//
//        idList.add("test1");
//        idList.add("test2");
//        idList.add("test3");
//
//        return idList;
    }


    private Optional<UserModel> getExistingUser(AuthenticationFlowContext context) {
        try {
            return Optional.of(AbstractIdpAuthenticator.getExistingUser(context.getSession(), context.getRealm(), context.getAuthenticationSession()));
        } catch (AuthenticationFlowException ex) {
            log.debug("No existing user in authSession", ex);
            return Optional.empty();
        }
    }
}
