package at.xidev.bikechallenge.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by int3r on 31.03.2014.
 */
public class FragmentSocial extends Fragment {
    public FragmentSocial() {
        // Required empty public constructor
    }

    public static FragmentSocial newInstance() {
        FragmentSocial fragment = new FragmentSocial();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_social, container, false);
    }
}
