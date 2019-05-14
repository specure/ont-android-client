package at.specure.androidX.data;

public class DataUtil {

    public static String parseNullValue(String value) {
        return value == null ? " - " : value;
    }
}
