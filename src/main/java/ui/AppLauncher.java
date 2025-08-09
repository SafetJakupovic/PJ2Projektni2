package main.java.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Ulazna tačka JavaFX aplikacije.
 * Učitava MainView.fxml i prikazuje glavni prozor.
 */
public class AppLauncher extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/view/MainView.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Pronalazac ruta");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}