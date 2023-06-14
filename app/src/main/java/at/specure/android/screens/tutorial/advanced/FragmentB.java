package at.specure.android.screens.tutorial.advanced;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.specure.opennettest.R;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class FragmentB extends Fragment {


    public FragmentB() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_b, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Context cx = getActivity().getApplicationContext();
        super.onViewCreated(view, savedInstanceState);

        Button buttonNeg = view.findViewById(R.id.button1);
        buttonNeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialAdvancedActivity) getActivity()).moveToAnotherPageOrDone();
            }
        });
    }
}
