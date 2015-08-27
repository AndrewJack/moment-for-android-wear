package technology.mainthread.apps.moment.ui.adapter;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.common.data.vo.Friend;

public class WearFriendsAdapter extends WearableListView.Adapter {

    private final List<Friend> friends;

    public static class FriendsViewHolder extends WearableListView.ViewHolder {
        @Bind(R.id.name)
        TextView txtDisplayName;

        public FriendsViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public WearFriendsAdapter(List<Friend> friends) {
        this.friends = friends;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new WearFriendsAdapter.FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        FriendsViewHolder friendsHolder = (FriendsViewHolder) holder;
        Friend friend = friends.get(position);
        friendsHolder.txtDisplayName.setText(friend.getDisplayName());
        holder.itemView.setTag(friend.getFriendId());
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }
}
