package com.example.demo.gm;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * 把 BouncyCastle 注册进 JCA（Java Cryptography Architecture）。
 *
 * 之后才能 {@code MessageDigest.getInstance("SM3", "BC")} 这种按名字取算法。
 * 类比：Node 里用第三方库之前要先 require / 注册插件。
 */
final class GmProviders {

    private GmProviders() {
    }

    static synchronized void install() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
