package com.hotel_management.Utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.stage.StageStyle;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static int subtractDate(LocalDate startDate, LocalDate endDate) {
        Date start = convertLocalDateToDate(startDate);
        Date end = convertLocalDateToDate(endDate);
        new SimpleDateFormat("yyyy-MM-dd");
        long diff = end.getTime() - start.getTime();
        TimeUnit timeUnit = TimeUnit.DAYS;
        long different = timeUnit.convert(diff, TimeUnit.MILLISECONDS);
        return (int) different;
    }

    public static Date convertLocalDateToDate(LocalDate dateToConvert) {
        return java.sql.Date.valueOf(dateToConvert);
    }

    public static void disablePastDates(DatePicker datePicker) {
        datePicker.setDayCellFactory(param -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
            }
        });
    }

    public static boolean containsNumber(String input) {
        String regex = ".*\\d.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public static void OptionPane(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean containsAlphabetic(String input) {
        for (char c : input.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                return true;
            }
        }
        return false;
    }
}
