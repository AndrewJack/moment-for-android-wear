package technology.mainthread.apps.moment.data.okhttp;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.Preconditions;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.http.HttpMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

final class OkHttpRequest extends LowLevelHttpRequest {

    private final OkHttpClient client;
    private final String method;
    private final Request.Builder requestBuilder;

    /**
     * @param client Http client
     * @param method Http method
     * @param url    Url
     */
    OkHttpRequest(OkHttpClient client, String method, URL url) {
        this.client = client;
        this.client.setFollowRedirects(false);
        this.method = method;
        this.requestBuilder = new Request.Builder().url(url);
    }

    @Override
    public void addHeader(String name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public void setTimeout(int connectTimeout, int readTimeout) {
        client.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        client.setReadTimeout(readTimeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public LowLevelHttpResponse execute() throws IOException {
        // write content
        RequestBody requestBody = null;
        if (getStreamingContent() != null) {
            String contentType = getContentType();
            if (contentType != null) {
                addHeader("Content-Type", contentType);
            }
            String contentEncoding = getContentEncoding();
            if (contentEncoding != null) {
                addHeader("Content-Encoding", contentEncoding);
            }
            long contentLength = getContentLength();
            if (contentLength >= 0) {
                addHeader("Content-Length", Long.toString(contentLength));
            }

            // add request body
            if (HttpMethod.permitsRequestBody(method)) {
                // parse media type
                MediaType mediaType = null;
                if (contentType != null) {
                    mediaType = MediaType.parse(contentType);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                getStreamingContent().writeTo(outputStream);
                requestBody = RequestBody.create(
                        mediaType,
                        outputStream.toByteArray()
                );
            } else {
                // cannot add body because it would change a GET method to POST
                // for HEAD, OPTIONS, or TRACE it would throw a IllegalArgumentException
                Preconditions.checkArgument(
                        contentLength == 0, "%s with non-zero content length is not supported", method);
            }
        }
        // send
        Request request = requestBuilder.method(method, requestBody).build();
        Response response = client.newCall(request).execute();
        return new OkHttpResponse(response);
    }
}
