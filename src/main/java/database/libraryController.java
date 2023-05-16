package database;

import gui.AlbumCard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class libraryController {

    public static Response addAlbumToDatabase(Connection conn, AlbumCard card) {
        try {
            String query = "SELECT COUNT(album_id) FROM albums WHERE album_id='" + card.getAlbumID() + "';";
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            int count = 0;
            while(rs.next()) count = rs.getInt("count");
            if(count > 0) return new Response(Status.FAILURE, "Album already in database");

            String update = "INSERT INTO albums (album_id, name, artist) VALUES ('" +
                    card.getAlbumID() + "', '" + card.getAlbumName() + "', '" + card.getArtist() +  "');";
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
}
