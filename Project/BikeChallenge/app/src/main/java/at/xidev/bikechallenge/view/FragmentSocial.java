package at.xidev.bikechallenge.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import at.xidev.bikechallenge.model.Friend;

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
        for (Friend friend : AppFacade.getInstance().getFriendsLists()) {
            View friendView = inflater.inflate(R.layout.fragment_social_list_item, friendsListContainer, false);
            TextView name = (TextView) friendView.findViewById(R.id.friend_name);
            TextView points = (TextView) friendView.findViewById(R.id.friend_points);
            // ImageView image = (ImageView) friendView.findViewById(R.id.friend_image);

            friendView.setId(friend.getId());
            name.setText(friend.getName());
            points.setText(friend.getPoints() + " Punkte");
            //image.setImageDrawable(curFriend.getImage());

            // Setup listeners
            friendView.setOnClickListener(fListener);
            friendView.setOnLongClickListener(fListener);

            // Add to view
            friendsListContainer.addView(friendView);
        }
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


    public class FriendsListListener implements View.OnClickListener, View.OnLongClickListener {
        @Override
        public void onClick(View v) {
            DetailFriendDialogFragment detailsDialog =
                    new DetailFriendDialogFragment(AppFacade.getInstance().getFriend(v.getId()));
            detailsDialog.show(getFragmentManager(), "detailFriend");
        }

        @Override
        public boolean onLongClick(View v) {
            DeleteFriendDialogFragment deleteDialog =
                    new DeleteFriendDialogFragment(AppFacade.getInstance().getFriend(v.getId()), v);
            deleteDialog.show(getFragmentManager(), "deleteFriend");
            return true;
        }
    }

    public class AddFriendDialogFragment extends DialogFragment {
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

                    Friend newFriend = AppFacade.getInstance().addFriend(value);
                    if (newFriend != null) {
                        reloadFriendsList();
                        Toast.makeText(getActivity(), newFriend.getName() + " added!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Couldn't find Friend", Toast.LENGTH_SHORT).show();
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

    public class DetailFriendDialogFragment extends DialogFragment {
        Friend friend;

        public DetailFriendDialogFragment(Friend friend) {
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
            points.setText(friend.getPoints() + " Points"); // TODO Strings
            km.setText(friend.getPoints() / 20 + " km"); // TODO Strings

            // Build
            builder.setView(view);
            return builder.create();
        }
    }


    public class DeleteFriendDialogFragment extends DialogFragment {
        Friend friend;
        View friendView;

        public DeleteFriendDialogFragment(Friend toDelete, View friendView) {
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
