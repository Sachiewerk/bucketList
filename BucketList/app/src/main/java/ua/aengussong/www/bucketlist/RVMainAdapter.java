package ua.aengussong.www.bucketlist;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Blob;
import java.sql.Timestamp;

/**
 * Created by coolsmileman on 24.05.2017.
 */

public class RVMainAdapter extends RecyclerView.Adapter<RVMainAdapter.WishViewHolder> {

    final private WishClickListener onWishClickListener;

    private Cursor cursor;

    RVMainAdapter(Cursor cursor, WishClickListener listener){
        onWishClickListener = listener;
        this.cursor = cursor;
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
        if(!cursor.moveToPosition(position))
            return;
        String title = cursor.getString(cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_TITLE));
       // byte[] image = cursor.getBlob(cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_IMAGE));
        int price = cursor.getInt(cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_PRICE));
       // int category = cursor.getInt(cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_CATEGORY));
        /*String description = cursor.getString(cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_DESCRIPTION));
        Timestamp targetDate = Timestamp.valueOf(cursor.getString(
                cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_TARGET_DATE)));
        Timestamp achievedDate = Timestamp.valueOf(cursor.getString(
                cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_ACHIEVED_DATE)));*/

        holder.rvWishTitle.setText(title);
       // holder.rvWishCategory.setText(String.valueOf(category));
        holder.rvWishPrice.setText(String.valueOf(price));

      /*  BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
        holder.rvWishImage.setImageBitmap(bitmap);
*/

/*        holder.bind(position);*/
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
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
/*        void bind(int position){
            rvWishImage.setImageResource(R.mipmap.ic_launcher);
            rvWishTitle.setText(String.valueOf(position));
            rvWishPrice.setText(String.valueOf(position+1));
            rvWishCategory.setText(String.valueOf(position+2));
        }*/

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            onWishClickListener.onWishClicked(clickedPosition);
        }
    }
}
