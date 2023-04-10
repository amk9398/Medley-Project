package tools;

public class AlbumCard {
    private String name;
    private String artist;
    private String imageReference;

    public AlbumCard(String name, String artist, String imageReference) {
        this.name = name;
        this.artist = artist;
        this.imageReference = imageReference;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getImageReference() {
        return imageReference;
    }
}
