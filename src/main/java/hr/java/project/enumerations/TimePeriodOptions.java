package hr.java.project.enumerations;

public enum TimePeriodOptions {

    MONTHLY("1 put mjesečno"),
    ANNUAL("1 put godišnje"),
    BIANNUAL("2 puta godišnje"),
    QUARTERLY("4 puta godišnje");

    private String s;

    TimePeriodOptions(String s) {
        this.s=s;
    }

    public String getString() {
        return s;
    }
}
