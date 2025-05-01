package hr.java.project.entities;

import java.time.format.DateTimeFormatter;

public sealed interface Financial permits FinanceObjects{
    public static DateTimeFormatter DATE_TIME_FORMAT= DateTimeFormatter.ofPattern("dd.MM.yyyy.");
}
