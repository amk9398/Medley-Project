package gui.model;

import api.spotify.albumController;
import api.spotify.artistController;
import database.Response;
import database.Status;
import database.libraryController;
import gui.util.AlbumCard;
import gui.util.ImageTools;
import gui.util.Observer;
import javafx.scene.image.Image;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AppModel {
    List<Observer<AppModel, String>> observers = new LinkedList<>();
    Connection conn;
    int userID;
    String token;

    public ArrayList<AlbumCard> libraryCards = new ArrayList<>();
    public ArrayList<AlbumCard> searchCards = new ArrayList<>();
    public String sortMethod = "name";
    public String sortOrder = "asc";
    private String currentScene = "library";
    public int numLibraryPages = 1;
    public int currentLibraryPage = 1;
    private HashMap<String, Image> artistImageMap = new HashMap<>();

    public AppModel(Connection conn, int userID, String token) {
        this.conn = conn;
        this.userID = userID;
        this.token = token;
        libraryCards = libraryController.getUserAlbums(conn, userID, sortMethod, sortOrder);
    }

    public void addObserver(Observer<AppModel, String> observer) {observers.add(observer);}

    private void updateObservers(String msg) {
        for (var observer : observers) {
            observer.update(this, msg);
        }
    }

    public String getCurrentScene() {return currentScene;}
    public void setCurrentScene(String scene) {
        currentScene = scene;
        updateObservers("set current scene");
    }

    public void updateSearchCards(String query) {
        try {
            if(query.length() > 0) searchCards = albumController.searchResults(query, token, 12);
            else searchCards = new ArrayList<>();
            updateObservers("update search cards");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void addAlbumToLibrary(AlbumCard albumCard) {
        Response res = libraryController.addAlbumToUserLibrary(conn, userID, albumCard);
        if (res.status == Status.ERROR) System.out.println(res.message);
        else libraryCards.add(albumCard);
        updateObservers("add album to library");
    }

    public void removeAlbumFromLibrary(String albumID) {
        libraryController.removeAlbumFromUserLibrary(conn, userID, albumID);
        libraryCards.removeIf(albumCard -> albumCard.getAlbumID().equals(albumID));
        updateObservers("remove album from library");
    }

    public void mergeSpotifyLibrary() {
        try {
            for(AlbumCard albumCard: albumController.getUserAlbums(token)) {
                addAlbumToLibrary(albumCard);
            }
        } catch (IOException ex) {ex.printStackTrace();}
        updateObservers("merge spotify library");
    }

    public void rateAlbum(String albumID, int rating) {
        Response res = libraryController.rateAlbum(conn, userID, albumID, rating);
        if(res.status == Status.ERROR) System.out.println(res.message);
        else {
            for (AlbumCard albumCard : libraryCards) {
                if (albumCard.getAlbumID().equals(albumID)) {
                    albumCard.setRating(rating);
                }
            }
        }
        updateObservers("rate album");
    }

    public void setSortMethod(String sortMethod) {
        if (this.sortMethod.equals(sortMethod)) {
            this.sortOrder = this.sortOrder.equals("asc") ? "desc" : "asc";
        } else {
            this.sortMethod = sortMethod;
            this.sortOrder = "asc";
        }
        libraryCards = libraryController.getUserAlbums(conn, userID, sortMethod, sortOrder);
        updateObservers("set sort method");
    }

    public Image getArtistImage(String artistID) {
        try {
            if (artistImageMap.containsKey(artistID)) {
                return artistImageMap.get(artistID);
            } else {
                Image image = ImageTools.retrieveImage(artistController.getArtistImageURL(token, artistID));
                artistImageMap.put(artistID, image);
                return image;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
