package org.gmod.schema.utils;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Represents computed properties of a polypeptide.
 *
 */
public class PeptideProperties implements Serializable {

    private static final long serialVersionUID = -2268543289671241155L;

    private double massInDaltons;
    private int aminoAcids;
    private double isoelectricPoint;
    private double charge;

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    /**
     * Get the number of amino acids in the polypeptide.
     * @return the number of amino acids in the polypeptide
     */
    public int getAminoAcids() {
        return aminoAcids;
    }

    public void setAminoAcids(int aminoAcids) {
        this.aminoAcids = aminoAcids;
    }

    /**
     * Get the charge.
     * @return the charge, formatted as a string. Nobody seems to know what units
     * this is in, or what it actually means. Should always be a (positive or negative)
     * multiple of 0.5.
     */
    public String getCharge() {
        return decimalFormat.format(charge);
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    /**
     * Get the isoelectric point.
     * @return the isoelectric point, rounded to two decimal places and formatted
     * as a string
     */
    public String getIsoelectricPoint() {
        return decimalFormat.format(isoelectricPoint);
    }

    public void setIsoelectricPoint(double isoelectricPoint) {
        this.isoelectricPoint = isoelectricPoint;
    }

    public boolean isHasMass() {
        return massInDaltons != 0;
    }

    public double getMassInDaltons() {
        return massInDaltons;
    }

    /**
     * Get the mass.
     * @return the mass in kiloDaltons, formatted as a string like "1.23kDa"
     */
    public String getMass() {
        return decimalFormat.format(massInDaltons / 1000) + " kDa";
    }

    public void setMass(double massInDaltons) {
        this.massInDaltons = massInDaltons;
    }
}
