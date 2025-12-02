package com.example.trainreservationsystem.utils;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;

public class Database {

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static Connection getConnection() throws Exception {
        String databaseUrl = dotenv.get("DATABASE_URL");

        // Fallback if .env is missing or variable not found (helpful for simple local
        // testing)
        if (databaseUrl == null) {
            // Default to standard local postgres if not set
            databaseUrl = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=password";
        }

        String cleanUrl = databaseUrl.startsWith("jdbc:") ? databaseUrl.substring(5) : databaseUrl;

        URI uri = URI.create(cleanUrl);

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + (uri.getPort() == -1 ? 5432 : uri.getPort())
                + uri.getPath();
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
