package hr.java.project.threads;

import hr.java.project.entities.ExpectedCashflowObject;
import hr.java.project.entities.FinanceObjects;
import hr.java.project.entities.ReminderEvent;
import hr.java.project.utils.DatabaseUtils;

import java.math.BigDecimal;

public abstract class SynchronizedThreadMethods {

    public static Boolean criticalSectionOccupied = false;

    public synchronized void updateExpectedCashflow(ExpectedCashflowObject expectedCashflowObject,
                                                    String categoryToUpdate){

        while (criticalSectionOccupied){
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        criticalSectionOccupied = true;

        DatabaseUtils.updateExpectedCashflowDatabase(expectedCashflowObject, categoryToUpdate);

        criticalSectionOccupied = false;

        notifyAll();
    }

    public synchronized void updateReminders(ReminderEvent<FinanceObjects<BigDecimal>, Integer> event,
                                             String reminderTitle){

        while (criticalSectionOccupied){
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        criticalSectionOccupied = true;

        DatabaseUtils.updateReminderDatabase(event, reminderTitle);

        criticalSectionOccupied = false;

        notifyAll();
    }

}
