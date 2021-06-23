package com.example.quizzzyadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Questionadapter extends RecyclerView.Adapter<Questionadapter.viewholder> {
    private List<Questionmodel> list;
    private String category;
    private Deletelistenernew deletelistenernew;

    public Questionadapter(List<Questionmodel> list, String category,Deletelistenernew deletelistenernew) {
        this.category=category;
        this.list = list;
        this.deletelistenernew=deletelistenernew;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.questionitem,parent,false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        String question=list.get(position).getQuestion();
        String answer=list.get(position).getAnswer();
        holder.setdata(question,answer,position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewholder extends RecyclerView.ViewHolder{
        private TextView question,answer;


        public viewholder(@NonNull View itemView) {
            super(itemView);
            question=itemView.findViewById(R.id.question);
            answer=itemView.findViewById(R.id.answer);
        }
        private void setdata(String question, String answer, final int position){

         this.question.setText(position+1+"."+question);
            this.answer.setText("Ans."+answer);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editintent=new Intent(itemView.getContext(),Addquestion.class);
                    editintent.putExtra("categoryname",category);
                    editintent.putExtra("setid",list.get(position).getSet());
                    editintent.putExtra("position",position);
                    itemView.getContext().startActivity(editintent);


                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deletelistenernew.onlongclick(position,list.get(position).getId());


                    return false;
                }
            });

        }
    }
    public interface Deletelistenernew{
        void onlongclick(int position,String id);
    }

}
