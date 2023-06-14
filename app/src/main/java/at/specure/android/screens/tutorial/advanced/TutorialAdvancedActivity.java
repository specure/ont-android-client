package at.specure.android.screens.tutorial.advanced;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.util.ArrayList;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import at.specure.android.configs.PermissionHandler;
import at.specure.android.configs.StartConfig;
import at.specure.android.screens.main.BasicActivity;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.location.RequestGPSPermissionInterface;
import at.specure.android.views.PagerIndicator;
import timber.log.Timber;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static at.specure.android.configs.PermissionHandler.isCoarseLocationPermitted;

/**
 * Tutorial Activity displays multiple fragments in defined order just to show new user how to
 * properly use the application.
 * <p/>
 * Created by Misiak on 11-Feb-16.
 */
public class TutorialAdvancedActivity extends BasicActivity implements RequestGPSPermissionInterface {

    public static final int PERM_REQ_LOC_COARSE_START = 1;
    private ViewPager viewPager;
    private boolean currentPage;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, TutorialAdvancedActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial__activity__layout);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM_REQ_LOC_COARSE_START:
                moveToAnotherPageOrDone();
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Timber.i("SDK_INT %s", String.valueOf(Build.VERSION.SDK_INT));
        viewPager = (ViewPager) findViewById(R.id.paper);
        viewPager.setAdapter(new TutorialAdapter(this.getSupportFragmentManager()));

        PagerIndicator indicator = (PagerIndicator) findViewById(R.id.pager_indicator);
        indicator.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    if (!isCoarseLocationPermitted(TutorialAdvancedActivity.this)) {
                        PermissionHandler.showLocationExplanationDialog(TutorialAdvancedActivity.this, PERM_REQ_LOC_COARSE_START, TutorialAdvancedActivity.this);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        TextView skipIntro = (TextView) findViewById(R.id.skip_intro);
        skipIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startApp();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Build.VERSION.SDK_INT < 16) {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
//        // for APi>16
//        if (Build.VERSION.SDK_INT > 16) {
//            View decorView = getWindow().getDecorView();
//            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(uiOptions);
//
//
//        }
//
//        try {
//            Window window = this.getWindow();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                window.setStatusBarColor(this.getResources().getColor(R.color.black));
//            }
//        } catch (RuntimeException e) {
//            //nothing
//        }


    }

    @Override
    public void requestPermission(int requestCodeFine) {
        checkForGPSPermissions(requestCodeFine, false);
    }

    public void checkForGPSPermissions(int requestCodeFine,
                                       boolean showRationaleDialog) {

        FragmentActivity activity = this;
        if (activity != null) {
            if (ContextCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (showRationaleDialog && shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        PermissionHandler.showLocationExplanationDialog(activity, requestCodeFine, this);
                    } else {
                        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, requestCodeFine);
                    }
                }
            }
        }
    }

    private void startApp() {
        Intent intent = new Intent(TutorialAdvancedActivity.this, MainActivity.class);
        TutorialAdvancedActivity.this.finish();
        startActivity(intent);
    }

    public void moveToAnotherPageOrDone() {
        if (viewPager != null) {
            int count = viewPager.getAdapter().getCount();
            int currentItem = viewPager.getCurrentItem();
            if (currentItem == (count - 1)) {
                startApp();
            } else {
                viewPager.arrowScroll(View.FOCUS_RIGHT);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        StartConfig.setTutorialDisplayed(this);
    }

    public int getCurrentPage() {
        if (viewPager != null) {
            return viewPager.getCurrentItem();
        }
        return 0;
    }

    public boolean isCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(boolean currentPage) {
        this.currentPage = currentPage;
    }

    public class TutorialAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;

        public TutorialAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<>();
            fragments.add(new FragmentA());
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)) {
                fragments.add(new FragmentB());
            }
            fragments.add(new FragmentC());
            fragments.add(new FragmentD());
            fragments.add(new FragmentE());
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
