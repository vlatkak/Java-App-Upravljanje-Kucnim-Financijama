package hr.java.project;

import hr.java.project.entities.ActualCashflowObject;
import hr.java.project.entities.ExpectedCashflowObject;
import hr.java.project.entities.FinanceObjects;
import hr.java.project.entities.ReminderEvent;
import hr.java.project.enumerations.UserRoles;
import hr.java.project.user.User;
import hr.java.project.utils.DatabaseUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LoginStartApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(LoginStartApplication.class);

    public static Optional<User> currentUser = Optional.empty();

    public static Stage mainStage;

    public static Stage getMainStage(){
        return mainStage;
    }

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Pokrenuta aplikacija...");
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("loginStart.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        mainStage = stage;
        stage.setTitle("Moje financije - login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}