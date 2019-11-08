package com.kubolab.gnss.casmLogger;

public class GnssRawData {

    public String PRN;
    public double Pseudorange;
    public double CarrierPhase;
    public double Cn0HZ;
    public String Type;
    public int GPSweek;
    public double GPSsecond;

    public String getPRN() {
        return PRN;
    }

    public void setPRN(String PRN) {
        this.PRN = PRN;
    }

    public double getPseudorange() {
        return Pseudorange;
    }

    public void setPseudorange(double pseudorange) {
        Pseudorange = pseudorange;
    }

    public double getCarrierPhase() {
        return CarrierPhase;
    }

    public void setCarrierPhase(double carrierPhase) {
        CarrierPhase = carrierPhase;
    }

    public double getCn0HZ() {
        return Cn0HZ;
    }

    public void setCn0HZ(double cn0HZ) {
        Cn0HZ = cn0HZ;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }


    public int getGPSweek() {
        return GPSweek;
    }

    public void setGPSweek(int GPSweek) {
        this.GPSweek = GPSweek;
    }

    public double getGPSsecond() {
        return GPSsecond;
    }

    public void setGPSsecond(double GPSsecond) {
        this.GPSsecond = GPSsecond;
    }

}

