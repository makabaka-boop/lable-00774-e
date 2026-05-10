package com.alert.util;

import cn.hutool.crypto.symmetric.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Component
public class EncryptUtil {
    @Value("${encrypt.key}")
    private String encryptKey;
    private AES aes;

    @PostConstruct
    public void init() {
        byte[] key = new byte[16];
        byte[] keyBytes = encryptKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 16));
        this.aes = new AES(key);
    }

    public String encrypt(String content) {
        if (content == null) return null;
        return aes.encryptHex(content);
    }

    public String decrypt(String content) {
        if (content == null) return null;
        try {
            return aes.decryptStr(content);
        } catch (Exception e) {
            return content;
        }
    }
}
