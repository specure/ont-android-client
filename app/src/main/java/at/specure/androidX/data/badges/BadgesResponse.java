package at.specure.androidX.data.badges;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BadgesResponse {

    @SerializedName("badges")
    ArrayList<Badge> badges;

}
