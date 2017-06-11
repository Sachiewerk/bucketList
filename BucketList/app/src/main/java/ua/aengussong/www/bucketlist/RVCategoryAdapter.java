package ua.aengussong.www.bucketlist;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import ua.aengussong.www.bucketlist.database.BucketListContracts;

/**
 * Created by coolsmileman on 06.06.2017.
 */

public class RVCategoryAdapter extends RecyclerView.Adapter<RVCategoryAdapter.CategoryViewHolder>{

    private Cursor cursor;
    private Context context;
    private RVCategoryAdapter.CategoryViewHolder categoryViewHolder;

    RVCategoryAdapter(Context context){
        this.context = context;
    }

    @Override
    public RVCategoryAdapter.CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.category_rv_layout,parent,false);
        categoryViewHolder = new RVCategoryAdapter.CategoryViewHolder(view);
        return new RVCategoryAdapter.CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RVCategoryAdapter.CategoryViewHolder holder, int position) {
        int idIndex = cursor.getColumnIndex(BucketListContracts.Category._ID);
        int titleIndex = cursor.getColumnIndex(BucketListContracts.Category.COLUMN_TITLE);

        cursor.moveToPosition(position);

        int id = cursor.getInt(idIndex);
        String title = cursor.getString(titleIndex);

        holder.itemView.setTag(id);
        holder.rvCategoryTitle.setText(title);


        GradientDrawable backgroundGradient = (GradientDrawable)holder.circle.getBackground();

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        backgroundGradient.setColor(color);
    }

    @Override
    public int getItemCount() {
        if(cursor == null)
            return 0;
        else
            return cursor.getCount();
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView rvCategoryTitle;
        ImageView circle;

        public CategoryViewHolder(View itemView) {
            super(itemView);

            rvCategoryTitle = (TextView) itemView.findViewById(R.id.rv_category_title);
            circle = (ImageView) itemView.findViewById(R.id.rv_category_circle);
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
