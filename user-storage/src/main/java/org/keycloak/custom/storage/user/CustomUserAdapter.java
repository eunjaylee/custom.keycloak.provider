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
package org.keycloak.custom.storage.user;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CustomUserAdapter extends AbstractUserAdapterFederatedStorage {
    private static final Logger logger = Logger.getLogger(CustomUserAdapter.class);
    protected UserEntity entity;

    protected SocialEntity socialEntity;

    protected String keycloakId;

    public CustomUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserEntity entity) {
        super(session, realm, model);
        this.entity = entity;
        keycloakId = StorageId.keycloakId(model, entity.getId());
    }

    public String getPassword() {
        return entity.getPassword();
    }

    public void setPassword(String password) {
        entity.setPassword(password);
    }

    @Override
    public String getUsername() {
        return entity.getUsername();
    }

    @Override
    public void setUsername(String username) {
        entity.setUsername(username);

    }

    @Override
    public void setEmail(String email) {
        entity.setEmail(email);
    }

    @Override
    public String getEmail() {
        return entity.getEmail();
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    @Override
    public void setSingleAttribute(String name, String value) {
//        if (name.equals("phone")) {
//            entity.setPhone(value);
//        } else {
            super.setSingleAttribute(name, value);
//        }
    }

    @Override
    public void removeAttribute(String name) {
//        if (name.equals("phone")) {
//            entity.setPhone(null);
//        } else {
            super.removeAttribute(name);
//        }
    }

    @Override
    public void setAttribute(String name, List<String> values) {
//        if (name.equals("phone")) {
//            entity.setPhone(values.get(0));
//        } else {
            super.setAttribute(name, values);
//        }
    }

    @Override
    public String getFirstAttribute(String name) {
//        if (name.equals("phone")) {
//            return entity.getPhone();
//        } else {
          return switch (name) {
              case "sub" -> entity.getMmbrNum();
              case "phone" -> entity.getPhone();
              case "fullname" -> entity.getMmbrName();
              case "signed_from" -> entity.getSignedFrom();
              case "isPwdChange" -> entity.isPwChange();
              case "mmbrPatrCode" -> entity.getMmbrPatrCode();
              case "isAvailableSocialLink" -> entity.isAvailableSocialLink();
              default -> super.getFirstAttribute(name);
          };
//        }
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attrs = super.getAttributes();
        MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();

        all.putAll(attrs);
        all.add("sub", entity.getMmbrNum());
        all.add("signed_from", entity.getSignedFrom());
        all.add("fullname", entity.getMmbrName());
        all.add("isPwdChange", entity.isPwChange());
        all.add("phone", entity.getPhone());
        all.add("mmbrPatrCode", entity.getMmbrPatrCode());
        all.add("isAvailableSocialLink",  entity.isAvailableSocialLink());
        return all;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        return switch (name) {
            case "sub"          -> Stream.of(entity.getMmbrNum());
//            case "email" -> Stream.of(entity.getEmail());
            case "phone"        -> Stream.of(entity.getPhone());
//            case "username" -> Stream.of(entity.getUsername());
            case "fullname"     -> Stream.of(entity.getMmbrName());
            case "signed_from"  -> Stream.of(entity.getSignedFrom());
            case "isPwdChange"  -> Stream.of(entity.isPwChange());
            case "mmbrPatrCode" -> Stream.of(entity.getMmbrPatrCode());
            case "isAvailableSocialLink"  -> Stream.of(entity.isAvailableSocialLink());
            default -> super.getAttributeStream(name);
        };
    }
}
