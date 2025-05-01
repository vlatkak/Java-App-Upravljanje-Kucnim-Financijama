module hr.java.production.projektvk {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.sql;


    opens hr.java.project to javafx.fxml;
    exports hr.java.project;
}