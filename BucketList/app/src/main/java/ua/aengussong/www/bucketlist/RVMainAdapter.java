package ua.aengussong.www.bucketlist;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ua.aengussong.www.bucketlist.database.BucketListContracts;

/**
 * Created by coolsmileman on 24.05.2017.
 */

public class RVMainAdapter extends RecyclerView.Adapter<RVMainAdapter.WishViewHolder> {

    final private WishClickListener onWishClickListener;

    private Cursor cursor;
    private Context context;
    private WishViewHolder wishViewHolder;

    RVMainAdapter(Context context, WishClickListener listener){
        onWishClickListener = listener;
        this.context = context;
    }

    @Override
    public WishViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.wishes_list_layout,parent,false);
        wishViewHolder = new WishViewHolder(view);
        return new WishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WishViewHolder holder, int position) {
        int idIndex = cursor.getColumnIndex(BucketListContracts.WishList._ID);
        int titleIndex = cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_TITLE);
        int priceIndex = cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_PRICE);

        cursor.moveToPosition(position);

        int id = cursor.getInt(idIndex);
        String title = cursor.getString(titleIndex);
       // byte[] image = cursor.getBlob(cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_IMAGE));
        int price = cursor.getInt(priceIndex);
       // int category = cursor.getInt(cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_CATEGORY));
        /*String description = cursor.getString(cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_DESCRIPTION));
        Timestamp targetDate = Timestamp.valueOf(cursor.getString(
                cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_TARGET_DATE)));
        Timestamp achievedDate = Timestamp.valueOf(cursor.getString(
                cursor.getColumnIndex(BucketListContracts.WishList.COLUMN_ACHIEVED_DATE)));*/

        holder.itemView.setTag(id);
        holder.rvWishTitle.setText(title);

       // holder.rvWishCategory.setText(String.valueOf(category));
        holder.rvWishPrice.setText(String.valueOf(price));

      /*  BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
        holder.rvWishImage.setImageBitmap(bitmap);
*/
    }

    @Override
    public int getItemCount() {
        if(cursor == null)
            return 0;
        else
            return cursor.getCount();
    }

    public interface WishClickListener{
        void onWishClicked(View view);
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

        @Override
        public void onClick(View v) {
            onWishClickListener.onWishClicked(v);
        }
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (cursor== c) {
            return null; // bc nothing has changed
        }
        Cursor temp = cursor;
        this.cursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }
}
