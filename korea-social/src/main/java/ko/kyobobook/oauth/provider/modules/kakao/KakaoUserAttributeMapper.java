package ko.kyobobook.oauth.provider.modules.kakao;

import org.keycloak.broker.oidc.mappers.UsernameTemplateMapper;

public class KakaoUserAttributeMapper extends UsernameTemplateMapper {
    private static final String[] cp = new String[] { KakaoProviderFactory.PROVIDER_ID };

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return "kakao-user-attribute-mapper";
    }
}
