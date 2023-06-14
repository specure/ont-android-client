package at.specure.androidX.data.badges;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

import timber.log.Timber;

public class BadgeObtainingCriteria {

    public static final String BOC_CRITERIA_OPERATOR_TYPE_EQUAL = "eq";
    public static final String BOC_CRITERIA_OPERATOR_TYPE_NOT_EQUAL = "nq";
    public static final String BOC_CRITERIA_OPERATOR_TYPE_GRATER_EQUAL = "ge";
    public static final String BOC_CRITERIA_OPERATOR_TYPE_GRATER = "gt";
    public static final String BOC_CRITERIA_OPERATOR_TYPE_LOWER_EQUAL = "le";
    public static final String BOC_CRITERIA_OPERATOR_TYPE_LOWER = "lt";

    public static final String BOC_CRITERIA_TYPE_DATE = "date";
    public static final String BOC_CRITERIA_TYPE_MEASUREMENT_COUNT = "measurement";


    @SerializedName("type")
    String type;

    @SerializedName("operator")
    String operator;

    @SerializedName("value")
    String value;


    public String getDisplayCriteria() {
        String criteria = null;

        if (type != null && operator != null && value != null)
        switch (type) {
            case BOC_CRITERIA_TYPE_DATE:
                switch (operator) {
                    case BOC_CRITERIA_OPERATOR_TYPE_EQUAL:
                        return value;

                    case BOC_CRITERIA_OPERATOR_TYPE_NOT_EQUAL:
                        return "<> " + value;

                    case BOC_CRITERIA_OPERATOR_TYPE_GRATER_EQUAL:
                        return ">= " + value;

                    case BOC_CRITERIA_OPERATOR_TYPE_GRATER:
                        return "> " + value;

                    case BOC_CRITERIA_OPERATOR_TYPE_LOWER:
                        return "< " + value;

                    case BOC_CRITERIA_OPERATOR_TYPE_LOWER_EQUAL:
                        return "<= " + value;
                }

            case BOC_CRITERIA_TYPE_MEASUREMENT_COUNT:
                switch (operator) {
                    case BOC_CRITERIA_OPERATOR_TYPE_EQUAL:
                        return value;
                    case BOC_CRITERIA_OPERATOR_TYPE_NOT_EQUAL:
                        return " " + value;
                    case BOC_CRITERIA_OPERATOR_TYPE_GRATER_EQUAL:
                        return " " + value;
                    case BOC_CRITERIA_OPERATOR_TYPE_GRATER:
                        return " " + value;
                    case BOC_CRITERIA_OPERATOR_TYPE_LOWER:
                        return " " + value;
                    case BOC_CRITERIA_OPERATOR_TYPE_LOWER_EQUAL:
                        return " " + value;
                }
        }
        return criteria;
    }

    /**
     * @return true if criteria is fullfilled
     */
    public boolean evaluateCondition(int measurementCount) {

        switch (type) {
            case BOC_CRITERIA_TYPE_DATE:

                Calendar instance = Calendar.getInstance();
                int dayNow = instance.get(Calendar.DAY_OF_MONTH);
                int monthNow = instance.get(Calendar.MONTH) + 1; // because month is numbered from 0

                Timber.e("BADGE evaluating for %s", value);

                String[] splitted = value.split("\\.");
                int dayCriteria = Integer.parseInt(splitted[0]);
                int monthCriteria = Integer.parseInt(splitted[1]);

                switch (operator) {
                    case BOC_CRITERIA_OPERATOR_TYPE_EQUAL:
                        if (dayNow == dayCriteria && monthNow == monthCriteria) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_NOT_EQUAL:
                        if (dayNow != dayCriteria || monthNow != monthCriteria) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_GRATER_EQUAL:
                        if ((dayNow >= dayCriteria && monthNow >= monthCriteria)
                                || (dayNow < dayCriteria && monthNow > monthCriteria)) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_GRATER:
                        if ((dayNow > dayCriteria && monthNow >= monthCriteria)
                                || (dayNow <= dayCriteria && monthNow > monthCriteria)) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_LOWER:
                        if ((dayNow < dayCriteria && monthNow <= monthCriteria)
                                || (dayNow >= dayCriteria && monthNow < monthCriteria)) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_LOWER_EQUAL:
                        if ((dayNow <= dayCriteria && monthNow <= monthCriteria)
                                || (dayNow > dayCriteria && monthNow < monthCriteria)) {
                            return true;
                        } else {
                            return false;
                        }
                }

                break;

            case BOC_CRITERIA_TYPE_MEASUREMENT_COUNT:

                int measurementCriteria = Integer.parseInt(value);
                switch (operator) {
                    case BOC_CRITERIA_OPERATOR_TYPE_EQUAL:
                        if (measurementCount == measurementCriteria) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_NOT_EQUAL:
                        if (measurementCount != measurementCriteria) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_GRATER_EQUAL:
                        if (measurementCount >= measurementCriteria) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_GRATER:
                        if (measurementCount > measurementCriteria) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_LOWER:
                        if (measurementCount < measurementCriteria) {
                            return true;
                        } else {
                            return false;
                        }
                    case BOC_CRITERIA_OPERATOR_TYPE_LOWER_EQUAL:
                        if (measurementCount <= measurementCriteria) {
                            return true;
                        } else {
                            return false;
                        }
                }
                break;

        }
        return false;
    }
}
