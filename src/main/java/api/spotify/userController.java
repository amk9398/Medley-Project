package api.spotify;

import api.tools.JsonBuilder;
import api.tools.JsonTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class userController {
    public static String getUserInfo(String authToken, String field) throws IOException {
        URL url = new URL("https://api.spotify.com/v1/me");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + authToken);

        int status = con.getResponseCode();
        Reader streamReader = null;
        if (status > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
        } else {
            streamReader = new InputStreamReader(con.getInputStream());
        }

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder builder = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            builder.append(inputLine).append("\n");
        }

        JsonTree jsonTree = JsonBuilder.parse(builder.toString());

        return switch (field) {
            case "username" -> jsonTree.get("display_name").retrieveValue();
            case "followers" -> jsonTree.get("followers").get("total").retrieveValue();
            case "id" -> jsonTree.get("id").retrieveValue();
            case "image" -> jsonTree.get("images").get("0").get("url").retrieveValue();
            default -> null;
        };
    }
}
