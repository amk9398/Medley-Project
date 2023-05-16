package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
            String update = "INSERT INTO users (username, num_albums, average_score) " +
                    "VALUES ('" + username + "', 0, 0);";
            Statement statement = conn.createStatement();
            statement.executeUpdate(update);
            return new Response(Status.SUCCESS, "");
        } catch (SQLException e) {
            return new Response(Status.ERROR, e.getMessage());
        }
    }

}
