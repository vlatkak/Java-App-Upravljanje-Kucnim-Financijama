package hr.java.project;

import hr.java.project.entities.*;
import hr.java.project.enumerations.TimePeriodOptions;
import hr.java.project.enumerations.UserRoles;
import hr.java.project.exceptions.EmptyStringException;
import hr.java.project.exceptions.ExistingEntityNameException;
import hr.java.project.exceptions.InvalidNumberException;
import hr.java.project.threads.UpdateRemindersThread;
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
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;

public class CreateReminderController implements Controller{

    private static final Logger logger = LoggerFactory.getLogger(CreateReminderController.class);

    @FXML
    private ComboBox<String> eventOptionsCombo;

    @FXML
    private TextField eventTileTxtField;

    @FXML
    private TextField amountTxtField;

    @FXML
    private ComboBox<String> timePeriodCombo;

    @FXML
    private Label specificDateLabel;

    @FXML
    private DatePicker specificDatePicker;

    @FXML
    private ComboBox<Integer> durationOptionsCombo;

    @FXML
    private Button deleteButton;

    public void initialize(){
        specificDatePicker.setEditable(false);

        ObservableList<String> eventOptionsObservableList = FXCollections.observableArrayList();
        Set<ReminderEvent<FinanceObjects<BigDecimal>, Integer>> eventSet = DatabaseUtils.getReminderSetFromDatabase();
        for(ReminderEvent e : eventSet){
            eventOptionsObservableList.add(e.getEventObject().getCategoryName());
        }
        eventOptionsObservableList.add("novo");
        eventOptionsCombo.setItems(eventOptionsObservableList);
        eventOptionsCombo.getSelectionModel().selectLast();

        ObservableList<String> timePeriodObservableList = FXCollections.observableArrayList();
        timePeriodObservableList.add(TimePeriodOptions.MONTHLY.getString());
        timePeriodObservableList.add(TimePeriodOptions.QUARTERLY.getString());
        timePeriodObservableList.add(TimePeriodOptions.BIANNUAL.getString());
        timePeriodObservableList.add("specifičan datum");
        timePeriodObservableList.add("");

        timePeriodCombo.setItems(timePeriodObservableList);
        timePeriodCombo.getSelectionModel().selectLast();

        ObservableList<Integer> durationOptions = FXCollections.observableArrayList();
        durationOptions.addAll(5, 4, 3, 2, 1);

        durationOptionsCombo.setItems(durationOptions);

        deleteButton.setVisible(false);
        specificDateLabel.setVisible(false);
        specificDatePicker.setVisible(false);
    }

