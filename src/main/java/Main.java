import java.io.IOException;
import java.util.HashMap;

public class Main {
    public Main() {
        try {
            HttpServer httpServer = new HttpServer(2000);

            httpServer.addContext("/test", new TestHandler());

            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main();
    }

    public class TestHandler extends Thread implements Handler {

        @Override
        public void handle(Request request) {
            HashMap<String, String> query = request.getQueryAsMap();


            request.addResponseHeader("Access-Allow-Content-Origin", "*");

            request.sendResponse(query.get("test"), 200);
        }
    }
}
