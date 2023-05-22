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
            String url = "jdbc:postgresql://localhost:5432/postgres";
            String user = "postgres";
            String password = "password";
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }


}
