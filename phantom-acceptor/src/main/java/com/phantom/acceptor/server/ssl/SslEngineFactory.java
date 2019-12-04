package com.phantom.acceptor.server.ssl;

import com.phantom.acceptor.config.AcceptorConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * SSL 双向认证
 *
 * @author Jianfeng Wang
 * @since 2019/12/4 14:17
 */
public class SslEngineFactory {

    public static SSLEngine getEngine(AcceptorConfig acceptorConfig) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(SslEngineFactory.class.getClassLoader().getResourceAsStream(acceptorConfig.getKeyStore()),
                    acceptorConfig.getSslPassword().toCharArray());
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, acceptorConfig.getSslPassword().toCharArray());

            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(SslEngineFactory.class.getClassLoader().getResourceAsStream(acceptorConfig.getKeyStore()),
                    acceptorConfig.getSslPassword().toCharArray());
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setNeedClientAuth(true);
            sslEngine.setUseClientMode(false);
            return sslEngine;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
