package at.xidev.bikechallenge.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by int3r on 31.03.2014.
 */
public class FragmentDrive extends Fragment {

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
        return rootView;
    }
}
