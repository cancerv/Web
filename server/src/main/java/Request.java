import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final String method;
    private final String route;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, String> queryParams;

    private final static int readSize = 1024;

    Request(String method, String route, Map<String, String> headers, String body, Map<String, String> queryParams) {
        this.method = method;
        this. route = route;
        this.headers = headers;
        this.body = body;
        this.queryParams = queryParams;
    }

    public static Request fromReader(BufferedReader in) throws IOException, RuntimeException, URISyntaxException {
        char[] requestIn = new char[readSize];
        StringBuilder requestString = new StringBuilder();

        while (true) {
            int read = in.read(requestIn, 0, readSize);
            if (read > 0) {
                requestString.append(String.valueOf(requestIn));
            }
            if (read < readSize) {
                break;
            }
        }

        String[] parts = requestString.toString().split("\r\n\r\n");
        String[] headersStrings = parts[0].split("\r\n");

        if (headersStrings.length == 0) {
            throw new RuntimeException("Bad request");
        }

        String[] routeParts = headersStrings[0].split(" ");

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < headersStrings.length; i++) {
            String[] headerParts = headersStrings[i].split(": ");
            headers.put(headerParts[0], headerParts[1]);
        }

        String decoded = URLDecoder.decode(routeParts[1], StandardCharsets.UTF_8);
        URI uri = new URI(decoded);
        List<NameValuePair> params = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);

        Map<String, String> queryParams = new HashMap<>();
        for (NameValuePair param : params) {
            queryParams.put(param.getName(), param.getValue());
        }

        return new Request(routeParts[0], uri.getPath(), headers, parts.length > 1 ? parts[1].trim() : null, queryParams);
    }

    public String getMethod() {
        return this.method;
    }

    public String getRoute() {
        return this.route;
    }

    public String getBody() {
        return this.body;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public Map<String, String> getQueryParams() {
        return this.queryParams;
    }

    public String getQueryParam(String name) {
        if (this.queryParams.containsKey(name)) {
            return this.queryParams.get(name);
        }
        return null;
    }
}
