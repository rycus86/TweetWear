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
import hu.rycus.tweetwear.util.AnimationUtil;

public class ListAdapter extends BaseAdapter {

    private static final String TAG = "ril." + ListAdapter.class.getSimpleName();

    private static final String KEY_PAGES = ListAdapter.class.getCanonicalName() + ".pages";
    private static final String KEY_ARCHIVE = ListAdapter.class.getCanonicalName() + ".archive";

    private ArrayList<SavedPage> pages = new ArrayList<SavedPage>();
    private boolean archive = false;

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
            viewHolder.imgArchiveOverlay = findView(view, R.id.img_archive_overlay);
            viewHolder.imgUser = findView(view, R.id.img_user);
            viewHolder.txtUsername = findView(view, R.id.txt_username);
            viewHolder.txtContent = findView(view, R.id.txt_content);
            viewHolder.txtTimestamp = findView(view, R.id.txt_timestamp);
            viewHolder.btnOpen = findView(view, R.id.btn_open);
            viewHolder.btnArchive = findView(view, R.id.btn_archive);
            view.setTag(viewHolder);

            viewHolder.btnOpen.setHapticFeedbackEnabled(true);
            viewHolder.btnOpen.setOnClickListener(openListener);

            viewHolder.btnArchive.setHapticFeedbackEnabled(true);
            viewHolder.btnArchive.setOnClickListener(archiveListener);

            viewHolder.btnOpen.setOnLongClickListener(showHintListener);
            viewHolder.btnArchive.setOnLongClickListener(showHintListener);
        }

        final SavedPage page = (SavedPage) getItem(position);
        final TweetData tweetData = TweetData.of(page.getTweet());

        Picasso.with(context)
                .load(page.getTweet().getUser().getProfileImageUrl())
                .into(viewHolder.imgUser);

        viewHolder.imgUnreadOverlay.setVisibility(page.isRead() ? View.GONE : View.VISIBLE);
        viewHolder.imgArchiveOverlay.setVisibility(page.isArchive() ? View.VISIBLE : View.GONE);

        viewHolder.txtUsername.setText(tweetData.getTitle());
        viewHolder.txtContent.setText(
                Html.fromHtml(tweetData.toFormattedHtml()));
        viewHolder.txtTimestamp.setText(
                Html.fromHtml(String.format("&mdash; %s", tweetData.getTimestamp())));

        viewHolder.btnOpen.setTag(page);

        if (archive) {
            viewHolder.btnArchive.setVisibility(View.GONE);
        } else {
            viewHolder.btnArchive.setVisibility(View.VISIBLE);
            viewHolder.btnArchive.setTag(new ViewAndPosition(view, position));
        }

        return view;
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findView(final View view, final int id) {
        return (T) view.findViewById(id);
    }

    public boolean onRestoreInstanceState(final Bundle savedState) {
        if (savedState != null) {
            archive = savedState.getBoolean(KEY_ARCHIVE, false);

            final ArrayList<SavedPage> savedPages = savedState.getParcelableArrayList(KEY_PAGES);
            if (savedPages != null) {
                pages = savedPages;
                notifyDataSetChanged();
                return true;
            }
        }

        return false;
    }

    public void onSaveInstanceState(final Bundle outState) {
        outState.putParcelableArrayList(KEY_PAGES, pages);
        outState.putBoolean(KEY_ARCHIVE, archive);
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(final Context context, final boolean archive) {
        if (this.archive == archive) {
            return;
        }

        this.archive = archive;

        loadContents(context);
    }

    public void clear() {
        this.pages = new ArrayList<SavedPage>(0);
        notifyDataSetChanged();
    }

    public void loadContents(final Context context) {
        new AsyncTask<Void, Void, ArrayList<SavedPage>>() {
            @Override
            protected ArrayList<SavedPage> doInBackground(final Void... params) {
                final Collection<SavedPage> pages;
                if (archive) {
                    pages = ReadItLater.queryArchives(context);
                } else {
                    pages = ReadItLater.query(context);
                }
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

    public void archivePage(final View view, final int position) {
        final Context context = view.getContext();
        final SavedPage page = (SavedPage) getItem(position);

        animatePageArchivation(view, position);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ReadItLater.archive(context, page);
            }
        });
    }

    private void animatePageArchivation(final View view, final int position) {
        AnimationUtil.fadeOutListItemRemoval(view, new Runnable() {
            @Override
            public void run() {
                deletePage(position);
            }
        });
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

    private final View.OnClickListener archiveListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final ViewAndPosition vp = (ViewAndPosition) v.getTag();
            archivePage(vp.view, vp.position);
        }
    };

    private final View.OnLongClickListener showHintListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View v) {
            final Context context = v.getContext();
            final CharSequence title = v.getContentDescription();
            Toast.makeText(context, title, Toast.LENGTH_SHORT).show();

            return true;
        }
    };

    private static class ViewAndPosition {

        View view;
        int position;

        private ViewAndPosition(final View view, final int position) {
            this.view = view;
            this.position = position;
        }
    }

    private static class ViewHolder {

        ImageView imgUnreadOverlay;
        ImageView imgArchiveOverlay;

        RoundedImageView imgUser;
        TextView txtUsername;
        TextView txtContent;
        TextView txtTimestamp;

        ImageButton btnOpen;
        ImageButton btnArchive;

    }

}
