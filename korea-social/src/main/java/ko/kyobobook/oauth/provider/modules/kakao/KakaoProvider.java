package ko.kyobobook.oauth.provider.modules.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;

public class KakaoProvider  extends AbstractOAuth2IdentityProvider implements SocialIdentityProvider {

    public static final String AUTH_URL = "https://kauth.kakao.com/oauth/authorize";
    public static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    public static final String PROFILE_URL = "https://kapi.kakao.com/v2/user/me";
//    public static final String DEFAULT_SCOPE = "basic";

    private static final Logger logger = Logger.getLogger(KakaoProvider.class);

    public KakaoProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return PROFILE_URL;
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        BrokeredIdentityContext user = new BrokeredIdentityContext("kakao." + getJsonProperty(profile, "id"), getConfig());

        logger.debugf("kakao broker json = %s", profile.toString());

        user.setBrokerUserId("kakao." + getJsonProperty(profile, "id"));

        JsonNode kakaoAccount = profile.get("kakao_account");

        user.setUsername(kakaoAccount.get("name").asText());
        user.setEmail(kakaoAccount.get("email").asText());

        String birthday = kakaoAccount.get("birthyear").asText() + "-" +kakaoAccount.get("birthday").asText().substring(0, 2) + "-" + kakaoAccount.get("birthday").asText().substring(2);

        logger.debugf("kakao birthday = %s", birthday);

//        user.setUserAttribute("ci", kakaoAccount.get("ci").asText());
        user.setUserAttribute("birthday", birthday);
        user.setUserAttribute("nickname", profile.has("nickname") ? profile.get("nickname").asText() : "");
        user.setUserAttribute("gender",   profile.has("nickname") ? profile.get("gender").asText() : "");
        user.setUserAttribute("ageRange", profile.has("nickname") ? profile.get("age_range").asText() : "");
        user.setUserAttribute("profileImageUrl", profile.has("nickname") ? profile.get("profile_image_url").asText() : "");

//        user.setIdpConfig();
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }


    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            BrokeredIdentityContext identity = extractIdentityFromProfile(null, doHttpGet(PROFILE_URL, accessToken));

            logger.debug("=======================");
            logger.debug(identity.toString());
            logger.debug("=======================");
            identity.setEmail(identity.getEmail());

            if (identity.getUsername() == null || identity.getUsername().isEmpty()) {
                identity.setUsername(identity.getEmail());
            }

            return identity;
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from kakao.", e);
        }
    }


    @Override
    protected String getDefaultScopes() {
        return "";
    }


    private JsonNode doHttpGet(String url, String bearerToken) throws IOException {
        JsonNode response = SimpleHttp.doGet(url, session).header("Authorization", "Bearer " + bearerToken).asJson();

        if (response.hasNonNull("serviceErrorCode")) {
            throw new IdentityBrokerException("Could not obtain response from [" + url + "]. Response from server: " + response);
        }

        return response;
    }

}
