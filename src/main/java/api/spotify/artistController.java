package api.spotify;

import api.tools.JsonBuilder;
import api.tools.JsonTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class artistController {
    public static JsonTree getArtistInfo(String authToken, String artistID) throws IOException {
        return getArtist(authToken, "https://api.spotify.com/v1/artists/" + artistID);
    }

    public static String getArtistName(String authToken, String artistID) throws IOException {
        return getArtist(authToken, "https://api.spotify.com/v1/artists/" + artistID)
                .get("name")
                .retrieveValue();
    }

    public static int getArtistFollowers(String authToken, String artistID) throws IOException {
        return Integer.parseInt(getArtist(authToken, "https://api.spotify.com/v1/artists/" + artistID)
                .get("followers")
                .get("total")
                .retrieveValue()
        );
    }

    public static ArrayList<String> getArtistGenres(String authToken, String artistID) throws IOException {
        String[] tokens =  getArtist(authToken, "https://api.spotify.com/v1/artists/" + artistID)
                .get("genres")
                .retrieveValue()
                .split("\"");
        return new ArrayList<>(Arrays.asList(tokens).subList(3, tokens.length));
    }

    public static String getArtistImageURL(String authToken, String artistID) throws IOException {
        return getArtist(authToken, "https://api.spotify.com/v1/artists/" + artistID)
                .get("images")
                .get("0")
                .get("url")
                .retrieveValue();
    }

    public static ArrayList<String> getArtistTracks(String authToken, String artistID) throws IOException {
        JsonTree jsonTree = getArtist(authToken, "https://api.spotify.com/v1/artists/" + artistID + "/top-tracks?market=US");
        ArrayList<String> tracks = new ArrayList<>();
        tracks.add(jsonTree.get("tracks").get("0").get("name").retrieveValue());
        tracks.add(jsonTree.get("tracks").get("1").get("name").retrieveValue());
        tracks.add(jsonTree.get("tracks").get("2").get("name").retrieveValue());
        return tracks;
    }

    public static String getRelatedArtist(String authToken, String artistID) throws IOException {
        return getArtist(authToken, "https://api.spotify.com/v1/artists/" + artistID + "/related-artists")
                .get("artists")
                .get("0")
                .get("name")
                .retrieveValue();
    }

    public static JsonTree getArtist(String authToken, String endpointURL) throws IOException {
        URL url = new URL(endpointURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + authToken);

        int status = con.getResponseCode();
        Reader streamReader;
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

        return JsonBuilder.parse(builder.toString());
    }
}
