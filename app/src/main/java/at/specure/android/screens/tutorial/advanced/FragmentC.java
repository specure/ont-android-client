package at.specure.android.screens.tutorial.advanced;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import com.specure.opennettest.R;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import at.specure.android.screens.preferences.AdvertisingIdClientLoader;
import timber.log.Timber;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class FragmentC extends Fragment {


    private Switch adsCheckBox;
    private Loader<Boolean> optAddLoader;

    public FragmentC() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_c, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Context cx = getActivity().getApplicationContext();
        super.onViewCreated(view, savedInstanceState);
        adsCheckBox = view.findViewById(R.id.fragment_c__switch);

        adsCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String action = "com.google.android.gms.settings.ADS_PRIVACY";
                    Intent settings = new Intent(action);
                    startActivity(settings);
//                        startActivity(new Intent(Settings.ACTION_PRIVACY_SETTINGS));
                    return;
                } catch (ActivityNotFoundException e) {
//                        Toast.makeText(PreferenceFragment.this.getActivity(), R.string.not_able_to_open_gps_settings, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        Button buttonNeg = view.findViewById(R.id.button1);
        buttonNeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialAdvancedActivity) getActivity()).moveToAnotherPageOrDone();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        optAddLoader = getLoaderManager().initLoader(0, null, new FragmentC.PrivacyLoaderCallbacks(this));
        optAddLoader.forceLoad();
    }

    public class PrivacyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Boolean> {

        FragmentC fragment;

        public PrivacyLoaderCallbacks(FragmentC fragment) {
            this.fragment = fragment;
        }

        @Override
        public AdvertisingIdClientLoader onCreateLoader(int id, Bundle args) {
            return new AdvertisingIdClientLoader(FragmentC.this.getActivity());
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Boolean> loader, Boolean enabledAdvertisingInfo) {
            Timber.e("Loader finished: %s" , enabledAdvertisingInfo);
            if (adsCheckBox != null) {
                adsCheckBox.setChecked(enabledAdvertisingInfo);
                /*adsPersonalisation.getEditor().commit();
                ListAdapter adapter = v.getAdapter();
                if (adapter instanceof BaseAdapter) {
                    ((BaseAdapter)adapter).notifyDataSetChanged();
                }*/
                Timber.e("Loader finished: %s" , enabledAdvertisingInfo);
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Boolean> loader) {

        }

    }
}
