package com.example.volunteerclient;

import com.example.volunteerclient.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import atlantafx.base.theme.PrimerLight;

public class VolunteerApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));

        LoginController loginController = new LoginController();
        loader.setController(loginController);

        Parent root = loader.load();
        root.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Волонтерская система");
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}