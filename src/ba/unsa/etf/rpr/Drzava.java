package ba.unsa.etf.rpr;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Drzava {

    private SimpleIntegerProperty id;
    private SimpleStringProperty naziv;
    private Grad glavniGrad;

    public Drzava() {
        id = new SimpleIntegerProperty(0);
        naziv = new SimpleStringProperty("");
        glavniGrad = new Grad();
    }

    public Drzava (Integer a, String n, Grad g) {
        id = new SimpleIntegerProperty(a);
        naziv = new SimpleStringProperty(n);
        glavniGrad = g;
    }


    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNaziv() {
        return naziv.get();
    }

    public SimpleStringProperty nazivProperty() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv.set(naziv);
    }

    public void setGlavniGrad(Grad g) {
        glavniGrad = g;
    }

    public Grad getGlavniGrad() {
        return glavniGrad;
    }
}
