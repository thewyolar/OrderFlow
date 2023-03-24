package com.thewyolar.orderflow.orderservice.util;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAEncryptor {
    private static PrivateKey privateKey;

    private static PublicKey publicKey;

    private static final String PRIVATE_KEY_STRING = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDPRijpTTjq/HBCAt3HwvPP3wdEg9cIkxzhlEX0fv8Ak1pdGGimjSi8gf3GZ1xQgYPHuJk9IIXLPN1AgCQbYdHfZVsp1f3q3ike2oUCwalQFiKaRtcIAgfxDZNd11+nBak5vCK+ElfyeWPy7CfDANdqj7ZQnJZKt3/YnJ/EQEha4XdX8Lh5nrX6S8vaJXZXkFRijDidI65NuBIfYwOHAfTMsxQY8OHaWPoHoqlTfT4y2zOISjXYizWalrKnt3wIMMUqn5QIpPT/D1Aq/bUqoFXQvCFlIxPXv70ODkMPffiOBxC9LckmH4BKypCh7/v4C2quC/h7Q3MVU+/VvD4I8oedAgMBAAECggEARxJ/pL/d7HVy6K+i7IRkpbqL+2pZqY8IMDFva07Akan1KkbcW3+7oCfSpN18Qf6m+SzLHrKwLehI1X0a5Gi+ViOjBs1hq89RukqjoTZWoCd25f+0bAKGRpmpFlxraBIA3NMt9o7DqVkGdJ2VZMA9HUT59to6jpQtOlE6V0jThpV61Oqev+OQGrfrgYj0pYk2eDxMD01v6hGwBo8mMHvimvnemmadred3wFpdW+7tapXdaQmP2nqbhew3vU2ghPcYyMK1PyTV3InoZtSVNs2r086CxxCp4nnmq9QPS/LrSo7N3quv0blX37ShhZfF22Lh3cqnLg6ERf7oc4uUNxr64QKBgQDYRLm/SzmtiJ+UTnP1WkporuCh94Dd8DUJKpwlUBsERjzmpmSSNgsWZahGFLQ+xBP2CLhqSe2Jv6jsiMCHqIEpWBkvY9yWKeTkpCjLswj7knUokd9oF9Dm+vCaUO+W8ciWaysJ00qfAzJ2hj6y3ND/jP2x9HNihTSJjSpAq5gyfwKBgQD1Wmuy+mvG7+UybO4J5GrgSxsuQXX77wmG/nJUyT4Hr8lGdgr9HNFss+hebzhw+HJkm6ansdt76WwdUgfW6E17MorKVHfuibVBI+SKjm7zD5bSqsLLVol69wPVCUMcpj6/EL1pDDAuAQD8MLroGiBLOVZl9deaN69SBf7O8i+/4wKBgQDDxpsbl36UICzNtaN1RRI856pEFOjcgHmSXbdSc8yxNuAksBY04aVx1f7Zyh5M/3VOcSB4X4of+5dw4G7hn2GlSrIp202xxiUF8bFYjS8hhbh3TG4gLgcXQa0TFRd+3kuGF2ezkkln3x4Me4RAqHKuFDUIplDZq9oeduL7/hpeAwKBgQCFUXOyFibYO2SEKPKTh32XkNgdI07T7Yh0xPAlKBC/poIs+llwPeeCNKPwJGk74ZRvHLDK8Hb99tFbgUZnPWVUqxsFZC6i/Er0MWeu7kRVaMQghs9AJC6iFXUMnnLML31q3tK9MPVBGiSJ5IS7N+8SUP6kepiD4PwwCrfxhWHy1wKBgQCkUpPmFS1F+YbCK8//S9xn8kzBgJjbKZBXTkqT0pEpCJ4Aeq/svX+af1IMd0rrTzI3RnW+nxC3VgkGALEfORUlzsYBrttM5nWAFkpBBMMl/OPvzkmz+ziXFuUQh7oADXdwbcoujfFiYvXeoU5xkUfixrZkQ8iYjRs2HK9OAw/RNg==";

    private static final String PUBLIC_KEY_STRING =  "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz0Yo6U046vxwQgLdx8Lzz98HRIPXCJMc4ZRF9H7/AJNaXRhopo0ovIH9xmdcUIGDx7iZPSCFyzzdQIAkG2HR32VbKdX96t4pHtqFAsGpUBYimkbXCAIH8Q2TXddfpwWpObwivhJX8nlj8uwnwwDXao+2UJyWSrd/2JyfxEBIWuF3V/C4eZ61+kvL2iV2V5BUYow4nSOuTbgSH2MDhwH0zLMUGPDh2lj6B6KpU30+MtsziEo12Is1mpayp7d8CDDFKp+UCKT0/w9QKv21KqBV0LwhZSMT17+9Dg5DD334jgcQvS3JJh+ASsqQoe/7+Atqrgv4e0NzFVPv1bw+CPKHnQIDAQAB";

    public RSAEncryptor() {
        this.init();
    }

    public void init() {
        try {
            X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(decode(PUBLIC_KEY_STRING));
            PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(decode(PRIVATE_KEY_STRING));

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            publicKey = keyFactory.generatePublic(keySpecPublic);
            privateKey = keyFactory.generatePrivate(keySpecPrivate);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации ключей", e);
        }
    }

    private static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
    private static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public String encrypt(String message) {
        try {
            byte[] messageToBytes = message.getBytes();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(messageToBytes);
            return encode(encryptedBytes);
        } catch (Exception e) {
            return null;
        }
    }

    public String decrypt(String encryptedMessage) {
        try {
            byte[] encryptedBytes = decode(encryptedMessage);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
            return new String(decryptedMessage, "UTF8");
        } catch (Exception e) {
            return null;
        }
    }
}
