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


import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="getUserByUsername", query="select u from UserEntity u where u.username = :username"),
        @NamedQuery(name="getUserByEmail",    query="select u from UserEntity u where u.email = :email"),
        @NamedQuery(name="getUserCount",      query="select count(u) from UserEntity u"),
        @NamedQuery(name="getAllUsers",       query="select u from UserEntity u"),

        @NamedQuery(name="searchForUser",     query="""
                                                    select u
                                                    from UserEntity u
                                                    where u.username like :search order by u.id
                                                    """),
})
@Entity
@Subselect("""
          select
            u.user_seq as id,
            u.user_id as username,
            u.mmbr_num as mmbrNum,
            u.user_password as password,
            k.user_name as mmbrName,
            e.email as email,
            p.phone as phone,
            e.signedFrom as signedFrom
         from  user_info u
        """
        )

@Immutable
public class UserEntity {
    @Id
    private String id;

    private String username;

    private String email;

    private String password;

    private String phone;

    private String mmbrNum;

    private String mmbrName;

    /**
     * 회원 유형 코드
     * 001:개인
     * 002:선서
     * 101:법인 사업자
     */
    private String mmbrPatrCode;

    /**
     * 회원을 식별하고 확인하기 위한 수단으로 사용되는 번호
     */
    private String mmbrIdnfNum;

    /**
     * 마케팅용 생일
     */
    private String mktgMuseBrdy;

    private String signedFrom;

    private String nextMdfc;

    public String getId() {
        return id;
    }

    public String getMmbrNum() {
        return mmbrNum;
    }

    public String getMmbrName() {
        return mmbrName;
    }

    public String getMmbrPatrCode() {
        return mmbrPatrCode;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getMmbrIdnfNum() {
        return mmbrIdnfNum;
    }

    public void setMmbrIdnfNum(String mmbrIdnfNum) {
        this.mmbrIdnfNum = mmbrIdnfNum;
    }

    public String getMktgMuseBrdy() {
        return mktgMuseBrdy;
    }

    public void setMktgMuseBrdy(String mktgMuseBrdy) {
        this.mktgMuseBrdy = mktgMuseBrdy;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getSignedFrom() {
        return signedFrom;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNextMdfc() {
        return nextMdfc;
    }

    public void setNextMdfc(String nextMdfc) {
        this.nextMdfc = nextMdfc;
    }

    public String isPwChange() {
        // null이면 오늘 변경이라 true // 변경일이 지났어도 true;
        if (nextMdfc == null) return "true";
        else if (LocalDate.parse(nextMdfc, DateTimeFormatter.ofPattern("yyyyMMdd")).isBefore(LocalDate.now())) return "true";
        return "false";
    }

    public String isAvailableSocialLink() {

        if ("101".equals(mmbrPatrCode)) return "Y";

        String brithDay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if("001".equals(mmbrPatrCode)) brithDay = mmbrIdnfNum;
        else if("005".equals(mmbrPatrCode)) brithDay = mktgMuseBrdy;

        if (ChronoUnit.YEARS.between(LocalDate.parse(brithDay, DateTimeFormatter.ofPattern("yyyyMMdd")), LocalDate.now()) < 14)
            return "Y";

        return "N";
    }

    public void setMmbrNum(String mmbrNum) {
        this.mmbrNum = mmbrNum;
    }

    public void setMmbrName(String mmbrName) {
        this.mmbrName = mmbrName;
    }

    public void setMmbrPatrCode(String mmbrPatrCode) {
        this.mmbrPatrCode = mmbrPatrCode;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSignedFrom(String signedFrom) {
        this.signedFrom = signedFrom;
    }
}
