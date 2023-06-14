package at.specure.android.screens.tutorial.advanced;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.specure.opennettest.R;

import at.specure.android.configs.TermsAndConditionsConfig;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentA extends Fragment {


    private WebView webView;

    public FragmentA() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_a, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Context cx = getActivity().getApplicationContext();
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.fragment_a__title);
        webView = view.findViewById(R.id.fragment_a__webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
//            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        TermsAndConditionsConfig.showPP(webView, cx);


        String s = title.getText().toString();
        s = s + " " + getString(R.string.app_name);
        title.setText(s);

        Button buttonNeg = view.findViewById(R.id.button1);
        buttonNeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TutorialAdvancedActivity) getActivity()).moveToAnotherPageOrDone();
            }
        });

    }

}
