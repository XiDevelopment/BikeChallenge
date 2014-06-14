package at.xidev.bikechallenge.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import at.xidev.bikechallenge.core.AppFacade;
import at.xidev.bikechallenge.model.Route;
import at.xidev.bikechallenge.model.User;


/**
 * Created by Michael Staudacher on 06.05.2014.
 */
public class FragmentDrive extends Fragment {
    private LocationManager locationManager;//Manager (to get the locations)
    private String positionProvider;        //GPS or Network Provider
    private boolean gpsEnabled = false;     //GPS allowed on Phone
    private boolean networkEnabled = false; //Network Locaton allowed on Phone
    private float distance = 0f;            //distance of the current route
    private float tempDistance;             //distance between the last two gps coordinates
    private boolean isTracking = false;     //true if currently tracking
    private boolean trackingStart = false;  //start position for tracking received
    private Location locationOld;           //second newest location (for distance calculation)
    private View trackingView;              //view: tracking (while tracking. not visible at start)
    private View startView;                 //view: start (before tracking)
    private TextView textViewDistance;      //textview: distance
    private TextView textViewTime;          //textview: time
    private TextView textViewSpeed;         //textview: speed
    private TextView textViewRouteDistance; //textview: distance (at save route)
    private TextView textViewRouteTime;     //textview: time (at save route)
    private TextView textViewRouteAVSpeed;  //textview: avspeed (at save route)
    private TextView textViewRouteCO2;      //textview: co2 (at save route)
    private TextView textViewRoutePoints;   //textview: points (at save route)
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
    private SaveRouteTask mSaveRouteTask = null;//task to save route to server
    private NotificationManager notificationManager;//notification manager for tracking notification
    private String timeString;              //tracking time as String
    private String meterString;             //tracked meters as String
    private RouteDialogFragment detailsDialog;//dialog to show and save route
    private View rootView;


    public static FragmentDrive newInstance() {
        FragmentDrive fragment = new FragmentDrive();
        return fragment;
    }


