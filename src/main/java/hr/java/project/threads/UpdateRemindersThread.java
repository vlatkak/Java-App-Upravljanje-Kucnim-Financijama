package hr.java.project.threads;

import hr.java.project.entities.FinanceObjects;
import hr.java.project.entities.ReminderEvent;

import java.math.BigDecimal;

public class UpdateRemindersThread extends SynchronizedThreadMethods implements Runnable{

    private ReminderEvent<FinanceObjects<BigDecimal>, Integer> event;

    private String reminderTitle;

    public UpdateRemindersThread(ReminderEvent<FinanceObjects<BigDecimal>, Integer> event, String reminderTitle) {
        this.event = event;
        this.reminderTitle = reminderTitle;
    }

    @Override
    public void run() {
        updateReminders(event, reminderTitle);
    }
}
