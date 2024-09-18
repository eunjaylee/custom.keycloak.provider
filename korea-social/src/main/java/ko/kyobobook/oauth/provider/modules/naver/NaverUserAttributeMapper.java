package ko.kyobobook.oauth.provider.modules.naver;

import org.keycloak.broker.oidc.mappers.UsernameTemplateMapper;

public class NaverUserAttributeMapper extends UsernameTemplateMapper {
    private static final String[] cp = new String[] { NaverProviderFactory.PROVIDER_ID };

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return "naver-user-attribute-mapper";
    }
}
