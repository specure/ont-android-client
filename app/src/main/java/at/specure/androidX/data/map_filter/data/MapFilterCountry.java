package at.specure.androidX.data.map_filter.data;

public class MapFilterCountry {

    String countryName;

    String countryCode;

    String id = "country";

    boolean isDefault = false;

    public MapFilterCountry(String countryCode, String countryName) {
        this.countryName = countryName;
        this.countryCode = countryCode;
    }


    public MapFilterCountry(String countryCode, String countryName, boolean isDefault) {
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.isDefault = isDefault;
    }

    public String getTitle() {
        return countryName;
    }

    public String getFilterValue() {
        return countryCode;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
