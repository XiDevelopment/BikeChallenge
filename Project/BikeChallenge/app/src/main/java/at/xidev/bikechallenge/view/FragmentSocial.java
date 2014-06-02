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
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import at.xidev.bikechallenge.core.AppFacade;
import at.xidev.bikechallenge.model.User;

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
            // ImageView image = (ImageView) friendView.findViewById(R.id.friend_image);

            // Set values
            friendView.setTag(friend.getName());
            name.setText(friend.getName());
            score.setText(friend.getScore().toString() + " " + getResources().getString(R.string.social_score));
            rank.setText(getResources().getString(R.string.social_rank) + ": " + rankCounter);
            //image.setImageDrawable(curFriend.getImage());

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
                // Get views
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
        // ImageView image = (ImageView) friendView.findViewById(R.id.user_image);

        // Set values
        userView.setTag(user.getName());
        name.setText(user.getName());
        score.setText(user.getScore().toString() + " " + getResources().getString(R.string.social_score));
        rank.setText(getResources().getString(R.string.social_rank) + ": " + rankCounter);
        //image.setImageDrawable(user.getImage());

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
            try {
                DetailFriendDialogFragment detailsDialog =
                        new DetailFriendDialogFragment(AppFacade.getInstance().getFriend((String) v.getTag()));
                detailsDialog.show(getFragmentManager(), "detailFriend");
            } catch (IOException ex) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            try {
                DeleteFriendDialogFragment deleteDialog =
                        new DeleteFriendDialogFragment(AppFacade.getInstance().getFriend((String) v.getTag()), v);
                deleteDialog.show(getFragmentManager(), "deleteFriend");
            } catch (IOException ex) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    private class AddFriendDialogFragment extends DialogFragment {
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

    private class DetailFriendDialogFragment extends DialogFragment {
        User friend;

        public DetailFriendDialogFragment() {
            // empty constructor necessary
        }

        public DetailFriendDialogFragment(User friend) {
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

            // Setup Values
            //TextView name = (TextView) view.findViewById(R.id.friend_detail_name);
            // TextView points = (TextView) view.findViewById(R.id.friend_detail_points);
            //TextView km = (TextView) view.findViewById(R.id.friend_detail_km);
            //name.setText(friend.getName());
            //points.setText(friend.getScore() + " Points"); // TODO Strings
            //km.setText(friend.getScore() / 20 + " km"); // TODO Stringspass

            // Build
            builder.setView(view);
            return builder.create();
        }
    }


    private class DeleteFriendDialogFragment extends DialogFragment {
        User friend;
        View friendView;
        TaskRemoveFriend task;

        public DeleteFriendDialogFragment(User toDelete, View friendView) {
            this.friend = toDelete;
            this.friendView = friendView;
            task = new TaskRemoveFriend();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.social_delete_message_start) + " " + friend.getName() + "?")
                    .setPositiveButton(getResources().getString(R.string.social_delete_button_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            task.execute(friend);
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

        // TODO
        private class TaskGetRoutes extends AsyncTask<User, Void, Boolean> {
            @Override
            protected Boolean doInBackground(User... params) {
                return null;
            }
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
            List<User> result = null;
            
            try {
                if (type)
                    result = AppFacade.getInstance().getFriends();
                else
                    result = AppFacade.getInstance().getFriendRequests();
            } catch(Exception ex) {
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
                if(hasConnection)
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
            Boolean result = false;
            
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
                    Toast.makeText(getActivity(), user.getName() + " " + getResources().getString(R.string.social_request_cancel) + "!", Toast.LENGTH_SHORT).show(); // TODO strings
                }
                reloadRequestList();
            } else {
                // TODO maybe better error handling
                // Not successful -> error
                if(hasConnection)
                    Toast.makeText(getActivity(), getResources().getString(R.string.social_request_error), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            }
            showProgress(false);
        }
    }

    private class TaskRemoveFriend extends AsyncTask<User, Void, Boolean> {
        User friend;
        boolean hasConnection = true;

        @Override
        protected Boolean doInBackground(User... params) {
            friend = params[0];
            
            Boolean result = false;
            try {
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
                Toast.makeText(getActivity(), friend.getName() + " " + getResources().getString(R.string.social_delete_successful) + "!", Toast.LENGTH_SHORT).show(); // TODO strings
            } else {
                if(hasConnection)
                    Toast.makeText(getActivity(), friend.getName() + " " + getResources().getString(R.string.social_delete_not_successful) + "!", Toast.LENGTH_SHORT).show(); // TODO strings
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
            
            Boolean result = false;
            
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
                Toast.makeText(getActivity(), getResources().getString(R.string.social_add_successful) + " " + name, Toast.LENGTH_SHORT).show(); // TODO strings
            } else {
                if(hasConnection)
                    Toast.makeText(getActivity(), getResources().getString(R.string.social_add_not_successful) + " " + name, Toast.LENGTH_SHORT).show(); // TODO strings
                else
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            }
            showProgress(false);
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
