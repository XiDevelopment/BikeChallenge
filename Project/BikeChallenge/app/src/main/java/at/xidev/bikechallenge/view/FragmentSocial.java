package at.xidev.bikechallenge.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    FriendsListListener fListener;

    LayoutInflater inflater;
    LinearLayout friendsListContainer;

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

        // Initialize friends list
        // Get Parent
        friendsListContainer = (LinearLayout) view.findViewById(R.id.friends_list);

        // Load Friendlist into LinearLayout
        reloadFriendsList();

        return view;
    }

    private void reloadFriendsList() {
        // Clear previous items
        friendsListContainer.removeAllViewsInLayout();

        // Add Items
        boolean isUserAdded = false;
        int rankCounter = 0;
        for (User friend : AppFacade.getInstance().getFriends()) {
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
            score.setText(friend.getScore().toString() + " Punkte");
            rank.setText("Rank: " + rankCounter);
            //image.setImageDrawable(curFriend.getImage());

            // Setup listeners
            friendView.setOnClickListener(fListener);
            friendView.setOnLongClickListener(fListener);

            // Add to view
            friendsListContainer.addView(friendView);
        }

        // if User not allready added, add to list
        if (!isUserAdded) {
            rankCounter++;
            friendsListContainer.addView(getUserView(rankCounter));
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
        score.setText(user.getScore().toString() + " Points");
        rank.setText("Rank: " + rankCounter);
        //image.setImageDrawable(user.getImage());

        // return view
        return userView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.social, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_social_add) {
            new AddFriendDialogFragment().show(getFragmentManager(), "addFriend");
        }
        return super.onOptionsItemSelected(item);
    }


    private class FriendsListListener implements View.OnClickListener, View.OnLongClickListener {
        @Override
        public void onClick(View v) {
            DetailFriendDialogFragment detailsDialog =
                    new DetailFriendDialogFragment(AppFacade.getInstance().getFriend((String) v.getTag()));
            detailsDialog.show(getFragmentManager(), "detailFriend");
        }

        @Override
        public boolean onLongClick(View v) {
            DeleteFriendDialogFragment deleteDialog =
                    new DeleteFriendDialogFragment(AppFacade.getInstance().getFriend((String) v.getTag()), v);
            deleteDialog.show(getFragmentManager(), "deleteFriend");
            return true;
        }
    }

    private class AddFriendDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Search a friend");
            builder.setMessage("Name or Email:");

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            builder.setView(input);

            builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();

                    boolean requestSend = AppFacade.getInstance().requestFriend(value);
                    if (requestSend) {
                        Toast.makeText(getActivity(), "Friend request sent to " + value, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Could not send a request to " + value, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dismiss();
                }
            });

            return builder.create();
        }
    }

    private class DetailFriendDialogFragment extends DialogFragment {
        User friend;

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
            TextView name = (TextView) view.findViewById(R.id.friend_detail_name);
            TextView points = (TextView) view.findViewById(R.id.friend_detail_points);
            TextView km = (TextView) view.findViewById(R.id.friend_detail_km);
            name.setText(friend.getName());
            points.setText(friend.getScore() + " Points"); // TODO Strings
            km.setText(friend.getScore() / 20 + " km"); // TODO Strings

            // Build
            builder.setView(view);
            return builder.create();
        }
    }


    private class DeleteFriendDialogFragment extends DialogFragment {
        User friend;
        View friendView;

        public DeleteFriendDialogFragment(User toDelete, View friendView) {
            this.friend = toDelete;
            this.friendView = friendView;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Remove " + friend.getName() + "?") // TODO Strings
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Try to remove from FriendsLists
                            if (AppFacade.getInstance().removeFriend(friend)) {
                                // if successful reload friends
                                reloadFriendsList();
                                Toast.makeText(getActivity(), friend.getName() + " removed!", Toast.LENGTH_SHORT).show(); // TODO Strings
                            } else {
                                Toast.makeText(getActivity(), friend.getName() + " not removed!", Toast.LENGTH_SHORT).show(); // TODO Strings
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
