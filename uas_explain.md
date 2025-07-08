# Implementasi Konsep Pemrograman di GitHub Repository Tracker

## 1. Pemrograman Generic
- **Lokasi**: `GenericTableModel.java`
- **Implementasi**:
  ```java
  public class GenericTableModel<T> extends AbstractTableModel {
      private final Function<T, Object>[] mappers;
      private final List<T> data;
  }
  ```
- **Kegunaan**: 
  - Model tabel yang fleksibel untuk berbagai tipe data
  - Bisa digunakan untuk menampilkan repository, commit, atau data lainnya
  - Menggunakan generic type `T` untuk tipe data yang dinamis

## 2. Thread
- **Lokasi**: 
  1. `TrackerFrame.java` - `RepoWorker`
  2. `UserProfilePanel.java` - Avatar loading
  3. `GithubRepoTracker.java` - Event Dispatch Thread
- **Implementasi**:
  ```java
  // Background tasks menggunakan SwingWorker
  private class RepoWorker extends SwingWorker<FetchResult, Void> {
      @Override
      protected FetchResult doInBackground() { ... }
  }

  // Concurrent operations dengan CompletableFuture
  CompletableFuture<GitHubUser> userFuture = CompletableFuture.supplyAsync(() -> {...});
  ```
- **Kegunaan**: 
  - Menjaga UI tetap responsif saat melakukan operasi berat
  - Loading data secara asynchronous
  - Parallel processing untuk fetch data

## 3. Serialisasi
- **Lokasi**: `Repo.java`
- **Implementasi**:
  ```java
  public class Repo implements Serializable {
      private static final long serialVersionUID = 1L;
      @SerializedName("stargazers_count")
      private int stargazersCount;
  }
  ```
- **Kegunaan**:
  - Konversi objek Java ke JSON dan sebaliknya
  - Penyimpanan data repository ke file
  - Integrasi dengan GitHub API

## 4. Internasionalisasi
- **Lokasi**: `ResourceManager.java`
- **Implementasi**:
  ```java
  public class ResourceManager {
      private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);
      public static void setLocale(Locale locale) { ... }
  }
  ```
- **Kegunaan**:
  - Dukungan multi bahasa (Indonesia dan Inggris)
  - Pergantian bahasa secara dinamis
  - Lokalisasi teks UI

## 5. Cryptography
- **Lokasi**: `HashUtil.java`
- **Implementasi**:
  ```java
  public static String sha256(String input) {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = md.digest(input.getBytes());
  }
  ```
- **Kegunaan**:
  - Enkripsi password pengguna
  - Keamanan data sensitif
  - Hashing untuk verifikasi data

## 6. Database (SQL)
- **Lokasi**: 
  1. `RepoDatabase.java`
  2. `UserDatabase.java`
- **Implementasi**:
  ```java
  // MySQL connection
  private static final String DB_URL = "jdbc:mysql://localhost:8889/github_tracker";
  // Prepared statements untuk keamanan
  String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
  ```
- **Kegunaan**:
  - Penyimpanan data pengguna dan repository
  - Caching data GitHub
  - Manajemen sesi pengguna

## 7. Multimedia
- **Lokasi**: `SoundPlayer.java`
- **Implementasi**:
  ```java
  public class SoundPlayer {
      public static void play(String resourcePath) {
          Media media = new Media(uri);
          MediaPlayer mediaPlayer = new MediaPlayer(media);
      }
  }
  ```
- **Kegunaan**:
  - Efek suara untuk feedback pengguna
  - Menggunakan JavaFX Media Player
  - Meningkatkan user experience

## 8. Network
- **Lokasi**: `GithubService.java`
- **Implementasi**:
  ```java
  public class GithubService {
      private static final String API_BASE = "https://api.github.com";
      private final HttpClient httpClient = HttpClient.newHttpClient();
  }
  ```
- **Kegunaan**:
  - Komunikasi dengan GitHub API
  - HTTP requests untuk fetch data
  - Handling network errors dan rate limiting
  - Parsing JSON responses

Semua konsep ini diintegrasikan dengan baik untuk membuat aplikasi yang:
- Responsif dan user-friendly
- Aman dan reliable
- Mendukung multiple bahasa
- Efisien dalam penggunaan resources
- Mudah dimaintain dan dikembangkan