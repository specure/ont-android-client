package at.specure.android.configs;

import static at.specure.android.configs.ConfigHelper.getSharedPreferences;
import static at.specure.androidX.data.badges.Badge.BADGE_CATEGORY_MEASUREMENT;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.appcompat.app.AlertDialog;

import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import at.specure.android.screens.badges.newbadges.BadgeListActivity;

/**
 * Class to handle badges
 * Created by michal.cadrik on 2/1/2018.
 */

public class BadgesConfig {


    public static final int BADGES_SHOW_MODE_ALL = 0;
    public static final int BADGES_SHOW_MODE_ONLY_GAINED = 1;


    public static int BADGES_SHOW_MODE = BADGES_SHOW_MODE_ONLY_GAINED;


    private static final String BADGES_RECEIVED_MAP_OLD = "badges_received_map";
    private static final String BADGES_RECEIVED_MAP = "badges_received_map_v2";


    public static boolean isBadgeReceived(at.specure.androidX.data.badges.Badge badge, Context context) {
        if (context != null) {
            ArrayList<Badge> receivedBadges = getReceivedBadges(context);
            for (Badge receivedBadge : receivedBadges) {
                if (receivedBadge.getId() == Integer.parseInt(badge.id)) {
                    return true;
                }
                //TODO: add check for old ids.... if left is 8 and right is 0 it is true also for ROOKIE BADGE and ...
            }
        }
        return false;
    }

    /**
     * Use this after measurement is done
     * @param context
     * @param badges
     * @return
     */
    public static boolean checkForGettingBadge(final Context context, List<at.specure.androidX.data.badges.Badge> badges) {
        return checkForGettingBadge(context, badges, true, false);
    }

    /**
     * Use this with @measurementCountCheckOnly set to true to open badge and gain it
     * @param context
     * @param badges
     * @param showDialog
     * @param measurementCountCheckOnly
     * @return
     */
    public static boolean checkForGettingBadge(final Context context, List<at.specure.androidX.data.badges.Badge> badges, boolean showDialog, boolean measurementCountCheckOnly) {
        boolean received = false;
        String badgeId = null;
        if (context != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            ArrayList<Badge> receivedBadges = getReceivedBadges(context);

            if (badges != null) {
                for (at.specure.androidX.data.badges.Badge badgeToCheck : badges) {
                    if (!isBadgeReceived(badgeToCheck, context)) {
                        boolean gained = badgeToCheck.evaluate(ConfigHelper.getTestCounter(context));
                        if (measurementCountCheckOnly) {
                            if (gained && badgeToCheck.category.equalsIgnoreCase(BADGE_CATEGORY_MEASUREMENT)) {
                                setBadgeReceived(context, badgeToCheck);
                                received = true;
                                badgeId = badgeToCheck.id;
                            }
                        } else {
                            if (gained) {
                                setBadgeReceived(context, badgeToCheck);
                                received = true;
                                badgeId = badgeToCheck.id;
                            }
                        }
                    }
                }
            }
            if ((received) && (showDialog)) {
                final String finalBadgeId = badgeId;
                AlertDialog alert = new AlertDialog.Builder(context).
                        setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
//                                Intent intent = new Intent(context, BadgesActivity.class);
                                Intent intent = new Intent(context, BadgeListActivity.class);
//                                intent.putExtra(BadgesActivity.BADGE_BUNDLE_KEY, new Badge(finalBadgeId, System.currentTimeMillis()));
                                intent.putExtra(BadgeListActivity.BADGE_BUNDLE_KEY, finalBadgeId);
                                context.startActivity(intent);
                            }
                        }).
                        setMessage(R.string.badges_earned_message).
                        setCancelable(false).
                        create();

                alert.show();
                return true;
            }
            if (received) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all received badges (id and timestamp of getting it)
     * @param context
     * @return
     */
    public static ArrayList<Badge> getReceivedBadgesOld(Context context) {
        Set<String> receivedBadgesIds;
        ArrayList<Badge> badges = new ArrayList<>();
        if (context != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            receivedBadgesIds = sharedPreferences.getStringSet(BADGES_RECEIVED_MAP_OLD, new HashSet<String>());
            Iterator<String> iterator = receivedBadgesIds.iterator();
            while (iterator.hasNext()) {
                badges.add(new Badge(iterator.next()));
            }
            return badges;
        }
        return badges;
    }

    /**
     * Get all received badges (id and timestamp of getting it)
     * @param context
     * @return
     */
    public static ArrayList<Badge> getReceivedBadges(Context context) {
        Set<String> receivedBadgesIds;
        ArrayList<Badge> badges = new ArrayList<>();
        if (context != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            receivedBadgesIds = sharedPreferences.getStringSet(BADGES_RECEIVED_MAP, new HashSet<String>());
            Iterator<String> iterator = receivedBadgesIds.iterator();
            while (iterator.hasNext()) {
                badges.add(new Badge(iterator.next()));
            }
            return badges;
        }
        return badges;
    }


    public static void setAllBadgesReceived(Context context, List<at.specure.androidX.data.badges.Badge> badges) {
        if (badges != null) {
            for (at.specure.androidX.data.badges.Badge badge: badges) {
                setBadgeReceived(context, badge);
            }
        }
    }
    /**
     * Save badge id to persistent storage and make it as received with current timestamp
     * @param context
     * @param badge
     */
    private static boolean setBadgeReceived(Context context, at.specure.androidX.data.badges.Badge badge) {
        if (context != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            Set<String> receivedBadgesIds = sharedPreferences.getStringSet(BADGES_RECEIVED_MAP, new HashSet<String>());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            receivedBadgesIds.add(badge.id + ";" + System.currentTimeMillis());
            editor.putStringSet(BADGES_RECEIVED_MAP, receivedBadgesIds);
            return editor.commit();
        }
        return false;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static boolean isBadgesFeatureEnabled(Context context) {
        if (context != null) {
            boolean enabledAppReview = context.getResources().getBoolean(R.bool.enabled_badges);
            return enabledAppReview;
        }
        return false;
    }

    public static long getReceivedDateOfBadge(at.specure.androidX.data.badges.Badge badge, Context context) {
        int badgeID = Integer.parseInt(badge.id);
        ArrayList<Badge> receivedBadges = getReceivedBadges(context);
        for (Badge receivedBadge : receivedBadges) {
            if (receivedBadge.getId() == badgeID) {
                return receivedBadge.getTimestampOfGet();
            }
        }
        return 0L;
    }



    /**
     * Class to load badges from shared preferences with all parsed information
     */
    public static class Badge implements Parcelable {

        int id;
        long timestampOfGet;

        public Badge(int id, long timestampOfGet) {
            this.id = id;
            this.timestampOfGet = timestampOfGet;
        }

        public Badge(String preferenceSaved) {
            String[] split = preferenceSaved.split(";");
            try {
                id = Integer.parseInt(split[0]);
                timestampOfGet = Long.parseLong(split[1]);
            } catch (Exception ignored) {
                //do nothing with parse exception
            }
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public long getTimestampOfGet() {
            return timestampOfGet;
        }

        public void setTimestampOfGet(long timestampOfGet) {
            this.timestampOfGet = timestampOfGet;
        }

        protected Badge(Parcel in) {
            id = in.readInt();
            timestampOfGet = in.readLong();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeLong(timestampOfGet);
        }

        @SuppressWarnings("unused")
        public static final Creator<Badge> CREATOR = new Creator<Badge>() {
            @Override
            public Badge createFromParcel(Parcel in) {
                return new Badge(in);
            }

            @Override
            public Badge[] newArray(int size) {
                return new Badge[size];
            }
        };
    }
}
