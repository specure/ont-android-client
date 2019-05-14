package at.specure.android.api.jsons.TestResult;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MeasuredValue {

    @SerializedName("title")
    String valueTitle;

    @SerializedName("classification")
    Integer classification;

    @SerializedName("value") // value with localised unit e.g. -26 dBm, 123 Mbit, ...
            String value;

    public MeasuredValue(String valueTitle, Integer classification, String value) {
        this.valueTitle = valueTitle;
        this.classification = classification;
        this.value = value;
    }

    public String getValueTitle() {
        return valueTitle;
    }

    public void setValueTitle(String valueTitle) {
        this.valueTitle = valueTitle;
    }

    public Integer getClassification() {
        return classification;
    }

    public void setClassification(Integer classification) {
        this.classification = classification;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnits() {
        if (this.value != null) {
            String[] splitted = this.value.split(" ");
            if (splitted.length > 1) {
                return splitted[1];
            }
        }
        return null;
    }

    public Float getFloatValue() {
        if (this.value != null) {
            String[] splitted = this.value.split(" ");
            if (splitted.length > 0) {
                try {
                    return Float.valueOf(splitted[0]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public String getStringValue() {
        if (this.value != null) {
            String[] splitted = this.value.split(" ");
            if (splitted.length > 0) {
                try {
                    return splitted[0];
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
