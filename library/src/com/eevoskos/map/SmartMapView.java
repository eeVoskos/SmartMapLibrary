package com.eevoskos.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * An extension of {@code MapView} that captures double taps for zooming and
 * supports the long press gesture.
 * 
 * @author Stratos Theodorou
 */
public class SmartMapView extends MapView implements OnGestureListener, OnDoubleTapListener {

    /**
     * Interface for listening for long-press events of the {@link SmartMapView}
     * , translated into {@link GeoPoint}s.
     * 
     * @author Stratos Theodorou
     * 
     */
    public interface OnLongPressListener {

        /**
         * Called when a long-press event has been captured on the
         * {@link SmartMapView}.
         * 
         * @param point
         *            The location of the long-press event, translated into a
         *            {@link GeoPoint}.
         */
        public void onLongPress(GeoPoint point);
    }

    private GestureDetector mGestureDetector;
    private OnLongPressListener mOnLongPressListener;

    public SmartMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SmartMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SmartMapView(Context context, String apiKey) {
        super(context, apiKey);
        init();
    }

    private void init() {
        setClickable(true);
        mGestureDetector = new GestureDetector(getContext(), this);
        mGestureDetector.setOnDoubleTapListener(this);
    }

    /**
     * Set a long-press event listener to the {@link SmartMapView}.
     * 
     * @param onLongPressListener
     *            The long-press listener to assign.
     */
    public void setOnLongPressListener(OnLongPressListener onLongPressListener) {
        mOnLongPressListener = onLongPressListener;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    /* OnDoubleTapListener */

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        getController().zoomInFixing((int) e.getX(), (int) e.getY());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    /* OnGestureListener */

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mOnLongPressListener != null) {
            mOnLongPressListener.onLongPress(getProjection().fromPixels((int) e.getX(),
                    (int) e.getY()));
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

}
