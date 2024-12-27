# FinancialApp
FinancialApp is a Java-based GUI application designed for managing financial transactions. The application allows users to add, delete, and export transactions to a PDF file. With the recent updates, the application now utilizes Spring Boot for backend services and stores transaction data in a MySQL database.

## Features
- User Authentication: Secure login for users.
- Transaction Management: Add, delete, and view transactions.
- Data Export: Export transaction data to PDF format.
- Database Integration: Store transaction data in a MySQL database.
  
## Technologies Used
- Java: The primary programming language for the application.
- Spring Boot: Framework for building the backend services.
- MySQL: Database for storing transaction data.
- JUnit: Testing framework for unit tests.
- iText: Library for generating PDF files.
- JSON: For data interchange.

## Prerequisites
Before running the application, ensure you have the following installed:
- Java Development Kit (JDK) 11 or higher
- MySQL Server
- Maven (for dependency management)
- Setup Instructions
  
## Clone the Repository:
```
git clone https://github.com/yourusername/FinancialApp.git
cd FinancialApp
```

## Configure MySQL Database:
Create a new database in MySQL:
```
CREATE DATABASE financial_app;
```
## Update the application.properties file in the src/main/resources directory with your MySQL credentials:
```
spring.datasource.url=jdbc:mysql://localhost:3306/financial_app
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

## Build the Application:

Use Maven to build the project:
```
mvn clean install
```
## Run the Application:
### Start the Spring Boot application:
```
mvn spring-boot:run
```

### Access the Application:
The application will be available at http://localhost:8080.

### Running Tests
To run the unit tests, use the following command:
```
mvn test
```

## Usage
- Login: Use the credentials admin for username and password for password to log in.
- Manage Transactions: Add, delete, and view transactions through the GUI.
- Export to PDF: Export your transaction data to a PDF file.

## Contributing
Contributions are welcome! Please feel free to submit a pull request or open an issue for any enhancements or bug fixes.
