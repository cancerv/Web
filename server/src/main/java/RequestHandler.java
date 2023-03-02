import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class RequestHandler implements Runnable {
    final private BufferedReader in;
    final private BufferedOutputStream out;
    final private Map<String, Map<String, Handler>> routes;

    public RequestHandler(BufferedReader in, BufferedOutputStream out, Map<String, Map<String, Handler>> routes) {
        this.in = in;
        this.out = out;
        this.routes = routes;
    }

    private void sendNotFoundError() throws IOException {
        this.out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        this.out.flush();
    }

    @Override
    public void run() {
        try {
            Request request = Request.fromReader(this.in);
            if (this.routes.containsKey(request.getMethod())) {
                Map<String, Handler> methodRoutes = this.routes.get(request.getMethod());
                if (methodRoutes.containsKey(request.getRoute())) {
                    Handler handler = methodRoutes.get(request.getRoute());
                    handler.handle(request, this.out);
                    return;
                }
            }
            this.sendNotFoundError();
        } catch (IOException | URISyntaxException exception) {
            exception.printStackTrace();
        }
    }
}
