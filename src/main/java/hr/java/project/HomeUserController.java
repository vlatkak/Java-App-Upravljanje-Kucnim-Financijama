package hr.java.project;

import hr.java.project.entities.FinanceObjects;
import hr.java.project.entities.ReminderEvent;
import hr.java.project.utils.DatabaseUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;

public class HomeUserController {

    private static final Logger logger = LoggerFactory.getLogger(HomeUserController.class);

    @FXML
    private Label welcomeText;

    @FXML
    private Label reminderText;

    public void initialize(){
        welcomeText.setText("Dobro došli "+ LoginStartApplication.currentUser.get().name() +"!");

        Set<ReminderEvent<FinanceObjects<BigDecimal>, Integer>> reminderSet = DatabaseUtils.getReminderSetFromDatabase();

        String displayString = "";
        Integer reminderCounter = 0;
        for(ReminderEvent<FinanceObjects<BigDecimal>, Integer> re : reminderSet){
            Boolean reminderNeedsShowing = DatabaseUtils.checkIfReminderNeedsShowing(re);
            if(reminderNeedsShowing){
                reminderCounter++;
                displayString = displayString + re.getEventObject().getCategoryName() +
                        " - " + re.getEventObject().getAmount() + "\n";
            }
        }
        if (reminderCounter==0){
            displayString = "Trenutno nemate nikakvih podsjetnika.";
        }
        reminderText.setText(displayString);
    }

    public void openProfitSaveScreen(){
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("profitSave.fxml"));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - spremi prihod");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }

    public void openExpenseSaveScreen(){
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("expenseSave.fxml"));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - spremi rashod");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }

    public void openCashflowOverviewScreen(){
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("cashflowOverview.fxml"));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - pregled prihoda i rashoda");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }

    public void openReminderCreationScreen(){
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("createReminder.fxml"));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - podsjetnici");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }

    public void logOut(){
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
