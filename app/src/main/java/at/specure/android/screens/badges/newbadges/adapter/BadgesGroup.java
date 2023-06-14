package at.specure.android.screens.badges.newbadges.adapter;

import static at.specure.androidX.data.badges.Badge.BADGE_CATEGORY_HOLIDAY;
import static at.specure.androidX.data.badges.Badge.BADGE_CATEGORY_MEASUREMENT;

import android.content.Context;

import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.specure.androidX.data.badges.Badge;
import at.specure.util.tools.expandablerecyclerview.models.ExpandableGroup;

public class BadgesGroup extends ExpandableGroup<Badge> {

    public static List<BadgesGroup> convertFromBadge(List<Badge> badges, Context context) {
        ArrayList<BadgesGroup> badgesGroups = new ArrayList<>();

        ArrayList<Badge> measurement = new ArrayList<>();
        ArrayList<Badge> others = new ArrayList<>();
        ArrayList<Badge> date = new ArrayList<>();

        if (badges != null) {
            for (Badge badge : badges) {
                switch (badge.category) {
                    case BADGE_CATEGORY_MEASUREMENT:
                        measurement.add(badge);
                        break;
                    case BADGE_CATEGORY_HOLIDAY:
                        date.add(badge);
                        break;
                    default:
                        others.add(badge);
                        break;
                }
            }
            Collections.sort(measurement, new Comparator<Badge>() {
                @Override
                public int compare(Badge o1, Badge o2) {
                    int first = Integer.parseInt(o1.getFirstCriteria());
                    int second = Integer.parseInt(o2.getFirstCriteria());
                    return Integer.compare(first, second);
                }
            });

            Collections.sort(date, new Comparator<Badge>() {
                @Override
                public int compare(Badge o1, Badge o2) {
                    String[] daymonth1 = o1.getFirstCriteria().split("\\.");
                    String[] daymonth2 = o2.getFirstCriteria().split("\\.");

                    if ((daymonth1[0].compareTo(daymonth2[0]) == 0)
                        && (daymonth1[1].compareTo(daymonth2[1]) == 0)) {
                        return 0;
                    } else if ((daymonth1[1].compareTo(daymonth2[1]) > 0)
                        || ((daymonth1[0].compareTo(daymonth2[0]) >= 0)
                            && (daymonth1[1].compareTo(daymonth2[1]) == 0))) {
                        return 1;
                    } else return -1;
                }
            });
            if (measurement.size() > 0) {
                badgesGroups.add(new BadgesGroup(context.getString(R.string.badges_category_measurements), measurement));
            }

            ArrayList<Badge> dateSorted = new ArrayList<>();

            Calendar instance = Calendar.getInstance();
            int month = instance.get(Calendar.MONTH) + 1;
            int day = instance.get(Calendar.DAY_OF_MONTH);


            int index = 0;
            for (Badge badge : date) {
                String[] split = badge.getFirstCriteria().split("\\.");
                if (((month < Integer.parseInt(split[1])))
                    || (month == Integer.parseInt(split[1]) && day <= Integer.parseInt(split[0]))) {
                    dateSorted.add(index++, badge);
                } else {
                    dateSorted.add(badge);
                }

            }
            if (dateSorted.size() > 0) {
                badgesGroups.add(new BadgesGroup(context.getString(R.string.badges_category_holidays), dateSorted));
            }

            if (others.size() > 0) {
                badgesGroups.add(new BadgesGroup(context.getString(R.string.other), others));
            }
        }
        return badgesGroups;
    }

    public BadgesGroup(String title, List<Badge> items) {
        super(title, items);
    }
}
