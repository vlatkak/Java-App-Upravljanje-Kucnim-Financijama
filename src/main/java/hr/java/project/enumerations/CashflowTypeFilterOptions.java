package hr.java.project.enumerations;

public enum CashflowTypeFilterOptions {
    EXPENSES("samo rashodi"),
    PROFITS("samo prihodi"),
    BOTH("i prihode i rashode");

    String s;

    CashflowTypeFilterOptions(String s) {
        this.s = s;
    }

    public String getString() {
        return s;
    }
}
