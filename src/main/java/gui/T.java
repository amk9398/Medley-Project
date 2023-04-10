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
import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;

import javafx.embed.swing.SwingFXUtils;
import tools.AlbumCard;
import tools.ParameterStringBuilder;

public class T extends Application implements Runnable {

    String ACCESS_TOKEN = "BQBytXIMtofYaw-edS874YgHBeSCgCiq1jTtPAgyWB-LnGPKUXvqCMvfuVXcCTrA9SytcG_StrZwKMueVVhhNF8kSbFAfOws6DbQ2sLAjiwI560Cj48J2bNo4CSumTjzYSRRjh0yOu6yCUH9P4UVEL5KizPW3hFoJNSlwilOTyBbA9BmY5PvRqFiob2fz1IwdBw";
    static String clientID = "469af18e875a4fa1a58390d147ed924e";
    static String clientSecret = "b142d702a4674c37b84c5928f482a7e5";
    String redirectURI = "http://localhost:8080";
    String code;

    ServerSocket serverSocket;
    Socket socket;
    Thread thread = new Thread(this);

    public T() throws IOException {
        // System.setProperty("webdriver.chrome.driver", "\"C:\\Users\\aaron\\Downloads\\chromedriver_win32\\chromedriver.exe\"");

        serverSocket = new ServerSocket(8080);


    }

    @Override
    public void run() {
        try {
            socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            socket.close();
            code = line.substring(11, line.length() - 9);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        thread.start();

        String albumName = "my beautiful dark twisted fantasy";
        requestUserLogin();



        String auth_token = AuthorizationFlowToken();

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
        String jsonString = "";
        while ((inputLine = in.readLine()) != null) {
            jsonString += inputLine;
        }
        in.close();
        con.disconnect();

    }

    public AlbumCard createAlbumCard(String albumName) {
        return null;
    }

    public void connectToAPI() throws IOException {

        int state = (int) (Math.random() * 16);
        String urlString = "https://accounts.spotify.com/authorize?" +
                        "client_id=" + clientID + "&" +
                        "response_type=code&" +
                        "redirect_uri" + redirectURI + "&" +
                        "scope=user-library-read&" +
                        "state=" + state;
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int status = con.getResponseCode();
        Reader streamReader = null;
        if (status > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
        } else {
            streamReader = new InputStreamReader(con.getInputStream());
        }
        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
        }
        in.close();
        con.disconnect();
    }


    public static Image retrieveImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        return image;
    }

    public static String getToken() throws IOException {
        String urlString = "https://accounts.spotify.com/api/token?grant_type=client_credentials";
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        Map<String, String> parameters = new HashMap<>();
        String decodedString = clientID + ":" + clientSecret;
        String encodedString = Base64.getEncoder().encodeToString(decodedString.getBytes());
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Authorization", "Basic " + encodedString);

        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
        out.flush();
        out.close();

        int status = con.getResponseCode();
        Reader streamReader = null;
        if (status > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
        } else {
            streamReader = new InputStreamReader(con.getInputStream());
        }
        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        String response = "";
        while ((inputLine = in.readLine()) != null) {
            response += inputLine;
        }
        String auth_token = response.split("\"")[3];
        in.close();
        con.disconnect();
        return auth_token;
    }

    public String requestUserLogin() throws IOException, InterruptedException {
        String urlString = "https://accounts.spotify.com/authorize?" +
                                "client_id=" + clientID + "&" +
                                "response_type=code" + "&" +
                                "redirect_uri=" + redirectURI;
        java.awt.Desktop.getDesktop().browse(URI.create(urlString));

       thread.join();

        return null;
    }

    public String AuthorizationFlowToken() throws IOException {
        String urlString = "https://accounts.spotify.com/api/token?" +
                            "grant_type=authorization_code&" +
                            "code=" + code + "&" +
                            "redirect_uri=" + redirectURI;
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        Map<String, String> parameters = new HashMap<>();
        String decodedString = clientID + ":" + clientSecret;
        String encodedString = Base64.getEncoder().encodeToString(decodedString.getBytes());
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Authorization", "Basic " + encodedString);

        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
        out.flush();
        out.close();

        int status = con.getResponseCode();
        Reader streamReader = null;
        if (status > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
        } else {
            streamReader = new InputStreamReader(con.getInputStream());
        }
        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        String response = "";
        while ((inputLine = in.readLine()) != null) {
            response += inputLine;
        }
        String auth_token = response.split("\"")[3];
        in.close();
        con.disconnect();
        return auth_token;
    }

}
