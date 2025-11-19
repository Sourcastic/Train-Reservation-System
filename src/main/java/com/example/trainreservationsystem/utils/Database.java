package com.example.trainreservationsystem.utils;
import java.sql.Connection;
import java.sql.DriverManager;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.util.Properties;


public class Database {

    private static final Dotenv dotenv = Dotenv.load();

    public static Connection getConnection() throws Exception {
        String databaseUrl = dotenv.get("DATABASE_URL");

        String cleanUrl = databaseUrl.startsWith("jdbc:") ? databaseUrl.substring(5) : databaseUrl;

        URI uri = URI.create(cleanUrl);

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + (uri.getPort() == -1 ? 5432 : uri.getPort()) + uri.getPath();
        if (uri.getQuery() != null) {
            jdbcUrl += "?" + uri.getQuery();
        }

        Properties props = new Properties();

        if (uri.getUserInfo() != null) {
            String[] userInfo = uri.getUserInfo().split(":");
            props.setProperty("user", userInfo[0]);
            if (userInfo.length > 1) {
                props.setProperty("password", userInfo[1]);
            }
        }

        if (uri.getQuery() != null && uri.getQuery().contains("sslmode=require")) {
            props.setProperty("sslmode", "require");
        }

        return DriverManager.getConnection(jdbcUrl, props);
    }
}
