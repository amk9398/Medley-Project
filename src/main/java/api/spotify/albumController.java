package api.spotify;

import gui.util.AlbumCard;
import api.tools.JsonBuilder;
import api.tools.JsonTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class albumController {
    public static ArrayList<AlbumCard> getUserAlbums(String authToken) throws IOException {
        int offset = 0;
        ArrayList<AlbumCard> albumCards = new ArrayList<>();
        while(true) {
            URL url = new URL("https://api.spotify.com/v1/me/albums/?limit=50&offset=" + 50*(offset++) + "&market=ES");
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

            JsonTree jsonTree = JsonBuilder.parse(builder.toString());
            int numAlbums = jsonTree.get("items").getKeys().size();
            System.out.println(numAlbums);
            for (int i = 0; i < numAlbums; i++) {
                JsonTree album = jsonTree.get("items").get(Integer.toString(i)).get("album");
                String name = album.get("name").retrieveValue();
                String id = album.get("id").retrieveValue();
                String artist = album.get("artists").get("0").get("name").retrieveValue();
                String artistID = album.get("artists").get("0").get("id").retrieveValue();
                String image = album.get("images").get("0").get("url").retrieveValue();
                albumCards.add(new AlbumCard(id, name, artist, artistID, image));
            }

            if(numAlbums < 50) break;
        }

        return albumCards;
    }


    public static ArrayList<AlbumCard> searchResults(String query, String auth_token, int limit) throws IOException {
        URL url = new URL("https://api.spotify.com/v1/search?q=" +
                query.replaceAll(" ", "%20") +
                "&type=album&limit=" + limit);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + auth_token);

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

        JsonTree jsonTree = JsonBuilder.parse(builder.toString());
        ArrayList<AlbumCard> albumCards = new ArrayList<>();
        for(int i = 0; i < limit; i++) {
            JsonTree album = jsonTree.get("albums").get("items").get(Integer.toString(i));
            String id = album.get("id").retrieveValue();
            String name = album.get("name").retrieveValue();
            String artist = album.get("artists").get("0").get("name").retrieveValue();
            String artistID = album.get("artists").get("0").get("id").retrieveValue();
            String image = album.get("images").get("0").get("url").retrieveValue();
            albumCards.add(new AlbumCard(id, name, artist, artistID, image));
        }

        return albumCards;
    }

}
