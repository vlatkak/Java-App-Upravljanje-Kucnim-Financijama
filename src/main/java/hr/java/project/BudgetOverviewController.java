package hr.java.project;

import hr.java.project.entities.ExpectedCashflowObject;
import hr.java.project.entities.ActualCashflowObject;
import hr.java.project.enumerations.ExpectedCashflowTypes;
import hr.java.project.enumerations.UserRoles;
import hr.java.project.utils.DatabaseUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import java.math.BigDecimal;
import java.util.List;

public class BudgetOverviewController {

    private static final Logger logger = LoggerFactory.getLogger(BudgetUpdateController.class);

    @FXML
    private TableView<ExpectedCashflowObject> budgetsTable;

    @FXML
    private TableColumn<ExpectedCashflowObject, String> categoryColumn;

    @FXML
    private TableColumn<ExpectedCashflowObject, String> budgetAmountColumn;

    @FXML
    private TableColumn<ExpectedCashflowObject, String> spentSoFarColumn;

    @FXML
    private TableColumn<ExpectedCashflowObject, String> timePeriodColumn;



    public void initialize(){
        categoryColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpectedCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExpectedCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getCategoryName());
            }
        });

        budgetAmountColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpectedCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExpectedCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getAmount().toString());
            }
        });

        spentSoFarColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpectedCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExpectedCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getProgressToAmount().toString());
            }
        });

        timePeriodColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpectedCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ExpectedCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getTimePeriod());
            }
        });

        List<ExpectedCashflowObject> budgetList =
                DatabaseUtils.getExpectedCashflowFromDatabase(ExpectedCashflowTypes.budget);

        for(ExpectedCashflowObject b : budgetList){
            b.setProgressToAmount(DatabaseUtils.getProgressToExpectedCashflow(b).multiply(BigDecimal.valueOf(-1)));
        }

        ObservableList<ExpectedCashflowObject> observableBudgetList = FXCollections.observableArrayList(budgetList);

        budgetsTable.setItems(observableBudgetList);
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

    public void openBudgetUpdateScreen(){
        FXMLLoader fxmlLoader = new FXMLLoader(LoginStartApplication.class.getResource("budgetUpdate.fxml"));
        try{
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginStartApplication.getMainStage().setTitle("Moje financije - uredi budžete");
            LoginStartApplication.getMainStage().setScene(scene);
            LoginStartApplication.getMainStage().show();
        }catch(IOException e){
            logger.error("Došlo je do pogreške prilikom otvaranja prozora");
            throw new RuntimeException(e);
        }
    }
}
