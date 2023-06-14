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

package at.specure.android.base;

import android.content.Context;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import at.specure.android.screens.main.InitialSetupInterface;
import at.specure.android.screens.main.MainActivity;
import timber.log.Timber;

public abstract class BaseFragment extends Fragment implements InitialSetupInterface {

    public abstract String setActionBarTitle();

    @Override
    public void onResume() {
        super.onResume();
        final FragmentActivity activity = getActivity();
        if (activity != null && activity instanceof MainActivity) {
            ((MainActivity) activity).updateTitle(setActionBarTitle());
            new Handler().post(new Runnable() {
                @Override
                public void run() {
//                    Timber.e("MENU setvisibleMenuItems BaseFragment - RESUME");
//                    Timber.e("FRAGMENT setActionBarItems RESUME");
//                    setActionBarItems(activity);
                }
            });
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
//                Timber.e("FRAGMENT setActionBarItems ATTACH");
//                Timber.e("MENU setvisibleMenuItems BaseFragment - ATTACH");
//                setActionBarItems(context);
            }
        });
    }

    @Override
    public void setActionBarItems(Context context) {
        if (context != null && context instanceof MainActivity) {
//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
                    setHasOptionsMenu(true);
                    Timber.e("MENU setvisibleMenuItems BaseFragment");
//                    ((MainActivity) context).setVisibleMenuItems();

//                }
//            });
//
        }
    }
}
