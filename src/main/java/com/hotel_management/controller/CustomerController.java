package com.hotel_management.controller;

import com.hotel_management.Utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import static com.hotel_management.storage.Database.getConnection;

public class CustomerController implements Initializable {

    public static int selectedRoomNumber;

    @FXML
    private TextField email;

    @FXML
    private RadioButton male;

    @FXML
    private RadioButton female;

    @FXML
    private DatePicker inDate;

    @FXML
    private TextField name;

    @FXML
    private TextField nationality;

    @FXML
    private DatePicker outDate;

    @FXML
    private TextField phone;

    @FXML
    private TextField price;

    @FXML
    private Button save;

    private Connection connection;

    private PreparedStatement pst;

    public static void setSelectedRoomNumber(int selectedRoomNumber) {
        CustomerController.selectedRoomNumber = selectedRoomNumber;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (selectedRoomNumber != 0) {
            String query = """
                    SELECT
                        c.*,
                        res.checkInDate,
                        res.checkOutDate,
                        (r.price * DATEDIFF(res.checkOutDate, res.checkInDate)) AS Total
                    FROM
                        customers c
                    INNER JOIN
                        reservations res ON c.customerID = res.customerID
                    INNER JOIN
                        rooms r ON r.roomNumber = res.roomNumber
                    WHERE
                        r.roomNumber = ? AND res.status = "Checked in";
                    """;
            try {
                pst = connection.prepareStatement(query);
                pst.setString(1, Integer.toString(selectedRoomNumber));
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    price.setText(rs.getString("Total"));
                    name.setText(rs.getString("customerName"));
                    email.setText(rs.getString("customerEmail"));
                    phone.setText(rs.getString("customerPhoneNo"));
                    setGender(rs.getString("customerGender"));
                    nationality.setText(rs.getString("customerNationality"));
                    inDate.setValue(rs.getDate("checkInDate").toLocalDate());
                    outDate.setValue(rs.getDate("checkOutDate").toLocalDate());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            price.setEditable(false);
            inDate.setEditable(false);
            Utils.disablePastDates(outDate);
            inDate.setOnMouseClicked(e -> {
                if (inDate.isShowing()) inDate.hide();
            });
        }
        ToggleGroup toggleGroup = new ToggleGroup();
        male.setToggleGroup(toggleGroup);
        female.setToggleGroup(toggleGroup);
    }


    public void handleSaveAction(ActionEvent actionEvent) {
        String query = """
                UPDATE customers c
                INNER JOIN
                    reservations res ON c.customerID = res.customerID
                INNER JOIN
                    rooms r ON r.roomNumber = res.roomNumber
                    SET
                    c.customerName = ?,
                    c.customerPhoneNo = ?,
                    c.customerGender = ?,
                    c.customerEmail = ?,
                    c.customerNationality = ?,
                    res.checkOutDate = ?
                WHERE
                    r.roomNumber = ? AND res.status = "Checked in";
                """;
        try {
            pst = connection.prepareStatement(query);
            pst.setString(7, Integer.toString(selectedRoomNumber));
            pst.setString(1, name.getText());
            pst.setString(2, phone.getText());
            pst.setString(3, getGender());
            pst.setString(4, email.getText());
            pst.setString(5, nationality.getText());
            pst.setString(6, outDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        save.getScene().getWindow().hide();
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

    private void setGender(String gender) {
        if (gender.equals("Male")) {
            male.setSelected(true);
        }
        else {
            female.setSelected(true);
        }
    }
}
