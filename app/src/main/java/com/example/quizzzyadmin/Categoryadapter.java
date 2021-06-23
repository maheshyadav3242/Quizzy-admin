package com.example.quizzzyadmin;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Categoryadapter  extends RecyclerView.Adapter<Categoryadapter.Viewholder> {

    private List<Categorymodel> categorymodelList;
    private Deletelistener deletelistener;

    public Categoryadapter(List<Categorymodel> categorymodelList,Deletelistener deletelistener) {
        this.categorymodelList = categorymodelList;
        this.deletelistener=deletelistener;
    }

    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item,parent,false);

        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        holder.setdata(categorymodelList.get(position).getUrl(),categorymodelList.get(position).getName(),categorymodelList.get(position).getKey(),position);

    }

    @Override
    public int getItemCount() {
        return categorymodelList.size();
    }


    class Viewholder extends RecyclerView.ViewHolder{
        private CircleImageView imageView;
        private TextView title;
        private ImageButton delete;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.myimageview);
            title=itemView.findViewById(R.id.titleimage);
            delete=itemView.findViewById(R.id.delete);
        }
        private void setdata(String url, final String title, final String key,final int position){
            Glide.with(itemView.getContext()).load(url).into(imageView);
            this.title.setText(title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(itemView.getContext(),Setsactivity.class);
                    intent.putExtra("title",title);
                    intent.putExtra("position",position);
                    intent.putExtra("key",key);
                    itemView.getContext().startActivity(intent);

                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletelistener.ondelete(key,position);
                }
            });

        }
    }
    public interface Deletelistener{
        public void ondelete(String key,int position);

    }

}
