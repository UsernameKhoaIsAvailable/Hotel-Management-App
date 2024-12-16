package com.hotel_management.controller;

import com.hotel_management.Utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import static com.hotel_management.Utils.Utils.*;
import static com.hotel_management.storage.Database.getConnection;

public class CheckInController implements Initializable {
    @FXML
    private Label amount;

    @FXML
    private Label days;

    @FXML
    private Label price;

    @FXML
    private TextField cEmail;

    @FXML
    private RadioButton male;

    @FXML
    private RadioButton female;

    @FXML
    private TextField cName;

    @FXML
    private TextField cNationality;

    @FXML
    private TextField cNumber;

    @FXML
    private TextField cPhone;

    @FXML
    private DatePicker inDate;

    @FXML
    private DatePicker outDate;

    @FXML
    private ComboBox<String> rNo;

    @FXML
    private ComboBox<String> rType;

    private Connection connection;

    private PreparedStatement pst;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        disablePastDates(inDate);
        disablePastDates(outDate);
        inDate.setDisable(true);
        outDate.setDisable(true);
        insertRoomType();
        ToggleGroup toggleGroup = new ToggleGroup();
        male.setToggleGroup(toggleGroup);
        female.setToggleGroup(toggleGroup);
    }

    private void insertRoomType() {
        rType.getItems().removeAll(rType.getItems());
        String query = "SELECT DISTINCT roomType FROM rooms";
        try {
            pst = connection.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String room_type = rs.getString("roomType");
                rType.getItems().add(room_type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertRoomNo() {
        rNo.getItems().removeAll(rNo.getItems());
        String type = rType.getSelectionModel().getSelectedItem();
        String query = "SELECT roomNumber FROM rooms WHERE roomType=? AND status='Not Booked'";
        try {
            pst = connection.prepareStatement(query);
            pst.setString(1, type);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String room_no = rs.getString("roomNumber");
                rNo.getItems().add(room_no);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleSelectRoomType(javafx.event.ActionEvent actionEvent) {
        if (!rType.getSelectionModel().getSelectedItem().isEmpty()) {
            insertRoomNo();
        }
    }

    public void handleSelectRoomNumber(javafx.event.ActionEvent actionEvent) {
        StringBuilder priceVal = new StringBuilder("Price: ");
        String no = rNo.getSelectionModel().getSelectedItem();
        String priceQuery = "SELECT price FROM rooms WHERE roomNumber=?";
        try {
            pst = connection.prepareStatement(priceQuery);
            pst.setString(1, no);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                priceVal.append(rs.getString("price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        price.setText(priceVal.toString());
        inDate.setDisable(false);
    }

    public void handleCheckInPick(javafx.event.ActionEvent actionEvent) {
        outDate.setDisable(false);
    }
    public void handleCheckOutPick(javafx.event.ActionEvent actionEvent) {
        LocalDate start = inDate.getValue();
        LocalDate end = outDate.getValue();
        int x = Utils.subtractDate(start, end);
        if (x == 0) {
            x = 1;
        }
        days.setText("Total days: " + x);
        int p = Integer.parseInt(price.getText().replace("Price: ", ""));
        amount.setText("Total Amount: " + (p * x));
    }

    public void handleSubmitAction(javafx.event.ActionEvent actionEvent) {
        String name = cName.getText();
        String email = cEmail.getText();
        String gender = getGender();
        String nationality = cNationality.getText();
        String number = cNumber.getText();
        String phone = cPhone.getText();
        String roomNo = rNo.getSelectionModel().getSelectedItem();
        String checkIn = inDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String checkOut = outDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (name.isBlank() || email.isBlank() || gender.isBlank() || nationality.isBlank() || number.isBlank() ||
                phone.isBlank() || roomNo.isBlank() || checkIn.isBlank() || checkOut.isBlank()) {
            OptionPane("Every Field is required", "Error Message");
        } else if (!email.contains("@")) {
            OptionPane("Enter proper email", "Error Message");
        } else if (containsNumber(name)) {
            OptionPane("Enter proper name", "Error Message");
        } else if (containsNumber(nationality)) {
            OptionPane("Enter proper nationality", "Error Message");
        } else if (phone.length() != 10) {
            OptionPane("Enter proper contact number", "Error Message");
        }else {
            String insertCustomer = """
                    INSERT INTO customers(customerIDNumber, customerName, customerNationality, customerGender, customerPhoneNo, customerEmail)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
            String insertReservation = "INSERT INTO reservations(customerID, roomNumber, checkInDate, checkOutDate) VALUES (?, ?, ?, ?)";
            String updateRoom = "UPDATE rooms SET status=\"Booked\" WHERE roomNumber=?";
            try {
                pst = connection.prepareStatement(insertCustomer);
                pst.setString(1, number);
                pst.setString(2, name);
                pst.setString(3, nationality);
                pst.setString(4, gender);
                pst.setString(5, phone);
                pst.setString(6, email);
                pst.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                String customerId = getCustomerId(number);
                pst = connection.prepareStatement(insertReservation);
                pst.setString(1, customerId);
                pst.setString(2, roomNo);
                pst.setString(3, checkIn);
                pst.setString(4, checkOut);
                pst.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                pst = connection.prepareStatement(updateRoom);
                pst.setString(1, roomNo);
                pst.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            OptionPane("Check In Successful", "Message");
        }
    }

    private String getCustomerId(String number) {
        String query = """
                SELECT c.customerId FROM customers c
                WHERE c.customerIdNumber = ?
                """;
        try {
            pst = connection.prepareStatement(query);
            pst.setString(1, number);
            ResultSet resultSet = pst.executeQuery();
            resultSet.next();
            return resultSet.getString("customerId");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getGender() {
        String gender = "";
        if (male.isSelected()) {
            gender = "Male";
        } else if (female.isSelected()) {
            gender = "Female";
        }
        return gender;
    }
}
