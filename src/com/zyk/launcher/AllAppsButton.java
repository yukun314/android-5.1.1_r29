package com.zyk.launcher;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by zyk on 2016/4/7.
 */
public class AllAppsButton extends TextView implements DropTarget, DragController.DragListener {

    private int mBottomDragPadding;
    protected Launcher mLauncher;
    private boolean isEnable = false;

    public AllAppsButton(Context context) {
        super(context);
        init();
    }

    public AllAppsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AllAppsButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mBottomDragPadding = getResources().getDimensionPixelSize(R.dimen.drop_target_drag_padding);
    }

    public void setIsEnable(boolean enable){
        isEnable = enable;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AllAppsButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setLauncher(Launcher launcher){
        mLauncher = launcher;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        System.out.println("allAppsButton onDragStart");
    }

    @Override
    public void onDragEnd() {
        isEnable = false;
        System.out.println("allAppsButton onDragEnd");
    }

    @Override
    public boolean isDropEnabled() {
        return isEnable;
    }

    @Override
    public void onDrop(DragObject dragObject) {
        System.out.println("allAppsButton onDrag");
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        System.out.println("allAppsButton onDragEnter");
    }

    @Override
    public void onDragOver(DragObject dragObject) {
        System.out.println("allAppsButton onDragOver");
    }

    @Override
    public void onDragExit(DragObject dragObject) {
        System.out.println("allAppsButton onDragExit");
    }

    @Override
    public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {
        System.out.println("allAppsButton onFlingToDelete");
    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        System.out.println("allAppsButton acceptDrop");
        return false;
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        super.getHitRect(outRect);
        outRect.bottom += mBottomDragPadding;

        int[] coords = new int[2];
        mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, coords);
        outRect.offsetTo(coords[0], coords[1]);
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {
        System.out.println("allAppsButton getLocationInDragLayer");
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }
}
