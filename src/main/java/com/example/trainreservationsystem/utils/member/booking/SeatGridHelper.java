package com.example.trainreservationsystem.utils.member.booking;

import java.util.Set;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Helper for creating seat selection grids.
 */
public class SeatGridHelper {
  private static final int SEATS_PER_ROW = 6;

  public static void createGrid(GridPane seatGrid, int totalSeats,
      Set<Integer> occupiedSeats,
      SeatSelectionHandler handler) {
    createGrid(seatGrid, 1, totalSeats, occupiedSeats, handler);
  }

  public static void createGrid(GridPane seatGrid, int seatStart, int seatEnd,
      Set<Integer> occupiedSeats,
      SeatSelectionHandler handler) {
    int seatNumber = seatStart;
    int totalSeats = seatEnd - seatStart + 1;
    int totalRows = calculateTotalRows(totalSeats);

    for (int row = 0; row < totalRows; row++) {
      addRowLabel(seatGrid, row);
      seatNumber = addSeatsInRow(seatGrid, row, seatNumber, seatEnd, occupiedSeats, handler);
    }
  }

  private static int calculateTotalRows(int totalSeats) {
    return (totalSeats + SEATS_PER_ROW - 1) / SEATS_PER_ROW;
  }

  private static void addRowLabel(GridPane seatGrid, int row) {
    Label rowLabel = new Label("Row " + (row + 1));
    rowLabel.getStyleClass().add("row-label");
    seatGrid.add(rowLabel, 0, row);
  }

  private static int addSeatsInRow(GridPane seatGrid, int row, int startSeat,
      int seatEnd, Set<Integer> occupiedSeats,
      SeatSelectionHandler handler) {
    int seatNumber = startSeat;

    for (int col = 0; col < SEATS_PER_ROW && seatNumber <= seatEnd; col++) {
      int column = calculateColumn(col);
      Button seatButton = createSeatButton(seatNumber, occupiedSeats, handler);
      seatGrid.add(seatButton, column, row);

      if (col == 2) {
        addAisle(seatGrid, row, column);
      }
      seatNumber++;
    }

    return seatNumber;
  }

  private static int calculateColumn(int col) {
    int column = col + 1; // Offset for row label
    if (col >= 3) {
      column++; // Add gap for aisle
    }
    return column;
  }

  private static void addAisle(GridPane seatGrid, int row, int column) {
    Label aisle = new Label(" ");
    aisle.setPrefWidth(20);
    seatGrid.add(aisle, column + 1, row);
  }

  private static Button createSeatButton(int seatNumber, Set<Integer> occupiedSeats,
      SeatSelectionHandler handler) {
    Button button = new Button(String.valueOf(seatNumber));
    button.setPrefSize(40, 40);
    button.setText(String.valueOf(seatNumber)); // Explicitly set text to ensure it's visible
    button.setMinSize(40, 40);
    button.setMaxSize(40, 40);

    if (occupiedSeats.contains(seatNumber)) {
      button.getStyleClass().add("seat-occupied");
      button.setDisable(true);
    } else {
      button.getStyleClass().add("seat-available");
      button.setOnAction(e -> handler.onSeatToggled(seatNumber, button));
    }

    return button;
  }

  public interface SeatSelectionHandler {
    void onSeatToggled(int seatNumber, Button button);
  }
}
