import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        server.addHandler("GET", "/messages", (Request request, BufferedOutputStream out) -> {
            final Path filePath = Path.of(".", "public", "classic.html");
            try {
                final String mimeType = Files.probeContentType(filePath);
                String template = Files.readString(filePath);

                template = template.replace("{time}", LocalDateTime.now().toString())
                                .replace("{name}", request.getQueryParam("name"));

                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + template.length() + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(template.getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.addHandler("POST", "/messages", (Request request, BufferedOutputStream out) -> {
            try {
                String body = request.getBody();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + request.getHeader("Content-Type") + "\r\n" +
                                "Content-Length: " + body.length() + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(body.getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.listen(9999);
    }
}