    public FragmentDrive() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_drive, container, false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        ImageView iv = (ImageView) rootView.findViewById(R.id.iv_user);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DialogFragment.show() will take care of adding the fragment
                // in a transaction.  We also want to remove any currently showing
                // dialog, so make our own transaction and take care of that here.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("AvatarDialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogAvatarSelection dialog = new DialogAvatarSelection(new DialogAvatarSelection.AvatarSelectionListener() {
                    @Override
                    public void onCloseDialog() {
                        ((MainActivity) getActivity()).reloadData();
                    }
                });
                dialog.show(getFragmentManager(), "AvatarDialog");
            }
        });

        //checks if gps is enable
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!networkEnabled) {
            //if network position disabled: set position to a static position
            now = new LatLng(47.2641, 11.3445); //UNI
        } else {
            positionProvider = locationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(positionProvider);

            //Coordinates of current Position (Network)
            now = new LatLng(location.getLatitude(), location.getLongitude());
        }

        //get mapfragment
        googleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        //get textviews
        trackingView = rootView.findViewById(R.id.tracking_view);
        startView = rootView.findViewById(R.id.start_view);
        textViewDistance = (TextView) rootView.findViewById(R.id.tv_distance);
        textViewTime = (TextView) rootView.findViewById(R.id.tv_time);
        textViewSpeed = (TextView) rootView.findViewById(R.id.tv_speed);
        //routeDetailsView = inflater.inflate(R.layout.fragment_drive_route_details, null);
        startButton = (Button) rootView.findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButton(view);
            }
        });

        //move Camera to position
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now, 18));

        //new position list (for all positions)
        positionlist = new ArrayList<>();

        // reload user details
        reload();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView name = (TextView) getActivity().findViewById(R.id.tv_name);
        TextView score = (TextView) getActivity().findViewById(R.id.tv_userpoints);
        name.setText(AppFacade.getInstance().getUser().getName());
        score.setText(AppFacade.getInstance().getUser().getScore() + getString(R.string.drive_points_unit));
    }


    @Override
    public void onDestroy() {
        if (isTracking) {
            //remove locationlistener (if started)
            locationManager.removeUpdates(locationListener);
            //remove notification (if started)
            notificationManager.cancelAll();
            googleMap.setMyLocationEnabled(false);
            this.enableAutoLock();
        }
        if (trackingStart) {
            //remove stopwatch (if started)
            handler.removeCallbacks(runnableStopwatch);
        }
        super.onDestroy();
    }


    //START BUTTON
    public void startButton(final View view) {

        //if tracking was not running before
        if (!isTracking) {

            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (gpsEnabled) {

                isTracking = true;
                googleMap.clear();
                textViewDistance.setTextSize(20);
                textViewDistance.setText(getString(R.string.drive_searching_for_gps));
                textViewTime.setText(getString(R.string.drive_empty_time));
                textViewSpeed.setText(getString(R.string.drive_empty) + getString(R.string.drive_speed_unit));
                startButton.setText(R.string.drive_stop);
                startButton.setBackgroundResource(R.drawable.red_button);

                //change from start to tracking (view)
                startView.setVisibility(View.GONE);
                trackingView.setVisibility(View.VISIBLE);

                //Notification (tracking notification if app is not in foreground)
                Intent notificationIntent = new Intent(getActivity(), getActivity().getClass());

                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //PendingIntent pendingIntent = PendingIntent.getActivity(ctx, NOT_USED, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getActivity())
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setLargeIcon(largeIcon)
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText(getString(R.string.drive_currently_tracking))
                                .setContentIntent(pendingIntent); //Required on Gingerbread and below

                notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, mBuilder.build());

                this.disableAutoLock();

                //create locationListener:
                locationListener = new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {
                        positionlist.add(new LatLng(location.getLatitude(), location.getLongitude()));

                        if (!trackingStart) { //tracking not started yet (0 or only imprecise positions yet)

                            googleMap.setMyLocationEnabled(true);
                            //start tracking only if accuracy is good enough. depending on amount of positions.
                            if ((positionlist.size() < 4)) {
                                if (location.getAccuracy() < 11.0) {
                                    trackingStart = true; //start it
                                } else {
                                    textViewDistance.setTextSize(20);
                                    textViewDistance.setText(getString(R.string.drive_waiting_for_better_gps_accuracy));
                                }
                            } else if (positionlist.size() < 7) {
                                if (location.getAccuracy() < 14.0) {
                                    trackingStart = true; //start it
                                }
                            } else if (positionlist.size() < 10) {
                                if (location.getAccuracy() < 19.0) {
                                    trackingStart = true; //start it
                                }
                            } else {
                                trackingStart = true; //start it (anyway)
                            }

                            //start tracking (if position is accurate)
                            if (trackingStart) {
                                //Toast.makeText(getActivity(), "5start: " + location.getAccuracy(), Toast.LENGTH_SHORT).show();
                                textViewDistance.setTextSize(40);
                                //delete imprecise positions (only add newest)
                                positionlist.clear();
                                positionlist.add(new LatLng(location.getLatitude(), location.getLongitude()));

                                //trackingStart = true; //start it
                                isTracking = true; //start (if not yet)

                                //stopwatch
                                startTime = System.currentTimeMillis();
                                startTimeDate = new Date();
                                //startTimeDate.setTime(System.currentTimeMillis());
                                handler = new Handler();
                                runnableStopwatch = new Runnable() {
                                    public void run() {
                                        long time = System.currentTimeMillis() - startTime;

                                        textViewTime.setText(getDriveTimeString(time));
                                        //textViewRouteTime.setText(timeString);

                                        //user not moving (no new gps coordinates)
                                        if ((System.currentTimeMillis() - timeNew) > 8000) {
                                            textViewSpeed.setText(getString(R.string.drive_empty) + getString(R.string.drive_speed_unit));
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
                            }
                        } else { //tracking is already running

                            //calculates distance between the 2 newest points
                            tempDistance = location.distanceTo(locationOld);
                            //adds this distance to the distance of the route
                            distance += tempDistance;

                            //positionNew = new LatLng(location.getLatitude(), location.getLongitude());
                            timeNew = System.currentTimeMillis();
                            timeDif = timeNew - timeOld;

                            //change Camera Position to current position
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionlist.get(positionlist.size() - 1), googleMap.getCameraPosition().zoom));

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
                        if (trackingStart) {
                            textViewDistance.setText(getDistanceString());
                        }
                        textViewSpeed.setText((int) speed + getString(R.string.drive_speed_unit));

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

            } else {
                //turn on GPS
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage(getString(R.string.drive_turn_on_gps_question));
                builder.setPositiveButton(getString(R.string.drive_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //bring user to (turn on GPS screen)
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        Toast.makeText(getActivity(), getString(R.string.drive_please_turn_on_gps), Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton(getString(R.string.drive_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Continue
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }


        } else {
            //tracking already running (stop)
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(getString(R.string.drive_stop_tracking_question));
            builder.setPositiveButton(getString(R.string.drive_stop_tracking), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Stop tracking
                    locationManager.removeUpdates(locationListener);
                    googleMap.setMyLocationEnabled(false);
                    isTracking = false;
                    notificationManager.cancelAll();
                    if (trackingStart) {
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
                                .position(positionlist.get(positionlist.size() - 1))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ziel)));

                        endTimeDate = new Date();
                        //stop stopwatch
                        handler.removeCallbacks(runnableStopwatch);

                    } else {
                        //stopped tracking before gps was ready
                        textViewDistance.setTextSize(40);
                        textViewDistance.setText(getString(R.string.drive_empty) + getString(R.string.drive_distance_unit1));

                        distance = 0f;
                        startTimeDate = new Date();
                        endTimeDate = new Date();
                    }

                    enableAutoLock();

                    //save route to server
                    if (distance > 0) {
                        //fill route with information
                        Route route = new Route();
                        route.setDistance(distance);
                        route.setStartTime(startTimeDate);
                        route.setStopTime(endTimeDate);
                        route.setUserId(AppFacade.getInstance().getUser().getId());
                        //start dialog to show and save route
                        detailsDialog = new RouteDialogFragment(route);
                        detailsDialog.show(getFragmentManager(), getString(R.string.drive_route));
                    }

                    //set speed to 0
                    textViewSpeed.setText(getString(R.string.drive_empty) + getString(R.string.drive_speed_unit));

                    //make stop button to start button again
                    startButton.setText(getString(R.string.drive_start));
                    startButton.setBackgroundResource(R.drawable.green_button);

                    //clean values
                    trackingStart = false;
                    positionlist.clear();
                    tempDistance = 0;
                    distance = 0;
                }
            });

            builder.setNegativeButton(getString(R.string.drive_continue_tracking), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Continue
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }


    /**
     * Represents an asynchronous task to commit the route to the server
     */
    public class SaveRouteTask extends AsyncTask<Void, Void, Boolean> {

        private final Route mRoute;
        private User user = null;
        private Thread t;

        private boolean hasConnection = true;

        SaveRouteTask(Route route) {
            mRoute = route;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //save route
                AppFacade.getInstance().saveRoute(mRoute);

                //update user
                AppFacade.getInstance().updateUser();

            } catch (IOException e) {
                Log.e("Drive", e.getMessage());
                hasConnection = false;
            }
            return user != null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (hasConnection)
                ((MainActivity) getActivity()).reloadData();
            else
                Toast.makeText(getActivity(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings({"deprecation"})
    public void disableAutoLock() {
        KeyguardManager keyguardManager;
        KeyguardManager.KeyguardLock lock;
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        keyguardManager = (KeyguardManager) getActivity().getSystemService(Activity.KEYGUARD_SERVICE);
        lock = keyguardManager.newKeyguardLock("lock");
        lock.disableKeyguard();
    }

    @SuppressWarnings({"deprecation"})
    public void enableAutoLock() {
        KeyguardManager keyguardManager;
        KeyguardManager.KeyguardLock lock;
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        keyguardManager = (KeyguardManager) getActivity().getSystemService(Activity.KEYGUARD_SERVICE);
        lock = keyguardManager.newKeyguardLock("lock");
        lock.reenableKeyguard();
    }

    private class RouteDialogFragment extends DialogFragment {
        //User friend;
        Route route;
        String distance;
        String time;
        String avspeed;
        String co2;
        String points;
        DecimalFormat df = new DecimalFormat("0.00");

        public RouteDialogFragment(Route route) {
            this.distance = getDistanceString();
            this.time = getDriveTimeString(route.getStopTime().getTime() - route.getStartTime().getTime());
            this.avspeed = calculateAverageSpeedString(route.getDistance(), (route.getStopTime().getTime() - route.getStartTime().getTime()));
            this.co2 = "" + df.format(route.getDistance() * 0.185) + "g";
            this.points = "" + ((int) (route.getDistance() / 200));
            this.route = route;
        }


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View view = inflater.inflate(R.layout.fragment_drive_route_details, null);

            //get views
            textViewRouteDistance = (TextView) view.findViewById(R.id.tv_route_distance);
            textViewRouteTime = (TextView) view.findViewById(R.id.tv_route_time);
            textViewRouteAVSpeed = (TextView) view.findViewById(R.id.tv_route_avspeed);
            textViewRouteCO2 = (TextView) view.findViewById(R.id.tv_route_co2);
            textViewRoutePoints = (TextView) view.findViewById(R.id.tv_route_points);

            //setup values
            textViewRouteDistance.setText(this.distance);
            textViewRouteTime.setText(this.time);
            textViewRouteAVSpeed.setText(this.avspeed);
            textViewRouteCO2.setText(this.co2);
            textViewRoutePoints.setText(this.points);


            // Build
            builder.setView(view);
            builder.setNegativeButton(getString(R.string.drive_delete_route), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });

            builder.setPositiveButton(getString(R.string.drive_save_route), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //save route
                    mSaveRouteTask = new SaveRouteTask(route);
                    mSaveRouteTask.execute((Void) null);
                }
            });

            return builder.create();
        }
    }

    private String getDistanceString() {
        if (distance > 0) {
            if (distance < 1000) {
                return ((int) distance + getString(R.string.drive_distance_unit1));
            } else {
                meterString = "";
                meterString += getString(R.string.drive_comma);
                int m = (int) (distance % 1000);
                if (m < 100) {
                    meterString += "0";
                    if (m < 10) {
                        meterString += "0";
                        if (m < 1) {
                            meterString += "0";
                        }
                    }
                }
                return ((int) (distance / 1000) + meterString + m + getString(R.string.drive_distance_unit2));
            }
        } else {
            return "0" + getString(R.string.drive_distance_unit1);
        }
    }

    private String getDriveTimeString(long time) {

        int sec = (int) ((time / 1000) % 60);
        int min = (int) ((time / 60000) % 60);
        int h = (int) (time / 3600000);

        timeString = "";

        timeString += h;
        timeString += ":";
        if (min < 10) {
            timeString += "0";
        }

        timeString += min;
        timeString += ":";
        if (sec < 10) {
            timeString += "0";
        }

        timeString += sec;

        return timeString;
    }

    private String calculateAverageSpeedString(float dist, long time) {
        int avSpeed = (int) ((dist / (time / 1000)) * 3.6);
        return "" + avSpeed + getString(R.string.drive_speed_unit);
    }

    public void reload() {
        User user = AppFacade.getInstance().getUser();

        // Update name
        TextView name = (TextView) rootView.findViewById(R.id.tv_name);
        name.setText(user.getName());

        // Update image
        ImageView image = (ImageView) rootView.findViewById(R.id.iv_user);
        image.setImageDrawable(AppFacade.getInstance().getAvatar(user.getAvatar(), getActivity()));

        // Update score
        TextView score = (TextView) rootView.findViewById(R.id.tv_userpoints);
        score.setText(user.getScore() + " points");
    }


}