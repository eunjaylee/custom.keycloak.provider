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

import io.jsonwebtoken.JwtParser;
import jakarta.ws.rs.core.Cookie;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.util.AcrStore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import io.jsonwebtoken.Claims;

public class CustomCookieAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(CustomCookieAuthenticator.class);

    public CustomCookieAuthenticator() {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Cookie sessionCookie = context.getSession().getContext().getRequestHeaders().getCookies().get("session");  // 이전 쿠키세션을 사용하는 경우

        if (sessionCookie == null) {
            context.attempted();
        } else {
//            String username = jwtParser.parseSignedClaims(sessionCookie.getValue()).getPayload().get("username").toString();

            // TODO 이전쿠키 세션으로 Rest등을 이용해서라도 회원ID를 가져올 수 있다면 가져와서 셋팅
            String username = "guest";

            if (username == null || "".equals(username)) {
                context.attempted();
            } else {
                RealmModel realm = context.getRealm();
                UserModel user = context.getSession().users().getUserByUsername(realm, username);
                context.setUser(user);

                AuthenticationSessionModel authSession = context.getAuthenticationSession();
                AcrStore acrStore = new AcrStore(context.getSession(), authSession);

                int previouslyAuthenticatedLevel = acrStore.getHighestAuthenticatedLevelFromPreviousAuthentication();
                acrStore.setLevelAuthenticatedToCurrentRequest(previouslyAuthenticatedLevel);
                context.success();
            }
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {

    }
}
