package gui.util;

public interface Observer<Subject, ClientData> {
    void update(Subject subject, ClientData clientData);
}
