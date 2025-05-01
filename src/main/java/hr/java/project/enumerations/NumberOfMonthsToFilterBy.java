package hr.java.project.enumerations;

public enum NumberOfMonthsToFilterBy {
    THIS_MONTH("samo trenutni mjesec"),
    SIX_MONTHS("proteklih 6 mjeseci"),
    WHOLE_YEAR("cijela godina");

    String s;

    NumberOfMonthsToFilterBy(String s) {
        this.s = s;
    }

    public String getString() {
        return s;
    }
}
