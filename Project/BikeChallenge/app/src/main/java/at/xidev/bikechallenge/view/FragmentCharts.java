package at.xidev.bikechallenge.view;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import at.xidev.bikechallenge.core.AppFacade;
import at.xidev.bikechallenge.model.Statistic;


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

    private View chartsProgressView;
    private SwipeRefreshLayout swipeLayout;

    private ChartsExpandableListAdapter chartsExpandableListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_charts, container, false);

        // Actionbar Menu Items
        setHasOptionsMenu(true);

        // Get progress view
        chartsProgressView = view.findViewById(R.id.charts_progress);

        // Setup expandable list and adapter
        chartsExpandableListAdapter = new ChartsExpandableListAdapter(view.getContext());
        ExpandableListView elv = (ExpandableListView) view.findViewById(R.id.charts_expandableListView);
        elv.setAdapter(chartsExpandableListAdapter);

        // Fill list
        reloadList();

        // Setup swipe refresh
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.charts_swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadList();
            }
        });
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.charts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_charts_refresh) {
            reloadList();
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadList() {
        // setup task und execute
        TaskGetStatistic task = new TaskGetStatistic();
        task.execute();
    }

    private void reloadList(Statistic statistic) {
        if (statistic == null)
            return;

        List<ChartCategory> expandableElements = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("0.00");

        ChartCategory category = new ChartCategory();
        category.name = getString(R.string.charts_economy);
        category.elements.add(new ChartElement(getString(R.string.charts_economy_emissions), df.format(statistic.getEmissions()) + " " + getString(R.string.charts_emission_metric)));
        category.elements.add(new ChartElement(getString(R.string.charts_economy_fuel), df.format(statistic.getFuel()) + " " + getString(R.string.charts_fuel_metric)));
        expandableElements.add(category);

        category = new ChartCategory();
        category.name = getString(R.string.charts_time);
        category.elements.add(new ChartElement(getString(R.string.charts_time_avg), AppFacade.getInstance().formatTimeElapsedSinceMillisecond(statistic.getAvgTime())));
        category.elements.add(new ChartElement(getString(R.string.charts_time_longest), AppFacade.getInstance().formatTimeElapsedSinceMillisecond(statistic.getLongestTime())));
        category.elements.add(new ChartElement(getString(R.string.charts_time_total), AppFacade.getInstance().formatTimeElapsedSinceMillisecond(statistic.getTotalTime())));
        List<Double> values = new ArrayList<>();
        for (Long l : statistic.getLast7DaysTimes())
            values.add(l.doubleValue());
        category.elements.add(new ChartElement(getString(R.string.charts_time_last7days), values));
        expandableElements.add(category);

        category = new ChartCategory();
        category.name = getString(R.string.charts_distances);
        category.elements.add(new ChartElement(getString(R.string.charts_distances_avg), df.format(statistic.getAvgDistance() / 1000.0) + " " + getString(R.string.charts_distance_metric)));
        category.elements.add(new ChartElement(getString(R.string.charts_distances_longest), df.format(statistic.getLongestDistance() / 1000.0) + " " + getString(R.string.charts_distance_metric)));
        category.elements.add(new ChartElement(getString(R.string.charts_distances_total), df.format(statistic.getTotalDistance() / 1000.0) + " " + getString(R.string.charts_distance_metric)));
        category.elements.add(new ChartElement(getString(R.string.charts_distances_last7days), statistic.getLast7DaysDistances()));
        expandableElements.add(category);

        category = new ChartCategory();
        category.name = getString(R.string.charts_global);
        category.elements.add(new ChartElement(getString(R.string.charts_global_emissions), df.format(statistic.getGlobalEmissions()) + " " + getString(R.string.charts_emission_metric)));
        category.elements.add(new ChartElement(getString(R.string.charts_global_fuel), df.format(statistic.getGlobalFuel()) + " " + getString(R.string.charts_fuel_metric)));
        category.elements.add(new ChartElement(getString(R.string.charts_global_time), AppFacade.getInstance().formatTimeElapsedSinceMillisecond(statistic.getGlobalTime())));
        category.elements.add(new ChartElement(getString(R.string.charts_global_distance), df.format(statistic.getGlobalDistance() / 1000.0) + " " + getString(R.string.charts_distance_metric)));
        expandableElements.add(category);

        chartsExpandableListAdapter.setElements(expandableElements);
    }

    private class ChartCategory {
        String name;
        List<ChartElement> elements;

        ChartCategory() {
            super();
            elements = new ArrayList<>();
        }
    }

    private class ChartElement {
        String name;
        String value;
        List<Double> values;
        boolean isDiagram = false;

        ChartElement(String name, String value) {
            this.name = name;
            this.value = value;
            this.isDiagram = false;
        }

        ChartElement(String name, List<Double> values) {
            this.name = name;
            this.values = values;
            this.isDiagram = true;
        }
    }

    private class ChartsExpandableListAdapter extends BaseExpandableListAdapter {
        Context context;
        LayoutInflater inflater;

        List<ChartCategory> elements;

        ChartsExpandableListAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            elements = new ArrayList<>();
        }

        public void setElements(List<ChartCategory> elements) {
            // Set new content
            this.elements = elements;

            // notify observers -> refreshes list
            this.notifyDataSetChanged();
        }

        /**
         * Gets the number of groups.
         *
         * @return the number of groups
         */
        @Override
        public int getGroupCount() {
            return elements.size();
        }

        /**
         * Gets the number of children in a specified group.
         *
         * @param groupPosition the position of the group for which the children
         *                      count should be returned
         * @return the children count in the specified group
         */
        @Override
        public int getChildrenCount(int groupPosition) {
            return elements.get(groupPosition).elements.size();
        }

        /**
         * Gets the data associated with the given group.
         *
         * @param groupPosition the position of the group
         * @return the data child for the specified group
         */
        @Override
        public Object getGroup(int groupPosition) {
            return elements.get(groupPosition);
        }

        /**
         * Gets the data associated with the given child within the given group.
         *
         * @param groupPosition the position of the group that the child resides in
         * @param childPosition the position of the child with respect to other
         *                      children in the group
         * @return the data of the child
         */
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return elements.get(groupPosition).elements.get(childPosition);
        }

        /**
         * Gets the ID for the group at the given position. This group ID must be
         * unique across groups. The combined ID (see
         * {@link #getCombinedGroupId(long)}) must be unique across ALL items
         * (groups and all children).
         *
         * @param groupPosition the position of the group for which the ID is wanted
         * @return the ID associated with the group
         */
        @Override
        public long getGroupId(int groupPosition) {
            return elements.get(groupPosition).hashCode();
        }

        /**
         * Gets the ID for the given child within the given group. This ID must be
         * unique across all children within the group. The combined ID (see
         * {@link #getCombinedChildId(long, long)}) must be unique across ALL items
         * (groups and all children).
         *
         * @param groupPosition the position of the group that contains the child
         * @param childPosition the position of the child within the group for which
         *                      the ID is wanted
         * @return the ID associated with the child
         */
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return elements.get(groupPosition).elements.get(childPosition).hashCode();
        }

        /**
         * Indicates whether the child and group IDs are stable across changes to the
         * underlying data.
         *
         * @return whether or not the same ID always refers to the same object
         * @see android.widget.Adapter#hasStableIds()
         */
        @Override
        public boolean hasStableIds() {
            return false;
        }

        /**
         * Gets a View that displays the given group. This View is only for the
         * group--the Views for the group's children will be fetched using
         * {@link #getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)}.
         *
         * @param groupPosition the position of the group for which the View is
         *                      returned
         * @param isExpanded    whether the group is expanded or collapsed
         * @param convertView   the old view to reuse, if possible. You should check
         *                      that this view is non-null and of an appropriate type before
         *                      using. If it is not possible to convert this view to display
         *                      the correct data, this method can create a new view. It is not
         *                      guaranteed that the convertView will have been previously
         *                      created by
         *                      {@link #getGroupView(int, boolean, android.view.View, android.view.ViewGroup)}.
         * @param parent        the parent that this view will eventually be attached to
         * @return the View corresponding to the group at the specified position
         */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View v = inflater.inflate(R.layout.fragment_charts_expandable_group, parent, false);
            TextView tv = (TextView) v.findViewById(R.id.tv_charts_expandable_group);
            tv.setText(elements.get(groupPosition).name);
            return v;
        }

        /**
         * Gets a View that displays the data for the given child within the given
         * group.
         *
         * @param groupPosition the position of the group that contains the child
         * @param childPosition the position of the child (for which the View is
         *                      returned) within the group
         * @param isLastChild   Whether the child is the last child within the group
         * @param convertView   the old view to reuse, if possible. You should check
         *                      that this view is non-null and of an appropriate type before
         *                      using. If it is not possible to convert this view to display
         *                      the correct data, this method can create a new view. It is not
         *                      guaranteed that the convertView will have been previously
         *                      created by
         *                      {@link #getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)}.
         * @param parent        the parent that this view will eventually be attached to
         * @return the View corresponding to the child at the specified position
         */
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChartElement child = elements.get(groupPosition).elements.get(childPosition);

            View v;
            if (child.isDiagram) {
                v = inflater.inflate(R.layout.fragment_charts_expandable_child_diagram, parent, false);

                // Set text view
                TextView tv = (TextView) v.findViewById(R.id.tv_charts_expandable_child_name);
                tv.setText(child.name);

                // init data
                GraphViewData[] graphData = new GraphViewData[child.values.size()];
                for (int i = 0; i < child.values.size(); i++)
                    graphData[i] = new GraphViewData(i + 1, child.values.get(i));
                GraphViewSeries graphSeries = new GraphViewSeries("", new GraphViewSeries.GraphViewSeriesStyle(context.getResources().getColor(R.color.social_detail_graph1), (int) context.getResources().getDimension(R.dimen.charts_line_thickness)), graphData);

                // init graph, with empty title
                GraphView graphView = new LineGraphView(context, "");

                // add data
                graphView.addSeries(graphSeries);

                // Set labels and style
                graphView.setHorizontalLabels(new String[]{"-6", "-5", "-4", "-3", "-2", "-1", getResources().getString(R.string.social_detail_graph_today)});
                graphView.getGraphViewStyle().setGridColor(context.getResources().getColor(R.color.dark_transparent));
                graphView.getGraphViewStyle().setHorizontalLabelsColor(context.getResources().getColor(R.color.black));
                graphView.getGraphViewStyle().setVerticalLabelsColor(context.getResources().getColor(R.color.black));
                graphView.getGraphViewStyle().setNumHorizontalLabels(7);
                graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if (!isValueX) {
                            DecimalFormat df = new DecimalFormat("0.0");
                            return df.format(value / 1000);
                        }
                        return null; // let graphview generate X-axis label for us
                    }
                });

                // Add to view
                LinearLayout layout = (LinearLayout) v.findViewById(R.id.charts_expandable_diagram_container);
                layout.addView(graphView);
            } else {
                v = inflater.inflate(R.layout.fragment_charts_expandable_child, parent, false);
                TextView tv1 = (TextView) v.findViewById(R.id.tv_charts_expandable_child_name);
                TextView tv2 = (TextView) v.findViewById(R.id.tv_charts_expandable_child_value);

                tv1.setText(child.name);
                tv2.setText(child.value);
            }
            return v;
        }

        /**
         * Whether the child at the specified position is selectable.
         *
         * @param groupPosition the position of the group that contains the child
         * @param childPosition the position of the child within the group
         * @return whether the child is selectable.
         */
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    /**
     * Task to get Statistics object
     */
    private class TaskGetStatistic extends AsyncTask<Void, Void, Statistic> {
        boolean hasConnection = true;

        @Override
        protected Statistic doInBackground(Void... params) {
            try {
                return AppFacade.getInstance().getStatistic();
            } catch (IOException ex) {
                hasConnection = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected void onPostExecute(Statistic statistic) {
            if (hasConnection)
                reloadList(statistic);
            else
                Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();

            showProgress(false);
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Shows the progress UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            // TODO why two times setVisibility()? Shouldn't be animate() sufficient?
            chartsProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            chartsProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    chartsProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            chartsProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
