package com.zyk.launcher.util;

import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.View;

import com.zyk.launcher.Alarm;
import com.zyk.launcher.CellLayout;
import com.zyk.launcher.DeviceProfile;
import com.zyk.launcher.DragView;
import com.zyk.launcher.DropTarget;
import com.zyk.launcher.FolderIcon;
import com.zyk.launcher.Hotseat;
import com.zyk.launcher.ItemInfo;
import com.zyk.launcher.Launcher;
import com.zyk.launcher.LauncherAppState;
import com.zyk.launcher.LauncherAppWidgetInfo;
import com.zyk.launcher.LauncherSettings;
import com.zyk.launcher.R;
import com.zyk.launcher.ShortcutInfo;
import com.zyk.launcher.backup.BackupProtos;

/**
 * Created by zyk on 2016/4/8.
 */
public class DragUtils {
    // Related to dragging, folder creation and reordering
    public static final int DRAG_MODE_NONE = 0;
    public static final int DRAG_MODE_CREATE_FOLDER = 1;//创建文件夹
    public static final int DRAG_MODE_ADD_TO_FOLDER = 2;//把移动的快捷方式添加到文件夹
    public static final int DRAG_MODE_REORDER = 3;//
    public int mDragMode = DRAG_MODE_NONE;

    public final Alarm mFolderCreationAlarm = new Alarm();
    public final Alarm mReorderAlarm = new Alarm();
    public FolderIcon.FolderRingAnimator mDragFolderRingAnimator = null;
    public FolderIcon mDragOverFolderIcon = null;
    public int mLastReorderX = -1;
    public int mLastReorderY = -1;
    private int[] mTempPt = new int[2];

    private final Launcher mLauncher;
    private final View view;

    public DragUtils(Launcher launcher, View view) {
        this.mLauncher = launcher;
        this.view = view;
    }

    public void cleanupFolderCreation() {
        if (mDragFolderRingAnimator != null) {
            mDragFolderRingAnimator.animateToNaturalState();
            mDragFolderRingAnimator = null;
        }
        mFolderCreationAlarm.setOnAlarmListener(null);
        mFolderCreationAlarm.cancelAlarm();
    }

    public void cleanupAddToFolder() {
        if (mDragOverFolderIcon != null) {
            mDragOverFolderIcon.onDragExit(null);
            mDragOverFolderIcon = null;
        }
    }

    public void cleanupReorder(boolean cancelAlarm) {
        // Any pending reorders are canceled
        if (cancelAlarm) {
            mReorderAlarm.cancelAlarm();
        }
        mLastReorderX = -1;
        mLastReorderY = -1;
    }

    public void setDragMode(int dragMode) {
        if (dragMode != mDragMode) {
            if (dragMode == DRAG_MODE_NONE) {
                cleanupAddToFolder();
                // We don't want to cancel the re-order alarm every time the target cell changes
                // as this feels to slow / unresponsive.
                cleanupReorder(false);
                cleanupFolderCreation();
            } else if (dragMode == DRAG_MODE_ADD_TO_FOLDER) {
                cleanupReorder(true);
                cleanupFolderCreation();
            } else if (dragMode == DRAG_MODE_CREATE_FOLDER) {
                cleanupAddToFolder();
                cleanupReorder(true);
            } else if (dragMode == DRAG_MODE_REORDER) {
                cleanupAddToFolder();
                cleanupFolderCreation();
            }
            mDragMode = dragMode;
        }
    }

    // This is used to compute the visual center of the dragView. This point is then
    // used to visualize drop locations and determine where to drop an item. The idea is that
    // the visual center represents the user's interpretation of where the item is, and hence
    // is the appropriate point to use when determining drop location.
    public float[] getDragViewVisualCenter(int x, int y, int xOffset, int yOffset,
                                           DragView dragView, float[] recycle, Resources resources) {
        float res[];
        if (recycle == null) {
            res = new float[2];
        } else {
            res = recycle;
        }

        // First off, the drag view has been shifted in a way that is not represented in the
        // x and y values or the x/yOffsets. Here we account for that shift.
        x += resources.getDimensionPixelSize(R.dimen.dragViewOffsetX);
        y += resources.getDimensionPixelSize(R.dimen.dragViewOffsetY);

        // These represent the visual top and left of drag view if a dragRect was provided.
        // If a dragRect was not provided, then they correspond to the actual view left and
        // top, as the dragRect is in that case taken to be the entire dragView.
        // R.dimen.dragViewOffsetY.
        int left = x - xOffset;
        int top = y - yOffset;

        // In order to find the visual center, we shift by half the dragRect
        res[0] = left + dragView.getDragRegion().width() / 2;
        res[1] = top + dragView.getDragRegion().height() / 2;

        return res;
    }

    public boolean isPointInSelfOverHotseat(int x, int y, Rect r) {
        if (r == null) {
            r = new Rect();
        }
        mTempPt[0] = x;
        mTempPt[1] = y;
        mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(view, mTempPt, true);

        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        r = grid.getHotseatRect();
        if (r.contains(mTempPt[0], mTempPt[1])) {
            return true;
        }
        return false;
    }

    public void mapPointFromSelfToHotseatLayout(Hotseat hotseat, float[] xy) {
        mTempPt[0] = (int) xy[0];
        mTempPt[1] = (int) xy[1];
        mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(view, mTempPt, true);
        mLauncher.getDragLayer().mapCoordInSelfToDescendent(hotseat.getLayout(), mTempPt);

        xy[0] = mTempPt[0];
        xy[1] = mTempPt[1];
    }

    /**
     * Convert the 2D coordinate xy from this CellLayout's coordinate space to
     * the parent View's coordinate space. The argument xy is modified with the return result.
     */
    public void mapPointFromChildToSelf(View v, float[] xy) {
        xy[0] += v.getLeft();
        xy[1] += v.getTop();
    }

    /**
     * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
     * coordinate space. The argument xy is modified with the return result.
     *
     * if cachedInverseMatrix is not null, this method will just use that matrix instead of
     * computing it itself; we use this to avoid redundant matrix inversions in
     * findMatchingPageForDragOver
     *
     */
    public void mapPointFromSelfToChild(View v, float[] xy, Matrix cachedInverseMatrix) {
        xy[0] = xy[0] - v.getLeft();
        xy[1] = xy[1] - v.getTop();
    }

    public float squaredDistance(float[] point1, float[] point2) {
        float distanceX = point1[0] - point2[0];
        float distanceY = point2[1] - point2[1];
        return distanceX * distanceX + distanceY * distanceY;
    }

    /**
     * Calculate the nearest cell where the given object would be dropped.
     *
     * pixelX and pixelY should be in the coordinate system of layout
     */
    public int[] findNearestArea(int pixelX, int pixelY,
                                  int spanX, int spanY, CellLayout layout, int[] recycle) {
        return layout.findNearestArea(
                pixelX, pixelY, spanX, spanY, recycle);
    }

}
