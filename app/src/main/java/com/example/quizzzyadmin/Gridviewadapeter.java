package com.example.quizzzyadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class Gridviewadapeter extends BaseAdapter {
    public List<String> sets;
    private String category;
    private Gridlistener listener;

    public Gridviewadapeter( List<String> sets,String category,Gridlistener listener) {
        this.listener=listener;
        this.sets = sets;
        this.category=category;
    }

    @Override
    public int getCount() {
        return sets.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View view;
        if(convertView==null){
            view= LayoutInflater.from(parent.getContext()).inflate(R.layout.setitem,parent,false);
        }
        else{
            view=convertView;
        }
        if(position==0){
            ((TextView)view.findViewById(R.id.settextview)).setText("+");
        }
        else {
            ((TextView)view.findViewById(R.id.settextview)).setText(String.valueOf(position));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position==0){
                    listener.addset();

                }else {
                    Intent intent=new Intent(parent.getContext(),Questiosactivity.class);
                    intent.putExtra("category",category);
                    intent.putExtra("setid",sets.get(position-1));
                parent.getContext().startActivity(intent);

                }

            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(position!=0){
                    listener.onlongclick(sets.get(position-1),position);
                }

                return false;
            }
        });

       /* ((TextView)view.findViewById(R.id.settextview)).setText(String.valueOf(position));*/
        return view;
    }
    public  interface Gridlistener{
        public void addset();
        void onlongclick(String setid,int position);
    }

}
