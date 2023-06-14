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

package at.specure.android.screens.main.main_fragment.view_handlers;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.specure.opennettest.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import at.specure.android.util.ViewHelper;

public abstract class ViewsHandler {

    ArrayList<Integer> viewsToSetGone;
    ArrayList<Integer> viewsToSetVisible;
    ArrayList<Integer> viewsToSetInvisble;
    HashMap<Integer, View.OnClickListener> onClickListeners;
    private View rootView;


    ViewsHandler(View rootView, HashMap<Integer, View.OnClickListener> onClickListeners) {
        this.rootView = rootView;
        this.viewsToSetVisible = new ArrayList<>();
        this.viewsToSetInvisble = new ArrayList<>();
        this.viewsToSetGone = new ArrayList<>();
        this.onClickListeners = onClickListeners;

        setOnClickListeners(rootView, onClickListeners);
    }

    void setOnClickListeners(View rootView, HashMap<Integer, View.OnClickListener> onClickListeners) {
        if ((onClickListeners != null) && (rootView != null)) {
            Set<Integer> keys = onClickListeners.keySet();
            for (Integer key : keys) {
                View viewById = rootView.findViewById(key);
                if (viewById != null) {
                    viewById.setOnClickListener(onClickListeners.get(key));
                }
            }
        }
    }

    public abstract void initializeViews(View rootView, Context context);

    void setViewVisibility() {
        changeViewsVisibility(viewsToSetGone, View.GONE);
        changeViewsVisibility(viewsToSetInvisble, View.INVISIBLE);
        changeViewsVisibility(viewsToSetVisible, View.VISIBLE);
    }

    private void changeViewsVisibility(ArrayList<Integer> views, int visibility) {
        if (views != null) {
            for (Integer id : views) {
                View viewToVisible = rootView.findViewById(id);
                setViewVisibility(viewToVisible, visibility);
            }
        }
    }

    private void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    protected void setOnClickListener(View view) {
        if ((view != null) && (onClickListeners != null)) {
            if (onClickListeners.containsKey(view.getId())) {
                view.setOnClickListener(onClickListeners.get(view.getId()));
            }
        }
    }

    void enableClickingOnButtons(final View rootView) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                View right_bottom_default_container = rootView.findViewById(R.id.main_fragment__bottom_info_container);
                if (right_bottom_default_container != null) {
                    ViewHelper.setClickableView(right_bottom_default_container, true);
                    ViewHelper.setFocusableView(right_bottom_default_container, true);
                }
            }
        });


    }

    void disableClickingOnButtons(final View rootView) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                View right_bottom_default_container = rootView.findViewById(R.id.main_fragment__bottom_info_container);
                if (right_bottom_default_container != null) {
                    ViewHelper.setClickableView(right_bottom_default_container, false);
                    ViewHelper.setFocusableView(right_bottom_default_container, false);
                }
            }
        });


    }
}
