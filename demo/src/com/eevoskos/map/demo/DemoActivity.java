package com.eevoskos.map.demo;

import android.os.Bundle;
import android.widget.Toast;

import com.eevoskos.map.SmartLocationOverlay;
import com.eevoskos.map.SmartMapView;
import com.eevoskos.map.SmartMapView.OnLongPressListener;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

public class DemoActivity extends MapActivity {

	// TODO Change this to your own Google Maps API key before you build
	private static final String MAPS_API_KEY = "0HsDmcb0JxOByuXpSPTvOMexGH4xdYDkbqhhvWQ";

	private SmartMapView mMapView;
	private SmartLocationOverlay mSmartLocationOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMapView = new SmartMapView(this, MAPS_API_KEY);
		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setOnLongPressListener(new OnLongPressListener() {
			@Override
			public void onLongPress(GeoPoint point) {
				Toast.makeText(DemoActivity.this,
						"[" + point.getLatitudeE6() + "," + point.getLongitudeE6() + "]",
						Toast.LENGTH_SHORT).show();
			}
		});

		setContentView(mMapView);

		mSmartLocationOverlay = new SmartLocationOverlay(this, mMapView);
		mSmartLocationOverlay.enableCompass();
		mSmartLocationOverlay.enableAccuracy();
		mSmartLocationOverlay.enableDirection();

		mMapView.getOverlays().add(mSmartLocationOverlay);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Enable the location listener on resume
		mSmartLocationOverlay.enableMyLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Disable the location listener on pause to save battery
		mSmartLocationOverlay.disableMyLocation();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
