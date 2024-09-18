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

package org.keycloak.custom.authenticators.broker;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.*;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * Same like classic username+password form, but for use in IdP linking.
 *
 * User identity is optionally established by the preceding idp-create-user-if-unique execution.
 * In this case username field will be pre-filled (but still changeable).
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SocialConfirmForm extends AbstractUsernameFormAuthenticator  implements Authenticator {

    private static final Logger logger = Logger.getLogger(SocialConfirmForm.class);

    @Override
    public void action(AuthenticationFlowContext context) {
        context.success();
    }


    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.challenge(challenge(context));
    }

    protected Response challenge(AuthenticationFlowContext context) {
        String userEmail = context.getUser().getFirstAttribute("email");
        logger.debugf("userEmail : %s", userEmail);

        AuthenticatorConfigModel authenticatorConfig = context.getRealm().getAuthenticatorConfigByAlias("ndev-setting");

        String redirectLink = authenticatorConfig.getConfig().get("redirectLink");
        // 기업의 경우 social 로그인시 도메인이 해당 기업 도메인과 일치해야만 통과하게 커스텀 할 수 있다.
        return context.form()
                .setAttribute("socialLinkDenyMsg", "허용되지 않은 이메일 계정입니다.")
                .setAttribute("redirectLink", redirectLink)
                .createForm("condition-confirm.ftl");
    }


    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }
}
