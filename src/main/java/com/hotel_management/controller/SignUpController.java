package com.hotel_management.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

public class SignUpController implements Initializable {
    @FXML
    private TextField address;

    @FXML
    private TextField answer;

    @FXML
    private RadioButton female;

    @FXML
    private RadioButton male;

    @FXML
    private TextField name;

    @FXML
    private TextField password;

    @FXML
    private ComboBox<String> question;

    @FXML
    private Button signup;

    @FXML
    private TextField username;

    private Connection connection;

    private PreparedStatement pst;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        question.getItems().removeAll(question.getItems());
        question.getItems().addAll("What is the name of your first pet?", "What was your first car?", "What elementary school did you attend?", "What is the name of the town where you were born?");
        ToggleGroup toggleGroup = new ToggleGroup();
        male.setToggleGroup(toggleGroup);
        female.setToggleGroup(toggleGroup);
    }

    @FXML
    public void handleSignupAction(javafx.event.ActionEvent actionEvent) throws SQLException {
        String name_text = name.getText();
        String username_text = username.getText();
        String password_text = password.getText();
        String gender_text = getGender();
        String question_text = question.getSelectionModel().getSelectedItem();
        String answer_text = answer.getText();
        String address_text = address.getText();
        connection = getConnection();
        if (name_text.isBlank() || username_text.isBlank() || password_text.isBlank() || gender_text.isBlank() ||
                question_text.isBlank() || answer_text.isBlank() || address_text.isBlank()) {
            OptionPane("Every Field is required", "Error Message");
        } else if (isUsernameExists(username_text)) {
            OptionPane("Username exists", "Error message");
        } else {
            String insert = """
                    INSERT INTO users(name, username, password, gender, securityQuestion, answer, address)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
            try {
                pst = connection.prepareStatement(insert);
                pst.setString(1, name_text);
                pst.setString(2, username_text);
                pst.setString(3, password_text);
                pst.setString(4, gender_text);
                pst.setString(5, question_text);
                pst.setString(6, answer_text);
                pst.setString(7, address_text);
                pst.executeUpdate();
                OptionPane("Register Successfully", "Message");
                signup.getScene().getWindow().hide();
                Stage login = new Stage();
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hotel_management/login.fxml")));
                Scene scene = new Scene(root);
                login.setScene(scene);
                login.show();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isUsernameExists(String username) {
        String query = """
                SELECT username FROM users
                WHERE username = ?      
                """;
        try {
            pst = connection.prepareStatement(query);
            pst.setString(1, username);
            ResultSet resultSet = pst.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @FXML
    public void handleLoginButton(javafx.event.ActionEvent actionEvent) throws IOException {
        signup.getScene().getWindow().hide();
        Stage login = new Stage();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/hotel_management/login.fxml")));
        Scene scene = new Scene(root);
        login.setScene(scene);
        login.show();
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
