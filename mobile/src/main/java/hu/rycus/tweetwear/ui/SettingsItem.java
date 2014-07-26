package hu.rycus.tweetwear.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class SettingsItem<Item> implements View.OnClickListener {

    private SettingsAdapter adapter;

    protected SettingsAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(final SettingsAdapter adapter) {
        this.adapter = adapter;
    }

    protected void initialize() {
    }

    public abstract Item getItem();

    protected abstract View createView(Context context, ViewGroup parent);

    public View getView(final Context context, final ViewGroup parent) {
        final View view = createView(context, parent);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    public final void onClick(final View v) {
        onClick(v.getContext());
    }

    protected void onClick(final Context context) {
    }

}
