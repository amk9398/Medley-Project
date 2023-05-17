package database;

import gui.AlbumCard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class libraryController {

    public static Response addAlbumToDatabase(Connection conn, AlbumCard card) {
        try {
            String query = "SELECT COUNT(album_id) FROM albums WHERE album_id='" + card.getAlbumID() + "';";
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            int count = 0;
            while(rs.next()) count = rs.getInt("count");
            if(count > 0) return new Response(Status.FAILURE, "Album already in database");

            String update = "INSERT INTO albums (album_id, name, artist, image_url) VALUES ('" +
                    card.getAlbumID() + "', '" + card.getAlbumName() + "', '" + card.getArtist() +
                    "', '" + card.getImageURL() + "');";
            statement.executeUpdate(update);
            return new Response(Status.SUCCESS, "");
        } catch (SQLException e) {
            return new Response(Status.ERROR, e.getMessage());
        }
    }

    public static Response addAlbumToUserLibrary(Connection conn, int user_id, AlbumCard card) {
        try {
            Response res = addAlbumToDatabase(conn, card);
            Statement statement = conn.createStatement();

            if(res.status == Status.ERROR) return res;
            if(res.status == Status.FAILURE) {
                String query = "SELECT COUNT(user_id) FROM user_albums WHERE user_id=" +
                        user_id + " AND album_id='" + card.getAlbumID() + "';";
                ResultSet rs = statement.executeQuery(query);
                int count = 0;
                while(rs.next()) count = rs.getInt("count");
                if(count > 0) return new Response(Status.FAILURE, "Album already in user library");
            }

            String update = "INSERT INTO user_albums (user_id, album_id) VALUES (" +
                    user_id + ", '" + card.getAlbumID() + "');";
            statement.executeUpdate(update);
            return new Response(Status.SUCCESS, "Album added successfully");
        } catch (SQLException e) {
            return new Response(Status.ERROR, e.getMessage());
        }
    }

    public static ArrayList<AlbumCard> getUserAlbums(Connection conn, int user_id) {
        ArrayList<AlbumCard> albumCards = new ArrayList<>();
        try {
            String query = "SELECT * FROM albums WHERE album_id IN (SELECT album_id " +
                    "FROM user_albums WHERE user_id=" + user_id + ");";
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            AlbumCard card = null;
            while(rs.next()) {
                card = new AlbumCard(rs.getString("album_id"), rs.getString("name"),
                        rs.getString("artist"), rs.getString("image_url"));
                albumCards.add(card);
            }

            for(AlbumCard albumCard : albumCards) {
                query = "SELECT score FROM user_albums WHERE user_id=" + user_id + " AND album_id='" + albumCard.getAlbumID() + "';";
                ResultSet rs1 = statement.executeQuery(query);
                while(rs1.next()) albumCard.setRating(rs1.getFloat("score"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return albumCards;
    }

    public static Response rateAlbum(Connection conn, int user_id, String album_id, int score) {
        try {
            String update = "UPDATE user_albums SET score=" + score + " WHERE user_id=" +
                    user_id + " AND album_id='" + album_id + "';";
            Statement statement = conn.createStatement();
            statement.executeUpdate(update);
            return new Response(Status.SUCCESS, "");
        } catch (SQLException e) {
            return new Response(Status.ERROR, e.getMessage());
        }
    }
}
