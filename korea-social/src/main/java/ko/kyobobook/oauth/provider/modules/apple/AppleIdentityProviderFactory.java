package ko.kyobobook.oauth.provider.modules.apple;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;

public class AppleIdentityProviderFactory extends AbstractIdentityProviderFactory<AppleIdentityProvider> implements SocialIdentityProviderFactory<AppleIdentityProvider> {

    public static final String PROVIDER_ID = "apple";

    @Override
    public String getName() {
        return "Apple";
    }

    @Override
    public AppleIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new AppleIdentityProvider(session, new AppleIdentityProviderConfig(model));
    }

    @Override
    public AppleIdentityProviderConfig createConfig() {
        return new AppleIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName("teamId");
        property.setLabel("Team ID");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Your 10-character Team ID obtained from your Apple developer account.");

        CONFIG_PROPERTIES.add(property);

        property = new ProviderConfigProperty();
        property.setName("keyId");
        property.setLabel("Key ID");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("A 10-character key identifier obtained from your Apple developer account.");

        CONFIG_PROPERTIES.add(property);

        property = new ProviderConfigProperty();
        property.setName("p8Content");
        property.setLabel("p8 Key");
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setHelpText("Raw content of Apple's p8 key file. Example (without quotes): \\\"-----BEGIN PRIVATE KEY-----!CONTENT!-----END PRIVATE KEY-----\\\" (may contain line-breaks '\\\\\\\\n' as well).");
        property.setSecret(true);
        CONFIG_PROPERTIES.add(property);
    }
}