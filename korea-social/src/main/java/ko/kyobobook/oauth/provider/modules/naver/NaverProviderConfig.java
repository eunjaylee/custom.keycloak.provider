package ko.kyobobook.oauth.provider.modules.naver;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class NaverProviderConfig  extends OIDCIdentityProviderConfig {
    NaverProviderConfig() {}

    NaverProviderConfig(IdentityProviderModel identityProviderModel) {
        super(identityProviderModel);
    }

    public String getKeyId() {
        return getConfig().get("keyId");
    }

    public String getTeamId() {
        return getConfig().get("teamId");
    }
}
