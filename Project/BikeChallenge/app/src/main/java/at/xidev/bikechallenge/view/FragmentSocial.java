package at.xidev.bikechallenge.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import at.xidev.bikechallenge.core.AppFacade;
import at.xidev.bikechallenge.model.Statistic;
import at.xidev.bikechallenge.model.User;

/**
 * Created by int3r on 31.03.2014.
 */
public class FragmentSocial extends Fragment {
    public FragmentSocial() {
        // Required empty public constructor
    }

    public static FragmentSocial newInstance() {
        return new FragmentSocial();
    }

    // OnClick Listener
    private FriendsListListener fListener;

    private LayoutInflater inflater;
    private LinearLayout friendsListContainer;
    private LinearLayout friendsRequestContainer;
    private LinearLayout friendsRequestListContainer;

    private View friendsProgressView;
    private SwipeRefreshLayout swipeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_social, container, false);

        // Actionbar Menu Items
        setHasOptionsMenu(true);

        // Initialize OnClick Listener
        fListener = new FriendsListListener();

        // Initialize friends view and progress view
        friendsProgressView = view.findViewById(R.id.friends_progress);

        // Initialize friend list
        // Get Parent
        friendsListContainer = (LinearLayout) view.findViewById(R.id.friends_list);
        friendsRequestContainer = (LinearLayout) view.findViewById(R.id.friends_request_container);
        friendsRequestListContainer = (LinearLayout) view.findViewById(R.id.friends_request_list);

        // Load friend list into LinearLayout
        reloadFriendsList();
        reloadRequestList();

        // Setup swipe refresh
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.social_swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadFriendsList();
                reloadRequestList();
            }
        });
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    public void reload() {
        reloadFriendsList();
        reloadRequestList();
    }

    private void reloadFriendsList() {
        // setup task und execute, true is for friend list
        TaskGetList friendListTask = new TaskGetList(true);
        friendListTask.execute();
    }

    private void reloadFriendsList(List<User> friends) {
        // Clear previous items
        friendsListContainer.removeAllViewsInLayout();

        // Add Items
        boolean isUserAdded = false;
        int rankCounter = 0;
        for (User friend : friends) {
            // if new friend has less score then user, put user first, then add friend
            if (!isUserAdded && friend.getScore() < AppFacade.getInstance().getUser().getScore()) {
                rankCounter++;
                friendsListContainer.addView(getUserView(rankCounter));
                isUserAdded = true;
            }

            // increment rank counter
            rankCounter++;

            View friendView = inflater.inflate(R.layout.fragment_social_list_item, friendsListContainer, false);
            TextView name = (TextView) friendView.findViewById(R.id.friend_name);
            TextView score = (TextView) friendView.findViewById(R.id.friend_score);
            TextView rank = (TextView) friendView.findViewById(R.id.friend_rank);
            ImageView image = (ImageView) friendView.findViewById(R.id.friend_image);

            // Set values
            friendView.setTag(friend.getName());
            name.setText(friend.getName());
            if (friend.getScore() == 1) {
                score.setText(friend.getScore().toString() + " " + getResources().getString(R.string.social_score_one));
            } else {
                score.setText(friend.getScore().toString() + " " + getResources().getString(R.string.social_score));
            }
            rank.setText(getResources().getString(R.string.social_rank) + ": " + rankCounter);
            image.setImageDrawable(AppFacade.getInstance().getAvatar(friend.getAvatar(), getActivity()));

            // Setup listeners
            friendView.setOnClickListener(fListener);
            friendView.setOnLongClickListener(fListener);

            // Add to view
            friendsListContainer.addView(friendView);
        }

        // if User not already added, add to list
        if (!isUserAdded) {
            rankCounter++;
            friendsListContainer.addView(getUserView(rankCounter));
        }
    }

    private void reloadRequestList() {
        // setup task und execute, false is for Invite List
        TaskGetList requestListTask = new TaskGetList(false);
        requestListTask.execute();
    }

    private void reloadInviteList(List<User> requests) {
        // get invites

        if (requests == null || requests.size() <= 0) {
            // Hide Invite List
            friendsRequestContainer.setVisibility(View.GONE);
        } else {
            // Clear previous items
            friendsRequestListContainer.removeAllViewsInLayout();

            for (final User user : requests) {
                // Get views'
                View requestView = inflater.inflate(R.layout.fragment_social_request_item, friendsRequestListContainer, false);
                TextView rName = (TextView) requestView.findViewById(R.id.friend_request_name);
                RelativeLayout rAccept = (RelativeLayout) requestView.findViewById(R.id.friend_request_accept);
                RelativeLayout rDecline = (RelativeLayout) requestView.findViewById(R.id.friend_request_decline);

                // Set values
                rName.setText(user.getName());

                // Add click listeners
                rAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TaskAnswerRequest answerTask = new TaskAnswerRequest(user);
                        answerTask.execute(true);
                    }
                });
                rDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TaskAnswerRequest answerTask = new TaskAnswerRequest(user);
                        answerTask.execute(false);
                    }
                });

                // Add to friend list
                friendsRequestListContainer.addView(requestView);
            }

            // Display invite list
            friendsRequestContainer.setVisibility(View.VISIBLE);
        }
    }

    private View getUserView(int rankCounter) {
        User user = AppFacade.getInstance().getUser();

        View userView = inflater.inflate(R.layout.fragment_social_list_item_self, friendsListContainer, false);
        TextView name = (TextView) userView.findViewById(R.id.user_name);
        TextView score = (TextView) userView.findViewById(R.id.user_score);
        TextView rank = (TextView) userView.findViewById(R.id.user_rank);
        ImageView image = (ImageView) userView.findViewById(R.id.user_image);

        // Set values
        userView.setTag(user.getName());
        name.setText(user.getName());
        if (user.getScore() == 1) {
            score.setText(user.getScore().toString() + " " + getResources().getString(R.string.social_score_one));
        } else {
            score.setText(user.getScore().toString() + " " + getResources().getString(R.string.social_score));
        }
        rank.setText(getResources().getString(R.string.social_rank) + ": " + rankCounter);
        image.setImageDrawable(AppFacade.getInstance().getAvatar(user.getAvatar(), getActivity()));

        // Setup listeners
        userView.setOnClickListener(fListener);

        // return view
        return userView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.social, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_social_add) {
            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("addFriend");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            new AddFriendDialogFragment().show(getFragmentManager(), "addFriend");
        } else if (id == R.id.action_social_refresh) {
            reloadFriendsList();
            reloadRequestList();
        }
        return super.onOptionsItemSelected(item);
    }


    private class FriendsListListener implements View.OnClickListener, View.OnLongClickListener {
        @Override
        public void onClick(View v) {
            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("detailFriend");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DetailFriendDialogFragment detailsDialog =
                    new DetailFriendDialogFragment((String) v.getTag());
            detailsDialog.show(getFragmentManager(), "detailFriend");
        }

        @Override
        public boolean onLongClick(View v) {
            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("deleteFriend");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DeleteFriendDialogFragment deleteDialog =
                    new DeleteFriendDialogFragment((String) v.getTag(), v);
            deleteDialog.show(getFragmentManager(), "deleteFriend");
            return true;
        }
    }

    public class AddFriendDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(getResources().getString(R.string.social_add_title));
            builder.setMessage(getResources().getString(R.string.social_add_message));

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            builder.setView(input);

            builder.setPositiveButton(getResources().getString(R.string.social_add_button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    TaskAddFriend task = new TaskAddFriend();
                    task.execute(input.getText().toString());
                }
            });

            builder.setNegativeButton(getResources().getString(R.string.social_add_button_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dismiss();
                }
            });

            return builder.create();
        }
    }

    public class DetailFriendDialogFragment extends DialogFragment {
        String friend;

        public DetailFriendDialogFragment() {
            // empty constructor necessary
        }

        public DetailFriendDialogFragment(String friend) {
            this.friend = friend;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View view = inflater.inflate(R.layout.fragment_social_detail, null);

            // Setup values via Task
            TaskGetFriendDetails task = new TaskGetFriendDetails(this, view, friend);
            task.execute();

            // Build
            builder.setView(view);
            return builder.create();
        }
    }


    public class DeleteFriendDialogFragment extends DialogFragment {
        String friendName;
        View friendView;

        public DeleteFriendDialogFragment(String toDelete, View friendView) {
            this.friendName = toDelete;
            this.friendView = friendView;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.social_delete_message_start) + " " + friendName + "?")
                    .setPositiveButton(getResources().getString(R.string.social_delete_button_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            TaskRemoveFriend task = new TaskRemoveFriend();
                            task.execute(friendName);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.social_delete_button_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private class TaskGetList extends AsyncTask<Void, Void, List<User>> {
        boolean type = true;
        boolean hasConnection = true;

        /**
         * @param type true for friend list, false for request list
         */
        protected TaskGetList(boolean type) {
            this.type = type;
        }

        @Override
        protected List<User> doInBackground(Void... params) {
            List<User> result;
            try {
                if (type)
                    result = AppFacade.getInstance().getFriends();
                else
                    result = AppFacade.getInstance().getFriendRequests();
            } catch (Exception ex) {
                hasConnection = false;
                result = null;
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected void onPostExecute(List<User> friends) {
            if (friends == null)
                if (hasConnection)
                    Toast.makeText(getActivity(), getResources().getString(R.string.social_list_error), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            else if (type)
                reloadFriendsList(friends);
            else
                reloadInviteList(friends);
            showProgress(false);
            swipeLayout.setRefreshing(false);
        }
    }

    private class TaskAnswerRequest extends AsyncTask<Boolean, Void, Boolean> {
        User user;
        boolean hasConnection = true;

        // action = true for accept, false for decline
        boolean action;

        protected TaskAnswerRequest(User user) {
            this.user = user;
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            action = params[0];
            Boolean result;

            try {
                if (action)
                    result = AppFacade.getInstance().acceptFriend(user);
                else
                    result = AppFacade.getInstance().declineFriend(user);
            } catch (IOException ex) {
                hasConnection = false;
                result = false;
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (action) {
                    // Successful and Accept Request
                    reloadFriendsList();
                    Toast.makeText(getActivity(), user.getName() + " " + getResources().getString(R.string.social_request_ok) + "!", Toast.LENGTH_SHORT).show();
                } else {
                    // Successful and Decline Request
                    Toast.makeText(getActivity(), user.getName() + " " + getResources().getString(R.string.social_request_cancel) + "!", Toast.LENGTH_SHORT).show();
                }
                reloadRequestList();
            } else {
                // Not successful -> error
                if (hasConnection)
                    Toast.makeText(getActivity(), getResources().getString(R.string.social_request_error), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            }
            showProgress(false);
        }
    }

    private class TaskRemoveFriend extends AsyncTask<String, Void, Boolean> {
        String friendName;
        boolean hasConnection = true;

        @Override
        protected Boolean doInBackground(String... params) {
            friendName = params[0];

            Boolean result;
            try {
                User friend = AppFacade.getInstance().getFriend(friendName);
                result = AppFacade.getInstance().removeFriend(friend);
            } catch (IOException ex) {
                hasConnection = false;
                result = false;
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                // if successful reload friends
                reloadFriendsList();
                Toast.makeText(getActivity(), friendName + " " + getResources().getString(R.string.social_delete_successful) + "!", Toast.LENGTH_SHORT).show();
            } else {
                if (hasConnection)
                    Toast.makeText(getActivity(), friendName + " " + getResources().getString(R.string.social_delete_not_successful) + "!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            }
            showProgress(false);
        }
    }

    private class TaskAddFriend extends AsyncTask<String, Void, Boolean> {
        String name;
        boolean hasConnection = true;

        @Override
        protected Boolean doInBackground(String... params) {
            name = params[0];

            Boolean result;
            try {
                result = AppFacade.getInstance().requestFriend(name);
            } catch (IOException ex) {
                hasConnection = false;
                result = false;
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getActivity(), getResources().getString(R.string.social_add_successful) + " " + name, Toast.LENGTH_SHORT).show();
            } else {
                if (hasConnection)
                    Toast.makeText(getActivity(), getResources().getString(R.string.social_add_not_successful) + " " + name, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            }
            showProgress(false);
        }
    }

    private class TaskGetFriendDetails extends AsyncTask<Void, Void, Pair<Statistic, Statistic>> {
        DialogFragment mDialog;
        View view;
        String friendName;
        User friend;
        boolean hasConnection = true;

        protected TaskGetFriendDetails(DialogFragment dialog, View view, String friend) {
            this.mDialog = dialog;
            this.view = view;
            this.friendName = friend;
        }

        @Override
        protected Pair<Statistic, Statistic> doInBackground(Void... params) {
            Pair<Statistic, Statistic> result;
            try {
                Statistic sUser = AppFacade.getInstance().getStatistic();
                Statistic sFriend;

                if (!friendName.equals(AppFacade.getInstance().getUser().getName())) {
                    friend = AppFacade.getInstance().getFriend(friendName);
                    if (friend != null)
                        sFriend = AppFacade.getInstance().getStatistic(friend);
                    else
                        return null;
                } else {
                    // sFriend also as sUser if users selects himself.
                    sFriend = sUser;
                    friend = AppFacade.getInstance().getUser();
                }

                result = new Pair<>(sUser, sFriend);
            } catch (IOException ex) {
                hasConnection = false;
                result = null;
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            view.findViewById(R.id.friends_detail_container).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.friends_detail_progress).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Pair<Statistic, Statistic> statistic) {
            // Check if error
            if (statistic == null) {
                if (hasConnection) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.social_detail_error), Toast.LENGTH_SHORT).show();
                    reload();
                } else
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
                mDialog.dismiss();
                return;
            }

            // Statistic objects of user and friend
            Statistic sOwn = statistic.first;
            Statistic sFriend = statistic.second;

            // Init text views
            TextView name = (TextView) view.findViewById(R.id.friend_detail_name);
            TextView graph_name = (TextView) view.findViewById(R.id.friend_detail_graph_friend_name);
            TextView score = (TextView) view.findViewById(R.id.friend_detail_score);
            TextView distance = (TextView) view.findViewById(R.id.friend_detail_distance);
            TextView time = (TextView) view.findViewById(R.id.friend_detail_time);
            TextView emission = (TextView) view.findViewById(R.id.friend_detail_emission);
            LinearLayout container = (LinearLayout) view.findViewById(R.id.friends_detail_graph_container);

            // Init and set avatar
            ImageView image = (ImageView) view.findViewById(R.id.friend_detail_image);
            if (friend != null)
                image.setImageDrawable(AppFacade.getInstance().getAvatar(friend.getAvatar(), getActivity()));

            // Set texts
            DecimalFormat df = new DecimalFormat("0.00");

            name.setText(friendName);
            graph_name.setText(friendName);
            score.setText(getResources().getString(R.string.social_detail_score) + " " + Math.round(sFriend.getScore()) + getResources().getString(R.string.social_detail_score_val));
            distance.setText(getResources().getString(R.string.social_detail_distance) + " " + df.format(sFriend.getTotalDistance() / 1000.0) + " " + getResources().getString(R.string.social_detail_score_val) + getString(R.string.social_detail_distance_val));
            time.setText(getResources().getString(R.string.social_detail_time) + " " + AppFacade.getInstance().formatTimeElapsedSinceMillisecond(sFriend.getTotalTime()));
            emission.setText(getResources().getString(R.string.social_detail_emission) + " " + df.format(sFriend.getEmissions()) + " " + getResources().getString(R.string.social_detail_emission_val));

            // Check if dialog is for user or friend
            if (friendName.equals(AppFacade.getInstance().getUser().getName())) {
                // user does not need a comparison
                container.setVisibility(View.GONE);

                // user maybe want to change his avatar from within his detail view
                image.setOnClickListener(new View.OnClickListener() {
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
                        final DialogAvatarSelection dialog = new DialogAvatarSelection(new DialogAvatarSelection.AvatarSelectionListener() {
                            @Override
                            public void onCloseDialog() {
                                ((MainActivity) getActivity()).reloadData();

                                // also dismiss detail friend dialog, so image is reloaded
                                mDialog.dismiss();
                            }
                        });
                        dialog.show(getFragmentManager(), "AvatarDialog");
                    }
                });
            } else {
                // Make Diagram
                // Data for user
                GraphView.GraphViewData[] graphData = new GraphView.GraphViewData[sOwn.getLast7DaysDistances().size()];
                for (int i = 0; i < sOwn.getLast7DaysDistances().size(); i++)
                    graphData[i] = new GraphView.GraphViewData(i + 1, sOwn.getLast7DaysDistances().get(i));
                GraphViewSeries graphSeriesUser = new GraphViewSeries("User", new GraphViewSeries.GraphViewSeriesStyle(getResources().getColor(R.color.social_detail_graph1), (int) getResources().getDimension(R.dimen.charts_line_thickness)), graphData);

                // Data for friend
                graphData = new GraphView.GraphViewData[sFriend.getLast7DaysDistances().size()];
                for (int i = 0; i < sFriend.getLast7DaysDistances().size(); i++)
                    graphData[i] = new GraphView.GraphViewData(i + 1, sFriend.getLast7DaysDistances().get(i));
                GraphViewSeries graphSeriesFriend = new GraphViewSeries("Friend", new GraphViewSeries.GraphViewSeriesStyle(getResources().getColor(R.color.social_detail_graph2), (int) getResources().getDimension(R.dimen.charts_line_thickness)), graphData);

                // init graph, with empty title
                GraphView graphView = new LineGraphView(getActivity(), "");

                // add data
                graphView.addSeries(graphSeriesUser);
                graphView.addSeries(graphSeriesFriend);

                // Set labels and style
                graphView.setHorizontalLabels(new String[]{"-6", "-5", "-4", "-3", "-2", "-1", getResources().getString(R.string.social_detail_graph_today)});
                graphView.getGraphViewStyle().setGridColor(getActivity().getResources().getColor(R.color.dark_transparent));
                graphView.getGraphViewStyle().setHorizontalLabelsColor(getActivity().getResources().getColor(R.color.black));
                graphView.getGraphViewStyle().setVerticalLabelsColor(getActivity().getResources().getColor(R.color.black));
                graphView.getGraphViewStyle().setNumHorizontalLabels(7);
                graphView.setShowLegend(false);
                graphView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 600));
                graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if (!isValueX) {
                            DecimalFormat df = new DecimalFormat("0.0");
                            if (value < 100)
                                return ((int) value) + "m";
                            else
                                return df.format(value / 1000) + "km";
                        }
                        return null; // let graphview generate X-axis label for us
                    }
                });

                // Add graph to view
                container.addView(graphView);
            }

            // Finish
            view.findViewById(R.id.friends_detail_container).setVisibility(View.VISIBLE);
            view.findViewById(R.id.friends_detail_progress).setVisibility(View.GONE);
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

            friendsProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            friendsProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    friendsProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            friendsProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
