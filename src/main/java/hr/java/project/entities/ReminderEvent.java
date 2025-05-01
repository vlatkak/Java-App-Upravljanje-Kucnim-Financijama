package hr.java.project.entities;

import hr.java.project.LoginStartApplication;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReminderEvent<T extends FinanceObjects<BigDecimal>, U extends Number> extends Entity implements Serializable {

    private T eventObject;

    private U daysBeforeReminder;

    public ReminderEvent(T eventObject, U daysBeforeReminder) {
        this.eventObject = eventObject;
        this.daysBeforeReminder = daysBeforeReminder;
        super.setDateOfCreation(LocalDate.now());
        if(LoginStartApplication.currentUser.isPresent()){
            super.setUser(LoginStartApplication.currentUser.get());
        }
    }

    public ReminderEvent() {}

    public T getEventObject() {
        return eventObject;
    }

    public void setEventObject(T eventObject) {
        this.eventObject = eventObject;
    }

    public U getDaysBeforeReminder() {
        return daysBeforeReminder;
    }

    public void setDaysBeforeReminder(U daysBeforeReminder) {
        this.daysBeforeReminder = daysBeforeReminder;
    }

    public static void serializeReminderEvent(List<ReminderEvent<FinanceObjects<BigDecimal>, Integer>> remiderEvents){
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream("dataFiles/serializedReminders.dat"))){
            oos.writeObject(remiderEvents);
        }
        catch (IOException ex){
            System.out.println("Pogreška u serijalizaciji");
        }
    }

    public static List<String> deserializeReminders(){
        List<String> stringList = new ArrayList<>();
        List<ReminderEvent<FinanceObjects<BigDecimal>, Integer>> reminderEvents = new ArrayList<>();
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream("dataFiles/serializedReminders.dat"));
            reminderEvents = (List<ReminderEvent<FinanceObjects<BigDecimal>, Integer>>)in.readObject();

            in.close();

            for (ReminderEvent e : reminderEvents){
                stringList.add(e.toString());
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return stringList;
    }

    @Override
    public String toString() {
        String string = "REMINDER - ";
        if(eventObject instanceof ActualCashflowObject ac){
            string = string + ac.toString();
        }
        else if (eventObject instanceof ExpectedCashflowObject ec){
            string = string + ec.toString();
        }
        if(Optional.ofNullable(daysBeforeReminder).isPresent()){
            string = string + daysBeforeReminder;
        }
        return string;
    }
}
