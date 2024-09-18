package org.keycloak.custom.authenticators;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

//import org.junit.jupiter.api.Test;
//import org.jboss.arquillian.junit.Arquillian;
//@RunWith(Arquillian.class)
public class SimpleTest {
    private static JwtParser parser = null;

    //    @Test
    public void kSimpleTest() {
        String brockerId = "google.12313";

        String[] temp = brockerId.split("\\.");

        System.out.println(temp[0]);
        System.out.println(temp[1]);

    }

    public static void tokenTest() {
        String token = "eyJhbGciOiJSUzUxMiJ9.eyJpYXQiOjE3MTY3NzQ5MjcsInN1YiI6IjEyMDAxNjE3ODAwIiwic2lnbmVkX2Zyb20iOm51bGwsInJvbGVzIjpbIm1lbWJlciIsInZlcmlmaWVkLXNlbGYiXSwibmlja25hbWUiOm51bGwsImZ1bGxuYW1lIjoi7J207J2A7J6sIiwidG9rZW5fdHlwZSI6ImFjY2VzcyIsInBpY3R1cmUiOm51bGwsInVzZXJuYW1lIjoicmltYXJzIiwiZXhwIjoxNzE2Nzc1NTI3fQ.PN3kxMKCtOMWScZ-emsg-uiOfr6c7vkhtGVRYSXnSMz_m96eLmKhFxche0D34urxi4lNvB7QEa5nIoqn8HN4I96D6Js02VL3GBXPnWz1Qu57WHDl45nOHQXnkMRGTgugljRnrPAMH1pWaYRXCKWlr7AgYz_-brRUX5Z6CfA7CNEtC_P5Aqg6oyV7xHvLXfgSkYblBi5yo-0xGO-iWY-gPKSu9jS1-JdD4h6ARqVENBDaeS22iti33DleMvhxKIU0mf9_4lBGsl1MY9uthxNOMexjLov5xwyFGE-VAJcGZ4INyaF2q_y2yNOFSeeye5mTzQf20dTN68UAVWcTz3eD8w";
        String token2 = "eyJhbGciOiJSUzUxMiJ9.eyJpYXQiOjE3MTY3NzQ1NTQsInN1YiI6IjEyMDAxNjE3ODAwIiwic2lnbmVkX2Zyb20iOiIxMDEiLCJyb2xlcyI6WyJtZW1iZXIiLCJ2ZXJpZmllZC1zZWxmIl0sIm5pY2tuYW1lIjpudWxsLCJmdWxsbmFtZSI6IuydtOydgOyerCIsInRva2VuX3R5cGUiOiJhY2Nlc3MiLCJwaWN0dXJlIjpudWxsLCJ1c2VybmFtZSI6InJpbWFycyIsImV4cCI6MTcxNjc3NTE1NH0.eitUUy6-HPaHsYgM8azEsuMDV7rpOy4FoLWS4ziuHZigR-Up82AOMXYiJPENZ9ygCkhLA2COduRWCbMkxn6fmkMXxdzUWOQaDLMk-xIay7S_9ZSdNoAWXia_U0LRwtDAUvI8G-xBWU511kD44LidxT0dYMytyHXEyd_0hYGtDgAoBBMG6a2tTfdZhBdrlKCRZgihjmb3CTv_yDcwfc6GZD_OjUK_T6rUTBoLI4xcNvdbdgOSNgYTRf3KdCKUf6D7N0EBeYBVxkxQwrv-_dbfwXWjX5lb5FKaaUh2ERlU8BJUIG3UwNckufj7Uw8lioje2cmFIo-hN93xQkVaCuoxbg";
        System.out.println(getClaims(token));
        System.out.println(getClaims(token2));
    }


    private static Claims getClaims (String accessToken) {
        return parser.parseSignedClaims(accessToken).getPayload();
    }


    private static PublicKey getPublicKeyFromBase64String(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKeyString = key.replaceAll("\\n", "").replaceAll("-{5}[ a-zA-Z]*-{5}", "");
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString));
        return keyFactory.generatePublic(keySpecX509);
    }

    public static void main(String args[]) {
        String pubkey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlswAbdgmf2a9l0wunklFjpEvDxZU2I49f3vntcdQQijiGAuReHnxNiUEY3K7wPTT30fSZROVL2ti/ZGDfbnTunh5BjZhSY856Wt1wiiq7JWmiN0vv5CaDgk3Ey7pSfImMyCqicpz/Qp/uOs7YFpPj4/cAREZsGk4xwKc6sKL1BbYZdRZ2Eq8g08kthLEF6yBCv/ZJdD2cMJK/fAx+HQ9vy25D7gKLkkRrnWtoRBn7xnTiHGOGf5S1/55dYkUu68kWVhPbHjOIRJN18sPx2BDpt3SOfLFaAwLl3PAIGJaLya82RaY64ZGNeE1Zq1ogjrWZSDN6U+7qN9gTO58s6IHkwIDAQAB";
        try {
            parser = Jwts.parser().verifyWith(getPublicKeyFromBase64String(pubkey)).build();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        tokenTest();
    }
}
