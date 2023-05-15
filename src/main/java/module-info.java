module MedleyBeta {
    requires transitive javafx.controls;
    requires java.desktop;
    requires javafx.swing;
    requires java.sql;
    exports gui;
    exports api.tools;
    exports api.spotify;
}