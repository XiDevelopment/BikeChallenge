package at.xidev.bikechallenge.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.SupportMapFragment;
import java.util.Date;
import java.util.ArrayList;


/**
 * Created by Michael Staudacher on 06.05.2014.
 */
public class FragmentDrive extends Fragment {
    private LocationManager locationManager;//Manager (to get the locations)
    private String provider;                //GPS or Network Provider
    private boolean gpsEnabled = false;     //GPS allowed on Phone
    private boolean networkEnabled = false; //Network Locaton allowed on Phone
    private float distance;                 //distance of the current route
    private float tempDistance;             //distance between the last two gps coordinates
    private boolean isTracking = false;     //true if currently tracking
    private boolean trackingStart = false;  //start position for tracking received
    private Location locationOld;           //second newest location (for distance calculation)
    private View trackingView;              //view: tracking (while tracking. not visible at start)
    private View startView;                 //view: start (before tracking)
    private TextView textViewDistance;      //textview: distance
    private TextView textViewTime;          //textview: time
    private TextView textViewSpeed;         //textview: speed
    private Button startButton;             //start/stop button
    private Handler handler;                //handler (for stopwatch)
    private Runnable runnableStopwatch;     //runnable (for stopwatch)
    private long startTime = 0;             //start time of the route
    private Date startTimeDate;             //start time as Date (for Database)
    private Date endTimeDate;               //end time as Date
    private long timeNew = 0;               //time for speed calculation
    private long timeOld = 0;               //time for speed calculation
    private long timeDif = 0;               //time for speed calculation
    private double speed = 0.0;             //current speed (km/h)
    private double oldSpeed = 0.0;          //old speed (gps bugs)
    private GoogleMap googleMap;            //google maps map
    private static final long minTime = 500;//ms (for GPS tracking)
    private static final float minDistance = 2;//meter (for GPS tracking)
    private LocationListener locationListener;//listener: tracks route
    private Location location;              //position before tracking (network pos. or static pos.)
    private LatLng now;                     //LatLng of position before tracking
    private ArrayList<LatLng> positionlist; //Arraylist with all tracked LatLng



    public static FragmentDrive newInstance() {
        FragmentDrive fragment = new FragmentDrive();
        return fragment;
    }



