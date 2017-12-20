/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package at.specure.android.screens.main;

import android.app.Activity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.specure.opennettest.R;

public class EndDrawerToggle implements DrawerLayout.DrawerListener {

    private DrawerLayout drawerLayout;
    private DrawerArrowDrawable arrowDrawable;
    private ImageButton toggleButton;
    private String openDrawerContentDesc;
    private String closeDrawerContentDesc;
    private DrawerActionListener listener;

    public EndDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes, DrawerActionListener listener) {

        this.drawerLayout = drawerLayout;
        this.listener = listener;
        this.openDrawerContentDesc = activity.getString(openDrawerContentDescRes);
        this.closeDrawerContentDesc = activity.getString(closeDrawerContentDescRes);

        arrowDrawable = new DrawerArrowDrawable(toolbar.getContext());
        arrowDrawable.setDirection(DrawerArrowDrawable.ARROW_DIRECTION_END);

        toggleButton = new ImageButton(toolbar.getContext(), null,
                R.attr.toolbarNavigationButtonStyle);
        toolbar.addView(toggleButton, new LayoutParams(GravityCompat.END));
        toggleButton.setImageDrawable(arrowDrawable);
        toggleButton.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                toggle();
                                            }
                                        }
        );
    }

    public void syncState() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            setPosition(1f);
        } else {
            setPosition(0f);
        }
    }

    public void toggle() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    public void setPosition(float position) {
        if (position == 1f) {
            arrowDrawable.setVerticalMirror(true);
            toggleButton.setContentDescription(closeDrawerContentDesc);
        } else if (position == 0f) {
            arrowDrawable.setVerticalMirror(false);
            toggleButton.setContentDescription(openDrawerContentDesc);
        }
        arrowDrawable.setProgress(position);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        setPosition(Math.min(1f, Math.max(0, slideOffset)));
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        setPosition(1f);
        if (listener != null) {
            listener.onDrawerOpen();
        }

    }

    @Override
    public void onDrawerClosed(View drawerView) {
        setPosition(0f);
        if (listener != null) {
            listener.onDrawerClose();
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
}
