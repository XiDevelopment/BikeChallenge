package at.xidev.bikechallenge.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.location.LocationManager;
import android.location.LocationListener;
import android.content.Context;
import android.location.Location;
import android.widget.Toast;
import android.widget.TextView;
import android.content.Intent;

import java.util.Date;
import java.util.ArrayList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.SupportMapFragment;


/**
 * Created by int3r on 31.03.2014.
 */
public class FragmentDrive extends Fragment {
    private LocationManager locationManager;
    private String provider;                //GPS or Network Provider
    private boolean gpsEnabled = false;     //GPS allowed on Phone
    private boolean networkEnabled = false; //Network Locaton allowed on Phone

    private float distance;                 //distance of the current route
    private float tempDistance;             //distance between the last two gps coordinates
    private boolean isTracking = false;     //true if currently tracking
    private boolean trackingStart = false;  //start position for tracking received
    private Location locationOld;           //second newest location (for distance calculation)
    //private LatLng positionNew;             //actual position (Lat Lng Coordinates)
    //private LatLng positionOld;             //second newest position
    //private LatLng positionOld2;            //third newest position (for better lines/edges)
    private TextView textViewDistance;      //textview: distance
    private TextView textViewTime;          //textview: time
    private TextView textViewSpeed;         //textview: speed
    private TextView textViewAvSpeed;       //textview: avspeed
    private TextView textViewCo2;           //textview: Co2
    private Handler handler;                //handler (for stopwatch)
    private Runnable runnableStopwatch;     //runnable (for stopwatch)
    private long startTime = 0;             //start time of the route
    private Date startTimeDate;             //start time as Date (for Database)
    private Date endTimeDate;               //end time as Date
    private long timeNew = 0;
    private long timeOld = 0;
    private long timeDif = 0;
    private double speed = 0.0;
    private double avspeed = 0.0;
    private GoogleMap googleMap;            //google maps map
    private static final long minTime = 500; //ms (for GPS tracking)
    private static final float minDistance = 2; //meter (for GPS tracking)
    private LocationListener locationListener; //listener: tracks route
    private double co2km = 132.5;           // g co2 for one km
    private double co2 = 0.0;               //g co2 for the current route

    //temp
    private Location location;
    private LatLng now;
    private int temp = 5;
    private ArrayList<LatLng> positionlist;




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

        //if (!gpsEnabled) {

        //}

        if (!networkEnabled){
            //turn on GPS
            //Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //startActivity(intent);
            now = new LatLng(47.2641, 11.3445); //UNI
            //Toast.makeText(getActivity(), R.string.please_turn_on_gps, Toast.LENGTH_LONG).show();
        }else {
            provider = locationManager.NETWORK_PROVIDER;

            location = locationManager.getLastKnownLocation(provider);
            //location = locationManager.getLastKnownLocation(provider);

            //Toast.makeText(getActivity(), "Lat: " + location.getLatitude() + " . Lon: " +
            //        location.getLongitude()  + " - " + location.getProvider(), Toast.LENGTH_LONG).show();

            //Coordinates of current Position (Network)
            now = new LatLng(location.getLatitude(), location.getLongitude());
        }

        //test Coordinates
        /*LatLng jackrickhome = new LatLng(47.266, 11.399);
        LatLng michome = new LatLng(47.259, 11.390);
        LatLng adihome = new LatLng(47.2638, 11.3766);
        LatLng uni = new LatLng(47.2641, 11.3445);*/


        //get mapfragment
        googleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        //get textviews
        textViewDistance = (TextView) rootView.findViewById(R.id.tv_distance);
        textViewTime = (TextView) rootView.findViewById(R.id.tv_time);
        textViewSpeed = (TextView) rootView.findViewById(R.id.tv_speed);
        textViewAvSpeed = (TextView) rootView.findViewById(R.id.tv_avspeed);
        textViewCo2 = (TextView) rootView.findViewById(R.id.tv_co2);


