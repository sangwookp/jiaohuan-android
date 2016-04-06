package com.jiaohuan.jiaohuan;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Collections;
import java.util.List;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.myViewHolder> {

    private LayoutInflater mLayoutInflater;

    List<OneRow> data = Collections.emptyList();

    public RecycleAdapter(Context context, List<OneRow> data){
        mLayoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    public OneRow getRow(int position) {
        return this.data.get(position);
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.single_row, parent, false);

        myViewHolder holder = new myViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(myViewHolder holder, int position) {

        OneRow current = data.get(position);

        holder.name.setText(current.getName());
        holder.company.setText(current.getCompany());

        holder.icon.setImageResource(current.getPic());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        TextView company;
        ImageView icon;

        public myViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            company = (TextView) itemView.findViewById(R.id.company);
            icon = (ImageView) itemView.findViewById(R.id.profile_pic);
        }
    }
}
