package at.specure.android.util.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import at.specure.android.api.calls.GetGeolocationTask;
import at.specure.android.configs.PermissionHandler;
import at.specure.android.util.EndStringTaskListener;
import timber.log.Timber;


/**
 * First instance should be created in app class
 */
public class GeoLocationX implements EndStringTaskListener {

    Context applicationContext;
    private static final long TIME_DELTA_THRESHOLD_MS = 20000; // we accept 20s old information
    private static final long TIME_DELAY_BETWEEN_UPDATES_MS = 5000;
    private static final long TIME_DELAY_ACCEPTABLE_BETTER_PRECISION_MS = 5000;
    private static final long ACCURACY_ACCEPTABLE_BETTER_PRECISION_M = 5;
    private static final float DISTANCE_BETWEEN_UPDATES_M = 5f;
    private static final float DISTANCE_BETWEEN_HUMAN_READABLE_FORM_UPDATES_M = 200f;
    private static final float TIME_DELAY_BETWEEN_HUMAN_READABLE_FORM_UPDATES_MAX_MS = 600000; // 10 min (new request will be fired after this time if location has no big change) (because of slow movement in urban areas on the feet)
    private static final float TIME_DELAY_BETWEEN_HUMAN_READABLE_FORM_UPDATES_MIN_MS = 20000; // 20 s (new request will be fired after this time only) (because of fast movement on the highways)

    private static final float ACCURACY_ACCEPTED = 100f;


    static GeoLocationX instance;
    ConcurrentLinkedQueue<LocationChangeListener> listeners;
    private static ConcurrentLinkedQueue<Location> savedlocations; // stores locations only if flag #recordLocationChanges is set to true (necessary during measurements)
    private static ConcurrentLinkedQueue<Location> locationBuffer; // store last few locations to make computing of distance more precise
    static AtomicBoolean recordLocationChanges = new AtomicBoolean(false);

    private AtomicReference<Location> locationNetwork = new AtomicReference<>();
    private AtomicReference<Location> locationGPS = new AtomicReference<>();
    private AtomicReference<Location> locationFused = new AtomicReference<>();

    private AtomicReference<Location> lastDecodedLocation = new AtomicReference<>();
    private AtomicReference<Location> locationToBeDecoded = new AtomicReference<>();
    private AtomicReference<String> locationDecoded = new AtomicReference<>();
    private AtomicReference<Location> lastNotifiedLocation = new AtomicReference<>();
    private AtomicBoolean newLocationDecoded = new AtomicBoolean(false);
    private AtomicBoolean changedAdapterState = new AtomicBoolean(false);
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest locationRequest;

    public String getDecodedPosition() {
        Location bestLocation = getBestLocation(true);
        if (bestLocation != null) {
            if (lastDecodedLocation.get() != null) {

                Float distance = countDistance(bestLocation, lastDecodedLocation.get());
                long timeDifference = Math.abs(lastDecodedLocation.get().getTime() - System.currentTimeMillis());

                if ((timeDifference > TIME_DELAY_BETWEEN_HUMAN_READABLE_FORM_UPDATES_MAX_MS)
                        || distance == null
                        || (distance != null && distance > DISTANCE_BETWEEN_HUMAN_READABLE_FORM_UPDATES_M && timeDifference > TIME_DELAY_BETWEEN_HUMAN_READABLE_FORM_UPDATES_MIN_MS)) {
                    GetGeolocationTask decodeLocationTask = new GetGeolocationTask(bestLocation.getLatitude(), bestLocation.getLongitude());
                    decodeLocationTask.setEndTaskListener(this);
                    locationToBeDecoded.set(bestLocation);
                    decodeLocationTask.execute();
                }

            } else {
                GetGeolocationTask decodeLocationTask = new GetGeolocationTask(bestLocation.getLatitude(), bestLocation.getLongitude());
                decodeLocationTask.setEndTaskListener(this);
                locationToBeDecoded.set(bestLocation);
                decodeLocationTask.execute();
            }
        }
        return locationDecoded.get();
    }


    LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Timber.d("LOCATION: GPS: %s", location);
            locationGPS.set(location);
            if (recordLocationChanges.get()) {
                addLocationToList(location);
            }
            notifyListeners(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            changedAdapterState.set(true);
            notifyListeners(getBestLocation(true));
        }

        @Override
        public void onProviderEnabled(String provider) {
            changedAdapterState.set(true);
            notifyListeners(getBestLocation(true));
        }

        @Override
        public void onProviderDisabled(String provider) {
            changedAdapterState.set(true);
            notifyListeners(getBestLocation(true));
        }
    };

    LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Timber.d("LOCATION: NETWORK: %s", location);
            locationNetwork.set(location);
            if (recordLocationChanges.get()) {
                addLocationToList(location);
            }
            notifyListeners(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            changedAdapterState.set(true);
            notifyListeners(getBestLocation(true));
        }

        @Override
        public void onProviderEnabled(String provider) {
            changedAdapterState.set(true);
            notifyListeners(getBestLocation(true));
        }

        @Override
        public void onProviderDisabled(String provider) {
            changedAdapterState.set(true);
            notifyListeners(getBestLocation(true));
        }
    };

    private void notifyListeners(Location location) {

        getDecodedPosition();

        if (location != null) {
            if ((lastDecodedLocation.get() == null || newLocationDecoded.get())
                    ||
                    (lastNotifiedLocation.get() == null || location.getLatitude() != lastNotifiedLocation.get().getLatitude() ||
                            (location.getLongitude() != lastNotifiedLocation.get().getLongitude()))
                    || changedAdapterState.get()) {

                if (listeners != null) {
                    boolean geolocationEnabled = isGeolocationEnabled(applicationContext);
                    for (LocationChangeListener listener : listeners) {
                        if (listener != null) {
                            listener.onLocationChange(location, locationDecoded.get(), geolocationEnabled);
                        }
                    }
                }
                changedAdapterState.set(false);
                newLocationDecoded.set(false);
                lastNotifiedLocation.set(location);
            }
        } else {
            if (listeners != null) {
                boolean geolocationEnabled = isGeolocationEnabled(applicationContext);
                for (LocationChangeListener listener : listeners) {
                    if (listener != null) {
                        listener.onLocationChange(null, "", geolocationEnabled);
                    }
                }
            }
        }
    }

    LocationCallback fusedLocationListener = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            for (Location location : locationResult.getLocations()) {
                if (location.getAccuracy() < 500) {
                    locationFused.set(location);
                    Timber.e("LOCATION: FUSED: %s", location);
                    if (recordLocationChanges.get()) {
                        addLocationToList(locationFused.get());
                    }
                    notifyListeners(location);
                    return;
                }
            }
        }
    };

    private void addLocationToList(Location location) {
        if (location != null) {
            if (savedlocations == null) {
                savedlocations = new ConcurrentLinkedQueue<>();
            }
            savedlocations.add(location);
        }
    }

    public static ConcurrentLinkedQueue<Location> getSavedlocations() {
        return savedlocations;
    }


    private GeoLocationX() {
    }

    public static GeoLocationX getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new GeoLocationX();
            instance.startListeningForLocationChanges(applicationContext);
        }
        instance.applicationContext = applicationContext;

        return instance;
    }

    private void startListeningForLocationChanges(Context context) {
        if (locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(TIME_DELAY_BETWEEN_UPDATES_MS);
            locationRequest.setFastestInterval(TIME_DELAY_BETWEEN_UPDATES_MS);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            startForce(context);
            getLastKnownLocation(context, null);
        }
    }

    private void startForce(Context context) {
        if ((context != null)) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationProviderClient.removeLocationUpdates(fusedLocationListener);
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, fusedLocationListener, Looper.myLooper());
        }
    }


    public void addListener(LocationChangeListener listener) {
        if (listeners == null) {
            listeners = new ConcurrentLinkedQueue<>();
        }

        if (listener == null) {
            return;
        }

        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(LocationChangeListener listener) {
        if (listeners == null) {
            return;
        }
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    public void removeAllListener() {
        if (listeners == null) {
            return;
        }
        listeners.clear();
    }

    private Location getBestLocation(boolean inAcceptableTimeRange) {

        ArrayList<Location> lastLocations = new ArrayList<>();
        if (locationFused.get() != null) {
            lastLocations.add(locationFused.get());
        }
        if (locationGPS.get() != null) {
            lastLocations.add(locationGPS.get());
        }
        if (locationNetwork.get() != null) {
            lastLocations.add(locationNetwork.get());
        }

        if (lastLocations.isEmpty()) {
            return null;
        }

        if (lastLocations.size() == 1) {
            return lastLocations.get(0);
        }

        Collections.sort(lastLocations, new Comparator<Location>() {
            @Override
            public int compare(Location o1, Location o2) {
                if (o1.getTime() < o2.getTime()) {
                    return 1;
                } else if (o1.getTime() < o2.getTime()) {
                    return -1;
                }
                return 0;
            }
        });

        long newestTime = lastLocations.get(0).getTime();
        float newestaccuracy = lastLocations.get(0).getAccuracy();
        Location locationToReturn = lastLocations.get(0);

        for (int i = 0; i < lastLocations.size(); i++) {
            if (newestTime - TIME_DELAY_ACCEPTABLE_BETTER_PRECISION_MS < lastLocations.get(i).getTime()) {
                if (newestaccuracy - ACCURACY_ACCEPTABLE_BETTER_PRECISION_M > lastLocations.get(i).getAccuracy()) {
                    locationToReturn = lastLocations.get(i);
                }
            } else {
                if (inAcceptableTimeRange) {
                    long bestLocationTime = locationToReturn.getTime();
                    long currentTimeMillis = System.currentTimeMillis();
                    long delta = currentTimeMillis - bestLocationTime;
                    if (delta <= TIME_DELTA_THRESHOLD_MS) {
                        return locationToReturn;
                    }
                    return null;
                } else {
                    return locationToReturn;
                }
            }
        }

        if (inAcceptableTimeRange) {
            long bestLocationTime = locationToReturn.getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long delta = currentTimeMillis - bestLocationTime;
            if (delta <= TIME_DELTA_THRESHOLD_MS) {
                return locationToReturn;
            }
            return null;
        } else {
            return locationToReturn;
        }
    }

    /**
     * Returns last known location of the device, but if it is too old, then returns null
     * too old means that time delta between current time and location time is more than
     *
     * @param context
     * @param listener
     * @return
     */
    public Location getLastKnownLocation(Context context, LocationChangeListener listener) {

        AtomicReference<Location> bestLocation = new AtomicReference<>();

        if (context != null) {

            AtomicReference<Location> locationGPSTemp = new AtomicReference<>();
            AtomicReference<Location> locationNetworkTemp = new AtomicReference<>();
            AtomicReference<Location> locationFusedTemp = new AtomicReference<>();

            locationGPSTemp.set(getLastKnownLocationGPS(context));
            locationNetworkTemp.set(getLastKnownLocationNetwork(context));
            locationFusedTemp.set(getLastKnownLocationFused(context));
            if (locationGPSTemp.get() != null) {
                locationGPS.set(locationGPSTemp.get());
            }
            if (locationNetworkTemp.get() != null) {
                locationNetwork.set(locationNetworkTemp.get());
            }
            if (locationFusedTemp.get() != null) {
                locationFused.set(locationFusedTemp.get());
            }
        }
        if (isGeolocationEnabled(applicationContext)) {
            bestLocation.set(getBestLocation(true));
        }
        if (listener != null) {
            addListener(listener);
        }

        if (bestLocation.get() != null) {
            long bestLocationTime = bestLocation.get().getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long delta = currentTimeMillis - bestLocationTime;
            if (delta <= TIME_DELTA_THRESHOLD_MS) {
                return bestLocation.get();
            }
        }

        return null;
    }

    /**
     * Returns last known location of the device no matter how old it is
     *
     * @param context
     * @param listener
     * @return
     */
    public Location getLastKnownLocationForced(Context context, LocationChangeListener listener) {

        AtomicReference<Location> bestLocation = new AtomicReference<>();
        if (context != null) {

            AtomicReference<Location> locationGPSTemp = new AtomicReference<>();
            AtomicReference<Location> locationNetworkTemp = new AtomicReference<>();
            AtomicReference<Location> locationFusedTemp = new AtomicReference<>();

            locationGPSTemp.set(getLastKnownLocationGPS(context));
            locationNetworkTemp.set(getLastKnownLocationNetwork(context));
            locationFusedTemp.set(getLastKnownLocationFused(context));

            if (locationGPSTemp != null) {
                locationGPS = locationGPSTemp;
            }
            if (locationNetworkTemp != null) {
                locationNetwork = locationNetworkTemp;
            }
            if (locationFusedTemp != null) {
                locationFused = locationFusedTemp;
            }
        }
        if (isGeolocationEnabled(applicationContext)) {
            bestLocation.set(getBestLocation(false));
        }
        if (listener != null) {
            addListener(listener);
        }

        if (bestLocation.get() != null) {
            long bestLocationTime = bestLocation.get().getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long delta = currentTimeMillis - bestLocationTime;
            if (delta <= TIME_DELTA_THRESHOLD_MS) {
                return bestLocation.get();
            }
        }
        return null;

    }

    @SuppressLint("MissingPermission")
    private Location getLastKnownLocationGPS(Context context) {
        if (context != null) {
            Looper mainLooper = Looper.getMainLooper();
            try {
                mainLooper.prepare();
            } catch (RuntimeException ignored) {
                //java.lang.RuntimeException: Only one Looper may be created per thread
            }
            AtomicReference<LocationManager> location_manager = new AtomicReference<>();

            location_manager.set((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
            Location location = null;
            if (location_manager.get() != null) {
                boolean gpsLocationEnabled = location_manager.get().isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (PermissionHandler.isCoarseLocationPermitted(context)) {
                    if (location_manager.get().getAllProviders().contains(LocationManager.GPS_PROVIDER) && gpsLocationEnabled) {
                        location_manager.get().requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, TIME_DELAY_BETWEEN_UPDATES_MS, DISTANCE_BETWEEN_UPDATES_M, gpsLocationListener);
                        location = location_manager.get().getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                } else {
                    return null;
                }
            }
            return location;
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    private Location getLastKnownLocationNetwork(Context context) {
        if (context != null) {
            Looper mainLooper = Looper.getMainLooper();
            try {
                mainLooper.prepare();
            } catch (RuntimeException ignored) {
                //java.lang.RuntimeException: Only one Looper may be created per thread
            }
            AtomicReference<LocationManager> location_manager = new AtomicReference<>();

            location_manager.set((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
            Location location = null;

            if (location_manager.get() != null) {
                boolean networkLocationEnabled = location_manager.get().isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (PermissionHandler.isCoarseLocationPermitted(context)) {

                    if (location_manager.get().getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && networkLocationEnabled) {
                        if ((PermissionHandler.isCoarseLocationPermitted(context))) {
                            location_manager.get().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_DELAY_BETWEEN_UPDATES_MS, DISTANCE_BETWEEN_UPDATES_M, networkLocationListener);
                        }
                        location = location_manager.get().getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                } else {
                    return location;
                }
            }
            return location;
        }
        return null;
    }

    private Location getLastKnownLocationFused(Context context) {
        if (context != null) {
            if (mFusedLocationProviderClient == null) {
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            }

            @SuppressLint("MissingPermission") final Task<Location> lastLocation = mFusedLocationProviderClient.getLastLocation();

            lastLocation.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location result = task.getResult();
                        if (result != null) {
                            locationFused.set(result);
                        }
                    }
                }
            });
        }
        return null;
    }

    public boolean isGeolocationEnabled(Context context) {
        if (context != null) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);// || locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
            } else {
                return false;
            }
        }
        return false;
    }


    public static void openGeolocationSettings(Activity context, boolean showDialog) {
        if (showDialog) {
            showDialogToOpenGPSSettings(context, context.getString(R.string.loop_mode_open_gps_settings));
        } else {
            openGPSSettings(context);
        }
    }

    private static void openGPSSettings(Activity activity) {
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        activity.startActivity(new Intent(action));
    }

    private static void showDialogToOpenGPSSettings(Activity activity, String dialogText) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final String message = dialogText;

        builder.setMessage(message)
                .setPositiveButton(R.string._ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                openGPSSettings(activity);
                                d.dismiss();
                            }
                        })
                .setNegativeButton(R.string._cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }


    /**
     * @param lastLocation     first location
     * @param lastTestLocation second location
     * @return distance in meters or null if one of the locations or both are null
     */
    public Float countDistance(Location lastLocation, Location lastTestLocation) {
        Float distance = null;
        if ((lastLocation != null) && (lastTestLocation != null)) {
            distance = lastLocation.distanceTo(lastTestLocation);
        }
        return distance;
    }

    @Override
    public void taskEnded(String result) {
        locationDecoded.set(result);
        lastDecodedLocation = locationToBeDecoded;
        newLocationDecoded.set(true);
    }


    public ArrayList<at.specure.android.api.jsons.Location> getTestResultLocations() {
        ArrayList<at.specure.android.api.jsons.Location> itemList = null;
        ConcurrentLinkedQueue<Location> savedlocations = getSavedlocations();

        if (savedlocations.size() > 0) {
            itemList = new ArrayList<>();

            for (Location savedlocation : savedlocations) {
                at.specure.android.api.jsons.Location tmpItem = toGeoLocationItem(savedlocation);
                itemList.add(tmpItem);
            }
        }
        return itemList;
    }


    /**
     * Hostorical context - may be deleted after TestClient class will be modified
     *
     * @return
     */
    public ArrayList<String> getCurLocationForTest() {
        Location bestLocation = getBestLocation(true);
        if (bestLocation != null) {
            at.specure.android.api.jsons.Location curLocation = toGeoLocationItem(bestLocation);
            final ArrayList<String> geoInfo = new ArrayList<String>(Arrays.asList(String.valueOf(curLocation.getTimestamp()),
                    String.valueOf(curLocation.getLatitude()), String.valueOf(curLocation.getLongitude()),
                    String.valueOf(curLocation.getAccuracy()), String.valueOf(curLocation.getAltitude()),
                    String.valueOf(curLocation.getBearing()), String.valueOf(curLocation.getSpeed()), curLocation.getProvider()));

            return geoInfo;
        } else
            return null;
    }

    public static at.specure.android.api.jsons.Location toGeoLocationItem(Location curLocation) {
        return new at.specure.android.api.jsons.Location(
                curLocation.getTime(),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? curLocation.getElapsedRealtimeNanos() : null,
                curLocation.getLatitude(),
                curLocation.getLongitude(),
                (double) curLocation.getAccuracy(),
                curLocation.getAltitude(),
                (double) curLocation.getBearing(),
                (double) curLocation.getSpeed(),
                curLocation.getProvider());
    }

    public void startRecordingPositions() {
        recordLocationChanges.set(true);
        Location bestLocation = getBestLocation(true);
        if (savedlocations != null) {
            savedlocations.clear();
        } else {
            savedlocations = new ConcurrentLinkedQueue<>();
        }
        if (bestLocation != null)
            savedlocations.add(bestLocation);
    }

    public void stopRecordingPositions() {
        recordLocationChanges.set(false);
    }
}
