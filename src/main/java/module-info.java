module com.arnobx86.smarthomeexpensemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.arnobx86.smarthomeexpensemanager to javafx.fxml;
    exports com.arnobx86.smarthomeexpensemanager;
}