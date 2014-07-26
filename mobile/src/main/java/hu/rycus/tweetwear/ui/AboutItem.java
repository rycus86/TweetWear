package hu.rycus.tweetwear.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.rycus.tweetwear.BuildConfig;
import hu.rycus.tweetwear.R;

public class AboutItem extends SettingsItem<Void> {

    @Override
    public Void getItem() {
        return null;
    }

    @Override
    protected View createView(final Context context, final ViewGroup parent) {
        final CharSequence versionInfo = String.format(context.getResources()
                .getText(R.string.about_version).toString(), BuildConfig.VERSION_NAME);
        final View view = LayoutInflater.from(context).inflate(R.layout.item_about, parent, false);
        final TextView txtVersion = (TextView) view.findViewById(R.id.txt_version);
        txtVersion.setText(Html.fromHtml(versionInfo.toString()));
        return view;
    }

    @Override
    protected void onClick(final Context context) {
        final Uri uri = Uri.parse(context.getString(R.string.about_link));
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

}
