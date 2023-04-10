module MedleyBeta {
    requires transitive javafx.controls;
    requires java.desktop;
    requires javafx.swing;
    requires org.seleniumhq.selenium.api;
    //requires org.seleniumhq.selenium.chrome_driver;
    exports gui;
}