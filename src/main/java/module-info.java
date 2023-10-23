module MedleyBeta {
    requires transitive javafx.controls;
    requires java.desktop;
    requires javafx.swing;
    requires java.sql;
    exports gui.scenes;
    exports api.tools;
    exports api.spotify;
    exports gui.ui;
    exports gui.model;
    exports gui.util;
    exports gui.main;
    exports gui.ui.widget;
}