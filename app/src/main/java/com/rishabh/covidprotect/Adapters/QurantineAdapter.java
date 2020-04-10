package com.rishabh.covidprotect.Adapters;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.rishabh.covidprotect.Models.ReportModel;
import com.rishabh.covidprotect.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class QurantineAdapter extends RecyclerView.Adapter<QurantineAdapter.MyHolder> {
    public List<ReportModel> data;
    public Context ctx;
    public QurantineAdapter(Context ctx, List<ReportModel> data) {
        this.ctx = ctx;
        this.data = data;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_report,parent,false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ReportModel val = data.get(position);
        String url=val.img;
        Glide.with(ctx)
                .load(url)
                .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                .apply(RequestOptions.circleCropTransform())

                .into(holder.imageView);


        String ts=getDate(Long.parseLong(val.report_time));

//        holder.report_time.setText(ctx.getResources().getString(R.string.reported_on)+ date + ctx.getResources().getString(R.string.at) + time);
        holder.report_time.setText(ts);

    }
    private String getDate(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = formatter.format(new Date(time));
        return dateString;
    }

    @Override
    public int getItemCount() {
        if(data == null) return 0;
        return data.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder{
        TextView report_time;
        ImageView imageView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            report_time = itemView.findViewById(R.id.report_time);
            imageView =itemView.findViewById(R.id.pic);


        }
    }
}
