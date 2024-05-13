CREATE DATABASE vehiclesystem;

CREATE TABLE role (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50)
);

CREATE TABLE license (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50)
);
ALTER TABLE license
ADD COLUMN description VARCHAR(100);

CREATE TABLE user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) DEFAULT '',
    phone_number VARCHAR(10) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL DEFAULT '',
    address VARCHAR(255) DEFAULT '',
    role_id INT,
    status VARCHAR(20),
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (role_id) REFERENCES role(id),
);


CREATE TABLE user_licenses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    licenses_id INT,
    FOREIGN KEY (user_id) REFERENCES user (id),
    FOREIGN KEY (licenses_id) REFERENCES license (id)
);

CREATE TABLE user_images (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    image_url VARCHAR(300),
    FOREIGN KEY (user_id) REFERENCES users(id)
);


CREATE TABLE vehicle (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    payload INT,
    material VARCHAR(100),
    status VARCHAR(20),
    license_plate VARCHAR(20)
);
ALTER TABLE vehicle
ADD COLUMN vehicle_type VARCHAR(50),
ADD COLUMN driver_license INT,
ADD COLUMN rental_price FLOAT,
ADD COLUMN created_at DATETIME,
ADD COLUMN updated_at DATETIME,
ADD COLUMN thumbnail VARCHAR(200),
ADD CONSTRAINT fk_driver_license FOREIGN KEY (driver_license) REFERENCES license(id);



CREATE TABLE vehicle_images (
    id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE CASCADE,
    image_url VARCHAR(300)
);


CREATE TABLE vehicle_maintenance_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT,
    maintenance_date DATE,
    maintenance_type VARCHAR(100),
    description TEXT,
    cost DECIMAL(10, 2),
    image_url VARCHAR(300),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id)
);
ALTER TABLE vehicle_maintenance_history
ADD COLUMN status VARCHAR(50);

CREATE TABLE delivery_order (
    id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT,
    user_id INT,
    start_place VARCHAR(255),
    end_place VARCHAR(255),
    start_date DATETIME,
    phone_number VARCHAR(20),
    distance FLOAT,
    duration FLOAT,
    status VARCHAR(20),
    vehicle_price DOUBLE,
    driver_price DOUBLE,
    sum_of_expense DOUBLE,
    profit DOUBLE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);


