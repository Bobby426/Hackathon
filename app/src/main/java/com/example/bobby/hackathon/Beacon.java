package com.example.bobby.hackathon;

/**
 * Created by Bobby on 04.01.2017.
 */

public class Beacon {
    private String macAdresse;
    private String beschreibung;

    public Beacon(String macAdresse, String beschreibung) {
        this.beschreibung = beschreibung;
        this.macAdresse = macAdresse;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getMacAdresse() {
        return macAdresse;
    }

    public void setMacAdresse(String macAdresse) {
        this.macAdresse = macAdresse;
    }
}
