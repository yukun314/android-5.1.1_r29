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

import android.view.View;

import com.zyk.launcher.DropTarget.DragObject;

/**
 * Interface defining an object that can originate a drag.
 * 代表着拖动的源头，跟DropTarget正好相反，DragSource的子类也就是Workspace，文件夹，widget列表
 */
public interface DragSource {
    /**
     * @return whether items dragged from this source supports
     */
    boolean supportsFlingToDelete();

    /**
     * @return whether items dragged from this source supports 'App Info'
     */
    boolean supportsAppInfoDropTarget();

    /**
     * @return whether items dragged from this source supports 'Delete' drop target (e.g. to remove
     * a shortcut.
     */
    boolean supportsDeleteDropTarget();

    /*
     * @return the scale of the icons over the workspace icon size
     */
    float getIntrinsicIconScaleFactor();

    /**
     * A callback specifically made back to the source after an item from this source has been flung
     * to be deleted on a DropTarget.  In such a situation, this method will be called after
     * onDropCompleted, and more importantly, after the fling animation has completed.
     */
    void onFlingToDeleteCompleted();

    /**
     * A callback made back to the source after an item from this source has been dropped on a
     * DropTarget.
     * 主要做一些善后的操作
     */
    void onDropCompleted(View target, DragObject d, boolean isFlingToDelete, boolean success);
}
