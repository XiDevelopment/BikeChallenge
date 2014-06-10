package at.xidev.bikechallenge.view;

import at.xidev.bikechallenge.core.AppFacade;
import at.xidev.bikechallenge.model.Route;
import at.xidev.bikechallenge.model.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ComponentName;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.SupportMapFragment;

import org.apache.http.conn.HttpHostConnectException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private float distance = 0f;                 //distance of the current route
    private float tempDistance;             //distance between the last two gps coordinates
    private boolean isTracking = false;     //true if currently tracking
    private boolean trackingStart = false;  //start position for tracking received
    private Location locationOld;           //second newest location (for distance calculation)
    private View trackingView;              //view: tracking (while tracking. not visible at start)
    private View startView;                 //view: start (before tracking)
    private TextView textViewDistance;      //textview: distance
    private TextView textViewTime;          //textview: time
    private TextView textViewSpeed;         //textview: speed
    private View routeDetailsView;
    private TextView textViewRouteDistance;
    private TextView textViewRouteTime;
    private TextView textViewRouteAVSpeed;
    private Button startButton;             //start/stop button
    private Handler handler;                //handler (for stopwatch)
    private Runnable runnableStopwatch;     //runnable (for stopwatch)
    private long startTime = 0;             //start time of the route
    private long endTime = 0;               //end time of the route
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
    private SaveRouteTask mSaveRouteTask = null;
    private NotificationManager notificationManager;
    private String timeString;
    private String meterString;
    private RouteDialogFragment detailsDialog;


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

        if (!networkEnabled) {
            //if network position disabled: set position to a static position
            now = new LatLng(47.2641, 11.3445); //UNI
        } else {
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
        //routeDetailsView = inflater.inflate(R.layout.fragment_drive_route_details, null);
        startButton = (Button) rootView.findViewById(R.id.button_start);
        //textViewRouteDistance = (TextView) routeDetailsView.findViewById(R.id.tv_route_distance);
        //textViewRouteTime = (TextView) routeDetailsView.findViewById(R.id.tv_route_time);
        //textViewRouteAVSpeed = (TextView) routeDetailsView.findViewById(R.id.tv_route_avspeed);

        //detailsDialog = new RouteDialogFragment();

        //move Camera to position
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now, 18));
        //map screenshot
        /*googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    public void onSnapshotReady(Bitmap bitmap) {
                        // Write image to disk
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream("/mnt/sdcard/map.png");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    }
                });
            }
        });*/

        //new positionlist (for all positions)
        positionlist = new ArrayList<LatLng>();

        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        TextView name = (TextView) getActivity().findViewById(R.id.tv_name);
        ImageView picture = (ImageView) getActivity().findViewById(R.id.iv_user);
        TextView score = (TextView) getActivity().findViewById(R.id.tv_userpoints);
        name.setText(AppFacade.getInstance().getUser().getName());
        score.setText(AppFacade.getInstance().getUser().getScore() + " points");
    }


    @Override
    public void onDestroy() {
        if (isTracking == true) {
            //remove locationlistener (if started)
            locationManager.removeUpdates(locationListener);
            //remove notification (if started)
            notificationManager.cancelAll();
            googleMap.setMyLocationEnabled(false);
            this.enableAutoLock();
        }
        if (trackingStart == true) {
            //remove stopwatch (if started)
            handler.removeCallbacks(runnableStopwatch);
        }
        super.onDestroy();
    }


    //START BUTTON
    public void startButton(final View view) {

        //if tracking was not running before
        if (isTracking == false) {

            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (gpsEnabled) {

                isTracking = true;
                googleMap.clear();
                textViewDistance.setTextSize(20);
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


                //Notification (tracking notification if app is not in foreground)
                final Intent emptyIntent = new Intent();
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
                                .setContentTitle("BikeChallenge")
                                .setContentText("currently tracking...")
                                .setContentIntent(pendingIntent); //Required on Gingerbread and below

                notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, mBuilder.build());


                /*KeyguardManager keyguardManager = (KeyguardManager)getActivity().getSystemService(getActivity().KEYGUARD_SERVICE);
                KeyguardManager.KeyguardLock lock = KeyguardManager.KeyguardLock(keyguardManager);//keyguardManager.KeyguardLock(keyguardManager);// .newKeyguardLock(KEYGUARD_SERVICE);

                DevicePolicyManager mDPM;
                ComponentName mDeviceAdminSample;

                mDPM = (DevicePolicyManager)getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
                mDeviceAdminSample = new ComponentName(getActivity(),
                        getActivity().getClass());

                Intent intent = new   Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, true);*/

                this.disableAutoLock();


                //create locationListener:
                locationListener = new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {
                        positionlist.add(new LatLng(location.getLatitude(), location.getLongitude()));
                        //Toast.makeText(getActivity(), "acc: " + location.getAccuracy(), Toast.LENGTH_SHORT).show();

                        if (trackingStart == false) { //tracking not started yet (0 or only imprecise positions yet)

                            googleMap.setMyLocationEnabled(true);
                            //start tracking only if accuracy is good enough. depending on amount of positions.
                            if ((positionlist.size() < 4)) {
                                if (location.getAccuracy() < 11.0) {
                                    trackingStart = true; //start it
                                    Toast.makeText(getActivity(), "1acc: " + location.getAccuracy() + " - " + positionlist.size(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "not1acc: " + location.getAccuracy() + " - " + positionlist.size(), Toast.LENGTH_SHORT).show();
                                    textViewDistance.setTextSize(20);
                                    textViewDistance.setText("waiting for better GPS accuracy...");
                                }
                            } else if (positionlist.size() < 7) {
                                if (location.getAccuracy() < 14.0) {
                                    trackingStart = true; //start it
                                    Toast.makeText(getActivity(), "2acc: " + location.getAccuracy() + " - " + positionlist.size(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "not2acc: " + location.getAccuracy() + " - " + positionlist.size(), Toast.LENGTH_SHORT).show();
                                }
                            } else if (positionlist.size() < 10) {
                                if (location.getAccuracy() < 19.0) {
                                    trackingStart = true; //start it
                                    Toast.makeText(getActivity(), "3acc: " + location.getAccuracy() + " - " + positionlist.size(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "not3acc: " + location.getAccuracy() + " - " + positionlist.size(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "4acc: " + location.getAccuracy() + " - " + positionlist.size(), Toast.LENGTH_SHORT).show();
                                trackingStart = true; //start it (anyway)
                            }

                            //start tracking (if position is accurate)
                            if (trackingStart == true) {
                                Toast.makeText(getActivity(), "5start: " + location.getAccuracy(), Toast.LENGTH_SHORT).show();
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

                                        textViewTime.setText(getRideTimeString(time));
                                        //textViewRouteTime.setText(timeString);

                                        //user not moving (no new gps coordinates)
                                        if ((System.currentTimeMillis() - timeNew) > 8000) {
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

                            //circle at current position
                            /*googleMap.addCircle(new CircleOptions()
                                    .center(positionlist.get(positionlist.size()-1))
                                    .radius(2)
                                    .strokeColor(0xFF0000FF)
                                    .fillColor(0xFF0000FF));*/

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
                        if (trackingStart == true) {
                            textViewDistance.setText(getDistanceString());
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

            } else {
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


        } else {
            //tracking already running (stop)
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(getString(R.string.stop_tracking_question));
            builder.setPositiveButton(getString(R.string.stop_tracking), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Stop tracking
                    locationManager.removeUpdates(locationListener);
                    googleMap.setMyLocationEnabled(false);
                    isTracking = false;
                    notificationManager.cancelAll();
                    if (trackingStart == true) {
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
                        endTime = System.currentTimeMillis();
                        //endTimeDate.setTime(System.currentTimeMillis());
                        //stop stopwatch
                        handler.removeCallbacks(runnableStopwatch);


                        //transmit route to server.......


                    } else {
                        //stoped tracking before gps was ready
                        textViewDistance.setTextSize(40);
                        textViewDistance.setText("0" + getString(R.string.distance_unit1));

                        distance = 0f;
                        startTimeDate = new Date();
                        endTimeDate = new Date();
                        endTime = System.currentTimeMillis() + 1;
                    }

                    //avs speed
                    //textViewRouteAVSpeed.setText( ((int) ((distance / ((endTime - startTime) / 1000)) * 3.6)) + getString(R.string.speed_unit) );

                    enableAutoLock();

                    //transmit rout to server test (also with 0m)

                    //fill route with information
                    Route route = new Route();
                    route.setDistance(distance);
                    route.setStartTime(startTimeDate);
                    route.setStopTime(endTimeDate);
                    route.setUserId(AppFacade.getInstance().getUser().getId());

                    //save route
                    mSaveRouteTask = new SaveRouteTask(route);
                    mSaveRouteTask.execute((Void) null);
                    //display route info
                    Toast.makeText(getActivity(), "std: " + startTimeDate + " - etd: " + endTimeDate + " dist: " + distance + " userID: " + AppFacade.getInstance().getUser().getId(), Toast.LENGTH_LONG).show();


                    //problem: second show ...
                    detailsDialog = new RouteDialogFragment(route);
                    detailsDialog.show(getFragmentManager(), "Route");


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


    /**
     * Represents an asynchronous task to commit the route to the server
     */
    public class SaveRouteTask extends AsyncTask<Void, Void, Boolean> {

        private final Route mRoute;
        private boolean noConnection = false;
        private String resp = "";
        private User user = null;
        private Thread t;

        SaveRouteTask(Route route) {
            mRoute = route;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                //TODO: maybe move that into AppFACADE
                t = new Thread() {
                    public void run() {
                        try {
                            sleep(30000);
                        } catch (InterruptedException e) {

                        }
                        if (mSaveRouteTask != null)
                            mSaveRouteTask.cancel(true);
                    }
                };
                t.start();
                //AppFacade.getInstance().login(mUsername, mPassword);
                //user = AppFacade.getInstance().getUser();


                //save route

                AppFacade.getInstance().saveRoute(mRoute);
                //AppFacade.getInstance().getRoutes(mRoute.getUserId());


            } catch (HttpHostConnectException e) {
                noConnection = true;
                //Toast.makeText(getApplicationContext(), R.string.error_no_connection, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                //TODO: exception handling
                e.printStackTrace();
            }
            return user != null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveRouteTask = null;

            if (success) {
                //save username and encrypted password
               /* SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("username", mUsername);
                editor.putString("password", mPassword);
                editor.putBoolean("loggedIn", true);
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                //calling finish to prevent back button functionalities
                finish();*/
            } else {
                /*showProgress(false);
                if(noConnection) {
                    Button mUsernameSignInButton = (Button) findViewById(R.id.login_sign_in_button);
                    mUsernameSignInButton.setError(getString(R.string.error_no_connection));
                    mUsernameSignInButton.requestFocus();
                } else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }*/
            }
        }

        @Override
        protected void onCancelled() {
            mSaveRouteTask = null;
            //showProgress(false);
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

        //getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        //DevicePolicyManager mDPM;
        //mDPM = (DevicePolicyManager)getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
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
        String dist;
        String tim;
        String avss;

        public RouteDialogFragment(Route route) {
            this.dist = getDistanceString();
            this.tim = getRideTimeString (route.getStopTime().getTime() - route.getStartTime().getTime());
            this.avss = calculateAverageSpeedString(route.getDistance(), (route.getStopTime().getTime() - route.getStartTime().getTime()));
            //this.avss = "" + ( (int) ((route.getDistance() / ((route.getStopTime().getTime() - route.getStartTime().getTime()) / 1000 )) * 3.6 )) + getString(R.string.speed_unit);
        }


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View view = inflater.inflate(R.layout.fragment_drive_route_details, null);

            // Setup Values


            /*//distance
            if (distance < 1000) {
                dist.setText(distance + getString(R.string.distance_unit1));
            } else {
                dist.setText((int) (distance / 1000) + meterString + (int) (distance % 1000) + getString(R.string.distance_unit2));
            }

            //time
            ti.setText(timeString);
            */
            routeDetailsView = inflater.inflate(R.layout.fragment_drive_route_details, null);
            textViewRouteDistance = (TextView) view.findViewById(R.id.tv_route_distance);
            textViewRouteTime = (TextView) view.findViewById(R.id.tv_route_time);
            textViewRouteAVSpeed = (TextView) view.findViewById(R.id.tv_route_avspeed);

            textViewRouteDistance.setText(this.dist);
            textViewRouteTime.setText(this.tim);
            textViewRouteAVSpeed.setText(this.avss);


            // Build
            builder.setView(view);
            return builder.create();
        }
    }

    private String getDistanceString() {
        if (distance > 0) {
            if (distance < 1000) {
                return ((int) distance + getString(R.string.distance_unit1));
            } else {
                meterString = new String();
                meterString += getString(R.string.comma);
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
                return ((int) (distance / 1000) + meterString + m + getString(R.string.distance_unit2));
            }
        } else {
            return "0" + getString(R.string.distance_unit1);
        }
    }

    private String getRideTimeString(long time) {

        int sec = (int) ((time / 1000) % 60);
        int min = (int) ((time / 60000) % 60);
        int h = (int) (time / 3600000);

        timeString=new String();

        timeString+=h;
        timeString+=":";
        if(min<10){
            timeString += "0";
        }

        timeString+=min;
        timeString+=":";
        if(sec<10){
            timeString += "0";
        }

        timeString+=sec;

        return timeString;
    }

    private String calculateAverageSpeedString(float dist, long time){
        int avsSpeed = (int) ((dist/(time/1000))*3.6);
        return "" + avsSpeed + getString(R.string.speed_unit);
    }



}
