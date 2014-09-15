package hu.rycus.tweetwear.ui.ril;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.ril.ReadItLater;
import hu.rycus.tweetwear.ril.SavedPage;

public class ListAdapter extends BaseAdapter {

    private static final String TAG = "ril." + ListAdapter.class.getSimpleName();

    private static final String KEY_PAGES = ListAdapter.class.getCanonicalName() + ".pages";

    private ArrayList<SavedPage> pages = new ArrayList<SavedPage>();

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public Object getItem(final int position) {
        return pages.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final Context context = parent.getContext();

        final View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = View.inflate(context, R.layout.item_page, null);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.imgUnreadOverlay = findView(view, R.id.img_unread_overlay);
            viewHolder.imgUser = findView(view, R.id.img_user);
            viewHolder.txtUsername = findView(view, R.id.txt_username);
            viewHolder.txtContent = findView(view, R.id.txt_content);
            viewHolder.txtTimestamp = findView(view, R.id.txt_timestamp);
            viewHolder.btnOpen = findView(view, R.id.btn_open);
            view.setTag(viewHolder);

            viewHolder.btnOpen.setHapticFeedbackEnabled(true);
            viewHolder.btnOpen.setOnClickListener(openListener);
            viewHolder.btnOpen.setOnLongClickListener(openHintListener);
        }

        final SavedPage page = (SavedPage) getItem(position);
        final TweetData tweetData = TweetData.of(page.getTweet());

        Picasso.with(context)
                .load(page.getTweet().getUser().getProfileImageUrl())
                .into(viewHolder.imgUser);

        viewHolder.imgUnreadOverlay.setVisibility(page.isRead() ? View.GONE : View.VISIBLE);

        viewHolder.txtUsername.setText(tweetData.getTitle());
        viewHolder.txtContent.setText(
                Html.fromHtml(tweetData.toFormattedHtml()));
        viewHolder.txtTimestamp.setText(
                Html.fromHtml(String.format("&mdash; %s", tweetData.getTimestamp())));

        viewHolder.btnOpen.setTag(page);

        return view;
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findView(final View view, final int id) {
        return (T) view.findViewById(id);
    }

    public boolean onRestoreInstanceState(final Bundle savedState) {
        if (savedState != null) {
            final ArrayList<SavedPage> savedPages = savedState.getParcelableArrayList(KEY_PAGES);
            if (savedPages != null) {
                pages = savedPages;
                return true;
            }
        }

        return false;
    }

    public void onSaveInstanceState(final Bundle outState) {
        outState.putParcelableArrayList(KEY_PAGES, pages);
    }

    public void loadContents(final Context context) {
        new AsyncTask<Void, Void, ArrayList<SavedPage>>() {
            @Override
            protected ArrayList<SavedPage> doInBackground(final Void... params) {
                final Collection<SavedPage> pages = ReadItLater.query(context);
                return new ArrayList<SavedPage>(pages);
            }

            @Override
            protected void onPostExecute(final ArrayList<SavedPage> savedPages) {
                pages = savedPages;
                notifyDataSetChanged();
            }
        }.execute();
    }

    public void markAsRead(final Context context, final SavedPage page) {
        if (page.isRead()) {
            return;
        }

        final long pageId = page.getId();
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(final Void... params) {
                return ReadItLater.markAsRead(context, pageId);
            }

            @Override
            protected void onPostExecute(final Integer result) {
                if (result > 0) {
                    page.setRead(true);
                    notifyDataSetChanged();
                }
            }
        }.execute();
    }

    public void deletePage(final int position) {
        pages.remove(position);
        notifyDataSetChanged();
    }

    private final View.OnClickListener openListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final Context context = v.getContext();

            String link = null;
            try {
                final SavedPage page = (SavedPage) v.getTag();
                link = page.getLink();

                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(intent);

                markAsRead(context, page);
            } catch (Exception ex) {
                Log.e(TAG, String.format("Failed to open link: %s", link), ex);
            }
        }
    };

    private final View.OnLongClickListener openHintListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View v) {
            final Context context = v.getContext();
            final CharSequence title = v.getContentDescription();
            Toast.makeText(context, title, Toast.LENGTH_SHORT).show();

            return true;
        }
    };

    private static class ViewHolder {

        ImageView imgUnreadOverlay;

        RoundedImageView imgUser;
        TextView txtUsername;
        TextView txtContent;
        TextView txtTimestamp;

        ImageButton btnOpen;

    }

}
