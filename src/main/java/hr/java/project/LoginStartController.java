package hr.java.project;

import hr.java.project.enumerations.UserRoles;
import hr.java.project.utils.FileUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginStartController {

    private static final Logger logger = LoggerFactory.getLogger(LoginStartController.class);

    @FXML
    private TextField userName;

    @FXML
    private TextField password;

    public void openHome(){
        String homeScreenFile = "homeUser.fxml";
        if(LoginStartApplication.currentUser.get().role().equals(UserRoles.admin)){
            homeScreenFile = "homeAdmin.fxml";
        }

        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource(homeScreenFile));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - dobro došli!");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }

    public void logIn(){
        String userNameStr = userName.getText();
        String passwordStr = password.getText();

        LoginStartApplication.currentUser = FileUtils.loginInfoSearch(userNameStr, passwordStr);
        if(LoginStartApplication.currentUser.isPresent()){
            openHome();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Neuspješan log in!");
            alert.setContentText("Račun u koji ste se pokušali ulogirati ne postoji, ili ste unijeli " +
                    "nevažeće podatke računa.");
            alert.showAndWait();
            logger.info("Neuspješan pokušaj log in-a");
        }
    }

    public void openCreateAccount(){
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("createAccount.fxml"));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - novi račun");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }
}
