package ko.kyobobook.oauth.provider.modules.kakao;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class KakaoProviderConfig extends OIDCIdentityProviderConfig {
    KakaoProviderConfig() {}

    KakaoProviderConfig(IdentityProviderModel identityProviderModel) {
        super(identityProviderModel);
    }

    public String getKeyId() {
        return getConfig().get("keyId");
    }

    public String getTeamId() {
        return getConfig().get("teamId");
    }
}
