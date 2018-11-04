import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class HttpServer {

    HashMap<String, Handler> contexts;
    private boolean acceptRequests;
    private ServerSocket serverSocket;


    /**
     * @param port the port used for the webserver
     * @throws IOException because initializing a {@link ServerSocket} does
     */
    public HttpServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        contexts = new HashMap<>();
    }

    /**
     * Stops the loop started in {@link #start()}
     */
    public void stop() {
        acceptRequests = false;
    }

    /**
     * Starts a loop that accepts any requests coming in and sending it to a {@link Handler} if necessary
     *
     * @throws IOException because serverSocket.accept() can throw an IOException
     */
    public void start() throws IOException {
        acceptRequests = true;
        while (acceptRequests) {
            Socket client = serverSocket.accept();
            new Thread(() -> {
                try {
                    Request request = new Request(client);
                    if (contexts.containsKey(request.getPath())) {
                        contexts.get(request.getPath()).handle(request);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Adds a context to the context HashMap
     *
     * @param path    the path to a context
     * @param handler the handler handling the request
     */
    void addContext(String path, Handler handler) {
        contexts.put(path, handler);
    }
}