    public void saveReminder(){
        String eventChoice = eventOptionsCombo.getValue();
        String titleString = eventTileTxtField.getText();
        String amountString = amountTxtField.getText();
        String timePeriodString = timePeriodCombo.getValue();
        Integer duration = durationOptionsCombo.getValue();
        if(eventChoice.equals("novo")){
            try{
                stringEmptyCheck(eventChoice);

                stringEmptyCheck(titleString);
                checkIfEntityNameExists(DatabaseUtils.checkIfReminderTitleExists(titleString));

                stringEmptyCheck(amountString);

                stringEmptyCheck(timePeriodString);

                if(timePeriodString.equals("specifičan datum")){
                    if (Optional.ofNullable(specificDatePicker.getValue()).isEmpty()){
                        throw new EmptyStringException();
                    }
                }

                ReminderEvent<FinanceObjects<BigDecimal>, Integer> reminderEvent =
                        createReminderEvent(titleString, amountString, timePeriodString, duration);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Spremanje entiteta");
                alert.setHeaderText("Jeste li sigurni da želite spremiti ovaj enititet?");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("POTVRDI");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("ODUSTANI");
                Optional<ButtonType> confirmation = alert.showAndWait();
                if (confirmation.get() == ButtonType.OK) {
                    DatabaseUtils.saveReminderToDatabase(reminderEvent);

                    FileUtils.remiderEvents.add(reminderEvent);
                    ReminderEvent.serializeReminderEvent(FileUtils.remiderEvents);
                }
            }
            catch (EmptyStringException | NumberFormatException | InvalidNumberException | ExistingEntityNameException e){
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
        else {
            try{
                checkIfEntityNameExists(DatabaseUtils.checkIfReminderTitleExists(titleString));

                if (timePeriodString.equals("specifičan datum")){
                    if (Optional.ofNullable(specificDatePicker.getValue()).isEmpty()){
                        throw new EmptyStringException();
                    }
                }

                ReminderEvent<FinanceObjects<BigDecimal>, Integer> reminderEvent =
                        createReminderEvent(titleString, amountString, timePeriodString, duration);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Izmjena entiteta");
                alert.setHeaderText("Jeste li sigurni da želite izmjeniti ovaj enititet?");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("POTVRDI");
                ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("ODUSTANI");
                Optional<ButtonType> confirmation = alert.showAndWait();
                if (confirmation.get() == ButtonType.OK) {
                    UpdateRemindersThread updateRemindersThread = new UpdateRemindersThread(reminderEvent, eventChoice);
                    Thread thread = new Thread(updateRemindersThread);
                    thread.start();
                    thread.join();

                    FileUtils.remiderEvents.add(reminderEvent);
                    ReminderEvent.serializeReminderEvent(FileUtils.remiderEvents);
                }
            }
            catch (NumberFormatException | InvalidNumberException | ExistingEntityNameException
                   | InterruptedException | EmptyStringException e){
                if (e instanceof ExistingEntityNameException){
                    logger.error("Uhvaćena iznimka ExistingEntityNameException");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Neadekvatno ime kategorije");
                    alert.setHeaderText("Uneseno ime kategorije već postoji. Molimo unesite jedinstveno ime.");
                    alert.showAndWait();
                }
                if(e instanceof EmptyStringException){
                    logger.error("Uhvaćena iznimka EmptyStringException");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Prazno polje!");
                    alert.setHeaderText("Ostavili ste neko od polja praznim.");
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

    }

    private ReminderEvent<FinanceObjects<BigDecimal>, Integer>
    createReminderEvent(String title, String amountString, String timePeriod, Integer duration)
            throws NumberFormatException, InvalidNumberException{

        Optional<ReminderEvent<FinanceObjects<BigDecimal>, Integer>> reminderEvent = Optional.empty();
        if(timePeriod.equals("specifičan datum")){
            LocalDate specificDate = specificDatePicker.getValue();
            ActualCashflowObject actualCashflowObject =
                    new ActualCashflowObject.ActualCashflowObjectBuilder().build();
            if(!amountString.isEmpty()){
                BigDecimal amount = new BigDecimal(amountString);
                checkAmmountNumberValidity(amount);
                actualCashflowObject.setAmount(amount);
            }
            if (!title.isEmpty()){
                actualCashflowObject.setCategoryName(title);
            }
            if (Optional.ofNullable(specificDate).isPresent()){
                actualCashflowObject.setDateOfTransaction(specificDate);
            }
            reminderEvent = Optional.of(new ReminderEvent<>(actualCashflowObject, duration));
        }
        else{
            ExpectedCashflowObject expectedCashflowObject =
                    new ExpectedCashflowObject.ExpectedCashflowBuilder().build();
            if(!amountString.isEmpty()){
                BigDecimal amount = new BigDecimal(amountString);
                checkAmmountNumberValidity(amount);
                expectedCashflowObject.setAmount(amount);
            }
            if (!title.isEmpty()){
                expectedCashflowObject.setCategoryName(title);
            }
            if (!timePeriod.isEmpty()){
                expectedCashflowObject.setTimePeriod(timePeriod);
            }
            reminderEvent = Optional.of(new ReminderEvent<>(expectedCashflowObject, duration));
        }

        return reminderEvent.get();
    }

    public void deleteReminder(){
        String eventChoice = eventOptionsCombo.getValue();
        if(!eventChoice.equals("novo")){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Brisanje entiteta");
            alert.setHeaderText("Jeste li sigurni da želite izbrisati ovaj enititet?");
            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("POTVRDI");
            ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("ODUSTANI");
            Optional<ButtonType> confirmation = alert.showAndWait();
            if (confirmation.get() == ButtonType.OK) {
                DatabaseUtils.deleteReminderFromDatabase(eventChoice);
            }
        }
    }

    public void checkEventOptionComboSelection(){
        String eventChoice = eventOptionsCombo.getValue();
        if(!eventChoice.equals("novo")){
            deleteButton.setVisible(true);
        }
        else {
            deleteButton.setVisible(false);
        }
    }

    public void checkTimePeriodOptionComboSelection(){
        String timePeriodString = timePeriodCombo.getValue();
        if(timePeriodString.equals("specifičan datum")){
            specificDateLabel.setVisible(true);
            specificDatePicker.setVisible(true);
        }
        else {
            specificDateLabel.setVisible(false);
            specificDatePicker.setVisible(false);
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
