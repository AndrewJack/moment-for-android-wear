package technology.mainthread.apps.moment.data.okhttp;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.SslUtils;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Thread-safe HTTP low-level transport based on the {@code java.net} package.
 * <p>
 * Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 * </p>
 */
public final class OkHttpTransport extends HttpTransport {

    /**
     * All valid request methods, sorted in ascending alphabetical order.
     */
    private static final String[] SUPPORTED_METHODS = {HttpMethods.DELETE,
            HttpMethods.GET,
            HttpMethods.HEAD,
            HttpMethods.OPTIONS,
            HttpMethods.POST,
            HttpMethods.PUT,
            HttpMethods.TRACE};

    static {
        Arrays.sort(SUPPORTED_METHODS);
    }

    /**
     * SSL socket factory or {@code null} for the default.
     */
    private final SSLSocketFactory sslSocketFactory;

    /**
     * Host name verifier or {@code null} for the default.
     */
    private final HostnameVerifier hostnameVerifier;
    private final Proxy proxy;
    private final OkHttpClient client;

    /**
     * Constructor with the default behavior.
     * <p/>
     * <p>
     * Instead use {@link OkHttpTransport.Builder} to modify behavior.
     * </p>
     */
    public OkHttpTransport() {
        this(null, null, null, null);
    }

    /**
     * @param client           OkHttpClient or {@code null} for the default
     * @param proxy            HTTP proxy or {@code null} to use the proxy settings from <a
     *                         href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">
     *                         system properties</a>
     * @param sslSocketFactory SSL socket factory or {@code null} for the default
     * @param hostnameVerifier host name verifier or {@code null} for the default
     */
    OkHttpTransport(OkHttpClient client, Proxy proxy, SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier) {
        this.client = client == null ? new OkHttpClient() : client;
        this.proxy = proxy;
        this.sslSocketFactory = sslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    public boolean supportsMethod(String method) {
        return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
    }

    @Override
    protected OkHttpRequest buildRequest(String method, String url) throws IOException {
        Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
        // connection with proxy settings
        URL connUrl = new URL(url);
        if (proxy != null) {
            client.setProxy(proxy);
        }

        // SSL settings
        if ("https".equals(connUrl.getProtocol())) {
            if (hostnameVerifier != null) {
                client.setHostnameVerifier(hostnameVerifier);
            }
            if (sslSocketFactory != null) {
                client.setSslSocketFactory(sslSocketFactory);
            }
        }
        return new OkHttpRequest(client, method, connUrl);
    }

    /**
     * Builder for {@link OkHttpTransport}.
     * <p/>
     * <p>
     * Implementation is not thread-safe.
     * </p>
     *
     * @since 1.13
     */
    public static final class Builder {

        /**
         * SSL socket factory or {@code null} for the default.
         */
        private SSLSocketFactory sslSocketFactory;

        /**
         * Host name verifier or {@code null} for the default.
         */
        private HostnameVerifier hostnameVerifier;

        /**
         * HTTP proxy or {@code null} to use the proxy settings from <a
         * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
         * properties</a>.
         */
        private Proxy proxy;

        private OkHttpClient client;

        /**
         * Sets the HTTP proxy or {@code null} to use the proxy settings from <a
         * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
         * properties</a>.
         * <p/>
         * <p>
         * For example:
         * </p>
         * <p/>
         * <pre>
         * setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)))
         * </pre>
         */
        public Builder setProxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * Sets the {@link OkHttpClient}
         * <b>This value is ignored if the {@link #setProxy} has been called with a non-null value.</b>
         * <p/>
         *
         * @since 1.20
         */
        public Builder setOkHttpClient(OkHttpClient client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the SSL socket factory based on root certificates in a Java KeyStore.
         * <p/>
         * <p>
         * Example usage:
         * </p>
         * <p/>
         * <pre>
         * trustCertificatesFromJavaKeyStore(new FileInputStream("certs.jks"), "password");
         * </pre>
         *
         * @param keyStoreStream input stream to the key store (closed at the end of this method in a
         *                       finally block)
         * @param storePass      password protecting the key store file
         * @since 1.14
         */
        public Builder trustCertificatesFromJavaKeyStore(InputStream keyStoreStream, String storePass)
                throws GeneralSecurityException, IOException {
            KeyStore trustStore = SecurityUtils.getJavaKeyStore();
            SecurityUtils.loadKeyStore(trustStore, keyStoreStream, storePass);
            return trustCertificates(trustStore);
        }

        /**
         * Sets the SSL socket factory based root certificates generated from the specified stream using
         * {@link java.security.cert.CertificateFactory#generateCertificates(java.io.InputStream)}.
         * <p/>
         * <p>
         * Example usage:
         * </p>
         * <p/>
         * <pre>
         * trustCertificatesFromStream(new FileInputStream("certs.pem"));
         * </pre>
         *
         * @param certificateStream certificate stream
         * @since 1.14
         */
        public Builder trustCertificatesFromStream(InputStream certificateStream)
                throws GeneralSecurityException, IOException {
            KeyStore trustStore = SecurityUtils.getJavaKeyStore();
            trustStore.load(null, null);
            SecurityUtils.loadKeyStoreFromCertificates(
                    trustStore, SecurityUtils.getX509CertificateFactory(), certificateStream);
            return trustCertificates(trustStore);
        }

        /**
         * Sets the SSL socket factory based on a root certificate trust store.
         *
         * @param trustStore certificate trust store (use for example {@link com.google.api.client.util.SecurityUtils#loadKeyStore}
         *                   or {@link com.google.api.client.util.SecurityUtils#loadKeyStoreFromCertificates})
         * @since 1.14
         */
        public Builder trustCertificates(KeyStore trustStore) throws GeneralSecurityException {
            SSLContext sslContext = SslUtils.getTlsSslContext();
            SslUtils.initSslContext(sslContext, trustStore, SslUtils.getPkixTrustManagerFactory());
            return setSslSocketFactory(sslContext.getSocketFactory());
        }

        /**
         * {@link com.google.api.client.util.Beta} <br/>
         * Disables validating server SSL certificates by setting the SSL socket factory using
         * {@link com.google.api.client.util.SslUtils#trustAllSSLContext()} for the SSL context and
         * {@link com.google.api.client.util.SslUtils#trustAllHostnameVerifier()} for the host name verifier.
         * <p/>
         * <p>
         * Be careful! Disabling certificate validation is dangerous and should only be done in testing
         * environments.
         * </p>
         */
        @Beta
        public Builder doNotValidateCertificate() throws GeneralSecurityException {
            hostnameVerifier = SslUtils.trustAllHostnameVerifier();
            sslSocketFactory = SslUtils.trustAllSSLContext().getSocketFactory();
            return this;
        }

        /**
         * Returns the SSL socket factory.
         */
        public SSLSocketFactory getSslSocketFactory() {
            return sslSocketFactory;
        }

        /**
         * Sets the SSL socket factory or {@code null} for the default.
         */
        public Builder setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }

        /**
         * Returns the host name verifier or {@code null} for the default.
         */
        public HostnameVerifier getHostnameVerifier() {
            return hostnameVerifier;
        }

        /**
         * Sets the host name verifier or {@code null} for the default.
         */
        public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * Returns a new instance of {@link OkHttpTransport} based on the options.
         */
        public OkHttpTransport build() {
            return new OkHttpTransport(client, proxy, sslSocketFactory, hostnameVerifier);
        }
    }
}
