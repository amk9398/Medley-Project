package tools;

public class AlbumCard {
    private String albumName;
    private String artist;
    private String imageReference;
    private int rating;

    public AlbumCard(String name, String artist, String imageReference) {
        this.albumName = name;
        this.artist = artist;
        this.imageReference = imageReference;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtist() {
        return artist;
    }

    public String getImageReference() {
        return imageReference;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
