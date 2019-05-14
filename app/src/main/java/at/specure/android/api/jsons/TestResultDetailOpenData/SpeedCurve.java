package at.specure.android.api.jsons.TestResultDetailOpenData;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import at.specure.android.api.jsons.Location;
import at.specure.android.api.jsons.Signal;

public class SpeedCurve {

    @SerializedName("download")
    List<GraphSpeedItem> downloadGraphItems;

    @SerializedName("upload")
    List<GraphSpeedItem> uploadGraphItems;

    @SerializedName("location")
    List<Location> locations;

    @SerializedName("signal")
    List<Signal> signal;

    public List<GraphSpeedItem> getDownloadGraphItems() {
        return downloadGraphItems;
    }

    public void setDownloadGraphItems(List<GraphSpeedItem> downloadGraphItems) {
        this.downloadGraphItems = downloadGraphItems;
    }

    public List<GraphSpeedItem> getUploadGraphItems() {
        return uploadGraphItems;
    }

    public void setUploadGraphItems(List<GraphSpeedItem> uploadGraphItems) {
        this.uploadGraphItems = uploadGraphItems;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public List<Signal> getSignal() {
        return signal;
    }

    public void setSignal(List<Signal> signal) {
        this.signal = signal;
    }
}
