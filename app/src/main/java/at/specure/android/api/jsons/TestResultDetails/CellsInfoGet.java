package at.specure.android.api.jsons.TestResultDetails;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CellsInfoGet {

    public static final String OBJECT_NAME = "cells_info";

    @SerializedName("cells_info")
    ArrayList<CellInfoGet> cellsInfo;

}
