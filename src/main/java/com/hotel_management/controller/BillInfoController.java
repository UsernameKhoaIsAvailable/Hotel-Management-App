package com.hotel_management.controller;

import com.hotel_management.model.Reservation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.hotel_management.storage.Database.getConnection;


public class BillInfoController implements Initializable {

    public static int selectedResID;

    public static Reservation selectedReservation;

    @FXML
    private TextField Amount;

    @FXML
    private Button pay;

    @FXML
    private TextField customerIDNumber;

    @FXML
    private TextField customerName;

    @FXML
    private TextField roomNumber;

    private Connection connection;

    private PreparedStatement pst;

    public static void setSelectedReservationID(int selectedReservationID) {
        selectedResID = selectedReservationID;
    }

    public static void setSelectedReservation(Reservation reservation) {
        selectedReservation = reservation;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (selectedResID != 0) {
            String query = """
                    SELECT
                        res.reservationID,
                        res.roomNumber,
                        c.customerIDNumber,
                        c.customerName,
                        (r.price * DATEDIFF(res.checkOutDate, res.checkInDate)) AS totalPrice
                    FROM
                        customers c
                    INNER JOIN
                        reservations res ON c.customerID = res.customerID
                    INNER JOIN
                        rooms r ON r.roomNumber = res.roomNumber
                    WHERE
                        res.reservationID = ?;
                    """;
            try {
                pst = connection.prepareStatement(query);
                pst.setString(1, Integer.toString(selectedResID));
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    roomNumber.setText(rs.getString("roomNumber"));
                    customerIDNumber.setText(rs.getString("customerIDNumber"));
                    customerName.setText(rs.getString("customerName"));
                    Amount.setText(rs.getString("totalPrice"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            roomNumber.setEditable(false);
            customerIDNumber.setEditable(false);
            customerName.setEditable(false);
            Amount.setEditable(false);
        }
    }

    public void handleBillAction(javafx.event.ActionEvent actionEvent) throws IOException {
        if (!selectedReservation.getStatus().equals("Checked Out")) {
            String insertBills = "INSERT INTO bills(reservationID, billDate, billAmount) VALUES (?, ?, ?)";
            String updateRoom = "UPDATE rooms SET status=\"Not Booked\" WHERE roomNumber=?";
            String updateReservation = "UPDATE reservations SET status=\"Checked Out\" WHERE reservationID=?";
            try {
                pst = connection.prepareStatement(insertBills);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                pst.setString(1, String.valueOf(selectedReservation.getResID()));
                pst.setString(2, selectedReservation.getCheckOutDate());
                pst.setString(3, String.valueOf(selectedReservation.getTotalPrice()));
                pst.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                pst = connection.prepareStatement(updateRoom);
                pst.setString(1, String.valueOf(selectedReservation.getRoomNumber()));
                pst.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                pst = connection.prepareStatement(updateReservation);
                pst.setString(1, String.valueOf(selectedReservation.getResID()));
                pst.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            CheckOutController controller = new CheckOutController();
            controller.refreshReservationList(connection);
        }
        pay.getScene().getWindow().hide();
    }
}
