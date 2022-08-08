module com.example.decompiler {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens dev.qruet.decompiler to javafx.fxml;
    exports dev.qruet.decompiler;
}