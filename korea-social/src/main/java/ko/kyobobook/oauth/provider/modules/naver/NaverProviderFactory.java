package ko.kyobobook.oauth.provider.modules.naver;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

public class NaverProviderFactory extends AbstractIdentityProviderFactory<NaverProvider> implements SocialIdentityProviderFactory<NaverProvider> {

    public static final String PROVIDER_ID = "naver";

    @Override
    public String getName() {
        return "Naver";
    }

    @Override
    public NaverProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new NaverProvider(session, new OAuth2IdentityProviderConfig(model));
    }

    @Override
    public OAuth2IdentityProviderConfig createConfig() {
        return new OAuth2IdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
