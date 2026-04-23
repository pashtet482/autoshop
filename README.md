# AutoShop — Web Information System for Wholesale Auto Parts Trading

## 📌 Project Description

AutoShop is a web-based information system designed to automate wholesale trading processes for automotive parts, components, and accessories, based on the example of “Prospekt LLC”.

The system provides functionality to:

* manage products and warehouse stock
* process customer orders
* track supplies and inventory movements
* manage users and roles
* provide access through a web interface

---

## 🛠 Technologies Used

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Spring Security

### Database

* PostgreSQL

### Frontend

* HTML5
* CSS3
* Vanilla JavaScript

---

## 🗂 System Features

* Product catalog management
* Warehouse stock tracking
* Order creation and processing
* Supply management
* User authentication and authorization
* REST API for client-server interaction

---

## 🏗 Architecture

The application follows a classic client-server architecture:

* Backend: REST API built with Spring Boot
* Frontend: Static web pages served by the backend
* Database: PostgreSQL for persistent data storage

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd autoshop
```

### 2. Configure database

Create a PostgreSQL database and update the configuration in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/autoshop
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Run the application

```bash
mvn spring-boot:run
```

---

## 🔐 Authentication

The system uses Spring Security for basic authentication and authorization.
User roles define access levels within the system.

---

## 📊 Database

The database schema includes entities such as:

* users
* products
* orders
* warehouses
* supplies

---

## 📎 Notes

This project is developed as part of a diploma work and demonstrates the design and implementation of an information system with a web interface.

---

## 👨‍💻 Author

Pashtet482
