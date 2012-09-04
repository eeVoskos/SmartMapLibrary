package com.eevoskos.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

/**
 * An extension of {@link MyLocationOverlay} that shows a directional arrow as a
 * map overlay for the user's location, if compass is enabled, instead of the
 * classic compass on the top left corner of the {@link MapView}.
 * 
 * @author Stratos Theodorou
 */
public class SmartLocationOverlay extends MyLocationOverlay {

	/**
	 * Interface to implement for an action to be performed when the user's
	 * location is changed. Usual actions include saving the current known
	 * location in {@link SharedPreferences} or otherwise updating the UI.
	 */
	public interface OnLocationChangedListener {
		public void onLocationChanged(Location location);
	}

	private MapView mMapView;
	private Bitmap mBitmap;
	private Paint mMarkerPaint, mAccuracyFillPaint, mAccuracyStrokePaint;
	private LevelListDrawable marker;
	private int level;
	private long lastRedrawTime;

	private boolean mIsAccuracyEnabled = false;
	private boolean mIsCompassEnabled = false;
	private boolean mIsDirectionEnabled = false;
	private boolean mIsFollowMyLocationEnabled = false;

	private OnLocationChangedListener mOnLocationChangedListener;

	public SmartLocationOverlay(Context context, MapView mapView) {
		this(context, mapView, null);
	}

	public SmartLocationOverlay(Context context, MapView mapView,
			OnLocationChangedListener onLocationChangedListener) {
		super(context, mapView);
		mMapView = mapView;
		/*marker = (LevelListDrawable) mapView.getResources().getDrawable(
				R.drawable.ic_my_location_animation);*/
		marker = new LevelListDrawable();
		Resources res = context.getResources();
		marker.addLevel(0, 2500, res.getDrawable(R.drawable.ic_my_location_anim0));
		marker.addLevel(2501, 5000, res.getDrawable(R.drawable.ic_my_location_anim1));
		marker.addLevel(5001, 7500, res.getDrawable(R.drawable.ic_my_location_anim2));
		marker.addLevel(7501, 10000, res.getDrawable(R.drawable.ic_my_location_anim3));
		level = marker.getLevel();
		lastRedrawTime = 0;

		// Create a paint with AntiAlias and Bitmap flags for proper, smooth
		// drawing of the marker
		mMarkerPaint = new Paint();
		mMarkerPaint.setFilterBitmap(true);
		mMarkerPaint.setAntiAlias(true);

		// Create the accuracy fill mMarkerPaint
		mAccuracyFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAccuracyFillPaint.setStyle(Style.FILL);
		mAccuracyFillPaint.setColor(0x220000FF);

		// Create the accuracy stroke mMarkerPaint
		mAccuracyStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAccuracyStrokePaint.setStyle(Style.STROKE);
		mAccuracyStrokePaint.setColor(0x660000FF);
		mAccuracyStrokePaint.setStrokeWidth(2);

		this.mOnLocationChangedListener = onLocationChangedListener;
	}

	public void setOnLocationChangedListener(
			OnLocationChangedListener onLocationChangedListener) {
		this.mOnLocationChangedListener = onLocationChangedListener;
	}

	/**
	 * Enable drawing the accuracy of the current location.
	 */
	public void enableAccuracy() {
		mIsAccuracyEnabled = true;
	}

	/**
	 * Disable drawing the accuracy of the current location.
	 */
	public void disableAccuracy() {
		mIsAccuracyEnabled = false;
	}

	@Override
	public synchronized boolean enableCompass() {
		mIsCompassEnabled = super.enableCompass();
		return mIsCompassEnabled;
	}

	@Override
	public synchronized void disableCompass() {
		super.disableCompass();
		mIsCompassEnabled = false;
	}

	/**
	 * Check if drawing the accuracy of the current location is enabled.
	 * 
	 * @return {@code true} if accuracy drawing is enabled, {@code false}
	 *         otherwise.
	 * 
	 */
	public boolean isAccuracyEnabled() {
		return mIsAccuracyEnabled;
	}

