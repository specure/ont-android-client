package at.specure.androidX.data.badges;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BadgeObtainingCriterias {

    @SerializedName("terms_operator")
    String operator;

    @SerializedName("criteria")
    ArrayList<BadgeObtainingCriteria> criterias;


}
