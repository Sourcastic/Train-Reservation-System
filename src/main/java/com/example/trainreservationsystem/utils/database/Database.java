package com.example.trainreservationsystem.utils.database;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Manages database connections.
 * Supports PostgreSQL with automatic mock mode fallback.
 */
public class Database {

  private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
  private static boolean useMockData = false;

  public static Connection getConnection() throws Exception {
    if (useMockData) {
      throw new Exception("MOCK_MODE");
    }

    String databaseUrl = dotenv.get("DATABASE_URL");
    if (databaseUrl == null) {
      System.out.println("⚠️  DATABASE_URL not found. Switching to mock data mode.");
      enableMockMode();
      throw new Exception("MOCK_MODE");
    }

    try {
      Properties props = new Properties();
      String jdbcUrl = parseDatabaseUrl(databaseUrl, props);

      System.out.println("🔌 Attempting to connect to database...");
      Connection conn = DriverManager.getConnection(jdbcUrl, props);
      System.out.println("✅ Database connection successful!");
      return conn;
    } catch (java.net.UnknownHostException e) {
      handleConnectionError(databaseUrl);
      throw new Exception("MOCK_MODE");
    } catch (Exception e) {
      System.err.println("❌ Database connection failed: " + e.getMessage());
      enableMockMode();
      throw new Exception("MOCK_MODE");
    }
  }

  private static String parseDatabaseUrl(String databaseUrl, Properties props) throws Exception {
    // Remove jdbc: prefix if present
    String urlPart = databaseUrl.startsWith("jdbc:postgresql://")
        ? databaseUrl.substring("jdbc:postgresql://".length())
        : databaseUrl.startsWith("postgresql://")
            ? databaseUrl.substring("postgresql://".length())
            : databaseUrl.startsWith("jdbc:")
                ? databaseUrl.substring(5) // Remove "jdbc:"
                : databaseUrl;

    if (urlPart == null || urlPart.isEmpty()) {
      throw new Exception("Invalid DATABASE_URL format");
    }

    // Extract credentials (username:password@host:port/database)
    int atIndex = urlPart.indexOf('@');
    String hostAndPath = urlPart;
    if (atIndex >= 0) {
      String credentials = urlPart.substring(0, atIndex);
      parseCredentials(credentials, props);
      hostAndPath = urlPart.substring(atIndex + 1);
    }

    // Extract query parameters
    int queryIndex = hostAndPath.indexOf('?');
    String pathPart = hostAndPath;
    if (queryIndex >= 0) {
      pathPart = hostAndPath.substring(0, queryIndex);
      String query = hostAndPath.substring(queryIndex + 1);
      parseQueryParams(query, props);
    }

    // Build clean JDBC URL without credentials
    return "jdbc:postgresql://" + pathPart;
  }

  private static void parseCredentials(String credentials, Properties props) {
    int colonIndex = credentials.indexOf(':');
    if (colonIndex >= 0) {
      props.setProperty("user", credentials.substring(0, colonIndex));
      try {
        String password = URLDecoder.decode(credentials.substring(colonIndex + 1), "UTF-8");
        props.setProperty("password", password);
      } catch (Exception e) {
        props.setProperty("password", credentials.substring(colonIndex + 1));
      }
    } else {
      props.setProperty("user", credentials);
    }
  }

  private static void parseQueryParams(String query, Properties props) {
    for (String param : query.split("&")) {
      String[] keyValue = param.split("=", 2);
      if (keyValue.length == 2) {
        props.setProperty(keyValue[0], keyValue[1]);
      }
    }
  }

  private static void handleConnectionError(String databaseUrl) {
    System.err.println("❌ Database connection failed: Cannot resolve hostname");
    System.err.println("⚠️  Switching to mock data mode.");
    enableMockMode();
  }

  public static boolean isMockMode() {
    return useMockData;
  }

  public static void enableMockMode() {
    useMockData = true;
  }
}
