/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zyk.launcher;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

/**
 * Interface defining an object that can receive a drag.
 *
 */
public interface DropTarget {

    public static final String TAG = "DropTarget";

    class DragObject {
        public int x = -1;
        public int y = -1;

        /** X offset from the upper-left corner of the cell to where we touched.  */
        public int xOffset = -1;

        /** Y offset from the upper-left corner of the cell to where we touched.  */
        public int yOffset = -1;

        /** This indicates whether a drag is in final stages, either drop or cancel. It
         * differentiates onDragExit, since this is called when the drag is ending, above
         * the current drag target, or when the drag moves off the current drag object.
         */
        public boolean dragComplete = false;

        /** The view that moves around while you drag.  */
        public DragView dragView = null;

        /** The data associated with the object being dragged */
        public Object dragInfo = null;

        /** Where the drag originated */
        public DragSource dragSource = null;

        /** Post drag animation runnable */
        public Runnable postAnimationRunnable = null;

        /** Indicates that the drag operation was cancelled */
        public boolean cancelled = false;

        /** Defers removing the DragView from the DragLayer until after the drop animation. */
        public boolean deferDragViewCleanupPostAnimation = true;

        public DragObject() {
        }
    }

    public static class DragEnforcer implements DragController.DragListener {
        int dragParity = 0;

        public DragEnforcer(Context context) {
            Launcher launcher = (Launcher) context;
            launcher.getDragController().addDragListener(this);
        }

        void onDragEnter() {
            dragParity++;
            if (dragParity != 1) {
                Log.e(TAG, "onDragEnter: Drag contract violated: " + dragParity);
            }
        }

        void onDragExit() {
            dragParity--;
            if (dragParity != 0) {
                Log.e(TAG, "onDragExit: Drag contract violated: " + dragParity);
            }
        }

        @Override
        public void onDragStart(DragSource source, Object info, int dragAction) {
            if (dragParity != 0) {
                Log.e(TAG, "onDragEnter: Drag contract violated: " + dragParity);
            }
        }

        @Override
        public void onDragEnd() {
            if (dragParity != 0) {
                Log.e(TAG, "onDragExit: Drag contract violated: " + dragParity);
            }
        }
    }

    /**
     * Used to temporarily disable certain drop targets
     *
     * @return boolean specifying whether this drop target is currently enabled
     */
    boolean isDropEnabled();

    /**
     * Handle an object being dropped on the DropTarget
     * 这是系统对onDrop的注释吗？
     * @param source DragSource where the drag started
     * @param x X coordinate of the drop location
     * @param y Y coordinate of the drop location
     * @param xOffset Horizontal offset with the object being dragged where the original
     *          touch happened
     * @param yOffset Vertical offset with the object being dragged where the original
     *          touch happened
     * @param dragView The DragView that's being dragged around on screen.
     * @param dragInfo Data associated with the object being dragged
     *
     */
    /**
     * 是松手时候发生的调用，
     * 做一些放下时候的操作，
     * 比如删除快捷方式的时候会在onDrop里面开始删除的操作。
     * @param dragObject
     */
    void onDrop(DragObject dragObject);

    /**
     * 刚刚进入DropTarget范围内的时候所调用的内容
     * 比如说我们拖动桌面的一个快捷方式，到桌面顶端的删除区域，
     * “删除”两字和手中的图标会变红，这些动作都是在onDragEnter回调中完成的
     * @param dragObject
     */
    void onDragEnter(DragObject dragObject);

    /**
     * 在某一DropTarget内部移动的时候会调用的回调，
     * 比如我们把手上的图标移动到两个图标中间的时候，
     * 会发生挤位的情况（就是桌面已有图标让出空位），
     * 基本上每个ACTION_MOVE操作都会调用他
     * @param dragObject
     */
    void onDragOver(DragObject dragObject);

    /**
     * 从某一DropTarget拖出时候会进行的回调，
     * 比如onDragEnter时变红的“删除”和图标会在这个调用中恢复正常
     * @param dragObject
     */
    void onDragExit(DragObject dragObject);

    /**
     * Handle an object being dropped as a result of flinging to delete and will be called in place
     * of onDrop().  (This is only called on objects that are set as the DragController's
     * fling-to-delete target.
     */
    void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec);

    /**
     * Check if a drop action can occur at, or near, the requested location.
     * This will be called just before onDrop.
     * 
     * @param source DragSource where the drag started
     * @param x X coordinate of the drop location
     * @param y Y coordinate of the drop location
     * @param xOffset Horizontal offset with the object being dragged where the
     *            original touch happened
     * @param yOffset Vertical offset with the object being dragged where the
     *            original touch happened
     * @param dragView The DragView that's being dragged around on screen.
     * @param dragInfo Data associated with the object being dragged
     * @return True if the drop will be accepted, false otherwise.
     */
    boolean acceptDrop(DragObject dragObject);

    // These methods are implemented in Views
    void getHitRectRelativeToDragLayer(Rect outRect);
    void getLocationInDragLayer(int[] loc);
    int getLeft();
    int getTop();
}
