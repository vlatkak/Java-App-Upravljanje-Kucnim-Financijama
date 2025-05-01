package hr.java.project;

import hr.java.project.entities.ExpectedCashflowObject;
import hr.java.project.enumerations.ExpectedCashflowTypes;
import hr.java.project.enumerations.UserRoles;
import hr.java.project.threads.FinancialStateCalculationThread;
import hr.java.project.utils.DatabaseUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ExpectedProfitOverviewController {

    private static final Logger logger = LoggerFactory.getLogger(ExpectedProfitOverviewController.class);

    @FXML
    public Label currentFinancialState;

    @FXML
    private TableView<ExpectedCashflowObject> expectedProfitsTable;

    @FXML
    private TableColumn<ExpectedCashflowObject, String> categoryColumn;

    @FXML
    private TableColumn<ExpectedCashflowObject, String> expectedAmountColumn;

    @FXML
    private TableColumn<ExpectedCashflowObject, String> payemntFrequencyColumn;

    @FXML
    private TableColumn<ExpectedCashflowObject, String> earnedSoFarColumn;

    public void initialize(){
        categoryColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpectedCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExpectedCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getCategoryName());
            }
        });

        expectedAmountColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpectedCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExpectedCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getAmount().toString());
            }
        });

        payemntFrequencyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpectedCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExpectedCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getTimePeriod());
            }
        });

        earnedSoFarColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpectedCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExpectedCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getProgressToAmount().toString());
            }
        });

        List<ExpectedCashflowObject> expectedProfitList =
                DatabaseUtils.getExpectedCashflowFromDatabase(ExpectedCashflowTypes.expected_profit);

        for(ExpectedCashflowObject p : expectedProfitList){
            p.setProgressToAmount(DatabaseUtils.getProgressToExpectedCashflow(p));
        }

        ObservableList<ExpectedCashflowObject> expectedProfitObservableList =
                FXCollections.observableArrayList(expectedProfitList);

        expectedProfitsTable.setItems(expectedProfitObservableList);

        Timeline refreshThread = new Timeline(new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FinancialStateCalculationThread calculationThread =
                        new FinancialStateCalculationThread(currentFinancialState);
                Platform.runLater(calculationThread);
            }
        }), new KeyFrame(Duration.seconds(1)));
        refreshThread.setCycleCount(Animation.INDEFINITE);
        refreshThread.play();
    }

    public void openExpectedProfitUpdateScreen(){
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("expectedProfitUpdate.fxml"));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - uredi očekivane prihode");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
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
