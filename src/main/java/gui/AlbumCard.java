package gui;

public class AlbumCard {
    private String albumName;
    private String artist;
    private String albumID;
    private int rating;


    public AlbumCard(String albumID, String name, String artist) {
        this.albumID = albumID;
        this.albumName = name;
        this.artist = artist;
    }

    public String getAlbumID() {
        return albumID;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtist() {
        return artist;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
