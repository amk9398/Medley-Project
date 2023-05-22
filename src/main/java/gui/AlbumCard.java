package gui;

public class AlbumCard {
    private final String albumName;
    private final String artist;
    private final String albumID;
    private final String imageURL;
    private float rating = 0;

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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Album(" + albumName + ", " + artist + ", " + albumID + ")";
    }
}
