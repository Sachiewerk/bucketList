package ua.aengussong.www.bucketlist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by coolsmileman on 24.05.2017.
 */

public class RVMainAdapter extends RecyclerView.Adapter<RVMainAdapter.WishViewHolder> {

    final private WishClickListener onWishClickListener;

    int count;

    RVMainAdapter(int count, WishClickListener listener){
        onWishClickListener = listener;
        this.count = count;
    }

    @Override
    public WishViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.wishes_list_layout,parent,false);
        WishViewHolder wishViewHolder = new WishViewHolder(view);
        return wishViewHolder;
    }

    @Override
    public void onBindViewHolder(WishViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public interface WishClickListener{
        void onWishClicked(int clickedPosition);
    }

    public class WishViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView rvWishImage;
        TextView rvWishTitle;
        TextView rvWishPrice;
        TextView rvWishCategory;

        public WishViewHolder(View itemView) {
            super(itemView);

            rvWishImage = (ImageView) itemView.findViewById(R.id.rv_wish_photo);
            rvWishTitle = (TextView) itemView.findViewById(R.id.rv_wish_title);
            rvWishPrice = (TextView) itemView.findViewById(R.id.rv_wish_price);
            rvWishCategory = (TextView) itemView.findViewById(R.id.rv_wish_category);

            itemView.setOnClickListener(this);
        }
        void bind(int position){
            rvWishImage.setImageResource(R.mipmap.ic_launcher);
            rvWishTitle.setText(String.valueOf(position));
            rvWishPrice.setText(String.valueOf(position+1));
            rvWishCategory.setText(String.valueOf(position+2));
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            onWishClickListener.onWishClicked(clickedPosition);
        }
    }
}
