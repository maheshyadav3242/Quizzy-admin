package com.example.quizzzyadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Questiosactivity extends AppCompatActivity {
    private Button add,excel;
    private RecyclerView newrecucleview;
    private Questionadapter questionadapter;
    public static List<Questionmodel>list;
    private Dialog loadingdialog;
    private Questionmodel questionmodel;
    private DatabaseReference myref;
    private TextView loadingteext;

    public  static final int CELL_COUNT=6;
    private String categoryname;
    private String setid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questiosactivity);

        Toolbar toolbar1=findViewById(R.id.toolbarnew1);
        setSupportActionBar(toolbar1);
        myref= FirebaseDatabase.getInstance().getReference();
        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loadingdialog);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingteext=loadingdialog.findViewById(R.id.textViewdailog);

      categoryname=getIntent().getStringExtra("category");
       setid=getIntent().getStringExtra("setid");

        getSupportActionBar().setTitle(categoryname);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        add=findViewById(R.id.addbutton1);
        excel=findViewById(R.id.excelbutton1);
        newrecucleview=findViewById(R.id.recycleview);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        newrecucleview.setLayoutManager(layoutManager);
        list=new ArrayList<>();
        questionadapter=new Questionadapter(list, categoryname, new Questionadapter.Deletelistenernew() {
            @Override
            public void onlongclick(final int position, final String id) {


                new AlertDialog.Builder(Questiosactivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert).setTitle("delete category").
                        setMessage("are you sure you want to delete question").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadingdialog.show();
                        myref.child("SETS").child(setid).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                  list.remove(position);
                                  questionadapter.notifyItemRemoved(position);

                                }else {
                                    Toast.makeText(Questiosactivity.this, "failed to delete", Toast.LENGTH_SHORT).show();
                                }
                                loadingdialog.dismiss();
                            }
                        });
                    }
                }).setNegativeButton("cancel",null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        });
        newrecucleview.setAdapter(questionadapter);

        getdata(categoryname,setid);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addquestion =new Intent(Questiosactivity.this,Addquestion.class);
                addquestion.putExtra("categoryname",categoryname);
                addquestion.putExtra("setid",setid);
                startActivity(addquestion);
            }
        });
        excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ActivityCompat.checkSelfPermission(Questiosactivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    selectfile();
                }else {
                    ActivityCompat.requestPermissions(Questiosactivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
                }

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectfile();
            }else{
                Toast.makeText(this, "please grant permision ", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void selectfile(){

        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent,"select file"),102);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==102){
            if(requestCode==RESULT_OK){
                String filepath=data.getData().getPath();
                if(filepath.endsWith(".xlsx")){
                    readfile(data.getData());
                }else{
                    Toast.makeText(this, "please an choose an excel file", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
        finish();
        }
        return super.onOptionsItemSelected(item);

    }
    private void getdata(String categoryname, final String setid){
        loadingdialog.show();

        myref.child("SETS").child(setid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            String id=dataSnapshot1.getKey();
                            String question=dataSnapshot1.child("question").getValue().toString();
                            String a=dataSnapshot1.child("optiona").getValue().toString();
                            String b=dataSnapshot1.child("optionb").getValue().toString();
                            String c=dataSnapshot1.child("optionc").getValue().toString();
                            String d=dataSnapshot1.child("optiond").getValue().toString();
                            String correctans=dataSnapshot1.child("correctans").getValue().toString();

                            list.add(new Questionmodel(id,question,a,b,c,d,correctans,setid));

                        }
                        loadingdialog.dismiss();
                        questionadapter.notifyDataSetChanged();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Questiosactivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                        loadingdialog.dismiss();
                        finish();

                    }
                });

    }
    private void readfile(final Uri fileuri) {
        loadingteext.setText("Scanning quesions..");
        loadingdialog.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {



        final HashMap<String,Object> parentmap=new HashMap<>();
        final List<Questionmodel>templist=new ArrayList<>();

        try {
            InputStream inputStream=getContentResolver().openInputStream(fileuri);
            try {
                XSSFWorkbook workbook=new XSSFWorkbook(inputStream);
                XSSFSheet sheet=workbook.getSheetAt(0);
                FormulaEvaluator formulaEvaluator=workbook.getCreationHelper().createFormulaEvaluator();

                int rowscount=sheet.getPhysicalNumberOfRows();
                if(rowscount>0){
                    for(int r=0;r<rowscount;r++){
                        Row row=sheet.getRow(r);

                        if(row.getPhysicalNumberOfCells()==CELL_COUNT){
                            String question=getcelldata(row,0,formulaEvaluator);
                            String a=getcelldata(row,1,formulaEvaluator);
                            String b=getcelldata(row,2,formulaEvaluator);
                            String c=getcelldata(row,3,formulaEvaluator);
                            String d=getcelldata(row,4,formulaEvaluator);
                            String correctans=getcelldata(row,5,formulaEvaluator);
                            if(correctans.equals(a)||correctans.equals(b)||correctans.equals(c)||correctans.equals(d)){
                                String id= UUID.randomUUID().toString();
                                HashMap<String,Object> questionmap=new HashMap<>();
                                questionmap.put("question",question);
                                questionmap.put("optiona",a);
                                questionmap.put("optionb",b);
                                questionmap.put("optionc",c);
                                questionmap.put("optiond",d);
                                questionmap.put("correctans",correctans);
                                questionmap.put("setno",setid);

                                parentmap.put(id,questionmap);
                                templist.add(new Questionmodel(id,question,a,b,c,d,correctans,setid));



                            }else {
                                final int finalR1 = r;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingteext.setText("loading..");
                                        loadingdialog.dismiss();

                                        Toast.makeText(Questiosactivity.this, "Row no."+(finalR1 +1)+"has no correct data", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                });

                        }


                        }else {
                            final int finalR = r;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingteext.setText("loading..");
                                    loadingdialog.dismiss();

                                    Toast.makeText(Questiosactivity.this, "Row no."+(finalR +1)+"has incorrect data", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            });

                        }


                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingteext.setText("uploading..");
                            FirebaseDatabase.getInstance().getReference().child("SETS").child(setid)
                                    .updateChildren(parentmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        list.addAll(templist);
                                        questionadapter.notifyDataSetChanged();

                                    }else{
                                        loadingteext.setText("loading..");


                                        Toast.makeText(Questiosactivity.this, "something went wrong", Toast.LENGTH_SHORT).show();


                                    }
                                    loadingdialog.dismiss();
                                }
                            });

                        }
                    });


                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingteext.setText("loading..");
                            loadingdialog.dismiss();

                            Toast.makeText(Questiosactivity.this, "file is empty", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return;
                }


            } catch (final IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingteext.setText("loading..");
                        loadingdialog.dismiss();

                        Toast.makeText(Questiosactivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }
        } catch (final FileNotFoundException e) {

            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingteext.setText("loading..");
                    loadingdialog.dismiss();
                    Toast.makeText(Questiosactivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        questionadapter.notifyDataSetChanged();
    }
    private String getcelldata(Row row, int cellposition, FormulaEvaluator formulaEvaluator){
        String value="";
        Cell cell=row.getCell(cellposition);
        switch (cell.getCellType()){
            case Cell.CELL_TYPE_BOOLEAN:
            return value+cell.getBooleanCellValue();

            case Cell.CELL_TYPE_NUMERIC:
                return value+cell.getNumericCellValue();

            case Cell.CELL_TYPE_STRING:
                return value+cell.getStringCellValue();

            default:
                return value;

        }
    }
}
