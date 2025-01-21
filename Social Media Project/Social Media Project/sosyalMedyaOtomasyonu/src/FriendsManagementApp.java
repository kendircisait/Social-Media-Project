import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class FriendsManagementApp extends Application {
    private static final String DATABASE_URL = "jdbc:sqlite:friends.db";
    private Connection connection;

    private Map<String, User> users = new HashMap<>();
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Arkadaş Yönetim Uygulaması");

        openDatabaseConnection();

        showLoginPanel();

        primaryStage.show();
    }

    private void openDatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
            createUsersTable();
            loadUsersFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUsersTable() {
        String query = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL)";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUsersFromDatabase() {
        String query = "SELECT * FROM users";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");

                User user = new User(name, username, password);
                users.put(username, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showLoginPanel() {
        GridPane loginPane = new GridPane();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(10));

        Label usernameLabel = new Label("Kullanıcı Adı:");
        TextField usernameField = new TextField();
        loginPane.add(usernameLabel, 0, 0);
        loginPane.add(usernameField, 1, 0);

        Label passwordLabel = new Label("Şifre:");
        PasswordField passwordField = new PasswordField();
        loginPane.add(passwordLabel, 0, 1);
        loginPane.add(passwordField, 1, 1);

        Button loginButton = new Button("Giriş Yap");
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            User user = getUser(username);

            if (user != null && user.authenticate(password)) {
                primaryStage.setScene(createFriendsScene(primaryStage, user));
            } else {
                showAlert(Alert.AlertType.ERROR, "Giriş Hatası", "Geçersiz kullanıcı adı veya şifre.");
            }
        });

        Button registerButton = new Button("Üye Ol");
        registerButton.setOnAction(event -> showRegistrationPanel());

        loginPane.add(loginButton, 1, 2);
        loginPane.add(registerButton, 1, 3);

        Scene loginScene = new Scene(loginPane, 400, 200);
        primaryStage.setScene(loginScene);
    }

    private void showRegistrationPanel() {
        GridPane registrationPane = new GridPane();
        registrationPane.setAlignment(Pos.CENTER);
        registrationPane.setHgap(10);
        registrationPane.setVgap(10);
        registrationPane.setPadding(new Insets(10));

        Label nameLabel = new Label("Ad:");
        TextField nameField = new TextField();
        registrationPane.add(nameLabel, 0, 0);
        registrationPane.add(nameField, 1, 0);

        Label usernameLabel = new Label("Kullanıcı Adı:");
        TextField usernameField = new TextField();
        registrationPane.add(usernameLabel, 0, 1);
        registrationPane.add(usernameField, 1, 1);

        Label passwordLabel = new Label("Şifre:");
        PasswordField passwordField = new PasswordField();
        registrationPane.add(passwordLabel, 0, 2);
        registrationPane.add(passwordField, 1, 2);

        Button registerButton = new Button("Kayıt Ol");
        registerButton.setOnAction(event -> {
            String name = nameField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Uyarı", "Tüm alanları doldurun.");
            } else if (getUser(username) != null) {
                showAlert(Alert.AlertType.WARNING, "Uyarı", "Bu kullanıcı adı zaten alınmış.");
            } else {
                User newUser = new User(name, username, password);
                addUser(newUser);
                showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Kayıt işlemi tamamlandı. Giriş yapabilirsiniz.");
                showLoginPanel();
            }
        });

        Button backButton = new Button("Geri");
        backButton.setOnAction(event -> showLoginPanel());

        registrationPane.add(registerButton, 1, 3);
        registrationPane.add(backButton, 0, 3);

        Scene registrationScene = new Scene(registrationPane, 400, 250);
        primaryStage.setScene(registrationScene);
    }

    private Scene createFriendsScene(Stage primaryStage, User user) {
        GridPane friendsPane = new GridPane();
        friendsPane.setAlignment(Pos.CENTER);
        friendsPane.setHgap(10);
        friendsPane.setVgap(10);
        friendsPane.setPadding(new Insets(10));

        Label welcomeLabel = new Label("Hoş Geldiniz, " + user.getName());
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        friendsPane.add(welcomeLabel, 0, 0, 2, 1);

        Label friendNameLabel = new Label("Arkadaş Adı:");
        TextField friendNameField = new TextField();
        friendsPane.add(friendNameLabel, 0, 1);
        friendsPane.add(friendNameField, 1, 1);

        Label genderLabel = new Label("Cinsiyet:");
        TextField genderField = new TextField();
        friendsPane.add(genderLabel, 0, 2);
        friendsPane.add(genderField, 1, 2);

        Label ageLabel = new Label("Yaş:");
        TextField ageField = new TextField();
        friendsPane.add(ageLabel, 0, 3);
        friendsPane.add(ageField, 1, 3);

        Label locationLabel = new Label("Yaşadığı Yer:");
        TextField locationField = new TextField();
        friendsPane.add(locationLabel, 0, 4);
        friendsPane.add(locationField, 1, 4);

        Button addFriendButton = new Button("Arkadaş Ekle");
        addFriendButton.setOnAction(event -> {
            String friendName = friendNameField.getText();
            String gender = genderField.getText();
            String age = ageField.getText();
            String location = locationField.getText();

            if (!friendName.isEmpty()) {
                Friend friend = new Friend(friendName, gender, age, location);
                user.addFriend(friend);
                showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Arkadaş eklendi: " + friend.getName());
                showFriends(user, friendsPane);
            } else {
                showAlert(Alert.AlertType.WARNING, "Uyarı", "Arkadaş adı boş olamaz.");
            }
        });

        Button deleteFriendButton = new Button("Arkadaş Sil");
        deleteFriendButton.setOnAction(event -> {
            String selectedFriendName = friendNameField.getText();
            if (!selectedFriendName.isEmpty()) {
                user.removeFriend(selectedFriendName);
                showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Arkadaş silindi: " + selectedFriendName);
                showFriends(user, friendsPane);
            } else {
                showAlert(Alert.AlertType.WARNING, "Uyarı", "Silmek istediğiniz arkadaşı seçin.");
            }
        });

        Button logoutButton = new Button("Çıkış Yap");
        logoutButton.setOnAction(event -> showLoginPanel());

        friendsPane.add(addFriendButton, 1, 5);
        friendsPane.add(deleteFriendButton, 1, 6);
        friendsPane.add(logoutButton, 0, 7);

        showFriends(user, friendsPane);

        Scene friendsScene = new Scene(friendsPane, 500, 400);
        return friendsScene;
    }

    private void showFriends(User user, GridPane friendsPane) {
        friendsPane.getChildren().removeIf(node -> node instanceof Label && ((Label) node).getText().startsWith("Arkadaş:"));

        int row = 8;
        for (Friend friend : user.getFriends().values()) {
            Label friendLabel = new Label("Arkadaş: " + friend.getName() +
                    " | Cinsiyet: " + friend.getGender() +
                    " | Yaş: " + friend.getAge() +
                    " | Yaşadığı Yer: " + friend.getLocation());
            friendsPane.add(friendLabel, 0, row++);
        }
    }

    private User getUser(String username) {
        return users.get(username);
    }

    private void addUser(User user) {
        if (getUser(user.getUsername()) == null) {
            users.put(user.getUsername(), user);
            saveUserToDatabase(user);
        } else {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Bu kullanıcı adı zaten alınmış.");
        }
    }

    private void saveUserToDatabase(User user) {
        String query = "INSERT INTO users (name, username, password) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getUsername());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeDatabaseConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        closeDatabaseConnection();
    }

    private class User {
        private String name;
        private String username;
        private String password;
        private Map<String, Friend> friends;

        public User(String name, String username, String password) {
            this.name = name;
            this.username = username;
            this.password = password;
            this.friends = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public boolean authenticate(String password) {
            return this.password.equals(password);
        }

        public void addFriend(Friend friend) {
            friends.put(friend.getName(), friend);
        }

        public void removeFriend(String friendName) {
            friends.remove(friendName);
        }

        public Map<String, Friend> getFriends() {
            return friends;
        }
    }

    private class Friend {
        private String name;
        private String gender;
        private String age;
        private String location;

        public Friend(String name, String gender, String age, String location) {
            this.name = name;
            this.gender = gender;
            this.age = age;
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public String getGender() {
            return gender;
        }

        public String getAge() {
            return age;
        }

        public String getLocation() {
            return location;
        }
    }
}
