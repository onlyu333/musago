package com.example.musagoboard;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class tbAdapter extends RecyclerView.Adapter<tbAdapter.ViewHolder> {
    private static final String TAG = "zzix";
    ArrayList<tbFreeBoard> items = new ArrayList<tbFreeBoard>();
    OnTbItemListener listener;//리스너 선언

//    public void addItems(ArrayList<tbFreeBoard> items) {
////        Log.d(TAG, "addItem :" +items.get(2));
////
////    }
        public void addItem(tbFreeBoard item) {
        Log.d(TAG, "addItem :" +item);
        items.add(item);

    }

    public void setItems(ArrayList<tbFreeBoard> items) {
        Log.d(TAG, "setItems ");
        this.items = items;
    }

    public tbFreeBoard getItem(int position) {
        return items.get(position);
    }

    public void setItem(int position, tbFreeBoard item) {
        items.set(position, item);
    }

    public void setOnItemClickListener(OnTbItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.content_item, parent, false);

        return new ViewHolder(itemView,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        tbFreeBoard item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        Log.i(TAG,"getItemCount");
        if (items != null) {
            return items.size();
        }
        return 0;

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView Cardtv_seqNo;
        TextView Cardtv_subject;
        TextView Cardtv_author;
        TextView Cardtv_regDate;


        public ViewHolder(View itemView,final OnTbItemListener listener) {
            super(itemView);

            // 뷰 객체에 대한 참조
            Cardtv_seqNo = itemView.findViewById(R.id.Cardtv_seqNo);
            Cardtv_subject = itemView.findViewById(R.id.Cardtv_subject);
            Cardtv_author = itemView.findViewById(R.id.Cardtv_author);
            Cardtv_regDate = itemView.findViewById(R.id.Cardtv_regDate);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    int position = getAdapterPosition();

                    if(listener != null){
                        listener.onItemClick(ViewHolder.this,v,position);
                    }
                }
            });

        }

        public void setItem(tbFreeBoard item) {

            Cardtv_seqNo.setText(Integer.toString(item.getSeqNo()));//Integer일때
            Cardtv_subject.setText(item.getSubject());
            Cardtv_author.setText(item.getAuthor());
            Cardtv_regDate.setText(item.getRegDate());
        }
    }


}
