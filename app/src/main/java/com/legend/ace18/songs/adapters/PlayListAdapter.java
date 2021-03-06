package com.legend.ace18.songs.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.legend.ace18.songs.R;
import com.legend.ace18.songs.model.PlayList;

import java.util.Collections;
import java.util.List;

/**
 * Created by Legend.ace18 on 7/27/2015.
 */
public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.MyViewHolder> {

    private Context context;
    private List<PlayList> playLists = Collections.emptyList();
    private LayoutInflater inflater;
    private int layout_id;
    private TouchListener touchListener;

    public PlayListAdapter(Context context, int layout_id, List<PlayList> playLists){
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.playLists = playLists;
        this.layout_id = layout_id;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(layout_id, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PlayList playList = playLists.get(position);
        holder.tv_title.setText(playList.getTitle());
    }

    public void setTouchListener(TouchListener touchListener){
        this.touchListener = touchListener;
    }

    @Override
    public int getItemCount() {
        return playLists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{

        private ImageView iv_cardImage;
        private TextView tv_title;
        private ImageButton btn_overflow;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_cardImage = (ImageView) itemView.findViewById(R.id.iv_cardImage);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            btn_overflow = (ImageButton) itemView.findViewById(R.id.btn_overflow);
            btn_overflow.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_UP:
                    //Toast.makeText(context, "from adapter", Toast.LENGTH_LONG).show();
                    touchListener.itemTouched(view, getAdapterPosition());
                    return true;

            }
            return false;
        }
    }

    public interface TouchListener{
        void itemTouched(View v, int position);
    }
}
