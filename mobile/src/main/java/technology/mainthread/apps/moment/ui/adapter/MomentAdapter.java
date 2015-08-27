package technology.mainthread.apps.moment.ui.adapter;

import android.content.res.Resources;
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
import technology.mainthread.apps.moment.BuildConfig;
import technology.mainthread.apps.moment.R;
import technology.mainthread.service.moment.momentApi.model.MomentResponse;

public class MomentAdapter extends RecyclerView.Adapter<MomentAdapter.ViewHolder> {

    private final Picasso picasso;
    private final List<MomentResponse> moments;
    private final OnSelectedListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.card_view)
        View cardView;
        @Bind(R.id.img_moment)
        ImageView imageView;
        @Bind(R.id.txt_sent_to_or_from)
        TextView textView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public MomentAdapter(Picasso picasso, List<MomentResponse> moments, OnSelectedListener listener) {
        this.picasso = picasso;
        this.moments = moments;
        this.listener = listener;
    }

    @Override
    public MomentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_moment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MomentAdapter.ViewHolder holder, int position) {
        final MomentResponse moment = moments.get(position);
        picasso.load(moment.getServingUrl()).into(holder.imageView);
        Resources resources = holder.textView.getResources();
        String text;
        if (moment.getSenderName() != null) {
            text = resources.getString(R.string.sent_by, moment.getSenderName());
        } else {
            text = resources.getString(R.string.sent_to, moment.getRecipientNames().get(0));
        }
        holder.textView.setText(text);
        if (BuildConfig.DEBUG) { // TODO: can only click in debug
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMomentSelected(moment.getMomentId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return moments.size();
    }

    public interface OnSelectedListener {
        void onMomentSelected(long momentId);
    }
}
