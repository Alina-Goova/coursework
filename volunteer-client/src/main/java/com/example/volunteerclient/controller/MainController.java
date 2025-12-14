package com.example.volunteerclient.controller;

import java.util.Optional;

import com.example.volunteerclient.model.Event;
import com.example.volunteerclient.model.Registration;
import com.example.volunteerclient.model.User;
import com.example.volunteerclient.service.ApiService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class MainController {

    private ApiService apiService;
    private User currentUser;

    private ObservableList<Event> events = FXCollections.observableArrayList();
    private FilteredList<Event> filteredEvents;
    private ObservableList<Registration> profileRegistrations = FXCollections.observableArrayList();

    // Элементы главной панели
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;

    // Вкладка мероприятий - Статистика
    @FXML private Label statTotalLabel;
    @FXML private Label statActiveLabel;
    @FXML private Label statPlannedLabel;
    @FXML private Label statCompletedLabel;

    // Вкладка мероприятий - Фильтры
    @FXML private ComboBox<String> filterStatusBox;
    @FXML private ComboBox<String> filterCategoryBox;
    @FXML private DatePicker filterDatePicker;
    
    // Вкладка мероприятий - Таблица
    @FXML private TableView<Event> eventsTable;
    @FXML private TableColumn<Event, Long> eventIdColumn;
    @FXML private TableColumn<Event, String> eventTitleColumn;
    @FXML private TableColumn<Event, String> eventLocationColumn;
    @FXML private TableColumn<Event, String> eventStartDateColumn;
    @FXML private TableColumn<Event, String> eventEndDateColumn;
    @FXML private TableColumn<Event, String> eventCapacityColumn;
    @FXML private TableColumn<Event, String> eventStatusColumn;
    @FXML private TableColumn<Event, String> eventCategoryColumn;
    @FXML private TableColumn<Event, String> eventOrganizerColumn;
    
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button refreshEventsButton;

    // Элементы для администратора
    @FXML private Tab adminTab;
    @FXML private VBox adminPanel;

    // Форма создания мероприятия
    @FXML private TextField eventTitleField;
    @FXML private TextArea eventDescriptionField;
    @FXML private TextField eventLocationField;
    @FXML private DatePicker eventStartDatePicker;
    @FXML private DatePicker eventEndDatePicker;
    @FXML private TextField eventMaxParticipantsField;
    @FXML private Button createEventButton;

    // Элементы профиля
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileRoleLabel;
    @FXML private Label profilePhoneLabel;
    @FXML private Label profileBioLabel;
    
    @FXML private TextField historySearchField;
    @FXML private ComboBox<String> historyStatusFilter;

    @FXML private TableView<Registration> profileEventsTable;
    @FXML private TableColumn<Registration, String> profileEventTitleColumn;
    @FXML private TableColumn<Registration, String> profileEventStartDateColumn;
    @FXML private TableColumn<Registration, String> profileEventStatusColumn;

    @FXML private AdminUsersController adminUsersController;
    @FXML private AdminRegistrationsController adminRegistrationsController;

    private javafx.collections.transformation.FilteredList<Registration> filteredRegistrations;

    // Конструктор по умолчанию
    public MainController() {
        this.apiService = new ApiService();
        // Создаем тестового пользователя для демонстрации
        this.currentUser = new User(1L, "guest@example.com", "GUEST", "Гость", "", "", "", "2025-01-01", true);
    }

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
        
        if (adminUsersController != null) {
            adminUsersController.setApiService(apiService);
        }
        if (adminRegistrationsController != null) {
            adminRegistrationsController.setApiService(apiService);
        }

        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
             if (adminUsersController != null) adminUsersController.loadUsers();
             if (adminRegistrationsController != null) adminRegistrationsController.loadRegistrations();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        setupWelcomeMessage();
        setupAdminFeatures();
    }

    @FXML
    public void initialize() {
        System.out.println("MainController initialized!");

        setupWelcomeMessage();
        setupEventsTable();
        setupEventFilters();
        setupProfileTable();
        loadEvents();
        setupAdminFeatures();
    }

    private void setupEventFilters() {
        if (filterStatusBox != null) {
            filterStatusBox.getItems().addAll("Все", "PLANNED", "ACTIVE", "COMPLETED", "CANCELLED");
            filterStatusBox.setValue("Все");
            filterStatusBox.valueProperty().addListener((obs, oldVal, newVal) -> updateEventFilter());
        }
        if (filterCategoryBox != null) {
            filterCategoryBox.getItems().addAll("Все", "Конференция", "Встреча", "Обучение", "Другое"); // Example categories
            filterCategoryBox.setValue("Все");
            filterCategoryBox.valueProperty().addListener((obs, oldVal, newVal) -> updateEventFilter());
        }
        if (filterDatePicker != null) {
            filterDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateEventFilter());
        }
    }

    @FXML
    private void clearFilters() {
        if (filterStatusBox != null) filterStatusBox.setValue("Все");
        if (filterCategoryBox != null) filterCategoryBox.setValue("Все");
        if (filterDatePicker != null) filterDatePicker.setValue(null);
        if (searchField != null) searchField.clear();
        updateEventFilter();
    }

    private void updateEventFilter() {
        String status = filterStatusBox != null ? filterStatusBox.getValue() : "Все";
        String category = filterCategoryBox != null ? filterCategoryBox.getValue() : "Все";
        java.time.LocalDate date = filterDatePicker != null ? filterDatePicker.getValue() : null;
        String search = searchField != null ? searchField.getText().toLowerCase().trim() : "";

        filteredEvents.setPredicate(event -> {
            boolean matchStatus = "Все".equals(status) || (event.getStatus() != null && event.getStatus().equals(status));
            boolean matchCategory = "Все".equals(category) || (event.getCategory() != null && event.getCategory().equals(category));
            boolean matchDate = date == null || (event.getStartDate() != null && event.getStartDate().toLocalDate().equals(date));
            boolean matchSearch = search.isEmpty() || event.getTitle().toLowerCase().contains(search);

            return matchStatus && matchCategory && matchDate && matchSearch;
        });
    }

    private void setupProfileTable() {
        if (profileEventsTable == null) return;

        if (profileEventTitleColumn != null) {
            profileEventTitleColumn.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        }
        if (profileEventStartDateColumn != null) {
            profileEventStartDateColumn.setCellValueFactory(new PropertyValueFactory<>("eventStartDate"));
        }
        if (profileEventStatusColumn != null) {
            profileEventStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        filteredRegistrations = new javafx.collections.transformation.FilteredList<>(profileRegistrations, p -> true);
        profileEventsTable.setItems(filteredRegistrations);

        if (historyStatusFilter != null) {
            historyStatusFilter.getItems().addAll("Все", "CONFIRMED", "CANCELLED");
            historyStatusFilter.setValue("Все");
            historyStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateHistoryFilter());
        }

        if (historySearchField != null) {
            historySearchField.textProperty().addListener((obs, oldVal, newVal) -> updateHistoryFilter());
        }

        profileEventsTable.setRowFactory(tv -> {
            TableRow<Registration> row = new TableRow<>() {
                @Override
                protected void updateItem(Registration item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setStyle("");
                    } else if ("CANCELLED".equals(item.getStatus())) {
                        setStyle("-fx-background-color: #f2f2f2; -fx-text-fill: #a0a0a0;");
                    } else {
                        setStyle("");
                    }
                }
            };

            ContextMenu contextMenu = new ContextMenu();
            MenuItem cancelItem = new MenuItem("Отменить регистрацию");
            cancelItem.setOnAction(event -> {
                Registration item = row.getItem();
                if (item != null) handleCancelRegistration(item);
            });
            
            row.setOnContextMenuRequested(e -> {
                contextMenu.getItems().clear();
                if (row.getItem() != null && "CONFIRMED".equals(row.getItem().getStatus())) {
                    contextMenu.getItems().add(cancelItem);
                }
            });

            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );
            return row;
        });
    }

    private void updateHistoryFilter() {
        String statusFilter = historyStatusFilter.getValue();
        String searchText = historySearchField.getText().toLowerCase().trim();

        filteredRegistrations.setPredicate(registration -> {
            boolean statusMatch = "Все".equals(statusFilter) || registration.getStatus().equals(statusFilter);
            boolean searchMatch = searchText.isEmpty() || registration.getEventTitle().toLowerCase().contains(searchText);
            return statusMatch && searchMatch;
        });
    }

    @FXML
    private void loadUserProfile() {
        if (currentUser == null) return;

        if (profileNameLabel != null) profileNameLabel.setText(currentUser.getName());
        if (profileEmailLabel != null) profileEmailLabel.setText(currentUser.getEmail());
        if (profileRoleLabel != null) profileRoleLabel.setText(currentUser.getRole());
        if (profilePhoneLabel != null) profilePhoneLabel.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "Не указан");
        if (profileBioLabel != null) profileBioLabel.setText(currentUser.getBio() != null ? currentUser.getBio() : "");

        apiService.getUserRegistrations(currentUser.getId())
                .thenAccept(registrations -> Platform.runLater(() -> {
                    profileRegistrations.setAll(registrations);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> showError("Не удалось загрузить историю регистраций: " + e.getMessage()));
                    return null;
                });
    }
    
    @FXML
    private void handleEditProfile() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Редактирование профиля");
        dialog.setHeaderText("Измените данные профиля");
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField(currentUser.getName() != null ? currentUser.getName() : "");
        nameField.setPromptText("ФИО (например, Иванов Иван Иванович)");

        TextField emailField = new TextField(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        emailField.setPromptText("email@example.com");

        TextField phoneField = new TextField(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
        phoneField.setPromptText("Телефон (например, +79991234567)");

        TextArea bioArea = new TextArea(currentUser.getBio() != null ? currentUser.getBio() : "");
        bioArea.setPromptText("Напишите немного о себе, ваших навыках и интересах...");
        bioArea.setPrefRowCount(3);

        TextField avatarUrlField = new TextField(currentUser.getAvatarUrl() != null ? currentUser.getAvatarUrl() : "");
        avatarUrlField.setPromptText("Ссылка на фото (http://...)");

        grid.add(new Label("Имя:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(emailField, 1, 1);
        grid.add(new Label("Телефон:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("О себе:"), 0, 3); grid.add(bioArea, 1, 3);
        grid.add(new Label("Аватар (URL):"), 0, 4); grid.add(avatarUrlField, 1, 4);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new User(currentUser.getId(), emailField.getText(), currentUser.getRole(),
                        nameField.getText(), phoneField.getText(), bioArea.getText(), avatarUrlField.getText(), currentUser.getRegistrationDate(), currentUser.isActive());
            }
            return null;
        });
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(updatedUser -> {
            apiService.updateProfile(currentUser.getId(), updatedUser.getName(), updatedUser.getEmail(), updatedUser.getPhoneNumber(), updatedUser.getBio(), updatedUser.getAvatarUrl())
                    .thenAccept(user -> Platform.runLater(() -> {
                        this.currentUser = user;
                        loadUserProfile();
                        setupWelcomeMessage();
                        showInfo("Профиль успешно обновлен!");
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> showError("Ошибка обновления профиля: " + e.getMessage()));
                        return null;
                    });
        });
    }

    @FXML
    private void handleChangePassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Смена пароля");
        dialog.setHeaderText("Введите старый и новый пароль");
        ButtonType changeButtonType = new ButtonType("Изменить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        PasswordField oldPasswordField = new PasswordField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        grid.add(new Label("Старый пароль:"), 0, 0); grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("Новый пароль:"), 0, 1); grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Подтверждение:"), 0, 2); grid.add(confirmPasswordField, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) { return newPasswordField.getText(); }
            return null;
        });
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
             String oldPass = oldPasswordField.getText().trim();
             String newPass = newPasswordField.getText();
             String confirmPass = confirmPasswordField.getText();
             if (!newPass.equals(confirmPass)) { showError("Новый пароль и подтверждение не совпадают!"); return; }
             if (newPass.length() < 8) { showError("Пароль должен быть не менее 8 символов!"); return; }
             apiService.changePassword(currentUser.getId(), oldPass, newPass)
                     .thenAccept(v -> Platform.runLater(() -> showInfo("Пароль успешно изменен!")))
                     .exceptionally(e -> {
                         Platform.runLater(() -> showError("Ошибка смены пароля: " + e.getMessage()));
                         return null;
                     });
        }
    }

    private void handleCancelRegistration(Registration registration) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Отмена регистрации");
        alert.setHeaderText("Вы уверены?");
        alert.setContentText("Это действие нельзя отменить.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                apiService.cancelRegistration(currentUser.getId(), registration.getEventId())
                        .thenAccept(v -> Platform.runLater(() -> {
                            loadUserProfile();
                            showInfo("Регистрация отменена.");
                        }))
                        .exceptionally(e -> {
                            Platform.runLater(() -> showError("Ошибка отмены: " + e.getMessage()));
                            return null;
                        });
            }
        });
    }

    private void setupWelcomeMessage() {
        if (welcomeLabel != null && currentUser != null) {
            welcomeLabel.setText("Добро пожаловать, " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        }
    }

    private void setupEventsTable() {
        if (eventsTable == null) return;

        if (eventIdColumn != null) eventIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (eventTitleColumn != null) eventTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (eventLocationColumn != null) eventLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        if (eventStartDateColumn != null) eventStartDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        if (eventEndDateColumn != null) eventEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        
        // New columns
        if (eventStatusColumn != null) eventStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (eventCategoryColumn != null) eventCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (eventOrganizerColumn != null) eventOrganizerColumn.setCellValueFactory(new PropertyValueFactory<>("organizerName"));

        if (eventCapacityColumn != null) {
            eventCapacityColumn.setCellValueFactory(cellData -> {
                Event event = cellData.getValue();
                Integer current = event.getCurrentParticipants();
                Integer max = event.getMaxParticipants();
                if (max == null || max == 0) return new javafx.beans.property.SimpleStringProperty(current + " / ∞");
                return new javafx.beans.property.SimpleStringProperty(current + " / " + max);
            });
        }

        eventsTable.setRowFactory(tv -> {
            TableRow<Event> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Удалить мероприятие");
            deleteItem.setOnAction(event -> {
                Event item = row.getItem();
                if (item != null) handleDeleteEvent(item);
            });
            MenuItem joinItem = new MenuItem("Принять участие");
            joinItem.setOnAction(event -> {
                Event item = row.getItem();
                if (item != null) handleJoinEvent(item);
            });

            row.setOnContextMenuRequested(e -> {
                contextMenu.getItems().clear();
                Event item = row.getItem();
                if (item == null) return;
                
                if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                    contextMenu.getItems().add(deleteItem);
                } else {
                    if (item.getMaxParticipants() != null && item.getMaxParticipants() > 0 && 
                        item.getCurrentParticipants() >= item.getMaxParticipants()) {
                        joinItem.setDisable(true);
                        joinItem.setText("Мест нет");
                    } else {
                        joinItem.setDisable(false);
                        joinItem.setText("Принять участие");
                    }
                    contextMenu.getItems().add(joinItem);
                }
            });
            
            row.contextMenuProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
            return row;
        });

        filteredEvents = new FilteredList<>(events, p -> true);
        eventsTable.setItems(filteredEvents);
        
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> updateEventFilter());
        }
    }

    private void handleJoinEvent(Event event) {
        if (currentUser == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Регистрация");
        confirm.setHeaderText("Зарегистрироваться на мероприятие '" + event.getTitle() + "'?");
        confirm.setContentText("Вы уверены?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                apiService.registerForEvent(currentUser.getId(), event.getId())
                    .thenAccept(v -> Platform.runLater(() -> {
                        showInfo("Вы успешно зарегистрировались!");
                        loadEvents();
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> showError("Ошибка регистрации: " + e.getMessage()));
                        return null;
                    });
            }
        });
    }

    private void handleDeleteEvent(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удалить мероприятие '" + event.getTitle() + "'?");
        alert.setContentText("Это действие нельзя отменить.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                apiService.deleteEvent(event.getId())
                        .thenAccept(v -> Platform.runLater(() -> {
                            events.remove(event);
                            showInfo("Мероприятие удалено.");
                        }))
                        .exceptionally(e -> {
                            Platform.runLater(() -> showError("Ошибка удаления: " + e.getMessage()));
                            return null;
                        });
            }
        });
    }

    private void setupAdminFeatures() {
        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole());
        if (adminTab != null) {
            adminTab.setDisable(!isAdmin);
            if (!isAdmin) mainTabPane.getTabs().remove(adminTab);
            else if (!mainTabPane.getTabs().contains(adminTab)) mainTabPane.getTabs().add(adminTab);
        }
        if (adminPanel != null) adminPanel.setVisible(isAdmin);
    }

    @FXML
    private void loadEvents() {
        System.out.println("Loading events...");
        if (refreshEventsButton != null) refreshEventsButton.setDisable(true);

        // Load stats
        apiService.getStatistics().thenAccept(stats -> Platform.runLater(() -> {
            if (statTotalLabel != null) statTotalLabel.setText("Всего: " + stats.get("total"));
            if (statActiveLabel != null) statActiveLabel.setText("Активно: " + stats.get("active"));
            if (statPlannedLabel != null) statPlannedLabel.setText("Планируется: " + stats.get("planned"));
            if (statCompletedLabel != null) statCompletedLabel.setText("Завершено: " + stats.get("completed"));
        }));

        apiService.getEvents()
                .thenAccept(loadedEvents -> Platform.runLater(() -> {
                    events.clear();
                    events.addAll(loadedEvents);
                    if (refreshEventsButton != null) refreshEventsButton.setDisable(false);
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showError("Не удалось загрузить мероприятия: " + e.getMessage());
                        if (refreshEventsButton != null) refreshEventsButton.setDisable(false);
                    });
                    return null;
                });
    }

    @FXML
    private void searchEvents() {
        updateEventFilter();
    }

    @FXML
    private void createEvent() {
        if (eventTitleField != null && eventLocationField != null) {
            String title = eventTitleField.getText().trim();
            String location = eventLocationField.getText().trim();

            if (title.isEmpty() || location.isEmpty()) {
                showError("Заполните обязательные поля");
                return;
            }

            Event newEvent = new Event();
            newEvent.setTitle(title);
            newEvent.setDescription(eventDescriptionField != null ? eventDescriptionField.getText() : "");
            newEvent.setLocation(location);
            if (eventStartDatePicker != null && eventStartDatePicker.getValue() != null)
                newEvent.setStartDate(eventStartDatePicker.getValue().atStartOfDay());
            else newEvent.setStartDate(java.time.LocalDateTime.now().plusDays(1));

            if (eventEndDatePicker != null && eventEndDatePicker.getValue() != null)
                newEvent.setEndDate(eventEndDatePicker.getValue().atTime(23, 59));
            else newEvent.setEndDate(java.time.LocalDateTime.now().plusDays(1).plusHours(2));
            
            if (eventMaxParticipantsField != null && !eventMaxParticipantsField.getText().isEmpty()) {
                try {
                    int max = Integer.parseInt(eventMaxParticipantsField.getText());
                    newEvent.setMaxParticipants(max);
                } catch (NumberFormatException e) {
                    showError("Неверный формат числа участников"); return;
                }
            } else newEvent.setMaxParticipants(0);
            
            newEvent.setStatus("PLANNED");
            newEvent.setCategory("Другое");
            newEvent.setHidden(false);
            if (currentUser != null) {
                newEvent.setOrganizerId(currentUser.getId());
            }

            apiService.createEvent(newEvent)
                    .thenAccept(createdEvent -> Platform.runLater(() -> {
                        events.add(createdEvent);
                        clearEventForm();
                        showInfo("Мероприятие создано успешно!");
                        loadEvents();
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> showError("Ошибка создания мероприятия: " + e.getMessage()));
                        return null;
                    });
        }
    }

    private void clearEventForm() {
        if (eventTitleField != null) eventTitleField.clear();
        if (eventDescriptionField != null) eventDescriptionField.clear();
        if (eventLocationField != null) eventLocationField.clear();
        if (eventStartDatePicker != null) eventStartDatePicker.setValue(null);
        if (eventEndDatePicker != null) eventEndDatePicker.setValue(null);
        if (eventMaxParticipantsField != null) eventMaxParticipantsField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Об авторе");
        alert.setHeaderText("Волонтерская информационная система");
        alert.setContentText("Разработано в рамках курсовой работы Автор: Гоова Алина Группа: ИД23-2 Год: 2025");
        alert.showAndWait();
    }

    @FXML
    private void exitApplication() {
        System.exit(0);
    }
}