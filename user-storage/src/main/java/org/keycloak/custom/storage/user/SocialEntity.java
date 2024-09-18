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


import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.io.Serializable;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="getProviderUserLink", query="select s from SocialEntity s where s.federatedUserId = :federatedUserId and s.identityProvider = :identityProvider"),
})
@Entity
@Subselect(""" 
            select  l.user_seq id, u.user_id as username, l.provider_id as federatedUserId, l.provider_code as identityProvider
            from stm.social_privider_link l
            inner join stm.user_info u
            on l.user_seq = u.user_seq
            """
)
@Immutable
@IdClass(SocialPK.class)
public class SocialEntity {

    private String id;

    private String username;

    @Id
    private String identityProvider;

    @Id
    private String federatedUserId;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(String identityProvider) {
        this.identityProvider = identityProvider;
    }

    public String getFederatedUserId() {
        return federatedUserId;
    }

    public void setFederatedUserId(String federatedUserId) {
        this.federatedUserId = federatedUserId;
    }
}


class SocialPK  implements Serializable {

    private String identityProvider;

    private String federatedUserId;
}