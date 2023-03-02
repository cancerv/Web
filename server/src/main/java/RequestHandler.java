import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class RequestHandler implements Runnable {
    final private BufferedReader in;
    final private OutputStream out;

    private final List<String> validPaths = List.of("/classic.html", "/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public RequestHandler(BufferedReader in, BufferedOutputStream out) {
        this.in = in;
        this.out = out;
    }

    private String getPath() throws RuntimeException, IOException {
        final var requestLine = this.in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            throw new RuntimeException("Bad request");
        }

        final var path = parts[1];
        if (!validPaths.contains(path)) {
            this.sendNotFoundError();
            throw new RuntimeException("Not found");
        }

        return path;
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

    private void sendOkResponse(byte[] content, String mimeType) throws IOException {
        this.out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        this.out.write(content);
        this.out.flush();
    }

    @Override
    public void run() {
        try {
            String requestPath = this.getPath();
            final Path filePath = Path.of(".", "public", requestPath);
            final String mimeType = Files.probeContentType(filePath);
            String template = Files.readString(filePath);

            if (requestPath.equals("/classic.html")) {
                template = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                );
            }

            this.sendOkResponse(template.getBytes(), mimeType);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
