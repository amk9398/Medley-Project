package gui;

import tools.ParameterStringBuilder;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class UserAuthentication implements Runnable {
    private final String clientID;
    private final String clientSecret;
    private final String redirectURI;
    private String code;

    Thread thread = new Thread(this);
    ServerSocket localHostServerSocket = new ServerSocket(8080);

    public UserAuthentication(String clientID, String clientSecret, String redirectURI) throws IOException {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.redirectURI = redirectURI;
    }

    @Override
    public void run() {
        try {
            Socket socket = localHostServerSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            socket.close();
            code = line.substring(11, line.length() - 9);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserAuthToken() {
        thread.start();
        requestUserLogin();
        String token = authorizationFlowToken();
        if(token.equals("")) System.out.println("ERROR: User Login Error");
        // TODO: Create User login error class
        return token;
    }

    private void requestUserLogin() {
        String urlString = "https://accounts.spotify.com/authorize?" +
                "client_id=" + clientID + "&" +
                "response_type=code" + "&" +
                "scope=user-library-read" + "&" +
                "redirect_uri=" + redirectURI;

        try {
            java.awt.Desktop.getDesktop().browse(URI.create(urlString));
            thread.join();
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        }
    }

    private String authorizationFlowToken() {
        String auth_token = "";
        String urlString = "https://accounts.spotify.com/api/token?" +
                "grant_type=authorization_code&" +
                "code=" + code + "&" +
                "redirect_uri=" + redirectURI;

        try {
            // open post connection
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            // set query headers
            Map<String, String> parameters = new HashMap<>();
            String decodedString = clientID + ":" + clientSecret;
            String encodedString = Base64.getEncoder().encodeToString(decodedString.getBytes());
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Authorization", "Basic " + encodedString);

            // execute request
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            out.flush();
            out.close();

            // check for error response code
            int status = con.getResponseCode();
            Reader streamReader = null;
            if (status > 299) {
                streamReader = new InputStreamReader(con.getErrorStream());
            } else {
                streamReader = new InputStreamReader(con.getInputStream());
            }
            BufferedReader in = new BufferedReader(streamReader);
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            // parse JSON response body for auth token
            auth_token = response.toString().split("\"")[3];

            in.close();
            con.disconnect();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return auth_token;
    }
}
