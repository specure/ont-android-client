package at.specure.android.screens.tutorial.advanced;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.specure.opennettest.R;

import at.specure.android.configs.PrivacyConfig;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class FragmentE extends Fragment {

    public FragmentE() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_e, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        View startApp = view.findViewById(R.id.start_button);
//        startApp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FragmentE.this.getActivity().finish();
//                InitialActivity.startActivity(null, FragmentE.this.getActivity());
//            }
//        });

        super.onViewCreated(view, savedInstanceState);

        Button buttonNeg = view.findViewById(R.id.button1);
        buttonNeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialAdvancedActivity) getActivity()).moveToAnotherPageOrDone();
            }
        });
        Button buttonPos = view.findViewById(R.id.button2);
        buttonPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrivacyConfig.setClientUUIDPersistent(getActivity().getApplicationContext(), true);
                ((TutorialAdvancedActivity) getActivity()).moveToAnotherPageOrDone();
            }
        });
    }
}
