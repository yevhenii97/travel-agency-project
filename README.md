# Travel Agency Management Service

Spring Boot web application for managing travel vouchers, user accounts, orders, balances, and role-based travel agency operations.

## Application Overview

Travel Agency Service allows users to browse available tours, filter vouchers by categories, order vouchers, deposit balance, and manage their own account.

Managers can control operational voucher state, such as marking vouchers as hot and changing voucher status.

Administrators can manage users and vouchers, including blocking users, promoting users to managers, and creating/editing/deleting vouchers.

## Main Capabilities

- User registration and login
- Role-based access control
- Voucher catalog
- Voucher filtering
- Voucher ordering
- User balance management
- Balance transaction history support
- Hot voucher management
- Voucher status management
- Admin user management
- Voucher CRUD management
- UI internationalization
- Validation and global error handling
- Logging

## Tech Stack

- Java
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- Thymeleaf
- Bootstrap
- H2 Database
- Bean Validation
- Logback
- BCrypt

## User Roles

### USER

- Register and sign in
- View available vouchers
- Filter vouchers
- Order vouchers
- View own vouchers
- Deposit balance
- Change password

### MANAGER

- Mark vouchers as hot
- Change voucher status

### ADMIN

- Create/Edit/Delete vouchers
- Manage users
- Block/Unblock users
- Promote users to manager

## Internationalization

Supported languages:

- English
- Ukrainian

Implemented through:

- messages.properties
- messages_uk.properties

## Local Run

```bash
mvn bootRun --args='--spring.profiles.active=dev'
```

Application URL:

```text
http://localhost:8080
```

## Swagger

```text
http://localhost:8080/swagger-ui.html
```

## H2 Console

```text
http://localhost:8080/h2-console
```
