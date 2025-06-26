module org.example.k {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;

    opens org.example.k to javafx.fxml;
    exports org.example.k;
}