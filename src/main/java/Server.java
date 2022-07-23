import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server extends Thread {

    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private volatile Socket connectSocket = null;

    private final Map<Integer, String> responseCodes;

    public Server() {
        responseCodes = new HashMap<>();
        responseCodes.put(200, "OK");
        responseCodes.put(404, "Not Found");
    }

    @Override
    public void run() {
        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                try (
                        final var socket = serverSocket.accept()
                ) {
                    connectSocket = socket;
                    Runnable connect = new Connect(connectSocket);
                    Future future = threadPool.submit(connect);
                    future.get();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private class Connect implements Runnable {

        private Socket socket;

        public Connect(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream())
            ) {

                final var requestLine = in.readLine();
                final var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    return;
                }

                var path = "";
                int indexParamSymbol = parts[1].indexOf("?");
                if (indexParamSymbol != -1) {
                    int indexParamSymbolEnd = parts[1].indexOf(".");
                    path = parts[1].replace(parts[1].substring(indexParamSymbol, indexParamSymbolEnd), "");
                } else {
                    path = parts[1];
                }
                if (!validPaths.contains(path)) {
                    writeResponse(out, 404, 0, null);
                    out.flush();
                    return;
                }

                new Request(parts[0], parts[2], parts[1]);

                final var filePath = Path.of(".", "public", path);
                final var mimeType = Files.probeContentType(filePath);

                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    writeResponse(out, 200, content.length, mimeType);
                    out.write(content);
                    out.flush();
                    return;
                }

                final var length = Files.size(filePath);
                writeResponse(out, 200, length, mimeType);
                Files.copy(filePath, out);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void writeResponse(BufferedOutputStream out, int code, long contentLength, String сontentType) throws IOException {
            out.write((
                    responseString(code, contentLength, сontentType)
            ).getBytes());
        }

        private String responseString(int code, long contentLength, String сontentType) {
            сontentType = сontentType == null ? "text" : сontentType;
            return String.format("HTTP/1.1 %d %s\r\n" +
                    "Content-Type: %s\r\n" +
                    "Content-Length: %d\r\n" +
                    "Connection: close\r\n" +
                    "\r\n", code, responseCodes.get(code), сontentType, contentLength);
        }
    }
}
