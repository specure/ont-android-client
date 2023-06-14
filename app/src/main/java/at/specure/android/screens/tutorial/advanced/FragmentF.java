package at.specure.android.screens.tutorial.advanced;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.specure.opennettest.R;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class FragmentF extends Fragment {

    public FragmentF() {
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
    }
}
