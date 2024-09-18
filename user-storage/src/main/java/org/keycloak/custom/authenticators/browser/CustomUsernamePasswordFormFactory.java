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

package org.keycloak.custom.authenticators.browser;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.quarkus.runtime.configuration.Configuration;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CustomUsernamePasswordFormFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "kbook-auth-username-password-form";
    public static final CustomUsernamePasswordForm SINGLETON = new CustomUsernamePasswordForm();

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return PasswordCredentialModel.TYPE;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "kbook Username Password Form";
    }

    @Override
    public String getHelpText() {
        return "Validates a username and password from login form.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(CustomUsernamePasswordForm.SITE_KEY);
        property.setLabel("Recaptcha Site Key");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Google Recaptcha Site Key" + Configuration.getOptionalValue("captchaSiteKey").orElse(""));

        property.setDefaultValue(Configuration.getOptionalValue("captchaSiteKey").orElse(""));
        CONFIG_PROPERTIES.add(property);

        property = new ProviderConfigProperty();
        property.setName(CustomUsernamePasswordForm.SITE_SECRET);
        property.setLabel("Recaptcha Secret");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Google Recaptcha Secret");

        property.setDefaultValue(Configuration.getOptionalValue("captchaSecret").orElse(""));
        CONFIG_PROPERTIES.add(property);

        property = new ProviderConfigProperty();
        property.setName(CustomUsernamePasswordForm.USE_RECAPTCHA_NET);
        property.setLabel("use recaptcha.net");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setHelpText("Use recaptcha.net? (or else google.com)");
        CONFIG_PROPERTIES.add(property);

        property = new ProviderConfigProperty();
        property.setName(CustomUsernamePasswordForm.START_RECAPTCHA_FROM);
        property.setLabel("Recaptcha - Login failed attempts(Number only)");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Number of the failed attempts to start recaptcha. ie. 3 means starting from 3 times failed attempts" + Configuration.getOptionalValue("captchaFailCnt").orElse("10"));
        property.setDefaultValue(Configuration.getOptionalValue("captchaFailCnt").orElse("10"));
        CONFIG_PROPERTIES.add(property);

        property = new ProviderConfigProperty();
        property.setName(CustomUsernamePasswordForm.VERIFIED_EMPLOYEE_REF);
        property.setLabel("employ ");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("kyobobook group ware : " + Configuration.getOptionalValue("groupWareDns").orElse("444"));
        property.setDefaultValue(Configuration.getOptionalValue("groupWareDns").orElse(""));
        CONFIG_PROPERTIES.add(property);
    }



}
