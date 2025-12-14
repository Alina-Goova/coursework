package com.example.volunteerclient.controller;

import com.example.volunteerclient.model.User;
import com.example.volunteerclient.service.ApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class AdminUsersController {

    private ApiService apiService;
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;

    @FXML private TextField userSearchField;
    @FXML private ComboBox<String> userRoleFilter;
    @FXML private ComboBox<String> userStatusFilter;
    @FXML private Button refreshUsersButton;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userStatusColumn;
    @FXML private TableColumn<User, String> userRegDateColumn;

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        if (apiService != null) {
            loadUsers();
        }
    }

    private void setupTable() {
        if (usersTable == null) return;

        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userRegDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        
        userStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActive() ? "Активен" : "Заблокирован"));

        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem editItem = new MenuItem("Редактировать");
            editItem.setOnAction(e -> handleEditUser(row.getItem()));

            MenuItem statusItem = new MenuItem("Блокировать/Разблокировать");
            statusItem.setOnAction(e -> handleToggleStatus(row.getItem()));

            MenuItem passwordItem = new MenuItem("Сбросить пароль");
            passwordItem.setOnAction(e -> handleResetPassword(row.getItem()));
            
            MenuItem deleteItem = new MenuItem("Удалить");
            deleteItem.setOnAction(e -> handleDeleteUser(row.getItem()));

            row.setOnContextMenuRequested(e -> {
                contextMenu.getItems().clear();
                if (row.getItem() != null) {
                    contextMenu.getItems().addAll(editItem, statusItem, passwordItem, deleteItem);
                }
            });

            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );
            return row;
        });

        filteredUsers = new FilteredList<>(usersList, p -> true);
        usersTable.setItems(filteredUsers);
    }

    private void setupFilters() {
        userRoleFilter.getItems().addAll("Все", "VOLUNTEER", "ADMIN");
        userRoleFilter.setValue("Все");
        
        userStatusFilter.getItems().addAll("Все", "Активен", "Заблокирован");
        userStatusFilter.setValue("Все");

        userSearchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        userRoleFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        userStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
    }

    private void updateFilter() {
        String searchText = userSearchField.getText().toLowerCase().trim();
        String roleFilter = userRoleFilter.getValue();
        String statusFilter = userStatusFilter.getValue();

        filteredUsers.setPredicate(user -> {
            boolean matchesSearch = searchText.isEmpty() || 
                                    user.getName().toLowerCase().contains(searchText) ||
                                    user.getEmail().toLowerCase().contains(searchText);
            
            boolean matchesRole = "Все".equals(roleFilter) || user.getRole().equals(roleFilter);
            
            boolean isUserActive = user.isActive();
            boolean matchesStatus = "Все".equals(statusFilter) || 
                                    (statusFilter.equals("Активен") && isUserActive) ||
                                    (statusFilter.equals("Заблокирован") && !isUserActive);

            return matchesSearch && matchesRole && matchesStatus;
        });
    }

    @FXML
    public void loadUsers() {
        if (apiService == null) return;
        
        if (refreshUsersButton != null) refreshUsersButton.setDisable(true);
        
        apiService.getAllUsers()
                .thenAccept(users -> Platform.runLater(() -> {
                    usersList.setAll(users);
                    if (refreshUsersButton != null) refreshUsersButton.setDisable(false);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                         showError("Ошибка загрузки пользователей: " + e.getMessage());
                         if (refreshUsersButton != null) refreshUsersButton.setDisable(false);
                    });
                    return null;
                });
    }

    private static class UserCreationData {
        String name;
        String email;
        String password;
        String role;
        
        UserCreationData(String name, String email, String password, String role) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }

    @FXML
    private void handleAddUser() {
        Dialog<UserCreationData> dialog = new Dialog<>();
        dialog.setTitle("Новый пользователь");
        dialog.setHeaderText("Создание пользователя");

        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("VOLUNTEER", "ADMIN");
        roleBox.setValue("VOLUNTEER");

        grid.add(new Label("Имя:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Пароль:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Роль:"), 0, 3);
        grid.add(roleBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new UserCreationData(
                    nameField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    roleBox.getValue()
                );
            }
            return null;
        });

        Optional<UserCreationData> result = dialog.showAndWait();

        result.ifPresent(data -> {
            apiService.createUser(data.email, data.password, data.name, data.role)
                    .thenAccept(createdUser -> Platform.runLater(() -> {
                        usersList.add(createdUser);
                        showInfo("Пользователь создан: " + createdUser.getEmail());
                    }))
                    .exceptionally(e -> {
                        Throwable cause = e;
                        while (cause.getCause() != null && (cause instanceof java.util.concurrent.CompletionException || cause instanceof RuntimeException)) {
                            cause = cause.getCause();
                        }
                        String msg = cause.getMessage();
                        Platform.runLater(() -> showError(msg));
                        return null;
                    });
        });
    }

    private void handleEditUser(User user) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Смена роли");
        dialog.setHeaderText("Изменить роль для " + user.getName());
        
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("VOLUNTEER", "ADMIN");
        roleBox.setValue(user.getRole());
        
        VBox content = new VBox(10, new Label("Роль:"), roleBox);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        
        ButtonType saveBtn = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        
        dialog.setResultConverter(btn -> btn == saveBtn ? roleBox.getValue() : null);
        
        dialog.showAndWait().ifPresent(newRole -> {
            if (!newRole.equals(user.getRole())) {
                apiService.updateUserRole(user.getId(), newRole)
                    .thenAccept(updated -> Platform.runLater(() -> {
                        int idx = usersList.indexOf(user);
                        if (idx >= 0) usersList.set(idx, updated);
                        showInfo("Роль обновлена.");
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> showError("Ошибка: " + e.getMessage()));
                        return null;
                    });
            }
        });
    }

    private void handleToggleStatus(User user) {
        boolean newStatus = !user.isActive();
        String action = newStatus ? "Разблокировать" : "Заблокировать";
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(action + " пользователя " + user.getName() + "?");
        
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                apiService.updateUserStatus(user.getId(), newStatus)
                    .thenAccept(updated -> Platform.runLater(() -> {
                        int idx = usersList.indexOf(user);
                        if (idx >= 0) usersList.set(idx, updated);
                        showInfo("Статус обновлен.");
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> showError("Ошибка: " + e.getMessage()));
                        return null;
                    });
            }
        });
    }

    private void handleResetPassword(User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Сброс пароля");
        dialog.setHeaderText("Установите новый пароль для " + user.getName());
        dialog.setContentText("Новый пароль:");
        
        dialog.showAndWait().ifPresent(newPass -> {
            if (newPass.length() < 1) return;
            apiService.adminChangePassword(user.getId(), newPass)
                .thenAccept(v -> Platform.runLater(() -> showInfo("Пароль изменен.")))
                .exceptionally(e -> {
                    Platform.runLater(() -> showError("Ошибка: " + e.getMessage()));
                    return null;
                });
        });
    }
    
    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление пользователя");
        alert.setHeaderText("Вы уверены, что хотите удалить " + user.getName() + "?");
        alert.setContentText("Это действие нельзя отменить. Если у пользователя есть регистрации, удаление не удастся.");
        
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                apiService.deleteUser(user.getId())
                    .thenAccept(v -> Platform.runLater(() -> {
                        usersList.remove(user);
                        showInfo("Пользователь удален.");
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> showError("Ошибка удаления: " + e.getMessage()));
                        return null;
                    });
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setContentText(message);
        alert.showAndWait();
    }
}