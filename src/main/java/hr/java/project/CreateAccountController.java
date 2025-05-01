package hr.java.project;

import hr.java.project.enumerations.UserRoles;
import hr.java.project.exceptions.EmptyStringException;
import hr.java.project.exceptions.ExistingEntityNameException;
import hr.java.project.exceptions.InvalidEmailException;
import hr.java.project.exceptions.InvalidNumberException;
import hr.java.project.user.User;
import hr.java.project.utils.DatabaseUtils;
import hr.java.project.utils.FileUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CreateAccountController implements Controller{

    private static final Logger logger = LoggerFactory.getLogger(CreateAccountController.class);

    @FXML
    private TextField name;

    @FXML
    private TextField email;

    @FXML
    private TextField userName;

    @FXML
    private TextField password;

    public void createAccount(){
        String nameStr = name.getText();
        String emailStr = email.getText();
        String userNameStr = userName.getText();
        String passwordStr = password.getText();

        try{
            stringEmptyCheck(nameStr);
            stringEmptyCheck(emailStr);
            stringEmptyCheck(userNameStr);
            stringEmptyCheck(passwordStr);

            checkIfEntityNameExists(FileUtils.checkIfUserNameExists(userNameStr));
            checkForEmailFormat(emailStr);

            User user = new User(0, nameStr, emailStr, userNameStr, UserRoles.user);

            FileUtils.saveNewUser(userNameStr, passwordStr);
            DatabaseUtils.saveUserToDatabase(user);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Kreiran račun!");
            alert.setContentText("Uspješno spremnljen novi korisnički račun.");
            alert.showAndWait();

        }catch (EmptyStringException | ExistingEntityNameException | InvalidEmailException e){
            if(e instanceof EmptyStringException){
                logger.error("Uhvaćena iznimka EmptyStringException");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Prazno polje!");
                alert.setHeaderText("Ostavili ste neko od polja praznim. Molimo popunite sva polja.");
                alert.showAndWait();
            }
            else if (e instanceof ExistingEntityNameException) {
                logger.error("Uhvaćena iznimka ExistingEntityNameException");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid username!");
                alert.setHeaderText("Već postoji korisnik s unesenim korisničkim imenom.");
                alert.showAndWait();
            }
            else{
                logger.error("Uhvaćena iznimka InvalidEmail");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid e-mail!");
                alert.setHeaderText("E-mail adresa koju ste unjeli nije u prihvatljivom formatu.");
                alert.showAndWait();
            }
            logger.info("Uhvaćena iznimka kod unosa podataka u GUI");
        }
    }

    public void back(){
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("loginStart.fxml"));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - login");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }
}
