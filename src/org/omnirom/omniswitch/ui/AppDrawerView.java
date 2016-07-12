/*
 *  Copyright (C) 2016 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omniswitch.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.omnirom.omniswitch.PackageManager;
import org.omnirom.omniswitch.SettingsActivity;
import org.omnirom.omniswitch.SwitchConfiguration;
import org.omnirom.omniswitch.SwitchManager;
import org.omnirom.omniswitch.Utils;
import org.omnirom.omniswitch.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppDrawerView extends GridView {
    private static final String TAG = "AppDrawerView";
    private static final boolean DEBUG = false;

    private SwitchConfiguration mConfiguration;
    private AppDrawerListAdapter mAppDrawerListAdapter;
    private boolean mTransparent;
    private SwitchManager mRecentsManager;

    public class AppDrawerListAdapter extends
            ArrayAdapter<PackageManager.PackageItem> {

        public AppDrawerListAdapter(Context context, int resource,
                List<PackageManager.PackageItem> values) {
            super(context, R.layout.package_item, resource, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PackageTextView item = null;
            if (convertView == null) {
                item = getPackageItemTemplate();
            } else {
                item = (PackageTextView) convertView;
            }
            PackageManager.PackageItem packageItem = getItem(position);

            item.setIntent(packageItem.getIntent());
            if (mConfiguration.mShowLabels) {
                item.setText(packageItem.getTitle());
            } else {
                item.setText("");
            }
            Drawable d = BitmapCache.getInstance(mContext).getResized(
                    mContext.getResources(), packageItem, mConfiguration,
                    mConfiguration.mIconSize);
            item.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
            return item;
        }
    }

    public AppDrawerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mConfiguration = SwitchConfiguration.getInstance(mContext);
        mAppDrawerListAdapter = new AppDrawerListAdapter(mContext,
                android.R.layout.simple_list_item_single_choice, PackageManager
                        .getInstance(mContext).getPackageList());
        setAdapter(mAppDrawerListAdapter);
        updateLayout();
        setVerticalScrollBarEnabled(false);

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                doOnCLickAction(position);
            }
        });

        setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                doOnLongCLickAction(position, view);
                return true;
            }
        });
    }

    public void setTransparentMode(boolean value) {
        mTransparent = value;
    }

    public void setRecentsManager(SwitchManager recentsManager) {
        mRecentsManager = recentsManager;
    }

    protected PackageTextView getPackageItemTemplate() {
        PackageTextView item = new PackageTextView(mContext);
        if (mTransparent) {
            item.setTextColor(Color.WHITE);
            item.setShadowLayer(5, 0, 0, Color.BLACK);
        } else {
            if (mConfiguration.mBgStyle == SwitchConfiguration.BgStyle.SOLID_LIGHT) {
                item.setTextColor(Color.BLACK);
                item.setShadowLayer(0, 0, 0, Color.BLACK);
            } else {
                item.setTextColor(Color.WHITE);
                item.setShadowLayer(5, 0, 0, Color.BLACK);
            }
        }
        item.setTextSize(mConfiguration.mLabelFontSize);
        item.setEllipsize(TextUtils.TruncateAt.END);
        item.setGravity(Gravity.CENTER);
        item.setLayoutParams(getListItemParams());
        item.setMaxLines(1);
        Typeface font = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
        item.setTypeface(font);
        if (mTransparent) {
            item.setBackgroundResource(R.drawable.ripple_dark);
        } else {
            item.setBackgroundResource(mConfiguration.mBgStyle == SwitchConfiguration.BgStyle.SOLID_LIGHT ? R.drawable.ripple_dark
                    : R.drawable.ripple_light);
        }
        return item;
    }

    public void updatePrefs(SharedPreferences prefs, String key) {
        if (DEBUG) {
            Log.d(TAG, "updatePrefs " + key);
        }
        if (key != null && Utils.isPrefKeyForForceUpdate(key)) {
            setAdapter(mAppDrawerListAdapter);
        }
        if (key != null && key.equals(PackageManager.PACKAGES_UPDATED_TAG)) {
            mAppDrawerListAdapter.notifyDataSetChanged();
        }
        updateLayout();
    }

    public void init() {
        mAppDrawerListAdapter.notifyDataSetChanged();
    }

    private void updateLayout() {
        setColumnWidth(mConfiguration.mMaxWidth + mConfiguration.mIconBorderHorizontal);
        int dividerHeight = mConfiguration.calcVerticalDivider(getHeight());
        setVerticalSpacing(dividerHeight);
        requestLayout();
    }

    protected LinearLayout.LayoutParams getListItemParams() {
        return new LinearLayout.LayoutParams(mConfiguration.mMaxWidth,
                mConfiguration.getItemMaxHeight());
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int dividerHeight = mConfiguration.calcVerticalDivider(h);
        setVerticalSpacing(dividerHeight);
    }

    protected void doOnCLickAction(int position) {
        PackageManager.PackageItem packageItem = PackageManager
                .getInstance(mContext).getPackageList().get(position);
        if (mRecentsManager != null) {
            mRecentsManager.startIntentFromtString(packageItem.getIntent(), true);
        } else {
            SwitchManager.startIntentFromtString(mContext, packageItem.getIntent());
        }
    }

    protected void doOnLongCLickAction(int position, View view) {
        PackageManager.PackageItem packageItem = PackageManager
                .getInstance(mContext).getPackageList().get(position);
        handleLongPressAppDrawer(packageItem, view);
    }

    private void handleLongPressAppDrawer(final PackageManager.PackageItem packageItem, View view) {
        final Context wrapper = new ContextThemeWrapper(mContext,
                mConfiguration.mBgStyle == SwitchConfiguration.BgStyle.SOLID_LIGHT
                ? R.style.PopupMenuLight : R.style.PopupMenuDark);
        final PopupMenu popup = new PopupMenu(wrapper, view);
        popup.getMenuInflater().inflate(R.menu.package_popup_menu,
                popup.getMenu());
        final List<String> favoritList = new ArrayList<String>();
        Utils.updateFavoritesList(mContext, mConfiguration, favoritList);
        boolean addFavEnabled = !favoritList.contains(packageItem.getIntent());
        if (!addFavEnabled) {
            popup.getMenu().removeItem(R.id.package_add_favorite);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.package_inspect_item) {
                    final String packageName = packageItem.getActivityInfo().packageName;
                    if (mRecentsManager != null) {
                        mRecentsManager.startApplicationDetailsActivity(packageName);
                    } else {
                        SwitchManager.startApplicationDetailsActivity(mContext, packageName);
                    }
                } else if (item.getItemId() == R.id.package_add_favorite) {
                    if (DEBUG) {
                        Log.d(TAG, "add " + packageItem.getIntent());
                    }
                    Utils.addToFavorites(mContext, packageItem.getIntent(),
                            favoritList);
                } else {
                    return false;
                }
                return true;
            }
        });
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            public void onDismiss(PopupMenu menu) {
            }
        });
        popup.show();
    }
}
