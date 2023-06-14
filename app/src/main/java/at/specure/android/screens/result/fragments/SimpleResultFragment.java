package at.specure.android.screens.result.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.google.gson.JsonArray;
import com.specure.opennettest.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import at.specure.android.api.jsons.TestResult.TestResult;
import at.specure.android.api.jsons.TestResultDetailOpenData.OpenDataJson;
import at.specure.android.base.BaseFragment;
import at.specure.android.screens.main.MainActivity;
import at.specure.client.v2.task.result.QoSServerResultCollection;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SimpleResultFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SimpleResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SimpleResultFragment extends BaseFragment implements SimpleResultController.ResultLoaderInterface {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TEST_UUID = "test_uuid_param";
    private static final String DEBUG_TAG = "SIMPLE_RESULTS_FRAG";

    // TODO: Rename and change types of parameters
    private String mTestUUID;

    private OnFragmentInteractionListener mListener;
    private SimpleResultController controller;
    private LayoutInflater inflater;
    private String openTestUuid;

    public SimpleResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment SimpleResultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SimpleResultFragment newInstance(String param1) {
        SimpleResultFragment fragment = new SimpleResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEST_UUID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTestUUID = getArguments().getString(ARG_TEST_UUID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        return inflater.inflate(R.layout.simple_result_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.page_title_main_result);
        else return "";
    }

    @Override
    public void setActionBarItems(Context context) {
        System.out.println("SET ACTIONBAR ITEMS");
        if (isAdded()) {
            ((MainActivity) getActivity()).setVisibleMenuItems(R.id.action_menu_help, R.id.action_menu_share);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        Button results = getView().findViewById(R.id.button_results);
        results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isAdded() && getActivity() != null) {
//                    ((MainActivity) getActivity()).showDetailedResultsAfterTest(mTestUUID);
                }
            }
        });
        if (controller == null) {
            controller = new SimpleResultController(this);
        }

        if (isAdded() && getActivity() != null) {
            controller.loadTestResults(mTestUUID, getActivity());
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onGraphDataLoaded(OpenDataJson openDataJson) {
        if (this.isAdded() && this.getActivity() != null) {
            if (inflater != null) {
                ViewGroup containerDown = getView().findViewById(R.id.result_download_square);
                ViewGroup containerUp = getView().findViewById(R.id.result_upload_square);

                LineChart chartDown = containerDown.findViewById(R.id.result_square__small_graph);
                View chartDownContainer = containerDown.findViewById(R.id.result_square__small_graph_container);
                LineChart chartUp = containerUp.findViewById(R.id.result_square__small_graph);
                View chartUpContainer = containerUp.findViewById(R.id.result_square__small_graph_container);

                ResultGraphHandler.fillResultGraph(chartDown, openDataJson.getSpeedCurve().getDownloadGraphItems(), this.getActivity());
                ResultGraphHandler.fillResultGraph(chartUp, openDataJson.getSpeedCurve().getUploadGraphItems(), this.getActivity());

                chartDownContainer.setVisibility(View.VISIBLE);
                chartDown.setViewPortOffsets(0, 0, 0, 0);
                chartUpContainer.setVisibility(View.VISIBLE);
                chartUp.setViewPortOffsets(0, 0, 0, 0);

//                chartDown.invalidate();
            }
        }
    }

    @Override
    public void onBasicResultLoaded(TestResult result) {
        if (this.isAdded() && this.getActivity() != null) {
            if (inflater != null) {
//                showData(getString(R.string.test_ping), result.getMeasuredValues().get(2), getString(R.string.test_ms), R.id.result_ping_square);
                showData(result.getMeasuredValues().get(2).getValueTitle(), result.getMeasuredValues().get(2).getStringValue(), result.getMeasuredValues().get(2).getUnits(), R.id.result_ping_square);
//                showData(getString(R.string.test_bottom_test_status_down), result.getMeasuredValues().get(0), getString(R.string.test_mbps), R.id.result_download_square);
                showData(result.getMeasuredValues().get(0).getValueTitle(), result.getMeasuredValues().get(0).getStringValue(), result.getMeasuredValues().get(0).getUnits(), R.id.result_download_square);
//                showData(getString(R.string.test_bottom_test_status_up), result.getMeasuredValues().get(1), getString(R.string.test_mbps), R.id.result_upload_square);
                showData(result.getMeasuredValues().get(1).getValueTitle(), result.getMeasuredValues().get(1).getStringValue(), result.getMeasuredValues().get(1).getUnits(), R.id.result_upload_square);
                showData(getString(R.string.test_bottom_test_status_jitter), result.getVoipResult().getMeanJitter().split(" ")[0], getString(R.string.test_ms), R.id.result_jitter_square);
                showData(getString(R.string.test_packet_loss), result.getVoipResult().getMeanPacketLossInPercent().split(" ")[0], "%", R.id.result_packet_loss_square);
            }
        }

    }

    @Override
    public void onDetailResultLoaded(JsonArray result) {
        //TODO: implement showing detailed results
    }

    private void showData(String title, String value, String units, int squareLayoutId) {
        if (this.isAdded() && this.getActivity() != null) {
            if (inflater != null) {

                ViewGroup container = getView().findViewById(squareLayoutId);

                ProgressBar progress = container.findViewById(R.id.result_square_progress_bar);
                TextView titleTV = container.findViewById(R.id.result_square_title);
                TextView valueTV = container.findViewById(R.id.result_square_value);
                TextView unitsTV = container.findViewById(R.id.result_square_units);

//                titleTV.setText(value.getValueTitle());
//                valueTV.setText(value.getStringValue());
//                unitsTV.setText(value.getUnits());
                titleTV.setText(title);
                valueTV.setText(value);
                unitsTV.setText(units);

                progress.setVisibility(View.GONE);

                titleTV.setVisibility(View.VISIBLE);
                valueTV.setVisibility(View.VISIBLE);
                unitsTV.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onQosResultLoaded(QoSServerResultCollection resultCollection, int percentage) {
        if (isAdded() && getActivity() != null) {
            String result = null;
            if (resultCollection != null) {
                result = String.valueOf(percentage);
            } else {
                result = "N/A";
            }
            showData(getString(R.string.result_page_title_qos), result, "%", R.id.result_qos_square);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void startShareResultsIntent() {
        try {
            final String shareText = controller.getResult().getShareText();
            final String shareSubject = controller.getResult().getShareSubject();

            final Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
            sendIntent.setType("text/plain");
            if (isAdded() && getActivity() != null) {
                getActivity().startActivity(Intent.createChooser(sendIntent, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
