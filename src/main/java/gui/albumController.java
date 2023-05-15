package gui;

import tools.AlbumCard;
import tools.JsonBuilder;
import tools.JsonTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class albumController {
    public static ArrayList<AlbumCard> getUserAlbums(String authToken) throws IOException {
        int limit = 5;
        URL url = new URL("https://api.spotify.com/v1/me/albums/?limit=" + limit + "&offset=0&market=ES");
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

        JsonBuilder jsonBuilder = new JsonBuilder();
        JsonTree jsonTree = jsonBuilder.parse(builder.toString());

        ArrayList<AlbumCard> albumCards = new ArrayList<>();
        for(int i = 0; i < limit; i++) {
            JsonTree album = jsonTree.get("items").get(Integer.toString(i)).get("album");
            String name = album.get("name").retrieveValue();
            String id = album.get("id").retrieveValue();
            String artist = album.get("artists").get("0").get("name").retrieveValue();
            albumCards.add(new AlbumCard(id, name, artist));
        }

        in.close();
        con.disconnect();
        return albumCards;
    }

    public static String getAlbumID(String query, String authToken) throws IOException {
        String searchResult = searchResults(query, authToken, 1);
        String albumID = null;
        boolean foundID = false;

        for(String line : searchResult.split("\n")) {
            if(line.contains("\"id\"")) {
                if (foundID) {albumID = line.substring(14, line.length()-2);}
                foundID = true;
            }
        }
        return albumID;
    }

    public static String retrieveImageUrl(String albumID, String auth_token) throws IOException {
        URL url = new URL("https://api.spotify.com/v1/albums/" +  albumID + "?market=ES");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + auth_token);

        int status = con.getResponseCode();
        Reader streamReader = null;
        if (status > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
        } else {
            streamReader = new InputStreamReader(con.getInputStream());
        }

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        String imageURL = "";
        while ((inputLine = in.readLine()) != null) {
            String line = inputLine.replaceAll("\\s", "");
            if (line.contains("\"url\"")) {
                int length = line.length();
                imageURL = line.substring(7, length-2);
                break;
            }
        }

        in.close();
        con.disconnect();

        return imageURL;
    }


    public static String searchResults(String query, String auth_token, int limit) throws IOException {
        URL url = new URL("https://api.spotify.com/v1/search?q=" +
                query.replaceAll(" ", "%20") +
                "&type=album&limit=" + limit);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + auth_token);

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

        in.close();
        con.disconnect();

        return builder.toString();
    }

}
