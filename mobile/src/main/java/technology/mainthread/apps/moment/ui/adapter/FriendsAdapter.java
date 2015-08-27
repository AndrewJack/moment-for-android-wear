package technology.mainthread.apps.moment.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.common.data.vo.Friend;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private final Picasso picasso;
    private final List<Friend> friends;
    private final FriendDeleteClickListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.txt_display_name)
        TextView displayName;
        @Bind(R.id.img_friend_profile_image)
        ImageView imageView;
        @Bind(R.id.btn_delete)
        View btnDelete;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public FriendsAdapter(Picasso picasso, List<Friend> friends, FriendDeleteClickListener listener) {
        this.picasso = picasso;
        this.friends = friends;
        this.listener = listener;
    }

    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendsAdapter.ViewHolder viewHolder, int position) {
        final Friend friend = friends.get(position);
        viewHolder.displayName.setText(friend.getDisplayName());
        picasso.load(friend.getProfileImageUrl()).into(viewHolder.imageView);
        viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFriendDeleteClicked(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void removeItem(Friend friend) {
        friends.remove(friend);
        notifyDataSetChanged();
    }

    public interface FriendDeleteClickListener {
        void onFriendDeleteClicked(Friend friend);
    }
}
