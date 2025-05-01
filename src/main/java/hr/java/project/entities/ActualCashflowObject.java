package hr.java.project.entities;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActualCashflowObject extends FinanceObjects<BigDecimal> implements Serializable{

    private LocalDate dateOfTransaction;

    public static class ActualCashflowObjectBuilder {
        private String categoryName;
        private BigDecimal amount;
        private LocalDate dateOfTransaction;

        public ActualCashflowObjectBuilder setCategoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public ActualCashflowObjectBuilder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public ActualCashflowObjectBuilder setDateOfTransaction(LocalDate dateOfTransaction) {
            this.dateOfTransaction = dateOfTransaction;
            return this;
        }

        public ActualCashflowObject build() {
            return new ActualCashflowObject(categoryName, amount, dateOfTransaction);
        }
    }

    public ActualCashflowObject(String categoryName, BigDecimal amount, LocalDate dateOfTransaction) {
        super(categoryName, amount);
        this.dateOfTransaction = dateOfTransaction;
    }

    public LocalDate getDateOfTransaction() {
        return dateOfTransaction;
    }

    public void setDateOfTransaction(LocalDate dateOfTransaction) {
        this.dateOfTransaction = dateOfTransaction;
    }

    public static void serializeActualCashflowObject(List<ActualCashflowObject> actualCashflowObjects){
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream("dataFiles/serializedActualCashflow.dat"))){
            oos.writeObject(actualCashflowObjects);
        }
        catch (IOException ex){
            System.out.println("Pogreška u serijalizaciji");
        }
    }

    public static List<String> deserializeActualCashflowObjects(){
        List<String> stringList = new ArrayList<>();
        List<ActualCashflowObject> actualCashflowObjects = new ArrayList<>();
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream("dataFiles/serializedActualCashflow.dat"));
            actualCashflowObjects = (List<ActualCashflowObject>)in.readObject();

            in.close();

            for (ActualCashflowObject ac : actualCashflowObjects){
                stringList.add(ac.toString());
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return stringList;
    }

    @Override
    public String toString() {
        String string = "ACTUAL CASHFLOW OBJECT - ";
        string = string + super.getUser().userName() + " - ";
        string = string + super.getDateOfCreation().format(DATE_TIME_FORMAT) + " - ";
        if(Optional.ofNullable(super.getCategoryName()).isPresent() && !super.getCategoryName().isEmpty()){
            string = string + super.getCategoryName() + "; ";
        }
        if(Optional.ofNullable(super.getAmount()).isPresent()){
            string = string + super.getAmount() + "; ";
        }
        if(Optional.ofNullable(dateOfTransaction).isPresent()){
            string = string + dateOfTransaction.format(DATE_TIME_FORMAT) + "; ";
        }
        return string;
    }
}
