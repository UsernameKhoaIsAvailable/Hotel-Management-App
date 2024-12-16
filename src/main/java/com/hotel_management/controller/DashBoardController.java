package com.hotel_management.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.hotel_management.storage.Database.getConnection;

public class DashBoardController implements Initializable {
    @FXML
    private Label avaRoom;

    @FXML
    private Label bookedRoom;

    @FXML
    private Label earning;

    @FXML
    private Label pending;

    @FXML
    private Label totalRoom;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Connection connection;
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String query = """
                SELECT
                    COUNT(r.roomNumber) AS totalRooms,
                    SUM(CASE WHEN r.status = 'Not Booked' THEN 1 ELSE 0 END) AS totalNotBooked,
                    SUM(CASE WHEN r.status = 'Booked' THEN 1 ELSE 0 END) AS totalBooked
                FROM rooms r;
                                
                """;
        String query2 = """
                SELECT
                    SUM(b.billAmount) AS totalEarnings,
                    (
                        SELECT SUM(r.price * DATEDIFF(res.checkOutDate, res.checkInDate)) AS Pending
                        FROM reservations res
                        INNER JOIN rooms r ON r.roomNumber = res.roomNumber
                        WHERE res.status = 'Checked In'
                    ) AS totalPendings
                FROM bills b
                INNER JOIN reservations res ON res.reservationID = b.reservationID;
                                
                """;
        PreparedStatement pst;
        try {
            pst = connection.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                totalRoom.setText(rs.getString("totalRooms"));
                bookedRoom.setText(rs.getString("totalBooked"));
                avaRoom.setText(rs.getString("totalNotBooked"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            pst = connection.prepareStatement(query2);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                earning.setText(rs.getString("totalEarnings"));
                pending.setText(rs.getString("totalPendings"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
