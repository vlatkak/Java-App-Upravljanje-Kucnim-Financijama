package hr.java.project.threads;

import hr.java.project.ExpectedProfitOverviewController;
import hr.java.project.entities.ActualCashflowObject;
import hr.java.project.utils.DatabaseUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.util.List;

public class FinancialStateCalculationThread implements Runnable{

    @FXML
    private Label financialStateSumString;

    public FinancialStateCalculationThread(Label financialStateSumString) {
        this.financialStateSumString = financialStateSumString;
    }

    @Override
    public void run() {
        BigDecimal financialStateSum = new BigDecimal(0);
        List<ActualCashflowObject> cashflowObjectList = DatabaseUtils.getActualCashflowObjectList();
        for(ActualCashflowObject o : cashflowObjectList){
            financialStateSum = financialStateSum.add(o.getAmount());
        }
        financialStateSumString.setText("Trenutni iznos na racunu: "+financialStateSum);
    }

}
