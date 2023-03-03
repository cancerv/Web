import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class Server {
    private final int threadPoolSize = 64;

    private Map<String, Map<String, Handler>> routes = new HashMap<>();

    public void listen(int port) {
        var pool = Executors.newFixedThreadPool(this.threadPoolSize);
        try {
            final var serverSocket = new ServerSocket(port);
            while (true) {
                final Socket socket = serverSocket.accept();
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
                pool.execute(new RequestHandler(in, out, routes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pool.shutdown();
    }

    public void addHandler(String method, String route, Handler handler) {
        if (!this.routes.containsKey(method)) {
            this.routes.put(method, new HashMap<>());
        }

        this.routes.get(method).put(route, handler);
    }
}
