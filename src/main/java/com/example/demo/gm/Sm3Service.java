package com.example.demo.gm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import org.springframework.stereotype.Service;

/**
 * SM3：摘要算法（类比 SHA-256）。
 *
 * 输入任意长度，输出固定 256 bit（32 字节 / 64 个 hex 字符）。
 * 不可逆，同一输入永远同一输出；改一个字符，摘要完全不同。
 */
@Service
public class Sm3Service {

    public Sm3Service() {
        GmProviders.install();
    }

    public Digest digest(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SM3", "BC");
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            return new Digest(GmHex.encode(hash), hash.length);
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new IllegalStateException("SM3 不可用", ex);
        }
    }

    public record Digest(String hex, int byteLength) {
    }
}
