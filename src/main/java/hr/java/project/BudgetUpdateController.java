package hr.java.project;

import hr.java.project.entities.ExpectedCashflowObject;
import hr.java.project.enumerations.ExpectedCashflowTypes;
import hr.java.project.enumerations.TimePeriodOptions;
import hr.java.project.exceptions.EmptyStringException;
import hr.java.project.exceptions.ExistingEntityNameException;
import hr.java.project.exceptions.InvalidNumberException;
import hr.java.project.threads.UpdateExpectedCashflowThread;
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
import java.util.List;
import java.util.Optional;

public class BudgetUpdateController implements Controller{

    private static final Logger logger = LoggerFactory.getLogger(BudgetUpdateController.class);

    @FXML
    private ComboBox<String> chooseCategoryCombo;

    @FXML
    private TextField categoryNameTxtField;

    @FXML
    private TextField ammountTxtField;

    @FXML
    private ComboBox<String> timePeriodCombo;

    @FXML
    private Button deleteButton;

    private List<ExpectedCashflowObject> budgetList =
            DatabaseUtils.getExpectedCashflowFromDatabase(ExpectedCashflowTypes.budget);

    public void initialize(){
        ObservableList<String> categoryOptions = FXCollections.observableArrayList();
        for(ExpectedCashflowObject b : budgetList){
            categoryOptions.add(b.getCategoryName());
        }

        categoryOptions.add("novo");

        chooseCategoryCombo.setItems(categoryOptions);
        chooseCategoryCombo.getSelectionModel().selectLast();

        ObservableList<String> timePeriodOptions = FXCollections.observableArrayList();
        timePeriodOptions.add(TimePeriodOptions.MONTHLY.getString());
        timePeriodOptions.add(TimePeriodOptions.ANNUAL.getString());
        timePeriodOptions.add(TimePeriodOptions.BIANNUAL.getString());
        timePeriodOptions.add(TimePeriodOptions.QUARTERLY.getString());
        timePeriodOptions.add("");

        timePeriodCombo.setItems(timePeriodOptions);
        timePeriodCombo.getSelectionModel().selectLast();

        deleteButton.setVisible(false);
    }

    public void saveMonthlyBudget(){
        String categoryString = chooseCategoryCombo.getValue();
        String categoryNameString = categoryNameTxtField.getText();
        String ammountString = ammountTxtField.getText();
        String timePeriodString = timePeriodCombo.getValue();
        if(categoryString.compareTo("novo")==0){
            try{

                stringEmptyCheck(categoryNameString);
                checkIfEntityNameExists(DatabaseUtils.checkIfExpectedCashflowCategoryNameExists(categoryNameString));
                stringEmptyCheck(ammountString);
                stringEmptyCheck(timePeriodString);

                BigDecimal amount = new BigDecimal(ammountString);
                checkAmmountNumberValidity(amount);

                ExpectedCashflowObject budget = new ExpectedCashflowObject.ExpectedCashflowBuilder()
                        .setCategoryName(categoryNameString)
                        .setAmount(amount)
                        .setTimePeriod(timePeriodString)
                        .build();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Spremanje entiteta");
                alert.setHeaderText("Jeste li sigurni da želite spremiti ovaj enititet?");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("POTVRDI");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("ODUSTANI");
                Optional<ButtonType> confirmation = alert.showAndWait();
                if (confirmation.get() == ButtonType.OK) {
                    DatabaseUtils.saveExpectedCashflowToDatabase(budget, ExpectedCashflowTypes.budget);

                    FileUtils.expectedCashflowObjects.add(budget);
                    ExpectedCashflowObject.serializeExpectedCashflowObject(FileUtils.expectedCashflowObjects);
                }

            }catch (NumberFormatException | InvalidNumberException | EmptyStringException | ExistingEntityNameException e){
                if(e instanceof EmptyStringException){
                    logger.error("Uhvaćena iznimka EmptyStringException");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Prazno polje!");
                    alert.setHeaderText("Ostavili ste neko od polja praznim. Molimo popunite sva polja.");
                    alert.showAndWait();
                }
                else if (e instanceof ExistingEntityNameException){
                    logger.error("Uhvaćena iznimka ExistingEntityNameException");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Neadekvatno ime kategorije");
                    alert.setHeaderText("Uneseno ime kategorije već postoji. Molimo unesite jedinstveno ime.");
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
        else{
            try{

                ExpectedCashflowObject budget = new ExpectedCashflowObject.ExpectedCashflowBuilder().build();
                if (!categoryNameString.isEmpty()){
                    checkIfEntityNameExists(DatabaseUtils
                            .checkIfExpectedCashflowCategoryNameExists(categoryNameString));
                }
                else{
                    categoryNameString = categoryString;
                }
                budget.setCategoryName(categoryNameString);

                if (!ammountString.isEmpty()) {
                    BigDecimal amount = new BigDecimal(ammountString);
                    checkAmmountNumberValidity(amount);
                    budget.setAmount(amount);
                }

                if (!timePeriodString.isEmpty()){
                    budget.setTimePeriod(timePeriodString);
                }

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Izmjena entiteta");
                alert.setHeaderText("Jeste li sigurni da želite izmjeniti ovaj enititet?");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("POTVRDI");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("ODUSTANI");
                Optional<ButtonType> confirmation = alert.showAndWait();
                if (confirmation.get() == ButtonType.OK){
                    UpdateExpectedCashflowThread updateExpectedCashflowThread =
                            new UpdateExpectedCashflowThread(budget, categoryString);
                    Thread thread = new Thread(updateExpectedCashflowThread);
                    thread.start();
                    thread.join();

                    FileUtils.expectedCashflowObjects.add(budget);
                    ExpectedCashflowObject.serializeExpectedCashflowObject(FileUtils.expectedCashflowObjects);
                }

            }
            catch (NumberFormatException | InvalidNumberException | ExistingEntityNameException | InterruptedException e){
                if(e instanceof ExistingEntityNameException){
                    logger.error("Uhvaćena iznimka ExistingEntityNameException");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Neadekvatno ime kategorije");
                    alert.setHeaderText("Uneseno ime kategorije već postoji. Molimo unesite jedinstveno ime.");
                    alert.showAndWait();
                }
                else{
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
    }

    public void deleteBudget(){
        String categoryString = chooseCategoryCombo.getValue();
        if (categoryString.compareTo("novo")!=0){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Brisanje entiteta");
            alert.setHeaderText("Jeste li sigurni da želite izbrisati ovaj enititet?");
            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("POTVRDI");
            ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("ODUSTANI");
            Optional<ButtonType> confirmation = alert.showAndWait();
            if (confirmation.get() == ButtonType.OK) {
                DatabaseUtils.deleteExpectedCashflowFromDatabase(categoryString);
            }
        }
    }

    public void CheckCategoryComboSelection(){
        if(chooseCategoryCombo.getValue().compareTo("novo")!=0){
            deleteButton.setVisible(true);
        }
        else{
            deleteButton.setVisible(false);
        }
    }

    public void back(){
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("budgetOverview.fxml"));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - budžeti");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }
}
