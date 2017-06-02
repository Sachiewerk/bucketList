package ua.aengussong.www.bucketlist;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ua.aengussong.www.bucketlist.database.BucketListContracts;

/**
 * Created by coolsmileman on 02.06.2017.
 */

public class RVAddMilestoneAdapter extends RecyclerView.Adapter<RVAddMilestoneAdapter.MilestoneViewHolder>{

    private Context context;
    private MilestoneViewHolder milestoneViewHolder;

    private ArrayList<String> milestonesArrayList;

    RVAddMilestoneAdapter(Context context, ArrayList<String> milestones){
        this.context = context;
        milestonesArrayList = milestones;
    }

    @Override
    public MilestoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.milestone_layout,parent,false);
        milestoneViewHolder = new MilestoneViewHolder(view);
//        return milestoneViewHolder;
        return new MilestoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MilestoneViewHolder holder, int position) {
        int id = position;

        holder.itemView.setTag(id);
        holder.newMilestone.setText(milestonesArrayList.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return milestonesArrayList.size();
    }

    public class MilestoneViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CheckBox newMilestone;

        public MilestoneViewHolder(View itemView) {
            super(itemView);

            newMilestone = (CheckBox) itemView.findViewById(R.id.milestone_checkbox);
        }

        @Override
        public void onClick(View v) {

        }
    }

}
