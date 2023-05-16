package gui;

public class AlbumCard {
    private String albumName;
    private String artist;
    private String albumID;
    private int rating;
    private String imageURL;

    public AlbumCard(String albumID, String name, String artist, String imageURL) {
        this.albumID = albumID;
        this.albumName = name;
        this.artist = artist;
        this.imageURL = imageURL;
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

    public String getImageURL() {return imageURL;}

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Album(" + albumName + ", " + artist + ", " + albumID + ")";
    }
}
