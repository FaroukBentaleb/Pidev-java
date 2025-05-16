# Learniverse

A comprehensive JavaFX-based learning platform that allows users to engage in educational competitions, manage learning resources, and interact with an educational community.

![Learniverse Platform](screenshots/main-dashboard.png)

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [System Architecture](#system-architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Performance Optimization](#performance-optimization)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [Testing](#testing)
- [License](#license)
- [Versioning](#versioning)
- [Contact](#contact)
- [Acknowledgments](#acknowledgments)

## Overview

Learniverse is an advanced educational platform built with Java and JavaFX. It aims to provide a comprehensive environment for interactive learning, educational competitions, resource management, and community engagement. The platform features a modern UI with material design elements and offers both user and administrative interfaces.

The project was developed as part of an academic initiative to create a versatile learning environment that combines gamification elements with educational resources. Learniverse targets students, educators, and educational institutions looking for an all-in-one learning management solution.

## Features

### User Authentication and Management
- **Secure Authentication**: Login/registration with password encryption using JBCrypt
- **Two-Factor Authentication**: Enhanced security with 2FA using Google Authenticator
- **Profile Management**: Customizable user profiles with avatars and personal information
- **Role-Based Access Control**: Different permission levels for students, educators, and administrators

### Educational Competitions
- **Competition Creation**: Educators can create custom competitions with specific rules and timeframes
- **Registration System**: Users can browse and register for upcoming competitions
- **Leaderboards**: Real-time ranking system to track participant performance
- **Certificate Generation**: Automatic certificate generation for competition winners
- **Categories and Tags**: Organize competitions by subject area, difficulty level, and skills required

### Learning Resource Management
- **Resource Upload**: Support for various document formats (PDF, DOCX, PPTX)
- **Organization System**: Categorize resources by subject, topic, and difficulty
- **Search Functionality**: Advanced search with filters to find specific resources
- **Rating System**: User ratings and reviews for educational materials
- **Bookmarking**: Save resources for later access

### Community Features
- **Discussion Forums**: Topic-based forums for community interaction
- **Direct Messaging**: Private communication between users
- **Activity Feed**: Updates on recent platform activities
- **Notifications**: Real-time alerts for important events and messages

### Administrative Tools
- **User Management**: Admin panel for managing user accounts
- **Content Moderation**: Tools for reviewing and approving user-generated content
- **Analytics Dashboard**: Insights into platform usage and user engagement
- **Reclamation Handling**: System for processing user complaints and feedback
- **System Monitoring**: Tools for tracking performance and resource usage

### Technical Features
- **Responsive UI**: Modern interface adapting to different screen sizes
- **Offline Support**: Limited functionality available without internet connection
- **Data Import/Export**: Tools for migrating data in and out of the platform
- **QR Code Integration**: Quick access to resources via QR code scanning
- **Multilingual Support**: Interface available in multiple languages

## Technologies Used

### Core Technologies
- **Java 17**: Primary programming language
- **JavaFX 21**: UI framework for desktop application
- **MySQL 8.0**: Relational database for data storage

### UI Components
- **MaterialFX**: Modern UI components for JavaFX
- **FontAwesomeFX**: Icon integration
- **Ikonli**: Additional icon packs integration
- **CSS**: Custom styling for UI components

### Security
- **JBCrypt**: Password hashing and security
- **Google Authenticator API**: Two-factor authentication
- **JWT**: Token-based authentication for API requests

### Data Processing
- **JDBC**: Database connectivity
- **Jackson & Gson**: JSON processing libraries
- **Apache POI**: Microsoft document format handling

### External APIs
- **Google Drive API**: Cloud storage integration
- **Twilio API**: SMS notifications
- **JavaMail API**: Email notifications and communication

### Development Tools
- **Maven**: Dependency management and build automation
- **Git**: Version control
- **JUnit**: Unit testing framework
- **Logback**: Logging framework

## System Architecture

Learniverse follows a layered architecture pattern:

1. **Presentation Layer**:
   - JavaFX UI components
   - FXML view templates
   - CSS styling

2. **Controller Layer**:
   - JavaFX controllers
   - Event handling
   - View-model binding

3. **Service Layer**:
   - Business logic
   - Service implementations
   - External API integrations

4. **Data Access Layer**:
   - Repository implementations
   - JDBC operations
   - Data models

5. **Database Layer**:
   - MySQL database
   - Connection pooling
   - Transaction management

![Architecture Diagram](diagrams/architecture.png)

## Prerequisites

Before you begin, ensure you have the following installed:
- JDK 17 or higher
- Maven 3.6 or higher
- MySQL Server 8.0 or higher
- Git (for cloning the repository)
- Internet connection (for Maven dependencies)
- At least 4GB RAM and 2GB free disk space
- Operating System: Windows 10+, macOS 10.15+, or Linux (Ubuntu 20.04+)

## Installation

### Step 1: Clone the repository
```bash
git clone https://github.com/yourusername/Learniverse.git
cd Learniverse
```

### Step 2: Set up the database
1. Install MySQL Server if not already installed
2. Log in to MySQL and create a new database:
   ```sql
   CREATE DATABASE learniverse;
   CREATE USER 'learniverse_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON learniverse.* TO 'learniverse_user'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. Import the database schema:
   ```bash
   mysql -u learniverse_user -p learniverse < database/schema.sql
   ```
4. (Optional) Import sample data:
   ```bash
   mysql -u learniverse_user -p learniverse < database/sample_data.sql
   ```

### Step 3: Configure the application
1. Copy `src/main/resources/config.properties.example` to `src/main/resources/config.properties`
2. Edit the configuration file with your database credentials and other settings:
   ```properties
   # Database Configuration
   db.url=jdbc:mysql://localhost:3306/learniverse
   db.username=learniverse_user
   db.password=your_password
   
   # Email Configuration
   mail.smtp.host=smtp.gmail.com
   mail.smtp.port=587
   mail.username=your_email@gmail.com
   mail.password=your_app_password
   
   # Twilio Configuration
   twilio.account.sid=your_account_sid
   twilio.auth.token=your_auth_token
   twilio.phone.number=your_twilio_phone_number
   ```

### Step 4: Build and run the application
```bash
# Build the project
mvn clean install

# Run the application
mvn javafx:run
```

## Configuration

### Database Configuration
The database connection is configured in `src/main/java/tn/learniverse/tools/MyConnection.java`. You can modify this file if you need to adjust connection parameters:

```java
// Database URL format: jdbc:mysql://hostname:port/database_name
private static final String URL = "jdbc:mysql://localhost:3306/learniverse";
private static final String USER = "learniverse_user";
private static final String PASSWORD = "your_password";
```

### Email Service Configuration
For email notifications to work, configure your email provider settings in the `config.properties` file.

### External API Configuration
To use external APIs (Google Drive, Twilio), you need to obtain API keys from these services and add them to the configuration file.

## Usage

### User Guide

1. **Starting the Application**:
   - Launch from your IDE or using Maven: `mvn javafx:run`
   - The login screen will appear on startup

2. **Registration Process**:
   - Click on "Create an Account" from the login screen
   - Fill in required details (email, password, name)
   - Verify your email using the verification link
   - Complete your profile with additional information

3. **Logging In**:
   - Enter your email and password
   - If 2FA is enabled, enter the verification code
   - You'll be directed to the main dashboard

4. **Navigating the Interface**:
   - Use the sidebar menu to access different sections
   - The top bar contains notifications, profile menu, and quick actions
   - Dashboard shows personalized content and recommendations

5. **Participating in Competitions**:
   - Browse competitions from the "Competitions" section
   - Register for a competition by clicking the "Join" button
   - Follow competition-specific instructions
   - Track your progress on the leaderboard

6. **Managing Learning Resources**:
   - Access resources from the "Resources" section
   - Upload new resources using the "Add Resource" button
   - Organize resources into collections
   - Rate and review resources you've used

7. **Administrative Functions** (Admin only):
   - Access the admin panel from the profile menu
   - Manage users, competitions, and resources
   - View system statistics and analytics
   - Handle user reclamations

### Keyboard Shortcuts
- `Ctrl+F`: Open search
- `Ctrl+N`: Create new resource/competition
- `Ctrl+H`: Return to dashboard
- `Ctrl+S`: Save current changes
- `F1`: Open help documentation

## Project Structure

```
.
├── database/                      # Database scripts and schema
├── diagrams/                      # Architecture and design diagrams
├── docs/                          # Documentation files
├── licenses/                      # License information for libraries
├── screenshots/                   # Application screenshots
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tn/
│   │   │       └── learniverse/
│   │   │           ├── controllers/     # UI controllers
│   │   │           │   ├── Competition/ # Competition-related controllers
│   │   │           │   ├── Reclamation/ # Reclamation-related controllers 
│   │   │           │   └── user/        # User-related controllers
│   │   │           ├── entities/        # Data models
│   │   │           ├── services/        # Business logic
│   │   │           │   ├── competition/ # Competition services
│   │   │           │   ├── resource/    # Resource management services
│   │   │           │   └── user/        # User management services
│   │   │           ├── tools/           # Utility classes
│   │   │           │   ├── auth/        # Authentication utilities
│   │   │           │   ├── email/       # Email sending utilities
│   │   │           │   └── validation/  # Data validation utilities
│   │   │           ├── tests/           # Test classes
│   │   │           └── Main.java        # Application entry point
│   │   └── resources/
│   │       ├── config/                  # Configuration files
│   │       ├── css/                     # Stylesheets
│   │       ├── fxml/                    # UI layout files
│   │       ├── images/                  # Image resources
│   │       └── templates/               # Email templates
│   └── test/                            # Test files
├── .gitignore
├── LICENSE
├── pom.xml                              # Maven configuration
└── README.md
```

## API Documentation

### Internal APIs

Internal APIs are documented using JavaDoc. Generate the documentation with:

```bash
mvn javadoc:javadoc
```

The generated documentation will be available in `target/site/apidocs/`.

### External APIs

The application integrates with the following external APIs:

1. **Google Drive API**
   - Used for cloud storage of resources
   - Documentation: [Google Drive API v3](https://developers.google.com/drive/api/v3/reference)
   - Implementation in: `src/main/java/tn/learniverse/services/GoogleDriveService.java`

2. **Twilio API**
   - Used for SMS notifications
   - Documentation: [Twilio Java SDK](https://www.twilio.com/docs/libraries/java)
   - Implementation in: `src/main/java/tn/learniverse/services/SmsService.java`

## Performance Optimization

Learniverse is optimized for performance in several ways:

1. **Database Optimization**:
   - Connection pooling to reduce connection overhead
   - Indexed queries for faster data retrieval
   - Lazy loading of data when appropriate

2. **UI Performance**:
   - Use of JavaFX virtual flow for large lists
   - Resource bundling and caching
   - Background loading of heavy content

3. **Memory Management**:
   - Resource disposal when no longer needed
   - Pagination of large data sets
   - Efficient image handling and scaling

## Troubleshooting

### Common Issues

1. **Database Connection Errors**:
   - Verify MySQL is running: `sudo systemctl status mysql`
   - Check connection credentials in `MyConnection.java`
   - Ensure the database exists: `SHOW DATABASES;` in MySQL

2. **JavaFX Errors**:
   - Ensure JavaFX modules are included: Check `module-info.java`
   - Verify FXML files are correct: Check for syntax errors
   - Path issues: Confirm resource paths are correct

3. **Performance Issues**:
   - Low memory: Increase JVM heap size with `-Xmx` flag
   - Slow startup: Check for heavy initialization processes
   - UI lag: Look for long operations on the JavaFX thread

### Logs

Application logs are stored in:
- Windows: `%APPDATA%\Learniverse\logs\`
- macOS: `~/Library/Logs/Learniverse/`
- Linux: `~/.local/share/Learniverse/logs/`

## Contributing

We welcome contributions to Learniverse! If you would like to contribute, please follow these steps:

1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature-name`)
3. Make your changes
4. Ensure code quality:
   - Run tests: `mvn test`
   - Check code style: `mvn checkstyle:check`
5. Commit your changes (`git commit -m 'Add some feature'`)
6. Push to the branch (`git push origin feature/your-feature-name`)
7. Open a Pull Request

### Code Style Guidelines

- Follow Java naming conventions
- Use meaningful variable and method names
- Write JavaDoc comments for public methods
- Keep methods focused and single-purpose
- Use appropriate design patterns

### Pull Request Process

1. Update the README.md with details of changes if applicable
2. Update the docs with any new features or API changes
3. The PR will be merged once reviewed and approved by a maintainer
4. Your contribution will be recognized in the Acknowledgments section

## Testing

The project uses JUnit for unit testing. Run tests with:

```bash
mvn test
```

### Test Coverage

Generate a test coverage report with:

```bash
mvn jacoco:report
```

The report will be available at `target/site/jacoco/index.html`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Versioning

We use [Semantic Versioning](https://semver.org/) for version management. The current version is 1.0.0.

### Release Notes

#### Version 1.0.0 (2024-05-15)
- Initial public release
- Complete competition management system
- User authentication with 2FA
- Resource management features
- Administrative dashboard

#### Version 0.9.0 (2024-04-10)
- Beta release for testing
- Core functionality implemented
- Known issues with resource upload
- Limited administrative features

## Contact

For any questions or suggestions, please reach out to:


- Project Repository: [GitHub](https://github.com/FaroukBentaleb/Pidev-java)

## Acknowledgments

- [JavaFX Community](https://openjfx.io/) for the UI framework
- [MaterialFX](https://github.com/palexdev/MaterialFX) for modern UI components
- All our contributors and testers
- [ESPRIT University](https://esprit.tn/) for supporting this project

---

© 2025 Learniverse Team. All Rights Reserved. 
