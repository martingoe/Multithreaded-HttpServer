import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Request {
    private String query;
    private String path;
    private String protocol;

    private OutputStream outputStream;

    private HashMap<String, String> requestHeaders;
    private HashMap<String, String> responseHeaders;

    /**
     * Returns the protocol
     * @return protocol used by the request
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the requestHeaders
     * @return requestHeaders sent by the request
     */
    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Returns the path
     * @return path used by the request
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the query
     * @return query used by the request as a {@link String}
     */
    public String getQueryAsString() {
        return query;
    }

    /**
     * Returns the query
     * @return query used by the request as a {@link HashMap}
     */
    public HashMap<String, String> getQueryAsMap() {
        HashMap<String, String> hashMap = new HashMap<>();

        String[] arguments = query.split("&");

        for (String argument : arguments) {
            String[] keyAndValue = argument.split("=");
            hashMap.put(keyAndValue[0], keyAndValue[1]);
        }
        return hashMap;
    }


    /**
     * class Constructor
     * Make the information given by the client/request usable
     *
     * @param client the {@link Socket} used to send the request
     */
    public Request(Socket client) throws IOException {
        String requestString = getRequestString(client);
        String str = requestString.substring(requestString.indexOf("\n") + 1);
        HashMap<String, String> additionalInformation = getAdditionalInformation(requestString);

        query = additionalInformation.get("query");
        path = additionalInformation.get("path");
        protocol = additionalInformation.get("protocol");

        responseHeaders = new HashMap<>();

        requestHeaders = (HashMap<String, String>) Arrays.stream(str.split("\n")).map(s -> s.split(":"))
                .collect(Collectors.toMap(e -> e[0], e -> (e[1])));


        this.outputStream = client.getOutputStream();
    }

    /**
     * Adds a context to {@link #responseHeaders}
     *
     * @param type the type of the header e.g. 'Content-Type'
     * @param value the value of the header added
     */
    void addResponseHeader(String type, String value) {
        responseHeaders.put(type, value);
    }

    /**
     * Sends a response to the client
     *
     * @param text the message sent
     * @param rCode the response Code used
     */
    void sendResponse(String text, int rCode) {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append(protocol + rCode + "\n");

        responseHeaders.forEach((key, value) -> responseStringBuilder.append(key + ": " + value + "\n"));

        responseStringBuilder.append("\n" + text);

        try {
            outputStream.write(responseStringBuilder.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets additional information from the first line of the request
     *
     * @param requestString the request sent
     * @return the {@link HashMap} containing the new information
     */
    private HashMap<String, String> getAdditionalInformation(String requestString) {
        HashMap<String, String> returnObject = new HashMap<>();

        requestString = requestString.replaceFirst("GET ", "");
        int indexAfterText = requestString.indexOf(" ");

        returnObject.put("protocol", requestString.substring(indexAfterText + 1));

        String url = requestString.substring(0, indexAfterText);
        if (requestString.contains("?")) {
            String splitPattern = Pattern.quote("?");
            String[] informationInURL = url.split(splitPattern);

            returnObject.put("path", informationInURL[0]);
            returnObject.put("query", informationInURL[1]);
        } else {
            returnObject.put("path", url);
            returnObject.put("query", null);
        }
        return returnObject;
    }

    /**
     * Gets the {@link String} sent by the client
     *
     * @param client the {@link Socket} having sent the request
     * @return the received request
     */
    private String getRequestString(Socket client) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            StringBuilder requestStringBuilder = new StringBuilder();
            String line;
            while (!(line = bufferedReader.readLine()).equals("")) {
                requestStringBuilder.append(line);
            }
            return requestStringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
