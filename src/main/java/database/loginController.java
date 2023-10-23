package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class loginController {

    public static Response attemptUserLogin(Connection conn, String username) {
        Response res = getUserID(conn, username);
        if(res.status == Status.SUCCESS) {
            return res;
        } else if(res.status == Status.FAILURE) {
            Response res2 = createNewUser(conn, username);
            if(res2.status == Status.SUCCESS) {
                return getUserID(conn, username);
            } else {
                return res2;
            }
        } else {
            return res;
        }
    }

    private static Response getUserID(Connection conn, String username) {
        try {
            String query = "SELECT user_id FROM users WHERE username='" + username + "';";
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            String id = "";
            while(rs.next()) id = rs.getString("user_id");
            return id.equals("") ? new Response(Status.FAILURE, "") : new Response(Status.SUCCESS, id);
        } catch (SQLException e) {
            return new Response(Status.ERROR, e.getMessage());
        }
    }

    private static Response createNewUser(Connection conn, String username) {
        try {
            String update = "INSERT INTO users (username, num_albums, average_score, albums_per_page, theme) " +
                    "VALUES ('" + username + "', 0, 0, 20, 'Classic');";
            Statement statement = conn.createStatement();
            statement.executeUpdate(update);
            return new Response(Status.SUCCESS, "");
        } catch (SQLException e) {
            return new Response(Status.ERROR, e.getMessage());
        }
    }

    public static Response updateUserPreferences(Connection conn, int user_id, int albums_per_page, String theme) {
        try {
            String update = "UPDATE users SET albums_per_page=" + albums_per_page +
                    ", theme='" + theme + "' WHERE user_id=" + user_id + ";";
            Statement statement = conn.createStatement();
            statement.executeUpdate(update);
            return new Response(Status.SUCCESS, "");
        } catch (SQLException e) {
            return new Response(Status.ERROR, e.getMessage());
        }
    }

    public static HashMap<String, String> getUserPreferences(Connection conn, int user_id) {
        HashMap<String, String> preferences = new HashMap<>();
        try {
            String query = "SELECT albums_per_page, theme FROM users WHERE user_id=" + user_id + ";";
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()) {
                preferences.put("albums_per_page", String.valueOf(rs.getInt("albums_per_page")));
                preferences.put("theme", rs.getString("theme"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preferences;
    }
}
