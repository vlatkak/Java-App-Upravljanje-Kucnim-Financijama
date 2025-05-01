package hr.java.project;

import hr.java.project.entities.ActualCashflowObject;
import hr.java.project.entities.ExpectedCashflowObject;
import hr.java.project.entities.ReminderEvent;
import hr.java.project.enumerations.UserRoles;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class EntityChangesOverviewController {

    private static final Logger logger = LoggerFactory.getLogger(EntityChangesOverviewController.class);

    @FXML
    private TableView<String> objectChangesTableview;

    @FXML
    private TableColumn<String, String> objectChangesStringColumn;

    public void initialize(){
        List<String> strings = ReminderEvent.deserializeReminders();
        strings.addAll(ActualCashflowObject.deserializeActualCashflowObjects());
        strings.addAll(ExpectedCashflowObject.deserializeExpectedCashflowObjects());
        ObservableList<String> observableObjectStringList = FXCollections.observableArrayList(strings);

        objectChangesStringColumn.setCellValueFactory(string -> new SimpleStringProperty(string.getValue()));

        objectChangesTableview.setItems(observableObjectStringList);
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
