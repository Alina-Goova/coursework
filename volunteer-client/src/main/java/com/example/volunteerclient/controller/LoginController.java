package com.example.volunteerclient.controller;

import com.example.volunteerclient.model.AuthResponse;
import com.example.volunteerclient.service.ApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    private ApiService apiService;

    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Button loginButton;
    @FXML private Label loginMessage;

    @FXML private TextField registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private TextField registerNameField;
    @FXML private ComboBox<String> registerRoleComboBox;
    @FXML private Button registerButton;
    @FXML private Label registerMessage;

    public LoginController() {
        this.apiService = new ApiService(); // Создаем ApiService внутри
    }

    @FXML
    public void initialize() {
        System.out.println("LoginController initialized!");

        if (registerRoleComboBox != null) {
            registerRoleComboBox.getItems().addAll("VOLUNTEER", "ADMIN");
            registerRoleComboBox.setValue("VOLUNTEER");
        }

        setupFieldListeners();
    }

    private void setupFieldListeners() {
        if (loginEmailField != null) {
            loginEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (loginMessage != null) loginMessage.setText("");
            });
        }
        if (loginPasswordField != null) {
            loginPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (loginMessage != null) loginMessage.setText("");
            });
        }
        if (registerEmailField != null) {
            registerEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (registerMessage != null) registerMessage.setText("");
            });
        }
        if (registerPasswordField != null) {
            registerPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (registerMessage != null) registerMessage.setText("");
            });
        }
        if (registerNameField != null) {
            registerNameField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (registerMessage != null) registerMessage.setText("");
            });
        }
    }

    @FXML
    private void handleLogin() {
        System.out.println("Login button clicked!");

        String email = loginEmailField.getText().trim();
        String password = loginPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            if (loginMessage != null) loginMessage.setText("Заполните все поля");
            return;
        }

        if (loginButton != null) loginButton.setDisable(true);
        if (loginMessage != null) loginMessage.setText("Вход...");

        apiService.login(email, password)
                .thenAccept(authResponse -> Platform.runLater(() -> {
                    if (loginMessage != null) loginMessage.setText("Успешный вход!");
                    apiService.setAuthToken(authResponse.getToken());
                    openMainWindow(authResponse);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        if (loginMessage != null) loginMessage.setText(msg);
                        if (loginButton != null) loginButton.setDisable(false);
                    });
                    return null;
                });
    }

    @FXML
    private void handleRegister() {
        System.out.println("Register button clicked!");

        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText();
        String name = registerNameField.getText().trim();
        String role = registerRoleComboBox != null ? registerRoleComboBox.getValue() : "VOLUNTEER";

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            if (registerMessage != null) registerMessage.setText("Заполните все поля");
            return;
        }

        if (registerButton != null) registerButton.setDisable(true);
        if (registerMessage != null) registerMessage.setText("Регистрация...");

        apiService.register(email, password, name, role)
                .thenAccept(authResponse -> Platform.runLater(() -> {
                    if (registerMessage != null) registerMessage.setText("Успешная регистрация!");
                    apiService.setAuthToken(authResponse.getToken());
                    openMainWindow(authResponse);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        if (registerMessage != null) registerMessage.setText(msg);
                        if (registerButton != null) registerButton.setDisable(false);
                    });
                    return null;
                });
    }

    private void openMainWindow(AuthResponse authResponse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            MainController mainController = loader.getController();
            mainController.setApiService(this.apiService);
            
            root.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            if (authResponse != null && authResponse.getUser() != null) {
                mainController.setCurrentUser(authResponse.getUser());
            }
            
            Scene scene = new Scene(root, 1200, 800);

            Stage stage = new Stage();
            stage.setTitle("Волонтерская система - Главная");
            stage.setScene(scene);
            stage.show();

            if (loginButton != null) {
                loginButton.getScene().getWindow().hide();
            } else if (registerButton != null) {
                 registerButton.getScene().getWindow().hide();
            }

        } catch (Exception e) {
            e.printStackTrace();
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileOutputStream("error_log.txt"))) {
                e.printStackTrace(pw);
            } catch (java.io.IOException ioEx) {
                ioEx.printStackTrace();
            }
            if (loginMessage != null) loginMessage.setText("Ошибка открытия главного окна: " + e.getMessage());
        }
    }
}