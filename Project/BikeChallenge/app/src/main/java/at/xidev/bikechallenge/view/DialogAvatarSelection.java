package at.xidev.bikechallenge.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import at.xidev.bikechallenge.core.AppFacade;

public class DialogAvatarSelection extends DialogFragment {
    public interface AvatarSelectionListener {
        void onCloseDialog();
    }

    AvatarSelectionListener listener;

    public DialogAvatarSelection(AvatarSelectionListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Add title
        builder.setTitle(getString(R.string.dialog_avatar_title));

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.dialog_avatar_selection, null);

        // Get grid view and set adapter
        GridView gridview = (GridView) view.findViewById(R.id.dialog_avatar_grid_view);
        gridview.setAdapter(new ImageAdapter(getActivity()));

        // Save DialogFragment for anonymous inner class
        final DialogFragment df = this;

        // Set onClickListener for grid view
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                TaskUpdateAvatar task = new TaskUpdateAvatar(view, df);
                task.execute(position);
            }
        });

        // Build
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // Notify listeners that dialog has finished
        listener.onCloseDialog();
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        // references to our images
        private List<Integer> mSmileyIds;

        public ImageAdapter(Context c) {
            mContext = c;

            // fill list
            mSmileyIds = new ArrayList<>();
            mSmileyIds.add(R.drawable.ic_action_person);
            for (int i = 1; i <= AppFacade.NUM_AVATARS; i++)
                mSmileyIds.add(c.getResources().getIdentifier("avatar" + i, "drawable", c.getPackageName()));
        }

        public int getCount() {
            return mSmileyIds.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);

                // size of element
                int dimen = 100;
                // conversion to DisplayPoints
                dimen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimen, getResources().getDisplayMetrics());

                int padding = 10;
                padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, getResources().getDisplayMetrics());

                // set attributes
                imageView.setLayoutParams(new GridView.LayoutParams(dimen, dimen));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(padding, padding, padding, padding);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mSmileyIds.get(position));
            return imageView;
        }
    }

    private class TaskUpdateAvatar extends AsyncTask<Integer, Void, Void> {
        View view;
        DialogFragment dialog;

        boolean isAvatar = false;
        boolean isUser = false;
        boolean isConnection = true;

        protected TaskUpdateAvatar(View view, DialogFragment dialog) {
            this.view = view;
            this.dialog = dialog;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                // set avatar id
                isAvatar = AppFacade.getInstance().setAvatar(params[0]);

                // update user object
                isUser = AppFacade.getInstance().updateUser();

            } catch (IOException ex) {
                // if no connection
                isConnection = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            view.findViewById(R.id.dialog_avatar_grid_view).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.dialog_avatar_progress).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isConnection == false)
                Toast.makeText(getActivity(), getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            else if (isAvatar == false)
                Toast.makeText(getActivity(), getResources().getString(R.string.dialog_avatar_error_avatar), Toast.LENGTH_SHORT).show();
            else if (isUser == false)
                Toast.makeText(getActivity(), getResources().getString(R.string.dialog_avatar_error_user), Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        }
    }
}
