package database;

import java.sql.*;

public class Database {

    private final Connection databaseConnection;

    public Database() {
        this.databaseConnection = connect();
    }

    public Connection getDatabaseConnection() {return databaseConnection;}

    public Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:postgresql://medley-database.c9sdfgddghu2.us-east-2.rds.amazonaws.com:5432/postgres";
            String user = "amk2142";
            String password = "EkHJ%mh_=53a.&&&";
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }


}
