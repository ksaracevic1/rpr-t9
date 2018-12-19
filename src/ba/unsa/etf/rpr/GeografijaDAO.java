package ba.unsa.etf.rpr;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class GeografijaDAO {

    private static GeografijaDAO instance = null;

    private static Connection conn;

    public GeografijaDAO() {
        conn = null;

        try {
            String url = "jdbc:sqlite:resources/baza.db";
            conn = DriverManager.getConnection(url);
            generirajBazu();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void obrisiTabele() throws SQLException {
        String sql = "DROP TABLE gradovi";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.execute();

        sql = "DROP TABLE drzave";

        stmt = conn.prepareStatement(sql);
        stmt.execute();
    }

    private void generirajBazu() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS gradovi (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	naziv text NOT NULL UNIQUE,\n"
                + " brojStanovnika integer,\n"
                + " drzava integer,\n"
                + "	FOREIGN KEY(drzava) REFERENCES drzave(id) ON DELETE CASCADE\n"
                + ");";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.execute();

        sql = "CREATE TABLE IF NOT EXISTS drzave (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	naziv text NOT NULL UNIQUE,\n"
                + " glavniGrad integer,\n"
                + "	FOREIGN KEY(glavniGrad) REFERENCES gradovi(id) ON DELETE CASCADE\n"
                + ");";

        stmt = conn.prepareStatement(sql);
        stmt.execute();

        Grad pariz = new Grad();
        pariz.setNaziv("Pariz");
        pariz.setBrojStanovnika(2200000);
        Drzava francuska = new Drzava();
        francuska.setNaziv("Francuska");
        francuska.setGlavniGrad(pariz);
        dodajDrzavu(francuska);
        dodajGrad(pariz);

        Grad london = new Grad();
        london.setNaziv("London");
        london.setBrojStanovnika(8136000);
        Drzava vb = new Drzava();
        vb.setNaziv("Velika Britanija");
        vb.setGlavniGrad(london);
        dodajDrzavu(vb);
        dodajGrad(london);

        Grad manchester = new Grad();
        manchester.setNaziv("Manchester");
        manchester.setBrojStanovnika(510746);
        manchester.setDrzava(vb);
        dodajGrad(manchester);

        Grad bec = new Grad();
        bec.setNaziv("Beč");
        bec.setBrojStanovnika(1867000);
        Drzava austrija = new Drzava();
        austrija.setNaziv("Austrija");
        austrija.setGlavniGrad(bec);
        dodajDrzavu(austrija);
        dodajGrad(bec);

        Grad graz = new Grad();
        graz.setNaziv("Graz");
        graz.setBrojStanovnika(283869);
        graz.setDrzava(austrija);
        dodajGrad(graz);
    }


    private static void initialize() {
        instance = new GeografijaDAO();
    }

    public static GeografijaDAO getInstance() {
        if (instance == null) initialize();
        return instance;
    }

    public static void removeInstance() {
        try {
            conn.close();
            conn = null;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        instance = null;
    }

    public Grad glavniGrad(String drzava) {
        Grad g = new Grad();
        if (nadjiDrzavu(drzava) == null)
            return null;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT gradovi.id, gradovi.naziv, brojStanovnika, drzava, " +
                    "drzave.id as d_id, drzave.naziv as d_naziv, drzave.glavniGrad as d_gg FROM gradovi INNER JOIN drzave ON " +
                    "gradovi.drzava = drzave.id WHERE drzave.naziv = ?");


            stmt.setString(1, drzava);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                g.setId(rs.getInt(1));
                g.setNaziv(rs.getString(2));
                g.setBrojStanovnika(rs.getInt(3));
                Drzava d = new Drzava();
                d.setId(rs.getInt(5));
                d.setNaziv(rs.getString(6));
                d.setGlavniGrad(g);
                g.setDrzava(d);
                return g;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void obrisiDrzavu(String drzava) {
        Drzava d;
        try {
            if ((d = nadjiDrzavu(drzava)) == null)
                return;
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM drzave WHERE naziv=?");
            stmt.setString(1, drzava);
            stmt.executeUpdate();
            obrisiGradove(d.getId());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public void obrisiGradove(Integer drzavaId) {
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM gradovi WHERE drzava=?");
            stmt.setInt(1, drzavaId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }


    public ArrayList<Grad> gradovi() {
        ArrayList<Grad> rezultat = new ArrayList<Grad>();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT gradovi.id, gradovi.naziv, brojStanovnika, drzava, " +
                    "drzave.id as d_id, drzave.naziv as d_naziv, drzave.glavniGrad as d_gg FROM gradovi INNER JOIN drzave ON " +
                    "gradovi.drzava = drzave.id ORDER BY brojStanovnika DESC");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Drzava d = new Drzava();
                Grad g = new Grad();
                g.setId(rs.getInt(1));
                g.setNaziv(rs.getString(2));
                g.setBrojStanovnika(rs.getInt(3));
                d.setId(rs.getInt(5));
                d.setNaziv(rs.getString(6));
                Grad gg = nadjiGradPoIDu(rs.getInt(7));
                d.setGlavniGrad(gg);
                g.setDrzava(d);
                rezultat.add(g);
            }
            return rezultat;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void dodajGrad(Grad grad) {
        try {
            if (nadjiGrad(grad.getNaziv()) != null)
                return;
            PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO gradovi(naziv, brojStanovnika, drzava) VALUES(?,?,?)");
            stmt.setString(1, grad.getNaziv());
            stmt.setInt(2, grad.getBrojStanovnika());
            stmt.setInt(3, grad.getDrzava().getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void dodajDrzavu(Drzava drzava) {
        try {
            if (nadjiDrzavu(drzava.getNaziv()) != null)
                return;
            PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO drzave(naziv, glavniGrad) VALUES(?,null)");
            stmt.setString(1, drzava.getNaziv());
            stmt.executeUpdate();
            Drzava d = nadjiDrzavu(drzava.getNaziv());
            drzava.getGlavniGrad().setDrzava(d);
            dodajGrad(drzava.getGlavniGrad());
            Grad g = nadjiGrad(drzava.getGlavniGrad().getNaziv());
            drzava.getGlavniGrad().setId(g.getId());
            drzava.setId(d.getId());
            g.setDrzava(drzava);
            izmijeniGrad(g);
            izmijeniDrzavu(drzava);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void izmijeniDrzavu(Drzava drzava) {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE drzave SET naziv=?, glavniGrad=? WHERE id=?");
            stmt.setString(1, drzava.getNaziv());
            stmt.setInt(2, drzava.getGlavniGrad().getId());
            stmt.setInt(3, drzava.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void izmijeniGrad(Grad grad) {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE gradovi SET naziv=?, brojStanovnika=?, drzava=? WHERE id=?");
            stmt.setString(1, grad.getNaziv());
            stmt.setInt(2, grad.getBrojStanovnika());
            stmt.setInt(3, grad.getDrzava().getId());
            stmt.setInt(4, grad.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Drzava nadjiDrzavu(String drzava) {
        Drzava d = new Drzava();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT id, naziv, glavniGrad FROM drzave WHERE naziv=?");
            stmt.setString(1, drzava);
            ResultSet rs = stmt.executeQuery();
            if (rs.isClosed())
                return null;
            d.setId(rs.getInt(1));
            d.setNaziv(rs.getString(2));
            return d;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Grad nadjiGrad(String grad) {
        Grad g = new Grad();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT id, naziv, brojStanovnika, drzava FROM gradovi WHERE naziv=?");
            stmt.setString(1, grad);
            ResultSet rs = stmt.executeQuery();
            if (rs.isClosed())
                return null;
            while (rs.next()) {
                g.setId(rs.getInt(1));
                g.setNaziv(rs.getString(2));
                g.setBrojStanovnika(rs.getInt(3));
                return g;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Grad nadjiGradPoIDu(Integer id) {
        Grad g = new Grad();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT id, naziv, brojStanovnika, drzava FROM gradovi WHERE id=?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                g.setId(rs.getInt(1));
                g.setNaziv(rs.getString(2));
                g.setBrojStanovnika(rs.getInt(3));
                return g;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
