package at.specure.android.screens.preferences;

import android.content.Context;

import androidx.loader.content.AsyncTaskLoader;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

import timber.log.Timber;

public class AdvertisingIdClientLoader extends AsyncTaskLoader<Boolean> {

    public AdvertisingIdClientLoader(Context context) {
        super(context);
    }

    @Override
    public Boolean loadInBackground() {
        AdvertisingIdClient.Info advertisingIdInfo = null;
        Timber.e("PRIVACY AD INFO: START");
        try {
            advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(this.getContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }

        if (advertisingIdInfo != null) {
            Timber.e("PRIVACY AD INFO: %s", advertisingIdInfo.isLimitAdTrackingEnabled());
            return advertisingIdInfo.isLimitAdTrackingEnabled();
        }

        return false;
    }
}