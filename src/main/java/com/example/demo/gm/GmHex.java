package com.example.demo.gm;

import java.util.HexFormat;

/**
 * hex 编解码。密钥 / 密文 / 签名都是字节，JSON 里用 hex 传最直观。
 */
final class GmHex {

    private static final HexFormat HEX = HexFormat.of();

    private GmHex() {
    }

    static String encode(byte[] bytes) {
        return HEX.formatHex(bytes);
    }

    static byte[] decode(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("hex 不能为空");
        }
        String normalized = hex.strip().replace(" ", "");
        if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
            normalized = normalized.substring(2);
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("hex 不能为空");
        }
        try {
            return HEX.parseHex(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("不是合法 hex 字符串");
        }
    }

    static byte[] decodeExact(String hex, int length, String name) {
        byte[] bytes = decode(hex);
        if (bytes.length != length) {
            throw new IllegalArgumentException(
                    name + " 必须是 " + length + " 字节，实际 " + bytes.length);
        }
        return bytes;
    }

    /** 私钥 D 值可能不足 32 字节（高位 0 被省略），左侧补零。 */
    static byte[] decodePadded(String hex, int length, String name) {
        byte[] bytes = decode(hex);
        if (bytes.length == length) {
            return bytes;
        }
        if (bytes.length > length) {
            throw new IllegalArgumentException(
                    name + " 太长: " + bytes.length + " > " + length);
        }
        byte[] padded = new byte[length];
        System.arraycopy(bytes, 0, padded, length - bytes.length, bytes.length);
        return padded;
    }
}
