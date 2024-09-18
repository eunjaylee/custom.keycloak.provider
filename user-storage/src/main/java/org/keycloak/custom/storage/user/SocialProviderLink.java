package org.keycloak.custom.storage.user;


import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.util.Date;


@NamedQueries({
        @NamedQuery(name="getCiMatchIds", query=" select m from social_provider_link m where m.ci = :ci and m.id != null order by m.creat_at  "),
})
@Subselect( """
            select distinct m.user_seq as id, m.user_id as username, d.ci, m.create_at 
               from social_privider_link d
             left outer join user_info m
             on m.user_seq = d.user_seq
           """
)
@Entity
@Immutable
public class SocialProviderLink {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatAt() {
        return create_at;
    }

    public void setCreateAt(Date createAt) {
        this.create_at = create_at;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    @Id
    private String id;

    private String username;

    private String ci;

    private Date create_at;
}
