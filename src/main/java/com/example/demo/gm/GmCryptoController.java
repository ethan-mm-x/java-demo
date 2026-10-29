package com.example.demo.gm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 国密练习接口。密钥 / 密文 / 签名一律 hex；明文按 UTF-8。
 *
 * <pre>
 * GET  /api/gm/demo           一键跑通 SM2/SM3/SM4
 * POST /api/gm/sm2/keypair
 * POST /api/gm/sm2/encrypt
 * POST /api/gm/sm2/decrypt
 * POST /api/gm/sm2/sign
 * POST /api/gm/sm2/verify
 * POST /api/gm/sm4/key
 * POST /api/gm/sm4/encrypt
 * POST /api/gm/sm4/decrypt
 * POST /api/gm/sm3/digest
 * </pre>
 */
@RestController
@RequestMapping("/api/gm")
public class GmCryptoController {

    private static final String DEMO_TEXT = "hello-国密";

    private final Sm2Service sm2;
    private final Sm3Service sm3;
    private final Sm4Service sm4;

    public GmCryptoController(Sm2Service sm2, Sm3Service sm3, Sm4Service sm4) {
        this.sm2 = sm2;
        this.sm3 = sm3;
        this.sm4 = sm4;
    }

    /** 生成一对密钥，走一遍加解密 + 签名验签 + SM3 + SM4，方便对照结果。 */
    @GetMapping("/demo")
    public Map<String, Object> demo() {
        Sm2Service.KeyPairHex sm2Keys = sm2.generateKeyPair();
        String sm2Cipher = sm2.encrypt(sm2Keys.publicKey(), DEMO_TEXT);
        String sm2Plain = sm2.decrypt(sm2Keys.privateKey(), sm2Cipher);
        String signature = sm2.sign(sm2Keys.privateKey(), DEMO_TEXT);
        boolean verified = sm2.verify(sm2Keys.publicKey(), DEMO_TEXT, signature);

        Sm3Service.Digest digest = sm3.digest(DEMO_TEXT);

        String sm4Key = sm4.generateKey();
        Sm4Service.CipherResult sm4Cipher = sm4.encrypt(sm4Key, DEMO_TEXT, null);
        String sm4Plain = sm4.decrypt(sm4Key, sm4Cipher.ciphertext(), sm4Cipher.iv());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("plaintext", DEMO_TEXT);
        body.put("encoding", "密钥/密文/签名/摘要都是 hex；明文 UTF-8");

        Map<String, Object> sm2Part = new LinkedHashMap<>();
        sm2Part.put("curve", Sm2Service.CURVE);
        sm2Part.put("cipherLayout", "C1||C3||C2（未压缩点 C1 带 04 前缀）");
        sm2Part.put("publicKey", sm2Keys.publicKey());
        sm2Part.put("privateKey", sm2Keys.privateKey());
        sm2Part.put("ciphertext", sm2Cipher);
        sm2Part.put("decrypted", sm2Plain);
        sm2Part.put("decryptOk", DEMO_TEXT.equals(sm2Plain));
        sm2Part.put("signature", signature);
        sm2Part.put("verified", verified);
        body.put("sm2", sm2Part);

        Map<String, Object> sm3Part = new LinkedHashMap<>();
        sm3Part.put("digest", digest.hex());
        sm3Part.put("byteLength", digest.byteLength());
        body.put("sm3", sm3Part);

        Map<String, Object> sm4Part = new LinkedHashMap<>();
        sm4Part.put("mode", "SM4/CBC/PKCS5Padding");
        sm4Part.put("key", sm4Key);
        sm4Part.put("iv", sm4Cipher.iv());
        sm4Part.put("ciphertext", sm4Cipher.ciphertext());
        sm4Part.put("decrypted", sm4Plain);
        sm4Part.put("decryptOk", DEMO_TEXT.equals(sm4Plain));
        body.put("sm4", sm4Part);

        return body;
    }

    @PostMapping("/sm2/keypair")
    public Sm2Service.KeyPairHex sm2KeyPair() {
        return sm2.generateKeyPair();
    }

    @PostMapping("/sm2/encrypt")
    public Map<String, String> sm2Encrypt(@Valid @RequestBody Sm2EncryptRequest request) {
        return Map.of("ciphertext", sm2.encrypt(request.publicKey(), request.plaintext()));
    }

    @PostMapping("/sm2/decrypt")
    public Map<String, String> sm2Decrypt(@Valid @RequestBody Sm2DecryptRequest request) {
        return Map.of("plaintext", sm2.decrypt(request.privateKey(), request.ciphertext()));
    }

    @PostMapping("/sm2/sign")
    public Map<String, String> sm2Sign(@Valid @RequestBody Sm2SignRequest request) {
        return Map.of("signature", sm2.sign(request.privateKey(), request.message()));
    }

    @PostMapping("/sm2/verify")
    public Map<String, Object> sm2Verify(@Valid @RequestBody Sm2VerifyRequest request) {
        boolean ok = sm2.verify(request.publicKey(), request.message(), request.signature());
        return Map.of("verified", ok);
    }

    @PostMapping("/sm4/key")
    public Map<String, String> sm4Key() {
        return Map.of("key", sm4.generateKey());
    }

    @PostMapping("/sm4/encrypt")
    public Sm4Service.CipherResult sm4Encrypt(@Valid @RequestBody Sm4EncryptRequest request) {
        return sm4.encrypt(request.key(), request.plaintext(), request.iv());
    }

    @PostMapping("/sm4/decrypt")
    public Map<String, String> sm4Decrypt(@Valid @RequestBody Sm4DecryptRequest request) {
        return Map.of("plaintext", sm4.decrypt(request.key(), request.ciphertext(), request.iv()));
    }

    @PostMapping("/sm3/digest")
    public Sm3Service.Digest sm3Digest(@Valid @RequestBody Sm3DigestRequest request) {
        return sm3.digest(request.message());
    }

    public record Sm2EncryptRequest(
            @NotBlank(message = "publicKey 不能为空") String publicKey,
            @NotBlank(message = "plaintext 不能为空") String plaintext) {
    }

    public record Sm2DecryptRequest(
            @NotBlank(message = "privateKey 不能为空") String privateKey,
            @NotBlank(message = "ciphertext 不能为空") String ciphertext) {
    }

    public record Sm2SignRequest(
            @NotBlank(message = "privateKey 不能为空") String privateKey,
            @NotBlank(message = "message 不能为空") String message) {
    }

    public record Sm2VerifyRequest(
            @NotBlank(message = "publicKey 不能为空") String publicKey,
            @NotBlank(message = "message 不能为空") String message,
            @NotBlank(message = "signature 不能为空") String signature) {
    }

    public record Sm4EncryptRequest(
            @NotBlank(message = "key 不能为空") String key,
            @NotBlank(message = "plaintext 不能为空") String plaintext,
            String iv) {
    }

    public record Sm4DecryptRequest(
            @NotBlank(message = "key 不能为空") String key,
            @NotBlank(message = "ciphertext 不能为空") String ciphertext,
            @NotBlank(message = "iv 不能为空") String iv) {
    }

    public record Sm3DigestRequest(
            @NotBlank(message = "message 不能为空") String message) {
    }
}
