package com.misc.shafagh.cif.impl.config.security;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author m_kor
 * date 12/2/2025
 */
@Configuration
public class RestClientConfiguration {
    @Value("${siam.ssl.keystore-address:}")
    private String siamKeystoreAddress;
    @Value("${siam.ssl.keystore-password:}")
    private String siamKeystorePassword;
    @Value("${siam.ssl.truststore-address:}")
    private String siamTruststoreAddress;
    @Value("${siam.ssl.truststore-password:}")
    private String siamTruststorePassword;
    @Value("${siam.ssl.tls-version:}")
    private String siamTlsVersion;

    @Bean(value = "sslBypass")
    RestClient.Builder getSSLBypassBuilder() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // Retain the legacy bean name while using normal certificate and hostname verification.
        CloseableHttpClient client = HttpClients.createSystem();
        return RestClient.builder().requestFactory(new HttpComponentsClientHttpRequestFactory(client));
    }

   /* @Bean(value = "siamSSL")
    @Conditional(SiamActivationCondition.class)
    RestClient.Builder getSiamSSLBuilder() throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
            return RestClient.builder().requestFactory(getFactory(siamKeystoreAddress, siamKeystorePassword, siamTruststoreAddress, siamTruststorePassword, siamTlsVersion));
    }*/

    private HttpComponentsClientHttpRequestFactory getFactory(String keystoreAddress, String keystorePassword, String truststoreAddress, String truststorePassword, String tlsVersion) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
        X509TrustManager jreTrustManager = getJreTrustManager();
        X509TrustManager myTrustManager = getMyTrustManager(truststoreAddress, truststorePassword);
        KeyManager[] keyManagers = getKeyManagers(keystoreAddress, keystorePassword);
        X509TrustManager mergedTrustManager = createMergedTrustManager(jreTrustManager, myTrustManager);
        SSLContext sslContext = getSslContext(mergedTrustManager, keyManagers, tlsVersion);
        CloseableHttpClient httpClient = createHttpClient(sslContext);
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        return factory;
    }

    private X509TrustManager getJreTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        return findDefaultTrustManager(null);
    }

    private X509TrustManager getMyTrustManager(String truststoreAddress, String truststorePassword) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        try (FileInputStream myKeys = new FileInputStream(truststoreAddress)) {
            KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            myTrustStore.load(myKeys, truststorePassword.toCharArray());
            return findDefaultTrustManager(myTrustStore);
        }
    }

    private X509TrustManager findDefaultTrustManager(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        return null;
    }

    private X509TrustManager createMergedTrustManager(X509TrustManager jreTrustManager, X509TrustManager customTrustManager) {
        return new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return jreTrustManager.getAcceptedIssuers();
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                try {
                    customTrustManager.checkServerTrusted(chain, authType);
                } catch (CertificateException e) {
                    jreTrustManager.checkServerTrusted(chain, authType);
                }
            }
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                jreTrustManager.checkClientTrusted(chain, authType);
            }
        };
    }

    private KeyManager[] getKeyManagers(String keystoreAddress, String keystorePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream in = new FileInputStream(keystoreAddress);
        keyStore.load(in, keystorePassword.toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }

    private SSLContext getSslContext(X509TrustManager mergedTrustManager, KeyManager[] keyManagers, String tlsVersion) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance(tlsVersion);
        sslContext.init(keyManagers, new TrustManager[] { mergedTrustManager }, null);

        return sslContext;
    }

    private CloseableHttpClient createHttpClient(SSLContext sslContext) {
        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier())
                .build();

        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .build();
    }
}
