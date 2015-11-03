package com.lemhell.beziercollage.recycler;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lemhell.beziercollage.R;
import com.lemhell.beziercollage.TemplateActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements Observer {

    private final Context context;
    private List<RecyclerItem> items;
    private EVENT_TYPE type;

    @Override
    public void update(Observable observable, Object data) {
        Intent intentBC;
        int id = ((TypeAndInt) data).integer;
        switch (type) {
            case DYNAMIC: {
                TemplateActivity.isStatic = false;
                intentBC = new Intent(BROADCAST_ONCLICK_DYNAMIC);
            }
            break;
            case STATIC: {
                intentBC = new Intent(BROADCAST_ONCLICK_STATIC);
                TemplateActivity.isStatic = true;
            }
            break;
            case FRAME: {
                intentBC = new Intent(BROADCAST_ONCLICK_FRAME);
            }
            break;
            case STICKER: {
                intentBC = new Intent(BROADCAST_ONCLICK_STICKER);
            }
            break;
            default: {
                intentBC = new Intent(BROADCAST_ONCLICK_DYNAMIC);
            }
            break;
        }
        intentBC.putExtra("IntId", items.get(id).getImageId());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentBC);
    }

    public enum EVENT_TYPE {STATIC, DYNAMIC, FRAME, STICKER}

    public static final String BROADCAST_ONCLICK_STATIC     = "BROADCAST_ONCLICK_STATIC";
    public static final String BROADCAST_ONCLICK_DYNAMIC    = "BROADCAST_ONCLICK_DYNAMIC";
    public static final String BROADCAST_ONCLICK_FRAME      = "BROADCAST_ONCLICK_FRAME";
    public static final String BROADCAST_ONCLICK_STICKER    = "BROADCAST_ONCLICK_STICKER";

    public RecyclerAdapter(Context context, List<RecyclerItem> items, EVENT_TYPE type) {
        this.context = context;
        this.items = items;
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new ViewHolder(v, type, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RecyclerItem item = items.get(position);
        Picasso.with(context)
                .load(item.getImageId())
                .resize(item.getSize(), item.getSize())
                .into(holder.image);
        holder.setPosition(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView image;
        private int position;
        private EVENT_TYPE type;
        private MyObservable observable;

        public ViewHolder(View itemView, EVENT_TYPE type, Observer observer) {
            super(itemView);
            itemView.setOnClickListener(this);
            image = (ImageView) itemView.findViewById(R.id.imageView);
            observable = new MyObservable();
            this.type = type;
            this.observable.addObserver(observer);
        }

        @Override
        public void onClick(View v) {
            observable.setChanged();
            observable.notifyObservers(new TypeAndInt(type, position));
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

    public static class TypeAndInt {
        public EVENT_TYPE type;
        public Integer integer;

        public TypeAndInt(EVENT_TYPE type, Integer integer) {
            this.type = type;
            this.integer = integer;
        }
    }

    public static class MyObservable extends Observable {
        public MyObservable() {
            super();
        }

        @Override
        protected void setChanged() {
            super.setChanged();
        }
    }
}
