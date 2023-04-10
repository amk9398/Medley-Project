package gui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.application.Application;
import javafx.stage.Stage;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import tools.AlbumCard;
import tools.ParameterStringBuilder;

public class T extends Application {

    static String clientID = "469af18e875a4fa1a58390d147ed924e";
    static String clientSecret = "b142d702a4674c37b84c5928f482a7e5";
    String redirectURI = "http://localhost:8080";


    @Override
    public void init() {

    }


    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        UserAuthentication userAuth = new UserAuthentication(clientID, clientSecret, redirectURI);
        String auth_token = userAuth.getUserAuthToken();

        String albumName = "atliens";

        HBox hbox = new HBox();
        ImageView imageView1 = new ImageView();
        imageView1.setImage(retrieveImage(retrieveImageUrl(retrieveAlbumID(albumName, auth_token), auth_token)));
        imageView1.setFitHeight(200);
        imageView1.setFitWidth(200);
        hbox.getChildren().add(imageView1);

        Scene testScene = new Scene(hbox);
        stage.setScene(testScene);
        stage.show();
    }


    public static Image retrieveImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        return image;
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


    public static String retrieveAlbumID(String albumName, String auth_token) throws IOException {
        URL url = new URL("https://api.spotify.com/v1/search?market=ES&q=" +
                                albumName.replaceAll(" ", "%20") +
                                "&type=album&limit=1");
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
        String albumID = "";
        boolean foundID = false;
        while ((inputLine = in.readLine()) != null) {
            String line = inputLine.replaceAll("\\s", "");
            if (line.contains("\"id\"")) {
                if (foundID) {albumID = line.substring(6, line.length()-2);}
                foundID = true;
            }
        }
        in.close();
        con.disconnect();

        return albumID;
    }

    public static void searchResults(String query, String auth_token) throws IOException {
        URL url = new URL("https://api.spotify.com/v1/search?q=" +
                query.replaceAll(" ", "%20") +
                "&type=album&limit=10");
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
        String jsonString = "";
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            jsonString += inputLine;
        }
        in.close();
        con.disconnect();

    }

}
