package at.specure.android.api.jsons.TestResult;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NetworkInfo {

    @SerializedName("title")
    String valueTitle;

    @SerializedName("value")
    String value;

    public NetworkInfo(String valueTitle, String value) {
        this.valueTitle = valueTitle;
        this.value = value;
    }

    public String getValueTitle() {
        return valueTitle;
    }

    public void setValueTitle(String valueTitle) {
        this.valueTitle = valueTitle;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
