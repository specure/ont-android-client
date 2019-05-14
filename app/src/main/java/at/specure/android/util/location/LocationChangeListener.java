package at.specure.android.util.location;

import android.location.Location;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public interface LocationChangeListener {

    /**
     * @param location current location got from one of the provider
     * @param geodecodedLocation human readable form of location
     */
    void onLocationChange(@Nullable Location location, @Nullable String geodecodedLocation, @NonNull boolean enabledGPS);
}