	/**
	 * Enable drawing a directional arrow, instead of the default marker.
	 */
	public void enableDirection() {
		mIsDirectionEnabled = true;
		// Direction needs compass enabled
		// Super is called so that the mIsCompassEnabled flag is not affected
		super.enableCompass();
	}

	/**
	 * Disable drawing the directional arrow, showing the default marker instead.
	 */
	public void disableDirection() {
		mIsDirectionEnabled = false;
		// Disable the compass if it is not explicitly enabled
		if (!mIsCompassEnabled) {
			super.disableCompass();
		}
	}

	/**
	 * Enable recentering the map over my location, on every location update.
	 */
	public void enableFollowMyLocation() {
		mIsFollowMyLocationEnabled = true;
	}

	/**
	 * Disable recentering the map over my location, on every location update.
	 */
	public void disableFollowMyLocation() {
		mIsFollowMyLocationEnabled = false;
	}

	/**
	 * Check if drawing the current direction is enabled.
	 * 
	 * @return {@code true} if direction drawing is enabled, {@code false}
	 *         otherwise.
	 * 
	 */
	public boolean isDirectionEnabled() {
		return mIsDirectionEnabled;
	}

	@Override
	public synchronized void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		if (mIsFollowMyLocationEnabled) {
			mMapView.getController().animateTo(getMyLocation());
		}
		if (mOnLocationChangedListener != null) {
			mOnLocationChangedListener.onLocationChanged(location);
		}
	}

	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView,
			Location lastFix, GeoPoint myLocation, long when) {
		// If direction is not enabled, draw the default marker
		if (!mIsDirectionEnabled) {
			super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
			return;
		}

		// Select a different level every 500 milliseconds from the
		// LevelListDrawable marker
		boolean shouldRedraw = when - lastRedrawTime > 500 || mBitmap == null;
		if (shouldRedraw) {
			level++;
			if (level >= 4)
				level = 0;
			marker.selectDrawable(level);
			mBitmap = ((BitmapDrawable) marker.getCurrent()).getBitmap();
			lastRedrawTime = when;
		}

		// Create a matrix for rotating and positioning the mBitmap
		Matrix matrix = new Matrix();

		// Set rotation if compass has orientation value
		float orientation = getOrientation();
		if (orientation == Float.NaN) {
			orientation = 0;
		}
		matrix.setRotate(orientation - 45, mBitmap.getWidth() / 2,
				mBitmap.getHeight() / 2);

		// Translate the GeoPoint to screen pixels
		Point pointOnScreen = mapView.getProjection().toPixels(myLocation, null);

		// Translate the mBitmap matrix to the proper location on screen
		matrix.postTranslate(pointOnScreen.x - (mBitmap.getWidth() / 2),
				pointOnScreen.y - (mBitmap.getHeight() / 2));

		// Draw accuracy before marker
		if (mIsAccuracyEnabled) {
			// Get the proportional accuracy radius based on the visible map
			int radius = (int) mapView.getProjection().metersToEquatorPixels(
					lastFix.getAccuracy());
			// Draw the color
			canvas.drawCircle(pointOnScreen.x, pointOnScreen.y, radius,
					mAccuracyFillPaint);
			// Draw the stroke
			canvas.drawCircle(pointOnScreen.x, pointOnScreen.y, radius,
					mAccuracyStrokePaint);
		}

		// Draw the marker on canvas
		canvas.drawBitmap(mBitmap, matrix, mMarkerPaint);

		// Invalidate the MapView
		mapView.postInvalidate();
	}

	@Override
	protected void drawCompass(Canvas canvas, float bearing) {
		// Draw the compass if it is explicitly enabled
		if (mIsCompassEnabled) {
			super.drawCompass(canvas, bearing);
		}
	}

}
