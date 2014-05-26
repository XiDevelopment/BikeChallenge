package at.xidev.bikechallenge.view;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.List;


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
        View view = inflater.inflate(R.layout.fragment_charts, container, false);


        List<ChartCategory> expandableElements = new ArrayList<>();
        ChartCategory category = new ChartCategory();
        category.name = "Test1";
        category.elems.add(new ChartElement("name1", "value1"));
        category.elems.add(new ChartElement("name2", "value2"));
        category.elems.add(new ChartElement("name3", "value3"));
        expandableElements.add(category);

        category = new ChartCategory();
        category.name = "Test2";
        category.elems.add(new ChartElement("name1", "value1"));
        category.elems.add(new ChartElement("name2", "value2"));
        expandableElements.add(category);

        category = new ChartCategory();
        category.name = "Test3";
        category.elems.add(new ChartElement("name1", "value1"));
        category.elems.add(new ChartElement("name2", "value2"));
        category.elems.add(new ChartElement("name3", "value3"));
        category.elems.add(new ChartElement("name2", "value2"));
        category.elems.add(new ChartElement("name3", "value3"));
        category.elems.add(new ChartElement("name2", "value2"));
        category.elems.add(new ChartElement("name3", "value3"));
        expandableElements.add(category);

        category = new ChartCategory();
        category.name = "Test4";
        category.elems.add(new ChartElement("name1", "value1"));
        category.elems.add(new ChartElement("name2", "value2"));
        expandableElements.add(category);

        category = new ChartCategory();
        category.name = "Test5 ()";
        category.elems.add(new ChartElement("Normal", "120 Points"));
        ChartElement elem = new ChartElement();
        elem.name = "Diagram Test";
        elem.values.add(5f);
        elem.values.add(1f);
        elem.values.add(4f);
        elem.values.add(8f);
        elem.values.add(12f);
        elem.values.add(6f);
        elem.isDiagram = true;
        category.elems.add(elem);
        category.elems.add(new ChartElement("Test", "100"));
        expandableElements.add(category);

        ExpandableListView elv = (ExpandableListView) view.findViewById(R.id.charts_expandableListView);
        elv.setAdapter(new ChartsExpandableListAdapter(view.getContext(), expandableElements));

        // Inflate the layout for this fragment
        return view;
    }

    private class ChartCategory {
        String name;
        List<ChartElement> elems;

        ChartCategory() {
            super();
            elems = new ArrayList<>();
        }
    }

    private class ChartElement {
        String name;
        String value;
        List<Float> values;
        boolean isDiagram = false;

        ChartElement() {
            super();
            values = new ArrayList<>();
        }

        ChartElement(String name, String value) {
            this();
            this.name = name;
            this.value = value;
        }
    }

    private class ChartsExpandableListAdapter extends BaseExpandableListAdapter {
        Context context;
        LayoutInflater inflater;

        List<ChartCategory> elements;

        ChartsExpandableListAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        }

        ChartsExpandableListAdapter(Context context, List<ChartCategory> elements) {
            this(context);
            this.elements = elements;
        }

        public void setElements(List<ChartCategory> elements) {
            this.elements = elements;
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
            return elements.get(groupPosition).elems.size();
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
            return elements.get(groupPosition).elems.get(childPosition);
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
            return elements.get(groupPosition).elems.get(childPosition).hashCode();
        }

        /**
         * Indicates whether the child and group IDs are stable across changes to the
         * underlying data.
         *
         * @return whether or not the same ID always refers to the same object
         * @see Adapter#hasStableIds()
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
            ChartElement child = elements.get(groupPosition).elems.get(childPosition);

            View v;
            if (child.isDiagram) {
                v = inflater.inflate(R.layout.fragment_charts_expandable_child_diagram, parent, false);

                // init example series data
                GraphViewSeries exampleSeries = new GraphViewSeries(new GraphView.GraphViewData[] {
                        new GraphViewData(1, 2.0d)
                        , new GraphViewData(2, 1.5d)
                        , new GraphViewData(3, 2.5d)
                        , new GraphViewData(4, 1.0d)
                });

                GraphView graphView = new LineGraphView(
                        context // context
                        , "GraphViewDemo" // heading
                );

                graphView.addSeries(exampleSeries); // data#
                graphView.setHorizontalLabels(new String[] {"2 days ago", "yesterday", "today", "tomorrow"});
                graphView.getGraphViewStyle().setGridColor(R.color.transparent);
                graphView.getGraphViewStyle().setNumHorizontalLabels(4);

                LinearLayout layout = (LinearLayout) v.findViewById(R.id.charts_expandable_diagram_container);
                layout.addView(graphView);

                // Add Test TV
                TextView tv = new TextView(context);
                tv.setText("TEST");
                layout.addView(tv);
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
}
