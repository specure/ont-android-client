package at.specure.androidX.data.badges;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.specure.opennettest.R;

import java.util.ArrayList;

public class Badge implements Parcelable {

    public static final String BOC_CRITERIAS_OPERATOR_OR = "or";
    public static final String BOC_CRITERIAS_OPERATOR_AND = "and";

    public static final String BADGE_CATEGORY_MEASUREMENT = "measurement";
    public static final String BADGE_CATEGORY_HOLIDAY= "holiday";

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("category")
    public String category;

    @SerializedName("image_link")
    public String imageUrl;

    @SerializedName("terms_operator")
    public String operator;

    @SerializedName("criteria")
    ArrayList<BadgeObtainingCriteria> criterias;

    public String getCriteriaForDisplay() {
        String criteriasDisplay = "";
        if (criterias != null) {
            for (BadgeObtainingCriteria criteria: criterias) {
                String displayCriteria = criteria.getDisplayCriteria();
                if (displayCriteria != null) {
                    if (criteriasDisplay.length() == 0) {
                        criteriasDisplay = displayCriteria;
                    } else {
                        switch (operator) {
                            case BOC_CRITERIAS_OPERATOR_AND:
                                criteriasDisplay = criteriasDisplay + " & " + displayCriteria;
                                break;
                            case BOC_CRITERIAS_OPERATOR_OR:
                                criteriasDisplay = criteriasDisplay + " | " + displayCriteria;
                                break;
                        }
                    }
                }
            }
        }
        return criteriasDisplay;
    }

    public String getCategory(Context context) {
        if (context != null) {
            switch (category) {
                case BADGE_CATEGORY_MEASUREMENT:
                    return context.getString(R.string.badges_category_measurements);
                case BADGE_CATEGORY_HOLIDAY:
                    return context.getString(R.string.badges_category_holidays);
            }
        }
        return category;
    }

    public boolean evaluate(int measurementsCount) {
        if (criterias != null) {
            switch (operator) {
                case BOC_CRITERIAS_OPERATOR_AND:
                    for (BadgeObtainingCriteria criteria: criterias) {
                        if (!criteria.evaluateCondition(measurementsCount)) {
                            return false;
                        }
                    }
                    return true;
                case BOC_CRITERIAS_OPERATOR_OR:
                    for (BadgeObtainingCriteria criteria: criterias) {
                        if (criteria.evaluateCondition(measurementsCount)) {
                            return true;
                        }
                    }
                    return false;
            }
        }
        return false;
    }

    protected Badge(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        category = in.readString();
        imageUrl = in.readString();
        operator = in.readString();
        if (in.readByte() == 0x01) {
            criterias = new ArrayList<BadgeObtainingCriteria>();
            in.readList(criterias, BadgeObtainingCriteria.class.getClassLoader());
        } else {
            criterias = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeString(imageUrl);
        dest.writeString(operator);
        if (criterias == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(criterias);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Badge> CREATOR = new Parcelable.Creator<Badge>() {
        @Override
        public Badge createFromParcel(Parcel in) {
            return new Badge(in);
        }

        @Override
        public Badge[] newArray(int size) {
            return new Badge[size];
        }
    };

    public String getFirstCriteria() {
        if (criterias != null) {
            for (BadgeObtainingCriteria criteria : criterias) {
                if (criteria != null) {
                    return criteria.value;
                }
            }
        }
        return null;
    }

}
