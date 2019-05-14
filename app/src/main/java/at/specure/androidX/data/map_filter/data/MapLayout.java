package at.specure.androidX.data.map_filter.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class MapLayout implements Parcelable {

    public static final String MAP_FILTER_LAYOUT_ACCESS_TOKEN = "MAP_FILTER_LAYOUT_ACCESS_TOKEN";
    public static final String MAP_FILTER_LAYOUT_API_LINK = "MAP_FILTER_LAYOUT_API_LINK";
    public static final String MAP_FILTER_LAYOUT_LAYER = "MAP_FILTER_LAYOUT_LAYER";

    @SerializedName("default")
    Boolean isDefault;

    @SerializedName("apiLink")
    String link;

    @SerializedName("title")
    String title;

    @SerializedName("accessToken")
    String token;

    @SerializedName("layer")
    String layer;

    public Boolean getDefault() {
        return isDefault != null && isDefault;
    }

    public String getTitle() {
        return title;
    }

    public String getFilterValue() {
        return link;
    }

//    public HashMap<String, String> getAdditionalParameters() {
    public Bundle getAdditionalParameters() {
        Bundle bundle = new Bundle();
        bundle.putString(MAP_FILTER_LAYOUT_ACCESS_TOKEN, token);
        bundle.putString(MAP_FILTER_LAYOUT_API_LINK, link);
        bundle.putString(MAP_FILTER_LAYOUT_LAYER, layer);
//        return new HashMap<String, String>() {
//            {
//                put(MAP_FILTER_LAYOUT_ACCESS_TOKEN, token);
//                put(MAP_FILTER_LAYOUT_API_LINK, link);
//                put(MAP_FILTER_LAYOUT_LAYER, layer);
//            }
//
//        };
        return bundle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.isDefault);
        dest.writeString(this.link);
        dest.writeString(this.title);
        dest.writeString(this.token);
        dest.writeString(this.layer);
    }

    public MapLayout() {
    }

    protected MapLayout(Parcel in) {
        this.isDefault = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.link = in.readString();
        this.title = in.readString();
        this.token = in.readString();
        this.layer = in.readString();
    }

    public static final Creator<MapLayout> CREATOR = new Creator<MapLayout>() {
        @Override
        public MapLayout createFromParcel(Parcel source) {
            return new MapLayout(source);
        }

        @Override
        public MapLayout[] newArray(int size) {
            return new MapLayout[size];
        }
    };
}
