package com.example.demo.gm;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * SM4：分组对称加密（类比 AES-128）。
 *
 * 密钥 128 bit（16 字节）。这里用 CBC + PKCS5Padding：
 * 每次加密需要 16 字节 IV（初始化向量），解密必须用同一个 IV。
 */
@Service
public class Sm4Service {

    private static final String TRANSFORMATION = "SM4/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 16;
    private static final int IV_SIZE = 16;

    public Sm4Service() {
        GmProviders.install();
    }

    public String generateKey() {
        byte[] key = new byte[KEY_SIZE];
        new SecureRandom().nextBytes(key);
        return GmHex.encode(key);
    }

    public CipherResult encrypt(String keyHex, String plaintext, String ivHex) {
        byte[] key = GmHex.decodeExact(keyHex, KEY_SIZE, "SM4 密钥");
        byte[] iv = resolveIv(ivHex);
        byte[] cipher = doCipher(Cipher.ENCRYPT_MODE, key, iv, plaintext.getBytes(StandardCharsets.UTF_8));
        return new CipherResult(GmHex.encode(cipher), GmHex.encode(iv));
    }

    public String decrypt(String keyHex, String ciphertextHex, String ivHex) {
        byte[] key = GmHex.decodeExact(keyHex, KEY_SIZE, "SM4 密钥");
        byte[] iv = GmHex.decodeExact(ivHex, IV_SIZE, "SM4 IV");
        byte[] plain = doCipher(Cipher.DECRYPT_MODE, key, iv, GmHex.decode(ciphertextHex));
        return new String(plain, StandardCharsets.UTF_8);
    }

    private static byte[] resolveIv(String ivHex) {
        if (ivHex == null || ivHex.isBlank()) {
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            return iv;
        }
        return GmHex.decodeExact(ivHex, IV_SIZE, "SM4 IV");
    }

    private static byte[] doCipher(int mode, byte[] key, byte[] iv, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, "BC");
            cipher.init(mode, new SecretKeySpec(key, "SM4"), new IvParameterSpec(iv));
            return cipher.doFinal(input);
        } catch (GeneralSecurityException ex) {
            String action = mode == Cipher.ENCRYPT_MODE ? "加密" : "解密";
            throw new IllegalArgumentException("SM4 " + action + "失败：" + ex.getMessage());
        }
    }

    public record CipherResult(String ciphertext, String iv) {
    }
}
