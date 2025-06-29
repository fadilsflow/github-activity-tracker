# GitHub Repository Tracker

A Java Swing application for tracking GitHub repositories and their activities. This application allows users to search for GitHub users, view their repositories, and monitor repository activities including commits, stars, and forks.

## Features

- User authentication and registration system
- GitHub user profile viewing with avatar and bio
- Repository listing with stars and forks count
- Commit history viewing for each repository
- Bilingual support (English and Indonesian)
- Modern UI with gradient backgrounds and smooth animations
- Local database caching for offline access
- Real-time repository activity tracking

## Requirements

- Java Development Kit (JDK) 11 or higher
- MySQL 5.7 or higher
- Maven 3.6 or higher
- Active internet connection for GitHub API access

## Database Setup

1. Create a MySQL database named `github_tracker`
2. Configure the database connection in:
   - `src/main/java/com/github/repo/tracker/db/UserDatabase.java`
   - `src/main/java/com/github/repo/tracker/db/RepoDatabase.java`
3. Default configuration:
   ```
   DB_URL = "jdbc:mysql://localhost:8889/github_tracker"
   DB_USER = "root"
   DB_PASS = "root"
   ```

## Building and Running

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/github-repo-tracker.git
   cd github-repo-tracker
   ```

2. Build the project:

   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   java -jar target/github-repo-tracker-1.0.jar
   ```

## Project Structure

- `src/main/java/com/github/repo/tracker/`

  - `ui/` - User interface components
  - `db/` - Database operations
  - `model/` - Data models
  - `network/` - GitHub API integration
  - `util/` - Utility classes

- `src/main/resources/`
  - `messages_en.properties` - English translations
  - `messages_id.properties` - Indonesian translations

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Notes

- The application requires an active internet connection to fetch data from GitHub
- GitHub API has rate limiting - you may need to wait if you exceed the limit
- Some features may require GitHub authentication in future versions