        //move Camera to position
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now, 18));

        //new positionlist for all positions
        positionlist = new ArrayList<LatLng>();

        return rootView;
    }



    @Override
    public void onDestroy() {
        if (isTracking == true){
            locationManager.removeUpdates(locationListener);
        }
        if (trackingStart == true){
            handler.removeCallbacks(runnableStopwatch);
        }
        super.onDestroy();
    }





    //START BUTTON
    public void startButton(View view) {

        if(isTracking == false){

            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (gpsEnabled) {

                isTracking = true;
                final View v = view;
                googleMap.clear();
                textViewDistance.setText("0"+getString(R.string.distance_unit1));
                textViewTime.setText("0:00:00");
                textViewSpeed.setText("0" + getString(R.string.speed_unit));
                textViewAvSpeed.setText(getString(R.string.searching_for_gps));
                textViewCo2.setText("0" + getString(R.string.co2saved_unit1) + " CO2");

                Toast.makeText(view.getContext(), getString(R.string.started), Toast.LENGTH_SHORT).show();


                //create locationListener
                locationListener = new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {
                        positionlist.add(new LatLng(location.getLatitude(), location.getLongitude()));

                        if (trackingStart == false ){ //if tracking not started yet
                            isTracking = true;
                            trackingStart = true; //start it

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
                                    //int ms = (int) (time % 1000);
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
                                        avspeed = (distance / ((System.currentTimeMillis() - startTime) / 1000)) * 3.6;
                                        textViewAvSpeed.setText((int) avspeed + " " + getString(R.string.speed_unit) + " Ø" );
                                    }

                                    handler.postDelayed(this, 500);
                                }
                            };
                            handler.post(runnableStopwatch);

                            locationOld = location;
                            //positionOld = new LatLng(location.getLatitude(), location.getLongitude());
                            //positionOld2 = positionOld;
                            timeNew = System.currentTimeMillis();
                            timeOld = timeNew;
                            //Toast.makeText(v.getContext(), " Start Lat: " + location.getLatitude() + " Lon: " +
                            //        location.getLongitude()  + " - " + location.getProvider(), Toast.LENGTH_SHORT).show();

                            //Start Marker
                            googleMap.addMarker(new MarkerOptions()
                                    //.position(positionOld)
                                    .position(positionlist.get(0))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
                        } else { //tracking is allready running

                            //calculates distance betweend the 2 newest points
                            tempDistance = location.distanceTo(locationOld);
                            //adds this distance to the distance of the route
                            distance += tempDistance;

                            //positionNew = new LatLng(location.getLatitude(), location.getLongitude());
                            timeNew = System.currentTimeMillis();
                            timeDif = timeNew - timeOld;

                            //message
                            //Toast.makeText(v.getContext(), "Dist: " + distance + " - " + tempDistance +" Lat: " + location.getLatitude() + " Lon: " +
                            //        location.getLongitude()  + " - " + location.getProvider(), Toast.LENGTH_SHORT).show();


                            //change Camera Position to current position
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionlist.get(positionlist.size()-1), googleMap.getCameraPosition().zoom));

                            //Polyline (line betweend the tracked coordinates)
                            /*googleMap.addPolyline(new PolylineOptions()
                            .add(positionOld2, positionOld, positionNew)
                            .width(8)
                            .color(0xFF0000FF));*/
                            googleMap.clear();
                            //Start Marker
                            googleMap.addMarker(new MarkerOptions()
                                    .position(positionlist.get(0))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));

                            googleMap.addPolyline(new PolylineOptions()
                                    .addAll(positionlist)
                                    .width(8)
                                    .color(0xFF0000FF));

                            //set positions/time for the next call
                            //positionOld2 = positionOld;
                            //positionOld = positionNew;
                            locationOld = location;
                            timeOld = timeNew;

                        }
                        //calculate speed/co2
                        Double oldSpeed = speed;
                        Double oldAvspeed = avspeed;
                        speed = (tempDistance / (timeDif / 1000)) * 3.6;
                        if (speed > 300) {
                            speed = oldSpeed;
                        }
                        avspeed = (distance / ((System.currentTimeMillis() - startTime) / 1000)) * 3.6;
                        if (avspeed > 300){
                            avspeed = oldAvspeed;
                        }
                        co2 = (distance/1000) * co2km;

                        //actualice distance/speed/co2 in the textview
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
                        textViewAvSpeed.setText((int) avspeed + getString(R.string.speed_unit) + " Ø" );
                        textViewCo2.setText((int) co2 + getString(R.string.co2saved_unit1) + " CO2");

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

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);

            }else{
                //turn on GPS
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                Toast.makeText(getActivity(), getString(R.string.please_turn_on_gps), Toast.LENGTH_LONG).show();
            }

        } else{
            //start button already pressed
            Toast.makeText(view.getContext(), getString(R.string.allready_started), Toast.LENGTH_SHORT).show();
        }

    }



    //STOP BUTTON
    public void stopButton(View view) {
        if(isTracking == true){

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(getString(R.string.stop_tracking_question));
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.stop_tracking), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Stop tracking
                    locationManager.removeUpdates(locationListener);
                    isTracking = false;
                    if (trackingStart == true){
                        //End Marker
                        googleMap.addMarker(new MarkerOptions()
                                .position(positionlist.get(positionlist.size()-1))
                                        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ziel)));

                        endTimeDate = new Date();
                        endTimeDate.setTime(System.currentTimeMillis());
                        handler.removeCallbacks(runnableStopwatch);
                    } else{
                        textViewAvSpeed.setText("0" + getString(R.string.speed_unit) + " Ø" );
                        startTime = 0;
                        distance = 0;
                    }

                    textViewSpeed.setText("0" + getString(R.string.speed_unit));
                    //Toast.makeText(view.getContext(), "stoped: startt: " + startTimeDate + " endt: " + endTimeDate, Toast.LENGTH_LONG).show();

                    //transmit route to server.......

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

        } else{
            Toast.makeText(view.getContext(), getString(R.string.not_started), Toast.LENGTH_SHORT).show();



        }
    }





}
