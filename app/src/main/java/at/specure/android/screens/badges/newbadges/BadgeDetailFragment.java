package at.specure.android.screens.badges.newbadges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.specure.opennettest.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import at.specure.android.configs.BadgesConfig;
import at.specure.android.configs.ConfigHelper;
import at.specure.androidX.data.badges.Badge;
import at.specure.androidX.data.badges.BadgesLiveData;
import at.specure.androidX.data.badges.BadgesViewModel;
import timber.log.Timber;

/**
 * A fragment representing a single Badge detail screen.
 * This fragment is either contained in a {@link BadgeListActivity}
 * in two-pane mode (on tablets) or a {@link BadgeDetailActivity}
 * on handsets.
 */
public class BadgeDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Badge mItem;
    private TextView authorDetailTV;
    private TextView earnedDateTV;
    private TextView earnedDateTitleTV;
    private ImageView pictureTV;
    private TextView subtitleTV;
    private Button originalButton;
    private TextView titleTV;
    private BadgesViewModel badgesModel;
    private LiveData<List<Badge>> data1;
    private List<Badge> badgesList;

    Observer<List<Badge>> observer = new Observer<List<Badge>>() {@Override
        public void onChanged(@Nullable List<Badge> badges) {
            if (badges != null) {
                Timber.i("Observing badges change: %s", badges);
                badgesList = badges;
                if (badgeId != null) {
                    mItem = ((BadgesLiveData) data1).getBadgeById(badgeId);
                    showBadge();
                }
            }
        }
    };
    private String badgeId;
    private ImageView picturePlaceholderTV;
    private TextView measurementCountTV;
    private TextView gainConditionTV;
    private TextView categoryTitleTV;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BadgeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

            badgeId = getArguments().getString(ARG_ITEM_ID);

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        data1.removeObserver(observer);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        badgesModel = ViewModelProviders.of(this).get(BadgesViewModel.class);
        data1 = badgesModel.getData();

        data1.observe(this.getViewLifecycleOwner(), observer);
    }

    public View createView(View view) {
        authorDetailTV = view.findViewById(R.id.badge_detail__author_detail);
        earnedDateTV = view.findViewById(R.id.badge_detail__earned_day);
        pictureTV = view.findViewById(R.id.badge_detail__picture);
        picturePlaceholderTV = view.findViewById(R.id.badge_detail__placeholder_picture);
        subtitleTV = view.findViewById(R.id.badge_detail__subtitle);
        titleTV = view.findViewById(R.id.badge_detail__title);
        earnedDateTitleTV = view.findViewById(R.id.badge_detail__earned_day_title);

        measurementCountTV = view.findViewById(R.id.badge_detail__measurement_count);
        gainConditionTV = view.findViewById(R.id.badge_detail__measurement_condition_text);
        categoryTitleTV = view.findViewById(R.id.badge_detail__category_title);

        return view;
    }

    private void showBadge() {
        if (mItem != null) {
            long receivedDateOfBadge = BadgesConfig.getReceivedDateOfBadge(mItem, this.getActivity());
            Date date = new Date();
            date.setTime(BadgesConfig.getReceivedDateOfBadge(mItem, this.getActivity()));
            String dateFormatted = DateFormat.getDateInstance(DateFormat.DEFAULT).format(date);

            BadgesConfig.checkForGettingBadge(this.getActivity(), badgesList, false, true);

            if (isAdded()) {
//                earnedDateTV.setText(dateFormatted);
                if (BadgesConfig.isBadgeReceived(mItem, this.getActivity())) {
                    picturePlaceholderTV.setImageResource(R.drawable.badge_placeholder_gained);
                    Glide.with(this.getActivity()).load(mItem.imageUrl).into(pictureTV);
                    subtitleTV.setText(mItem.description);
                    earnedDateTitleTV.setVisibility(View.VISIBLE);
                    earnedDateTV.setText(dateFormatted);
                    measurementCountTV.setVisibility(View.GONE);
                    gainConditionTV.setVisibility(View.GONE);

                } else {
                    measurementCountTV.setVisibility(View.VISIBLE);
                    gainConditionTV.setVisibility(View.VISIBLE);
//                    mItem.getConditionForDisplay()

                    String firstCriteria = mItem.getFirstCriteria();
                    if (mItem.category.equalsIgnoreCase(Badge.BADGE_CATEGORY_MEASUREMENT)) {
                        int testCounter = ConfigHelper.getTestCounter(this.getActivity());
                        int i = Integer.parseInt(firstCriteria);
                        measurementCountTV.setText(String.valueOf(i));
                        gainConditionTV.setText(String.format(getString(R.string.badges_measurement_condition), String.valueOf(i - testCounter), mItem.title));

                    } else if (mItem.category.equalsIgnoreCase(Badge.BADGE_CATEGORY_HOLIDAY)) {
                        measurementCountTV.setVisibility(View.GONE);
                        gainConditionTV.setText(String.format(getString(R.string.badges_holiday_condition), firstCriteria));
                    } else {
                        measurementCountTV.setVisibility(View.GONE);
                        gainConditionTV.setVisibility(View.GONE);
                    }
                    picturePlaceholderTV.setImageResource(R.drawable.badge_placeholder);
                    earnedDateTitleTV.setVisibility(View.GONE);
                }

                categoryTitleTV.setText(mItem.getCategory(this.getActivity()));
                titleTV.setText(mItem.title);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.badge_detail__fragment, container, false);
        createView(rootView);
        // Show the dummy content as text in a TextView.
//        if (mItem != null) {
//            ((TextView) rootView.findViewById(R.id.badge_detail)).setText(mItem.details);
//        }

        return rootView;
    }

//    @Override
//    public String setActionBarTitle() {
//        FragmentActivity activity = getActivity();
//        if (activity != null) {
//            return activity.getString(R.string.badges_title);
//        }
//            return "";
//    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (data1 != null && data1.getValue() != null) {
            observer.onChanged(data1.getValue());
        }
    }
}
