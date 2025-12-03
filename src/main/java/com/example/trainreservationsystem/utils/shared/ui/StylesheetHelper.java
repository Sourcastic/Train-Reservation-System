package com.example.trainreservationsystem.utils.shared.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Utility class to help apply stylesheets consistently across the application.
 * Handles resource loading for Java modules.
 */
public class StylesheetHelper {

  private static final String STYLESHEET_PATH = "/com/example/trainreservationsystem/stylesheet.css";

  /**
   * Applies the global stylesheet to a Scene.
   *
   * @param scene The Scene to apply the stylesheet to
   */
  public static void applyStylesheet(Scene scene) {
    if (scene == null) {
      return;
    }
    try {
      java.net.URL stylesheetUrl = StylesheetHelper.class.getResource(STYLESHEET_PATH);
      if (stylesheetUrl != null) {
        String externalForm = stylesheetUrl.toExternalForm();
        if (!scene.getStylesheets().contains(externalForm)) {
          scene.getStylesheets().add(externalForm);
        }
      } else {
        System.err.println("⚠️ Warning: Stylesheet not found at " + STYLESHEET_PATH);
      }
    } catch (Exception e) {
      System.err.println("⚠️ Warning: Could not load stylesheet: " + e.getMessage());
    }
  }

  /**
   * Applies the global stylesheet to a Parent node by accessing its scene.
   *
   * @param parent The Parent node to apply the stylesheet to
   */
  public static void applyStylesheet(Parent parent) {
    if (parent == null) {
      return;
    }
    Scene scene = parent.getScene();
    if (scene != null) {
      applyStylesheet(scene);
    } else {
      // If scene is not yet available, add a listener to apply when scene becomes
      // available
      parent.sceneProperty().addListener((observable, oldScene, newScene) -> {
        if (newScene != null) {
          applyStylesheet(newScene);
        }
      });
    }
  }

  /**
   * Gets the stylesheet URL as a string for use in FXML.
   * Note: This may not work in all cases with Java modules.
   * Prefer using applyStylesheet() programmatically.
   *
   * @return The stylesheet URL as a string, or null if not found
   */
  public static String getStylesheetUrl() {
    try {
      java.net.URL stylesheetUrl = StylesheetHelper.class.getResource(STYLESHEET_PATH);
      return stylesheetUrl != null ? stylesheetUrl.toExternalForm() : null;
    } catch (Exception e) {
      return null;
    }
  }
}
