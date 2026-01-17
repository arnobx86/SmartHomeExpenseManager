CREATE DATABASE IF NOT EXISTS smart_home_expense_db;
USE smart_home_expense_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Default login: arnob / arnobx86
INSERT IGNORE INTO users (username, password) VALUES ('arnob', 'arnobx86');

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT IGNORE INTO categories (name) VALUES ('Food'), ('Transport'), ('Utilities'), ('Entertainment'), ('Health'), ('Others');

CREATE TABLE IF NOT EXISTS expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50),
    amount DOUBLE NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (category) REFERENCES categories(name) ON DELETE SET NULL ON UPDATE CASCADE
);
