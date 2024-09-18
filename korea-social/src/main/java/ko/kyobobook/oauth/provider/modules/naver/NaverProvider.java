package ko.kyobobook.oauth.provider.modules.naver;

import com.fasterxml.jackson.databind.JsonNode;
import ko.kyobobook.oauth.provider.modules.apple.AppleIdentityProvider;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;


import org.jboss.logging.Logger;


public class NaverProvider  extends AbstractOAuth2IdentityProvider implements SocialIdentityProvider {

    private static Logger logger = Logger.getLogger(NaverProvider.class);

    public static final String AUTH_URL = "https://nid.naver.com/oauth2.0/authorize";
    public static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    public static final String PROFILE_URL = "https://openapi.naver.com/v1/nid/me";
    public static final String DEFAULT_SCOPE = "basic";

    public NaverProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
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

//        BrokeredIdentityContext user = this.getFederatedIdentity(profile.get("response").asText());

        logger.debugf("naver broker json = %s", profile.toString());


        BrokeredIdentityContext user = new BrokeredIdentityContext("naver." + profile.get("response").get("id").asText(), getConfig());
        user.setBrokerUserId("naver." + profile.get("response").get("id").asText());

        logger.debugf("naver broker userId = %s", user.getBrokerUserId());

        logger.debugf("naver profile = %s", profile.get("response").toString());

        String email = profile.get("response").get("email").asText();

//        user.setIdpConfig(getConfig());
        user.setLastName(profile.get("response").get("name").asText());

        user.setEmail(email);
//        user.setUserAttribute("ci",               profile.get("response").has("ci") ? profile.get("response").get("ci").asText() : "");
        user.setUserAttribute("birthday",         profile.get("response").get("birthday").asText());
        user.setUserAttribute("ageRange",         profile.get("response").has("age") ? profile.get("response").get("age").asText() : "");
        user.setUserAttribute("profileImageUrl",  profile.get("response").has("profile_image") ? profile.get("response").get("profile_image").asText() : "");
        user.setUserAttribute("gender",           profile.get("response").has("gender") ? profile.get("response").get("gender").asText() : "");
        user.setUserAttribute("nickname",         profile.get("response").has("nickname") ? profile.get("response").get("nickname").asText() : "");

        user.setIdp(this);


//        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "userid"));
//
//        user.setUsername(getJsonProperty(profile, "userid"));
//        user.setName(getJsonProperty(profile, "realname"));
//        user.setIdpConfig(getConfig());
//        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;

    }


    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            JsonNode profile = SimpleHttp.doGet(PROFILE_URL, session).param("access_token", accessToken).asJson();

            return extractIdentityFromProfile(null, profile);
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from naver.", e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

}
