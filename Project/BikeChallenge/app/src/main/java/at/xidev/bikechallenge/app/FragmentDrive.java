package at.xidev.bikechallenge.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.location.LocationManager;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.widget.Toast;
import android.widget.Button;
import android.content.Intent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.SupportMapFragment;


/**
 * Created by int3r on 31.03.2014.
 */
public class FragmentDrive extends Fragment {
    private LocationManager locationManager;
    private String provider;
    private Location location;
    private LatLng now;

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


        //get Position
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled && !networkEnabled) {
            //turn on GPS (or WLAN)
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            now = new LatLng(47.2641, 11.3445);
            Toast.makeText(getActivity(), "BITTE GPS EINSCHALTEN", Toast.LENGTH_LONG).show();
        }else if (!gpsEnabled && networkEnabled){
            //only network is available
            provider = locationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(provider);
            Toast.makeText(getActivity(), "Lat: " + location.getLatitude() + " . Lon: " +
                    location.getLongitude()  + " - " + location.getProvider(), Toast.LENGTH_LONG).show();

            //Coordinates of current Position (WLAN)
            now = new LatLng(location.getLatitude(), location.getLongitude());
        }else{
            //gps and network are available. use gps
            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);
            location = locationManager.getLastKnownLocation(provider);
            Toast.makeText(getActivity(), "Lat: " + location.getLatitude() + " . Lon: " +
                    location.getLongitude()  + " - " + location.getProvider(), Toast.LENGTH_LONG).show();

            //Coordinates of current Position (GPS)
            now = new LatLng(location.getLatitude(), location.getLongitude());
        }





        //Coordinates of Jakob and Ricks Home
        LatLng jackrickhome = new LatLng(47.266, 11.399);
        LatLng michome = new LatLng(47.259, 11.390);
        LatLng adihome = new LatLng(47.2638, 11.3766);
        LatLng uni = new LatLng(47.2641, 11.3445);


        //get Mapfragment
        GoogleMap googleMap;
        googleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        //move Camera to position
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now, 16));

        //Map Marker
        googleMap.addMarker(new MarkerOptions()
                .position(jackrickhome)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        //Polyline
        googleMap.addPolyline(new PolylineOptions()
                .add(jackrickhome, michome, adihome, uni, now)
                .width(8)
                .color(0xFF0000FF));

        return rootView;
    }




}
