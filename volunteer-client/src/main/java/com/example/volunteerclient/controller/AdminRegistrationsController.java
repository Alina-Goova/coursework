package com.example.volunteerclient.controller;

import com.example.volunteerclient.VolunteerApp;
import com.example.volunteerclient.model.Registration;
import com.example.volunteerclient.service.ApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AdminRegistrationsController {

    @FXML
    private TableView<Registration> registrationsTable;
    @FXML
    private TableColumn<Registration, Long> regIdColumn;
    @FXML
    private TableColumn<Registration, String> regUserColumn;
    @FXML
    private TableColumn<Registration, String> regEventColumn;
    @FXML
    private TableColumn<Registration, String> regDateColumn;
    @FXML
    private TableColumn<Registration, String> regStatusColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterBox;

    private ApiService apiService;
    private ObservableList<Registration> allRegistrations = FXCollections.observableArrayList();

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    @FXML
    public void initialize() {
        regIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        regUserColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        regEventColumn.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));
        regDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        regStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusFilterBox.setItems(FXCollections.observableArrayList("Все", "CONFIRMED", "CANCELLED", "REJECTED"));
        statusFilterBox.setValue("Все");

        ContextMenu contextMenu = new ContextMenu();
        MenuItem confirmItem = new MenuItem("Подтвердить");
        confirmItem.setOnAction(e -> updateStatus("CONFIRMED"));
        
        MenuItem rejectItem = new MenuItem("Отклонить");
        rejectItem.setOnAction(e -> updateStatus("REJECTED"));
        
        MenuItem cancelItem = new MenuItem("Отменить");
        cancelItem.setOnAction(e -> updateStatus("CANCELLED"));

        contextMenu.getItems().addAll(confirmItem, rejectItem, cancelItem);
        
        registrationsTable.setRowFactory(tv -> {
            TableRow<Registration> row = new TableRow<>();
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );
            return row;
        });
    }

    @FXML
    public void loadRegistrations() {
        if (apiService == null) return;
        
        apiService.getAllRegistrations().thenAccept(registrations -> {
            Platform.runLater(() -> {
                allRegistrations.setAll(registrations);
                filterRegistrations();
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить регистрации: " + e.getMessage()));
            return null;
        });
    }

    @FXML
    private void filterRegistrations() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = statusFilterBox.getValue();

        List<Registration> filtered = allRegistrations.stream()
                .filter(r -> {
                    boolean matchesSearch = (r.getUserName() != null && r.getUserName().toLowerCase().contains(searchText)) ||
                                            (r.getEventTitle() != null && r.getEventTitle().toLowerCase().contains(searchText));
                    
                    boolean matchesStatus = statusFilter == null || statusFilter.equals("Все") || r.getStatus().equals(statusFilter);

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        registrationsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void updateStatus(String newStatus) {
        Registration selected = registrationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите регистрацию");
            return;
        }

        apiService.updateRegistrationStatus(selected.getId(), newStatus)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        loadRegistrations();
                        showAlert("Успех", "Статус обновлен на " + newStatus);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Ошибка", "Не удалось обновить статус: " + e.getMessage()));
                    return null;
                });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}