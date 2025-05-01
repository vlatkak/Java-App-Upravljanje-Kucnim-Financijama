package hr.java.project.threads;

import hr.java.project.entities.ExpectedCashflowObject;

public class UpdateExpectedCashflowThread extends SynchronizedThreadMethods implements Runnable{

    private ExpectedCashflowObject expectedCashflowObject;

    private String categoryToUpdate;

    public UpdateExpectedCashflowThread(ExpectedCashflowObject expectedCashflowObject, String categoryToUpdate) {
        this.expectedCashflowObject = expectedCashflowObject;
        this.categoryToUpdate = categoryToUpdate;
    }

    @Override
    public void run() {
        super.updateExpectedCashflow(expectedCashflowObject, categoryToUpdate);
    }
}
