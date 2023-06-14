package at.specure.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.specure.opennettest.R;

public class PagerIndicator extends LinearLayout {

    @DrawableRes
    int selectedIcon;
    @DrawableRes
    int unSelectedIcon;

    public PagerIndicator(Context context) {
        this(context, null, 0);
    }

    public PagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PagerIndicator,
                0, 0);

        try {
            selectedIcon = a.getResourceId(R.styleable.PagerIndicator_selected_bullet, R.drawable.pager_indicator_selected);
            unSelectedIcon = a.getResourceId(R.styleable.PagerIndicator_unselected_bullet, R.drawable.pager_indicator_unselected);
        } finally {
            a.recycle();
        }
    }

    public void setViewPager(ViewPager viewPager) {
        PagerAdapter viewPagerAdapter = viewPager.getAdapter();

        if (viewPagerAdapter == null) {
            return;
        }

        removeAllViews();
        int padding = getResources().getDimensionPixelSize(R.dimen.shared__page_indicator_margin);
        for (int i = 0; i < viewPagerAdapter.getCount(); i++) {
            ImageView img = new ImageView(getContext());
            img.setLayoutParams(new LinearLayoutCompat.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            img.setPadding(padding, 0, padding, 0);
            addView(img);
        }

        checkBullets(viewPager.getCurrentItem());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                checkBullets(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void checkBullets(int selected) {

        for (int i = 0; i < getChildCount(); i++) {
            if (selected == i) {
                ((ImageView) getChildAt(i)).setImageResource(selectedIcon);
            } else {
                ((ImageView) getChildAt(i)).setImageResource(unSelectedIcon);
            }
        }
    }
}
