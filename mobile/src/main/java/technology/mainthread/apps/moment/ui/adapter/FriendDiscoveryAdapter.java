package technology.mainthread.apps.moment.ui.adapter;

import android.content.res.Resources;
import android.support.annotation.ColorRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.ui.view.LoaderCheckBox;
import technology.mainthread.service.moment.friendApi.model.FriendResponse;

public class FriendDiscoveryAdapter extends RecyclerView.Adapter<FriendDiscoveryAdapter.ViewHolder> {

    private final List<FriendResponse> friends;
    private final FriendAddListener listener;
    private final int textColor;

    static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.txt_display_name)
        TextView displayName;
        @Bind(R.id.loader_add)
        LoaderCheckBox loaderCheckBox;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public FriendDiscoveryAdapter(List<FriendResponse> friends, FriendAddListener listener, @ColorRes int textColor) {
        this.friends = friends;
        this.listener = listener;
        this.textColor = textColor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_discovered, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final FriendResponse friend = friends.get(position);

        viewHolder.displayName.setText(friend.getDisplayName());
        Resources resources = viewHolder.displayName.getResources();
        viewHolder.displayName.setTextColor(resources.getColor(textColor));

        viewHolder.loaderCheckBox.setListener(new LoaderCheckBox.Listener() {
            @Override
            public void onCheckChanged(LoaderCheckBox loaderCheckBox, boolean isChecked) {
                listener.onAddClicked(loaderCheckBox, friend.getFriendId(), isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public interface FriendAddListener {
        void onAddClicked(LoaderCheckBox view, long friendId, boolean isChecked);
    }

}
