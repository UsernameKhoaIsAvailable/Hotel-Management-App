package com.hotel_management.controller;

import com.hotel_management.model.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.hotel_management.storage.Database.getConnection;

public class CheckOutController implements Initializable {

    @FXML
    private TableColumn<Reservation, String> checkIn;

    @FXML
    private TableColumn<Reservation, String> checkOut;

    @FXML
    private TableColumn<Reservation, String> customerName;

    @FXML
    private TableColumn<Reservation, String> roomNumber;

    @FXML
    private TableView<Reservation> roomTable;

    @FXML
    private TextField search;

    @FXML
    private TableColumn<Reservation, String> totalDays;

    @FXML
    private TableColumn<Reservation, String> totalPrice;

    @FXML
    private TableColumn<?, ?> status;

    @FXML
    private ComboBox<String> sort;

    private Connection connection;

    public static final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    public static final List<Reservation> reservationList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sort.getItems().removeAll(sort.getItems());
        sort.getItems().addAll("Today", "Checked In", "Checked Out");
        roomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        customerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        checkIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        totalDays.setCellValueFactory(new PropertyValueFactory<>("totalDays"));
        totalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        initReservationList();
        roomTable.setItems(reservations);
    }

    public void refreshReservationList(Connection connection) {
        this.connection = connection;
        initReservationList();
    }
    private void initReservationList() {
        reservationList.clear();
        reservations.clear();
        String query = """
                SELECT
                    res.status,
                    res.reservationID,
                    res.roomNumber,
                    c.customerName,
                    res.checkInDate,
                    res.checkOutDate,
                    DATEDIFF(res.checkOutDate, res.checkInDate) AS totalDays,
                    (r.price * DATEDIFF(res.checkOutDate, res.checkInDate)) AS totalPrice
                FROM
                    customers c
                INNER JOIN
                    reservations res ON c.customerID = res.customerID
                INNER JOIN
                    rooms r ON r.roomNumber = res.roomNumber;
                """;
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int res_id = Integer.parseInt(rs.getString("reservationID"));
                int room_no = Integer.parseInt(rs.getString("roomNumber"));
                String cus_name = rs.getString("customerName");
                String check_in = rs.getString("checkInDate");
                String check_out = rs.getString("checkOutDate");
                int total_price = Integer.parseInt(rs.getString("totalPrice"));
                int total_days = Integer.parseInt(rs.getString("totalDays"));
                String res_status = rs.getString("status");
                reservationList.add(new Reservation(res_id, room_no, cus_name, check_in, check_out, total_days, total_price, res_status));
                reservations.add(new Reservation(res_id, room_no, cus_name, check_in, check_out, total_days, total_price, res_status));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchByRoomNumber(String s) {
        CheckOutController.reservations.clear();
        for (int i = 0; i < reservationList.size(); i++) {
            if (Integer.toString(reservationList.get(i).getRoomNumber()).indexOf(s) == 0) {
                CheckOutController.reservations.add(reservationList.get(i));
            }
        }
    }

    public void handleSearchKey(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_RELEASED) {
            String s = search.getText();
            searchByRoomNumber(s);
        }
    }

    public void clickItem(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            if (roomTable.getSelectionModel().getSelectedItem() != null) {
                Reservation selectedReservation = roomTable.getSelectionModel().getSelectedItem();
                BillInfoController.setSelectedReservationID(selectedReservation.getResID());
                BillInfoController.setSelectedReservation(selectedReservation);
                if (!BillInfoController.selectedReservation.getStatus().equals("Checked Out")) {
                    Stage add = new Stage();
                    Parent root;
                    root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hotel_management/billinfo.fxml")));
                    Scene scene = new Scene(root);
                    add.setScene(scene);
                    add.show();
                }
            }
        }
    }

    public void handleComboboxSelection(javafx.event.ActionEvent actionEvent) {
        if (sort.getSelectionModel().getSelectedItem().equals("Today")) {
            reservations.clear();
            for (int i = 0; i < reservationList.size(); i++) {
                if (reservationList.get(i).getCheckOutDate().equals(java.time.LocalDate.now().toString()) &&
                        reservationList.get(i).getStatus().equals("Checked In")) {
                    reservations.add(reservationList.get(i));
                }
            }
        } else if (sort.getSelectionModel().getSelectedItem().equals("Checked In")) {
            reservations.clear();
            for (int i = 0; i < reservationList.size(); i++) {
                if (reservationList.get(i).getStatus().equals("Checked In")) {
                    reservations.add(reservationList.get(i));
                }
            }
        } else if (sort.getSelectionModel().getSelectedItem().equals("Checked Out")) {
            reservations.clear();
            for (int i = 0; i < reservationList.size(); i++) {
                if (reservationList.get(i).getStatus().equals("Checked Out")) {
                    reservations.add(reservationList.get(i));
                }
            }
        }
    }
}
