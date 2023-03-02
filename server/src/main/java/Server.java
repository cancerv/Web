import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Server {
    private final int threadPoolSize = 64;

    public void listen(int port) {
        var pool = Executors.newFixedThreadPool(this.threadPoolSize);
        try {
            final var serverSocket = new ServerSocket(port);
            while (true) {
                final Socket socket = serverSocket.accept();
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
                pool.execute(new RequestHandler(in, out));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pool.shutdown();
    }
}
