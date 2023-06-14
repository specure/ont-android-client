package at.specure.androidX.data.test;

import at.specure.android.test.SpeedTestStatViewController;

public class TestResultView {

    SpeedTestStatViewController.InfoStat typeOfResult;
    public double maxValue;
    public double value;
    Boolean isMedian;
    String formatedValue;
    String formattedUnits;


    public SpeedTestStatViewController.InfoStat getTypeOfResult() {
        return typeOfResult;
    }

    public int getTitle() {
        return typeOfResult.getTextResId();
    }

    public String getDisplayValue() {
        return formatedValue;
    }

    public String getDisplayUnits() {
       return formattedUnits;
    }

    private String setShowValue(double value) {
        if (typeOfResult == SpeedTestStatViewController.InfoStat.QOS) {
            this.formatedValue = (int) value  + "/" + (int) maxValue;
            this.formattedUnits = "";
        } else {
            String format = typeOfResult.format((long) value);
            String[] s = format.split(" ");
            this.formatedValue = s[0];
            if (s.length > 1) {
                this.formattedUnits = s[1];
            } else {
                this.formattedUnits = "";
            }
        }
        return this.formatedValue;
    }

    /**
     * For median loaded values
     * @param typeOfResult
     * @param value
     * @param isMedian
     */
    public TestResultView(SpeedTestStatViewController.InfoStat typeOfResult, String value, Boolean isMedian) {
        this.typeOfResult = typeOfResult;
        this.formatedValue = value;
        this.formattedUnits = typeOfResult.getUnits();
        this.isMedian = isMedian;
    }

    /**
     * For current results
     * @param typeOfResult
     * @param maxValue
     * @param value
     * @param isMedian
     */
    public TestResultView(SpeedTestStatViewController.InfoStat typeOfResult, double maxValue, double value, Boolean isMedian) {
        this.typeOfResult = typeOfResult;
        this.maxValue = maxValue;
        this.value = (typeOfResult == SpeedTestStatViewController.InfoStat.DOWNLOAD || typeOfResult == SpeedTestStatViewController.InfoStat.UPLOAD) ? value*1000 : value;
        setShowValue(this.value);
        this.isMedian = isMedian;
    }

    private void setShowUnits() {
        String format = typeOfResult.format((long) value);
        String[] s = format.split(" ");
        if (s.length > 1) {
            this.formattedUnits = s[1];
        } else {
            this.formattedUnits = "";
        }
    }

    public boolean isMedian() {
        return this.isMedian;
    }
}