    public FragmentDrive() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_drive, container, false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //checks if gps is enable
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!networkEnabled){
            //if network position disabled: set position to a static position
            now = new LatLng(47.2641, 11.3445); //UNI
        }else {
            provider = locationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(provider);

            //Coordinates of current Position (Network)
            now = new LatLng(location.getLatitude(), location.getLongitude());
        }

        //get mapfragment
        googleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        //get textviews
        trackingView = (View) rootView.findViewById(R.id.tracking_view);
        startView = (View) rootView.findViewById(R.id.start_view);
        textViewDistance = (TextView) rootView.findViewById(R.id.tv_distance);
        textViewTime = (TextView) rootView.findViewById(R.id.tv_time);
        textViewSpeed = (TextView) rootView.findViewById(R.id.tv_speed);
        startButton = (Button) rootView.findViewById(R.id.button_start);

        //move Camera to position
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now, 18));

        //new positionlist (for all positions)
        positionlist = new ArrayList<LatLng>();

        return rootView;
    }



    @Override
    public void onDestroy() {
        if (isTracking == true){
            //remove locationlistener (if started)
            locationManager.removeUpdates(locationListener);
        }
        if (trackingStart == true){
            //remove stopwatch (if started)
            handler.removeCallbacks(runnableStopwatch);
        }
        super.onDestroy();
    }



    //START BUTTON
    public void startButton(View view) {

        //if tracking was not running before
        if(isTracking == false){

            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (gpsEnabled) {

                isTracking = true;
                googleMap.clear();
                textViewDistance.setText(getString(R.string.searching_for_gps));
                textViewTime.setText("0:00:00");
                textViewSpeed.setText("0 " + getString(R.string.speed_unit));
                startButton.setText("stop");
                startButton.setBackgroundResource(R.drawable.red_button);

                //change from start to tracking (view)
                startView.setVisibility(View.GONE);
                trackingView.setVisibility(View.VISIBLE);

                //toast message: started
                Toast.makeText(view.getContext(), getString(R.string.started), Toast.LENGTH_SHORT).show();

                //create locationListener:
                locationListener = new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {
                        positionlist.add(new LatLng(location.getLatitude(), location.getLongitude()));

                        if (trackingStart == false ){ //tracking not started yet (0 positions yet)
                            trackingStart = true; //start it
                            isTracking = true; //start (if not yet)

                            //stopwatch
                            startTime = System.currentTimeMillis();
                            startTimeDate = new Date();
                            startTimeDate.setTime(System.currentTimeMillis());
                            handler=new Handler();
                            runnableStopwatch = new Runnable()
                            {
                                public void run()
                                {
                                long time = System.currentTimeMillis() - startTime;
                                int sec = (int) ((time / 1000)%60);
                                int min = (int) ((time / 60000)%60);
                                int h = (int) (time / 3600000);

                                String timeString = new String();
                                timeString += h;
                                timeString += ":";
                                if (min < 10){
                                    timeString += "0";
                                }
                                timeString += min;
                                timeString += ":";
                                if (sec < 10){
                                    timeString += "0";
                                }
                                timeString += sec;

                                textViewTime.setText(timeString);

                                //user not moving (no new gps coordinates)
                                if ((System.currentTimeMillis() - timeNew) > 8000){
                                    textViewSpeed.setText("0 " + getString(R.string.speed_unit));
                                }

                                handler.postDelayed(this, 500);
                                }
                            };
                            handler.post(runnableStopwatch);

                            locationOld = location;
                            timeNew = System.currentTimeMillis();
                            timeOld = timeNew;

                            //Start Marker
                            googleMap.addMarker(new MarkerOptions()
                                    .position(positionlist.get(0))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
                        } else { //tracking is already running

                            //calculates distance between the 2 newest points
                            tempDistance = location.distanceTo(locationOld);
                            //adds this distance to the distance of the route
                            distance += tempDistance;

                            //positionNew = new LatLng(location.getLatitude(), location.getLongitude());
                            timeNew = System.currentTimeMillis();
                            timeDif = timeNew - timeOld;

                            //change Camera Position to current position
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionlist.get(positionlist.size()-1), googleMap.getCameraPosition().zoom));

                            //clear map
                            googleMap.clear();

                            //Start Marker
                            googleMap.addMarker(new MarkerOptions()
                                    .position(positionlist.get(0))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));

                            //line(s)
                            googleMap.addPolyline(new PolylineOptions()
                                    .addAll(positionlist)
                                    .width(8)
                                    .color(0xFF0000FF));

                            //circle at current position
                            googleMap.addCircle(new CircleOptions()
                                    .center(positionlist.get(positionlist.size()-1))
                                    .radius(2)
                                    .strokeColor(0xFF0000FF)
                                    .fillColor(0xFF0000FF));

                            //set location/time for the next call
                            locationOld = location;
                            timeOld = timeNew;

                        }

                        //calculate speed
                        oldSpeed = speed;
                        speed = (tempDistance / (timeDif / 1000)) * 3.6;
                        if (speed > 300) {
                            speed = oldSpeed;
                        }

                        //actualice distance/speed in the textviews
                        if (distance < 1000){
                            textViewDistance.setText((int) distance + getString(R.string.distance_unit1));
                        }else{
                            String meterString = new String();
                            meterString += getString(R.string.comma);
                            int m = (int) (distance%1000);
                            if(m < 100){
                                meterString += "0";
                                if (m < 10){
                                    meterString += "0";
                                    if (m < 1){
                                        meterString += "0";
                                    }
                                }
                            }
                            textViewDistance.setText((int) (distance/1000) + meterString + m + getString(R.string.distance_unit2) );
                        }
                        textViewSpeed.setText((int) speed + getString(R.string.speed_unit));

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                    }
                };

                //start locationManager
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);

            }else{
                //turn on GPS
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Turn on GPS?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //bring user to (turn on GPS screen)
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        Toast.makeText(getActivity(), getString(R.string.please_turn_on_gps), Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Continue
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }



        } else{
            //tracking already running (stop)
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(getString(R.string.stop_tracking_question));
            builder.setPositiveButton(getString(R.string.stop_tracking), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Stop tracking
                    locationManager.removeUpdates(locationListener);
                    isTracking = false;
                    if (trackingStart == true){
                        //clear map
                        googleMap.clear();
                        //Start Marker
                        googleMap.addMarker(new MarkerOptions()
                                .position(positionlist.get(0))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
                        //line(s)
                        googleMap.addPolyline(new PolylineOptions()
                                .addAll(positionlist)
                                .width(8)
                                .color(0xFF000099));
                        //End Marker
                        googleMap.addMarker(new MarkerOptions()
                                .position(positionlist.get(positionlist.size()-1))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ziel)));

                        endTimeDate = new Date();
                        endTimeDate.setTime(System.currentTimeMillis());
                        //stop stopwatch
                        handler.removeCallbacks(runnableStopwatch);


                        //transmit route to server.......


                    } else{
                        //stoped tracking before gps was ready
                        textViewDistance.setText("0" + getString(R.string.distance_unit1));
                    }

                    //set speed to 0
                    textViewSpeed.setText("0" + getString(R.string.speed_unit));

                    //make stop button to start button again
                    startButton.setText("start");
                    startButton.setBackgroundResource(R.drawable.green_button);

                    //clean values
                    trackingStart = false;
                    positionlist.clear();
                    tempDistance = 0;
                    distance = 0;
                }
            });

            builder.setNegativeButton(getString(R.string.continue_tracking), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Continue
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }


    //STOP BUTTON (Removed)
    public void stopButton(View view) {
        //
    }



}
