package com.hotel_management.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.hotel_management.Utils.Utils.OptionPane;
import static com.hotel_management.storage.Database.getConnection;


public class LoginController implements Initializable {

    @FXML
    private Button login;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    public void handleSignupButton(javafx.event.ActionEvent actionEvent) throws IOException {
        login.getScene().getWindow().hide();
        Stage signup = new Stage();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hotel_management/signup.fxml")));
        Scene scene = new Scene(root);
        signup.setScene(scene);
        signup.show();
    }

    @FXML
    public void handleLoginAction(javafx.event.ActionEvent actionEvent) throws IOException, SQLException {
        Connection connection = getConnection();
        String query = "SELECT * FROM users WHERE username=? AND password=?";
        try {
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, username.getText());
            pst.setString(2, password.getText());
            ResultSet rs = pst.executeQuery();
            int count = 0;
            while (rs.next()) {
                HomePageController.name = rs.getString("name");
                count = 1;
            }
            if (count == 1) {
                login.getScene().getWindow().hide();
                Stage signup = new Stage();
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hotel_management/homepage.fxml")));
                Scene scene = new Scene(root);
                signup.setScene(scene);
                signup.show();
            } else {
                OptionPane("Username or Password is not Correct", "Error Message");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForgotAction(javafx.event.ActionEvent actionEvent) throws IOException {
        login.getScene().getWindow().hide();
        Stage signup = new Stage();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hotel_management/forgotpassword.fxml")));
        Scene scene = new Scene(root);
        signup.setScene(scene);
        signup.show();
    }
}