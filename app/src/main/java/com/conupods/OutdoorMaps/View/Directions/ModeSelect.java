package com.conupods.OutdoorMaps.View.Directions;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.conupods.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Duration;
import com.google.maps.model.TravelMode;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ModeSelect extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "ModeSelect" ;
    private GoogleMap mMap;
    private LatLng mOrigin;
    private LatLng mDestination;
    GeoApiContext GAC;

    private Intent mPreviousActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GAC = new GeoApiContext.Builder()
                .apiKey(getString(R.string.Google_API_Key))
                .build();

        // add info to buttons
        mPreviousActivityIntent = getIntent();
        mOrigin = mPreviousActivityIntent.getParcelableExtra("fromLatLng");
        mDestination = mPreviousActivityIntent.getParcelableExtra("toLatLng");

        // Compute the directions for each modes we use
        for (TravelMode mode : TravelMode.values()) {
            if (!mode.equals(TravelMode.BICYCLING) && !mode.equals(TravelMode.UNKNOWN)) {
                computeDirections(mOrigin, mDestination, mode);
            }
        }

        // Set the from and to fields
        String from_location = mPreviousActivityIntent.getStringExtra("fromString");
        Button fromButton = (Button) findViewById(R.id.modeSelect_from);
        fromButton.setText("From: " + from_location);

        String to_location = mPreviousActivityIntent.getStringExtra("toString");
        Button toButton = (Button) findViewById(R.id.modeSelect_to);
        toButton.setText("To: " + to_location);

        Button walkingBTN = (Button) findViewById(R.id.modeSelect_walkingButton);
        walkingBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchModeSelectIntent(TravelMode.WALKING);
            }
        });

        Button drivingBTN = (Button) findViewById(R.id.modeSelect_drivingButton);
        drivingBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchModeSelectIntent(TravelMode.DRIVING);
            }
        });

        Button transitBTN = (Button) findViewById(R.id.modeSelect_transitButton);
        transitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchModeSelectIntent(TravelMode.TRANSIT);
            }
        });
    }

    // OnClick for origin and destination can't be implemented until Mike's PR is approved
    public void onClickSetOrigin(View view) {
//        Intent setOriginIntent = new Intent(this, target.class);
//        startActivity(setOriginIntent);
    }

    public void onClickSetDestination(View view) {
//        Intent setDestinationIntent = new Intent(this, target.class);
//        startActivity(setDestinationIntent);
    }

    public void onClickSelectWalking(View view) {
        launchModeSelectIntent(TravelMode.WALKING);
    }

    public void onClickDriving(View view) {
        launchModeSelectIntent(TravelMode.DRIVING);
    }

    public void onClickPublicTransit(View view) {
        launchModeSelectIntent(TravelMode.TRANSIT);
    }

    // Needs separate logic
    public void onClickShuttle(View view) {

    }

    // Extracted details related to creating and launching intents for different modes
    private void launchModeSelectIntent(TravelMode mode) {
        Intent modeSelectIntent = new Intent(this, Navigation.class);
        modeSelectIntent.putExtra("origin", mOrigin);
        modeSelectIntent.putExtra("destination", mDestination);
        modeSelectIntent.putExtra("mode", mode);
        startActivity(modeSelectIntent);
    }

    // Sends Directions API request
    // Calls function to update the view elements on success
    public void computeDirections(LatLng origin, LatLng destination, TravelMode mode) {

        DirectionsApiRequest directions = new DirectionsApiRequest(GAC);

        directions.origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude));
        directions.destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude));
        directions.mode(mode);

        directions.setCallback(new PendingResult.Callback<DirectionsResult>() {

            @Override
            public void onResult(DirectionsResult result) {
                updateView(result, mode);
//                addPolyLinesToMap(result, mode);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.d("OUTDOORSERVICES", "Failed to get directions");
            }
        });
    }

    // Updates the view elements given DirectionResult and a TravelMode
    // Gets details from the result and selects the view elements with the mode
    // Grabs the main thread so that DirectionResult is not null
    private void updateView(final DirectionsResult result, TravelMode mode) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                // Compute arrival time from duration
                Duration duration = result.routes[0].legs[0].duration;
                long durationInMs = 1000 * duration.inSeconds;

                Date currentTime = Calendar.getInstance().getTime();
                long currentTimeInMs = currentTime.getTime();

                Date arrivalTime = new Date(currentTimeInMs + durationInMs);

                // Format start and end times
                SimpleDateFormat formatPattern = new SimpleDateFormat("h:mm a");

                String startTimeFormatted = formatPattern.format(currentTime);
                String endTimeFormatted = formatPattern.format(arrivalTime);

                switch (mode) {
                    case DRIVING:
                        TextView drivingDuration = (TextView) findViewById(R.id.modeSelect_drivingDuration);
                        drivingDuration.setText(duration.humanReadable);

                        TextView drivingTimes = (TextView) findViewById(R.id.modeSelect_drivingTimes);
                        drivingTimes.setText(startTimeFormatted + " - " + endTimeFormatted);

                        break;
                    case WALKING:
                        TextView walkingDuration = (TextView) findViewById(R.id.modeSelect_walkingDuration);
                        walkingDuration.setText(duration.humanReadable);

                        TextView walkingTimes = (TextView) findViewById(R.id.modeSelect_walkingTimes);
                        walkingTimes.setText(startTimeFormatted + " - " + endTimeFormatted);

                        break;
                    case TRANSIT:
                        TextView transitDuration = (TextView) findViewById(R.id.modeSelect_publicTransitDuration);
                        transitDuration.setText(duration.humanReadable);

                        TextView transitTimes = (TextView) findViewById(R.id.modeSelect_publicTransitTimes);
                        transitTimes.setText(startTimeFormatted + " - " + endTimeFormatted);

                        break;
                    default:
                        Log.d("Unexpected Mode", "updateViews: received result with unexpected transit mode");
                }
            }
        });
    }

    // polyline function - can be modified to work with a single route
    private void addPolyLinesToMap(final DirectionsResult result, TravelMode mode) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (DirectionsRoute route: result.routes) {
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();
                    for (com.google.maps.model.LatLng latLng: decodedPath) {
                        newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));
                    }

                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                }
            }
        });
    }


    // add the onClickListeners
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mOrigin));
    }
}