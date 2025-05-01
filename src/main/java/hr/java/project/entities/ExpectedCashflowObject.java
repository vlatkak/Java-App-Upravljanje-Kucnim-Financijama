package hr.java.project.entities;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpectedCashflowObject extends FinanceObjects<BigDecimal> implements Serializable {

    private BigDecimal progressToAmount;

    private String timePeriod;

    public static class ExpectedCashflowBuilder {
        private String categoryName;
        private BigDecimal amount;
        private BigDecimal progressToAmount;
        private String timePeriod;

        public ExpectedCashflowBuilder setCategoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public ExpectedCashflowBuilder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public ExpectedCashflowBuilder setProgressToAmount(BigDecimal progressToAmount) {
            this.progressToAmount = progressToAmount;
            return this;
        }

        public ExpectedCashflowBuilder setTimePeriod(String timePeriod) {
            this.timePeriod = timePeriod;
            return this;
        }

        public ExpectedCashflowObject build() {
            return new ExpectedCashflowObject(categoryName, amount, progressToAmount, timePeriod);
        }
    }

    public ExpectedCashflowObject(String categoryName, BigDecimal amount, BigDecimal actualAmount,
                                  String paymentFrequency) {
        super(categoryName, amount);
        this.progressToAmount = actualAmount;
        this.timePeriod = paymentFrequency;
        super.setDateOfCreation(LocalDate.now());
    }

    public void setProgressToAmount(BigDecimal progressToAmount) {
        this.progressToAmount = progressToAmount;
    }

    public BigDecimal getProgressToAmount() {
        return progressToAmount;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public static void serializeExpectedCashflowObject(List<ExpectedCashflowObject> expectedCashflowObjects){
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream("dataFiles/serializedExpectedCashflow.dat"))){
            oos.writeObject(expectedCashflowObjects);
        }
        catch (IOException ex){
            System.out.println("Pogreška u serijalizaciji");
        }
    }

    public static List<String> deserializeExpectedCashflowObjects(){
        List<String> stringList = new ArrayList<>();
        List<ExpectedCashflowObject> expectedCashflowObjects = new ArrayList<>();
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream("dataFiles/serializedExpectedCashflow.dat"));
            expectedCashflowObjects = (List<ExpectedCashflowObject>)in.readObject();

            in.close();

            for (ExpectedCashflowObject ec : expectedCashflowObjects){
                stringList.add(ec.toString());
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return stringList;
    }

    @Override
    public String toString() {
        String string = "EXPECTED CASHFLOW OBJECT - ";
        string = string + super.getUser().userName() + " - ";
        string = string + super.getDateOfCreation().format(DATE_TIME_FORMAT) + " - ";
        if(Optional.ofNullable(super.getCategoryName()).isPresent() && !super.getCategoryName().isEmpty()){
            string = string + super.getCategoryName() + "; ";
        }
        if(Optional.ofNullable(super.getAmount()).isPresent()){
            string = string + super.getAmount() + "; ";
        }
        if(Optional.ofNullable(timePeriod).isPresent()){
            string = string + timePeriod + "; ";
        }
        return string;
    }
}
