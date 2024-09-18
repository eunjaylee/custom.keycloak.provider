package org.keycloak.custom.storage.user;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.io.Serializable;

@NamedQueries({
    @NamedQuery(name="getBioAuthSertNum", query="select a from MobileAuthEntity a where a.userId = :userId and a.authType = :authType")
})
@Entity
@Subselect(" TODO ")
@IdClass(MobilePK.class)
@Immutable
public class MobileAuthEntity {

    // 회원 고유 번호
    @Id
    private String userId;

    // 디바이스 번호
    private String deviceId;

    // 간편번호
    private String sertNum;

    // 인증타입
    @Id
    private String authType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSertNum() {
        return sertNum;
    }

    public void setSertNum(String sertNum) {
        this.sertNum = sertNum;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }
}

class MobilePK  implements Serializable {

    private String userId;

    private String authType;
}