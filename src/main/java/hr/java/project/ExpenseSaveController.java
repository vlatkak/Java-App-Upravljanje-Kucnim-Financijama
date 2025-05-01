package hr.java.project;

import hr.java.project.entities.ExpectedCashflowObject;
import hr.java.project.entities.ActualCashflowObject;
import hr.java.project.enumerations.ExpectedCashflowTypes;
import hr.java.project.enumerations.UserRoles;
import hr.java.project.exceptions.EmptyStringException;
import hr.java.project.exceptions.InvalidNumberException;
import hr.java.project.utils.DatabaseUtils;
import hr.java.project.utils.FileUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ExpenseSaveController implements Controller{

    private static final Logger logger = LoggerFactory.getLogger(ExpenseSaveController.class);

    @FXML
    private ComboBox<String> chooseCategoryCombo;

    @FXML
    private TextField categoryNameTxtField;

    @FXML
    private Label categoryNameLabel;

    @FXML
    private TextField ammountTxtField;

    @FXML
    private DatePicker datePicker;

    private List<ExpectedCashflowObject> budgetList =
            DatabaseUtils.getExpectedCashflowFromDatabase(ExpectedCashflowTypes.budget);

    public void initialize(){
        datePicker.setEditable(false);

        ObservableList<String> categoryOptions = FXCollections.observableArrayList();
        for(ExpectedCashflowObject b : budgetList){
            categoryOptions.add(b.getCategoryName());
        }
        categoryOptions.add("novo");

        chooseCategoryCombo.setItems(categoryOptions);
        chooseCategoryCombo.getSelectionModel().selectLast();

        datePicker.setValue(LocalDate.now());
    }

    public void saveExpense(){
        try{
            String categoryString = chooseCategoryCombo.getValue();
            if(categoryString.compareTo("novo")==0){
                categoryString=categoryNameTxtField.getText();
                stringEmptyCheck(categoryString);
            }
            String ammountString = ammountTxtField.getText();
            stringEmptyCheck(ammountString);
            BigDecimal ammount = new BigDecimal(ammountTxtField.getText());
            checkAmmountNumberValidity(ammount);
            if (Optional.ofNullable(datePicker.getValue()).isEmpty()){
                throw new EmptyStringException();
            }
            LocalDate date = datePicker.getValue();
            ActualCashflowObject expense =
                    new ActualCashflowObject.ActualCashflowObjectBuilder()
                            .setCategoryName(categoryString)
                            .setAmount(ammount.multiply(BigDecimal.valueOf(-1)))
                            .setDateOfTransaction(date)
                            .build();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Spremanje entiteta");
            alert.setHeaderText("Jeste li sigurni da želite spremiti ovaj enititet?");
            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("POTVRDI");
            ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("ODUSTANI");
            Optional<ButtonType> confirmation = alert.showAndWait();
            if (confirmation.get() == ButtonType.OK) {
                DatabaseUtils.saveActualCashflowObjectToDatabase(expense);

                FileUtils.actualCashflowObjects.add(expense);
                ActualCashflowObject.serializeActualCashflowObject(FileUtils.actualCashflowObjects);
            }
        }
        catch (EmptyStringException | NumberFormatException | InvalidNumberException e){
            if(e instanceof EmptyStringException){
                logger.error("Uhvaćena iznimka EmptyStringException");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Prazno polje!");
                alert.setHeaderText("Ostavili ste neko od polja praznim. Molimo popunite sva polja.");
                alert.showAndWait();
            }
            else {
                logger.error("Uhvaćena iznimka InvalidNumberException ili NumberFormatException");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Neprihvatljiv unos!");
                alert.setHeaderText("Na mjesto novčanog iznosa ste unjeli neprihvatljivu vrijednost.");
                alert.setContentText("Novčani iznos mora biti pozitivna brojčana vrijednost");
                alert.showAndWait();
            }
            logger.info("Uhvaćena iznimka kod unosa podataka u GUI");
        }
    }

    public void CheckCategoryComboSelection(){
        if(chooseCategoryCombo.getValue().compareTo("novo")==0){
            categoryNameLabel.setVisible(true);
            categoryNameTxtField.setVisible(true);
        }
        else{
            categoryNameLabel.setVisible(false);
            categoryNameTxtField.setVisible(false);
        }
    }

    public void back(){
        String homeScreenFile = "homeUser.fxml";
        if(LoginStartApplication.currentUser.get().role().equals(UserRoles.admin)){
            homeScreenFile = "homeAdmin.fxml";
        }

        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource(homeScreenFile));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - dobro došli");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }
}
