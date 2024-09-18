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

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.NewCookie;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;

import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;

import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.connections.jpa.JpaConnectionProvider;

import org.keycloak.custom.storage.user.SocialEntity;
import org.keycloak.http.HttpResponse;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;


import java.net.URI;
import java.util.List;

/**
 * @author <a href="mailto:Ryan.Slominski@gmail.com">Ryan Slominski</a>
 * ref : IdpCreateUserIfUniqueAuthenticator
 */
public class IdpProviderAutoLinkAuthenticator extends AbstractIdpAuthenticator {

    private static final Logger logger = Logger.getLogger(IdpProviderAutoLinkAuthenticator.class);

    @Override
    protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
    }

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {

        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();

        if (context.getAuthenticationSession().getAuthNote(EXISTING_USER_INFO) != null) {
            context.attempted();
            return;
        }

        String username = checkBrokerUser(context, serializedCtx, brokerContext);

        if (username != null) {
            UserModel user = session.users().getUserByUsername(realm, username);
            context.setUser(user);
            context.success();
            return;
        }
        context.attempted();
    }




    // Could be overriden to detect duplication based on other criterias (firstName, lastName, ...)
    protected String checkBrokerUser(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();

        EntityManager em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();

        logger.debugf("borker userID = %s", brokerContext.getBrokerUserId());


        String[] brockerId = brokerContext.getBrokerUserId().split("\\.");

        if(brockerId.length == 2) {
            logger.debugf("brokerId : %s", brokerContext.getBrokerUserId());
            String provider = brockerId[0];
            String providerLinkId = brockerId[1];

            logger.debugf("provider : %s  //  providerLinkId : %s", provider, providerLinkId);

            TypedQuery<SocialEntity> query = em.createNamedQuery("getProviderUserLink", SocialEntity.class);
            query.setParameter("identityProvider", provider);
            query.setParameter("federatedUserId", providerLinkId);

            List<SocialEntity> result = query.getResultList();

            if (result.isEmpty()) return null;

            if (result.size() == 1) {
                 return result.get(0).getUsername();
            }
        }

        return null;
    }


    protected void setCookie(AuthenticationFlowContext context, String provider) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        int maxCookieAge = 60 * 60 * 24 * 30; // 30 days

        logger.debugf("provider cookie = %s", provider);

        URI uri = context.getUriInfo().getBaseUriBuilder().path("realms").path(context.getRealm().getName()).build();
        addCookie(context, "sns_provider", provider,
                uri.getRawPath(),
                null, null,
                maxCookieAge,
                false, true);
    }

    public void addCookie(AuthenticationFlowContext context, String name, String value, String path, String domain, String comment, int maxAge, boolean secure, boolean httpOnly) {
        HttpResponse response = context.getSession().getContext().getHttpResponse();
        response.setCookieIfAbsent(new NewCookie(name, value));
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

}
