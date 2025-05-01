package hr.java.project;

import hr.java.project.entities.ActualCashflowObject;
import hr.java.project.enumerations.CashflowTypeFilterOptions;
import hr.java.project.enumerations.NumberOfMonthsToFilterBy;
import hr.java.project.enumerations.UserRoles;
import hr.java.project.utils.DatabaseUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CashflowOverviewController {

    private static final Logger logger = LoggerFactory.getLogger(CashflowOverviewController.class);

    private static final DateTimeFormatter DATE_TIME_FORMAT= DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    @FXML
    private Label yearFilterLabel;

    @FXML
    private ComboBox<Integer> yearFilterCombo;

    @FXML
    private ComboBox<String> monthNumberFilterCombo;

    @FXML
    private ComboBox<String> typeFilterCombo;

    @FXML
    private TableView<ActualCashflowObject> cashflowOverviewTable;

    @FXML
    private TableColumn<ActualCashflowObject, String> dateColumn;

    @FXML
    private TableColumn<ActualCashflowObject, String> amountColumn;

    @FXML
    private TableColumn<ActualCashflowObject, String> categoryColumn;

    @FXML
    private TableColumn<ActualCashflowObject, String> userColumn;

    public void initialize(){

        ObservableList<Integer> yearOptions = FXCollections.observableArrayList();
        for(int i=0; i<5; i++){
            yearOptions.add(Year.now().getValue()-i);
        }
        yearFilterCombo.setItems(yearOptions);
        yearFilterCombo.getSelectionModel().selectFirst();

        ObservableList<String> monthNumberOptions = FXCollections.observableArrayList();
        monthNumberOptions.add(NumberOfMonthsToFilterBy.THIS_MONTH.getString());
        monthNumberOptions.add(NumberOfMonthsToFilterBy.SIX_MONTHS.getString());
        monthNumberOptions.add(NumberOfMonthsToFilterBy.WHOLE_YEAR.getString());
        monthNumberFilterCombo.setItems(monthNumberOptions);
        monthNumberFilterCombo.getSelectionModel().selectFirst();

        ObservableList<String> typeOptions = FXCollections.observableArrayList();
        typeOptions.add(CashflowTypeFilterOptions.PROFITS.getString());
        typeOptions.add(CashflowTypeFilterOptions.EXPENSES.getString());
        typeOptions.add(CashflowTypeFilterOptions.BOTH.getString());
        typeFilterCombo.setItems(typeOptions);
        typeFilterCombo.getSelectionModel().selectLast();

        yearFilterCombo.setVisible(false);
        yearFilterLabel.setVisible(false);

        userColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ActualCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ActualCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getUser().userName());
            }
        });

        dateColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ActualCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ActualCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getDateOfTransaction().format(DATE_TIME_FORMAT));
            }
        });

        amountColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ActualCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ActualCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getAmount().toString());
            }
        });

        categoryColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ActualCashflowObject, String>,
                ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ActualCashflowObject, String> param) {
                return new ReadOnlyStringWrapper(param.getValue().getCategoryName());
            }
        });
    }

    public void showFilteredValues(){
        List<ActualCashflowObject> cashflowObjectList = DatabaseUtils.getActualCashflowObjectList();
        String monthNumber = monthNumberFilterCombo.getValue();
        Year year = Year.of(yearFilterCombo.getValue());
        String type = typeFilterCombo.getValue();
        List<ActualCashflowObject> filteredCashflowObjectList =
                DatabaseUtils.actualCashflowFilter(cashflowObjectList, year, monthNumber, type);

        if (LoginStartApplication.currentUser.get().role().equals(UserRoles.user)){
            filteredCashflowObjectList = filteredCashflowObjectList.stream()
                    .filter(o -> o.getUser().userName().equals(LoginStartApplication.currentUser.get().userName()))
                    .collect(Collectors.toList());
        }

        ObservableList<ActualCashflowObject> observableCashflowList =
                FXCollections.observableArrayList(filteredCashflowObjectList);

        cashflowOverviewTable.setItems(observableCashflowList);
    }

    public void CheckCategoryComboSelection(){
        System.out.println("action!");
        if(monthNumberFilterCombo.getValue().equals(NumberOfMonthsToFilterBy.WHOLE_YEAR.getString())){
            yearFilterCombo.setVisible(true);
            yearFilterLabel.setVisible(true);
        }
        else{
            yearFilterCombo.setVisible(false);
            yearFilterLabel.setVisible(false);
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
