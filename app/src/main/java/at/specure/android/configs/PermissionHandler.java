package at.specure.android.configs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.specure.opennettest.R;

import at.specure.android.util.location.RequestGPSPermissionInterface;

/**
 * Handling app permissions
 * Created by michal.cadrik on 27-Feb-18.
 */

public class PermissionHandler {

    public static boolean isCoarseLocationPermitted(Context context) {

        boolean accessToLocationGranted = false;
        if (context != null) {
            accessToLocationGranted = (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        }

        return accessToLocationGranted;
    }

//    public static boolean isSomeLocationPermitted(Context context) {
//
//        boolean accessToLocationGranted = false;
//
//        if (context != null) {
//            accessToLocationGranted = !(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
//        }
//
//        return accessToLocationGranted;
//    }

    public static void showLocationExplanationDialog(final Activity activity, final int requestCodeFine, final RequestGPSPermissionInterface requestGPSPermission) {
        if (activity != null) {
            AlertDialog alert = new AlertDialog.Builder(activity).
                    setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @SuppressLint("NewApi")
                        public void onClick(DialogInterface dialog, int which) {
                            if (((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) && !activity.isDestroyed()) && !activity.isFinishing()) {
                                if (requestGPSPermission != null)
                                requestGPSPermission.requestPermission(requestCodeFine);
                            }
                        }
                    }).
                    setMessage(R.string.permission_explanation_location).
                    setCancelable(false).
                    create();

            alert.show();
        }
    }

}
