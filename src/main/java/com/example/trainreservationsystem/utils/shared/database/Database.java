package com.example.trainreservationsystem.utils.shared.database;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Singleton database connection manager.
 * Maintains a single shared connection for the application.
 */
public class Database {

  private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
  private static Database instance;
  private Connection connection;

  private Database() {
    // Private constructor for singleton
  }

  public static synchronized Database getInstance() {
    if (instance == null) {
      instance = new Database();
    }
    return instance;
  }

  public static Connection getConnection() throws Exception {
    Database db = getInstance();

    // Check if connection exists and is valid
    if (db.connection == null || db.connection.isClosed()) {
      synchronized (Database.class) {
        // Double-check after acquiring lock
        if (db.connection == null || db.connection.isClosed()) {
          db.connection = db.createConnection();
        }
      }
    } else {
      // Validate connection without throwing exception if validation fails
      try {
        if (!db.connection.isValid(2)) {
          synchronized (Database.class) {
            // Double-check after acquiring lock
            if (!db.connection.isValid(2)) {
              try {
                db.connection.close();
              } catch (SQLException e) {
                // Ignore close errors
              }
              db.connection = db.createConnection();
            }
          }
        }
      } catch (SQLException e) {
        // Connection validation failed, recreate it
        synchronized (Database.class) {
          try {
            if (db.connection != null && !db.connection.isClosed()) {
              db.connection.close();
            }
          } catch (SQLException ex) {
            // Ignore close errors
          }
          db.connection = db.createConnection();
        }
      }
    }

    // Return a wrapper that prevents closing the singleton connection
    return new NonClosingConnection(db.connection);
  }

  public static void closeConnection() {
    Database db = getInstance();
    if (db.connection != null) {
      try {
        db.connection.close();
        System.out.println("✅ Database connection closed");
      } catch (SQLException e) {
        System.err.println("❌ Error closing database connection: " + e.getMessage());
      }
    }
  }

  private Connection createConnection() throws Exception {
    String databaseUrl = dotenv.get("DATABASE_URL");
    if (databaseUrl == null || databaseUrl.isEmpty()) {
      throw new Exception("DATABASE_URL not found in environment variables");
    }

    try {
      Properties props = new Properties();
      String jdbcUrl = parseDatabaseUrl(databaseUrl, props);
      Connection conn = DriverManager.getConnection(jdbcUrl, props);
      return conn;
    } catch (Exception e) {
      System.err.println("❌ Database connection failed: " + e.getMessage());
      throw e;
    }
  }

  private String parseDatabaseUrl(String databaseUrl, Properties props) throws Exception {
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

  private void parseCredentials(String credentials, Properties props) {
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

  private void parseQueryParams(String query, Properties props) {
    for (String param : query.split("&")) {
      String[] keyValue = param.split("=", 2);
      if (keyValue.length == 2) {
        props.setProperty(keyValue[0], keyValue[1]);
      }
    }
  }
}
