/*
 *  Copyright (C) 2013 The OmniROM Project
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
package org.omnirom.omniswitch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public final class TaskDescription {
    final ResolveInfo resolveInfo;
    final int taskId; // application task id for curating apps
    final int persistentTaskId; // persistent id
    final Intent intent; // launch intent for application
    final String packageName; // used to override animations (see onClick())
    final CharSequence description;
    final int stackId;
    private Drawable mIcon; // application package icon
    private CharSequence mLabel; // application package label
    private boolean mIsActive;
    private boolean mKilled;
    private ActivityInfo mActivityInfo;
    private ThumbChangeListener mListener;
    private boolean mThumbLoading;
    private boolean mThumbPreloaded;
    private Bitmap mThumb;

    public static interface ThumbChangeListener {
        public void thumbChanged(int pesistentTaskId, Bitmap thumb);
        public int getPersistentTaskId();
    }

    public TaskDescription(int _taskId, int _persistentTaskId,
            ResolveInfo _resolveInfo, Intent _intent, String _packageName,
            CharSequence _description, boolean activeTask, int _stackId) {
        resolveInfo = _resolveInfo;
        intent = _intent;
        taskId = _taskId;
        persistentTaskId = _persistentTaskId;
        stackId = _stackId;

        description = _description;
        packageName = _packageName;
        mActivityInfo = resolveInfo.activityInfo;
        mIsActive = activeTask;
    }

    public TaskDescription() {
        resolveInfo = null;
        intent = null;
        taskId = -1;
        persistentTaskId = -1;
        stackId = -1;

        description = null;
        packageName = null;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public boolean isNull() {
        return resolveInfo == null;
    }

    // mark all these as locked?
    public CharSequence getLabel() {
        return mLabel;
    }

    public void setLabel(CharSequence label) {
        mLabel = label;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    public int getTaskId() {
        return taskId;
    }

    public Intent getIntent() {
        return intent;
    }

    public int getPersistentTaskId() {
        return persistentTaskId;
    }

    public int getStackId() {
        return stackId;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isKilled() {
        return mKilled;
    }

    public void setKilled() {
        this.mKilled = true;
    }

    public ActivityInfo getActivityInfo() {
        return mActivityInfo;
    }

    @Override
    public String toString() {
        return mLabel.toString();
    }

    public void setThumb(Bitmap thumb) {
        callListener(thumb);
    }

    public void setThumbChangeListener(ThumbChangeListener client) {
        mListener = client;
    }

    private void callListener(final Bitmap thumb) {
        if (mListener != null) {
            // only call back if the listener is still the one attached to us
            if (mListener.getPersistentTaskId() == persistentTaskId) {
                mListener.thumbChanged(persistentTaskId, thumb);
            }
        }
    }

    public boolean isThumbLoading() {
        return mThumbLoading;
    }

    public void setThumbLoading(boolean thumbLoading) {
        this.mThumbLoading = thumbLoading;
    }

    public void setThumbPreloaded(Bitmap thumb) {
        mThumbPreloaded = true;
        mThumb = thumb;
    }

    public Bitmap getThumbPreloaded() {
        return mThumb;
    }

    public boolean isThumbPreloaded() {
        return mThumbPreloaded;
    }
}
