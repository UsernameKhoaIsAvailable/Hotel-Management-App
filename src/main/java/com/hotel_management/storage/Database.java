package com.hotel_management.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/hotel_management?allowMultiQueries=true";
    private static final String USER = "root";
    private static final String PASS = "12345678";

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DATABASE_URL, USER, PASS);
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public static void init() throws SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        String sqlQuery = """
                        CREATE TABLE IF NOT EXISTS users (
                        id INT NOT NULL AUTO_INCREMENT,
                        name varchar(200) NOT NULL,
                        username varchar(200) NOT NULL,
                        password varchar(200) NOT NULL,
                        gender varchar(20) NOT NULL,
                        securityQuestion varchar(100) NOT NULL,
                        answer varchar(200) NOT NULL,
                        address varchar(200) NOT NULL,
                        status varchar(20) DEFAULT NULL,
                        PRIMARY KEY (id)
                        );
                                                
                        ALTER TABLE users AUTO_INCREMENT=1;
                        
                        CREATE TABLE IF NOT EXISTS customers (
                        customerID INT NOT NULL AUTO_INCREMENT,
                        customerIDNumber INT NOT NULL,
                        customerName varchar(50) NOT NULL,
                        customerNationality varchar(50) NOT NULL,
                        customerGender varchar(50) NOT NULL,
                        customerPhoneNo INT NOT NULL,
                        customerEmail varchar(50) NOT NULL,
                        PRIMARY KEY (customerID)
                        );
                        
                        ALTER TABLE customers AUTO_INCREMENT=1;
                        
                        CREATE TABLE IF NOT EXISTS rooms (
                        roomNumber varchar(20) NOT NULL,
                        roomType varchar(50) NOT NULL,
                        price INT NOT NULL,
                        status varchar(50) NOT NULL DEFAULT 'Not Booked',
                        PRIMARY KEY (roomNumber)
                        );
                        
                        CREATE TABLE IF NOT EXISTS reservations (
                        reservationID INT NOT NULL AUTO_INCREMENT,
                        customerID INT NOT NULL,
                        roomNumber varchar(20) NOT NULL,
                        checkInDate DATE NOT NULL,
                        checkOutDate DATE NOT NULL,
                        status varchar(20) NOT NULL DEFAULT 'Checked in',
                        PRIMARY KEY (reservationID),
                        KEY fk_customers_res (customerID),
                        KEY fk_rooms_res (roomNumber),
                        CONSTRAINT fk_customers_res FOREIGN KEY (customerID)
                        REFERENCES customers (customerID) ON UPDATE CASCADE,
                        CONSTRAINT fk_rooms_res FOREIGN KEY (roomNumber)
                        REFERENCES rooms (roomNumber) ON UPDATE CASCADE
                        );
                        
                        ALTER TABLE reservations AUTO_INCREMENT=1;
                                                
                        CREATE TABLE IF NOT EXISTS bills (
                        billID INT NOT NULL AUTO_INCREMENT,
                        reservationID INT NOT NULL,
                        billDate DATE NOT NULL,
                        billAmount INT NOT NULL,
                        PRIMARY KEY (billID),
                        UNIQUE KEY fk_bills_res (reservationID) USING BTREE,
                        CONSTRAINT fk_bills_res FOREIGN KEY (reservationID)
                        REFERENCES reservations (reservationID) ON UPDATE CASCADE
                        );
                        
                        ALTER TABLE bills AUTO_INCREMENT=1;
                """;
        statement.executeUpdate(sqlQuery);
        statement.close();
        connection.close();
    }
}
