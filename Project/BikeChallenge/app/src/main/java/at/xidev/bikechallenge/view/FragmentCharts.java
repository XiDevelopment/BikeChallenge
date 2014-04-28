package at.xidev.bikechallenge.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by int3r on 31.03.2014.
 */
public class FragmentCharts extends Fragment {

    public static FragmentCharts newInstance() {
        FragmentCharts fragment = new FragmentCharts();
        return fragment;
    }

    public FragmentCharts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_charts, container, false);
    }


}
