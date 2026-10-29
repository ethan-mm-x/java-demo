package com.example.demo.gm;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;
import org.springframework.stereotype.Service;

/**
 * SM2：椭圆曲线上的非对称算法（类比 ECDSA + ECIES，曲线固定为 sm2p256v1）。
 *
 * <ul>
 *   <li>加密：公钥加密，私钥解密 —— 保密</li>
 *   <li>签名：私钥签名，公钥验签 —— 防篡改 + 确认来源</li>
 * </ul>
 *
 * 密文排列用国标常见的 C1||C3||C2（C1=临时点, C3=SM3 校验, C2=密文）。
 * 签名默认 userId 是 ASCII {@code 1234567812345678}（GM/T 0009）。
 */
@Service
public class Sm2Service {

    static final String CURVE = "sm2p256v1";

    private static final ECNamedCurveParameterSpec BC_SPEC =
            ECNamedCurveTable.getParameterSpec(CURVE);
    private static final ECDomainParameters DOMAIN = new ECDomainParameters(
            BC_SPEC.getCurve(), BC_SPEC.getG(), BC_SPEC.getN(), BC_SPEC.getH());
    private static final ECNamedCurveSpec JCE_SPEC = new ECNamedCurveSpec(
            CURVE, BC_SPEC.getCurve(), BC_SPEC.getG(), BC_SPEC.getN(), BC_SPEC.getH());

    public Sm2Service() {
        GmProviders.install();
    }

    public KeyPairHex generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", "BC");
            generator.initialize(new ECGenParameterSpec(CURVE), new SecureRandom());
            KeyPair pair = generator.generateKeyPair();

            byte[] publicKey = ((BCECPublicKey) pair.getPublic()).getQ().getEncoded(false);
            byte[] privateKey = BigIntegers.asUnsignedByteArray(
                    32, ((BCECPrivateKey) pair.getPrivate()).getD());
            return new KeyPairHex(GmHex.encode(publicKey), GmHex.encode(privateKey));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SM2 生成密钥失败", ex);
        }
    }

    public String encrypt(String publicKeyHex, String plaintext) {
        byte[] publicKey = decodePublicKey(publicKeyHex);
        byte[] plain = plaintext.getBytes(StandardCharsets.UTF_8);

        ECPublicKeyParameters pub = new ECPublicKeyParameters(
                BC_SPEC.getCurve().decodePoint(publicKey), DOMAIN);

        SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        engine.init(true, new ParametersWithRandom(pub, new SecureRandom()));
        try {
            return GmHex.encode(engine.processBlock(plain, 0, plain.length));
        } catch (InvalidCipherTextException ex) {
            throw new IllegalArgumentException("SM2 加密失败", ex);
        }
    }

    public String decrypt(String privateKeyHex, String ciphertextHex) {
        byte[] d = GmHex.decodePadded(privateKeyHex, 32, "SM2 私钥");
        byte[] cipher = GmHex.decode(ciphertextHex);

        ECPrivateKeyParameters priv = new ECPrivateKeyParameters(new BigInteger(1, d), DOMAIN);
        SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        engine.init(false, priv);
        try {
            byte[] plain = engine.processBlock(cipher, 0, cipher.length);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (InvalidCipherTextException ex) {
            throw new IllegalArgumentException("SM2 解密失败：密文无效或密钥不匹配");
        }
    }

    public String sign(String privateKeyHex, String message) {
        try {
            Signature signature = Signature.getInstance("SM3withSM2", "BC");
            signature.initSign(toPrivateKey(privateKeyHex));
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            return GmHex.encode(signature.sign());
        } catch (GeneralSecurityException ex) {
            throw new IllegalArgumentException("SM2 签名失败：" + ex.getMessage());
        }
    }

    public boolean verify(String publicKeyHex, String message, String signatureHex) {
        try {
            Signature signature = Signature.getInstance("SM3withSM2", "BC");
            signature.initVerify(toPublicKey(publicKeyHex));
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            return signature.verify(GmHex.decode(signatureHex));
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] decodePublicKey(String hex) {
        byte[] bytes = GmHex.decode(hex);
        if (bytes.length == 64) {
            byte[] uncompressed = new byte[65];
            uncompressed[0] = 0x04;
            System.arraycopy(bytes, 0, uncompressed, 1, 64);
            return uncompressed;
        }
        if (bytes.length == 65 && bytes[0] == 0x04) {
            return bytes;
        }
        throw new IllegalArgumentException(
                "SM2 公钥须是未压缩点：65 字节(04||X||Y) 或 64 字节(X||Y)");
    }

    private static PrivateKey toPrivateKey(String hex) throws GeneralSecurityException {
        byte[] d = GmHex.decodePadded(hex, 32, "SM2 私钥");
        ECPrivateKeySpec spec = new ECPrivateKeySpec(new BigInteger(1, d), JCE_SPEC);
        return KeyFactory.getInstance("EC", "BC").generatePrivate(spec);
    }

    private static PublicKey toPublicKey(String hex) throws GeneralSecurityException {
        byte[] encoded = decodePublicKey(hex);
        ECPoint q = BC_SPEC.getCurve().decodePoint(encoded);
        ECPublicKeySpec spec = new ECPublicKeySpec(
                new java.security.spec.ECPoint(
                        q.getAffineXCoord().toBigInteger(),
                        q.getAffineYCoord().toBigInteger()),
                JCE_SPEC);
        return KeyFactory.getInstance("EC", "BC").generatePublic(spec);
    }

    public record KeyPairHex(String publicKey, String privateKey) {
    }
}
