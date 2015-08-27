package technology.mainthread.apps.moment.data.okhttp;

import android.support.annotation.NonNull;

import com.google.api.client.http.LowLevelHttpResponse;
import com.squareup.okhttp.Response;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

final class OkHttpResponse extends LowLevelHttpResponse {

    private final Response response;

    OkHttpResponse(Response response) {
        this.response = response;
    }

    @Override
    public int getStatusCode() {
        return response.code();
    }

    @Override
    public InputStream getContent() throws IOException {
        InputStream in = response.body().byteStream();
        return in == null ? null : new SizeValidatingInputStream(in);
    }

    @Override
    public String getContentEncoding() {
        return response.header("Content-Encoding");
    }

    @Override
    public long getContentLength() {
        String string = response.header("Content-Length");
        return string == null ? -1 : Long.parseLong(string);
    }

    @Override
    public String getContentType() {
        return response.header("Content-Type");
    }

    @Override
    public String getReasonPhrase() {
        return response.message();
    }

    @Override
    public String getStatusLine() {
        String result = response.protocol().toString();
        return result != null && result.startsWith("HTTP/1.") ? result : null;
    }

    @Override
    public int getHeaderCount() {
        return response.headers().size();
    }

    @Override
    public String getHeaderName(int index) {
        return response.headers().name(index);
    }

    @Override
    public String getHeaderValue(int index) {
        return response.headers().value(index);
    }

    /**
     * A wrapper around the base {@link InputStream} that validates EOF returned by the read calls.
     *
     * @since 1.20
     */
    private final class SizeValidatingInputStream extends FilterInputStream {

        private long bytesRead = 0;

        public SizeValidatingInputStream(InputStream in) {
            super(in);
        }

        /**
         * java.io.InputStream#read(byte[], int, int) swallows IOException thrown from read() so we have
         * to override it.
         *
         * @see "http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8-b132/java/io/InputStream.java#185"
         */
        @Override
        public int read(@NonNull byte[] b, int off, int len) throws IOException {
            int n = in.read(b, off, len);
            if (n == -1) {
                throwIfFalseEOF();
            } else {
                bytesRead += n;
            }
            return n;
        }

        @Override
        public int read() throws IOException {
            int n = in.read();
            if (n == -1) {
                throwIfFalseEOF();
            } else {
                bytesRead++;
            }
            return n;
        }

        // Throws an IOException if gets an EOF in the middle of a response.
        private void throwIfFalseEOF() throws IOException {
            long contentLength = getContentLength();
            if (contentLength == -1) {
                // If a Content-Length header is missing, there's nothing we can do.
                return;
            }
            // According to RFC2616, message-body is prohibited in responses to certain requests, e.g.,
            // HEAD. Nevertheless an entity-header (possibly with non-zero Content-Length) may be present.
            // Thus we exclude the case where bytesRead == 0.
            //
            // See http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.4 for details.
            if (bytesRead != 0 && bytesRead < contentLength) {
                throw new IOException("Connection closed prematurely: bytesRead = " + bytesRead
                        + ", Content-Length = " + contentLength);
            }
        }
    }
}
