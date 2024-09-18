package ko.kyobobook.oauth.provider.modules.kakao;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

public class KakaoProviderFactory extends AbstractIdentityProviderFactory<KakaoProvider> implements SocialIdentityProviderFactory<KakaoProvider> {

    public static final String PROVIDER_ID = "kakao";

    @Override
    public String getName() {
        return "Kakao";
    }

    @Override
    public KakaoProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new KakaoProvider(session, new OAuth2IdentityProviderConfig(model));
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
