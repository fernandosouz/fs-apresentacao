package com.example.fernandosouza.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import javax.security.auth.callback.Callback;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView textTitle;
    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;

    private TextView mDisplayDateInicio;
    private TextView mDisplayDateFim;
    private DatePickerDialog.OnDateSetListener mDateSetListenerInicio;
    private DatePickerDialog.OnDateSetListener mDateSetListenerFim;


    public String endDate = "";
    public String initDate = "";
    public int[] arrayGraphs = new int[8];
    public int graphAtual = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTitle = (TextView) findViewById(R.id.textTitle);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);


        textTitle.setPadding(30, 40, 10, 0);
        textView.setPadding( 0, 80, 0, 0);
        textView2.setPadding(0, 160, 0, 0);
        textView3.setPadding(0, 160, 0, 0);
        textView4.setPadding(0, 160, 0, 0);
        arrayGraphs[0] = 0;
        arrayGraphs[1] = R.id.graph;
        arrayGraphs[2] = R.id.graph2;
        arrayGraphs[3] = R.id.graph3;
        arrayGraphs[4] = R.id.graph4;
        arrayGraphs[5] = R.id.graph5;
        arrayGraphs[6] = R.id.graph6;
        arrayGraphs[7] = R.id.graph7;

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        month = month + 1;
        String monthString = Integer.toString(month);
        String dayString = Integer.toString(day);

        if(Integer.toString(month).length() == 1){
            monthString = "0" + Integer.toString(month);
        }
        if(Integer.toString(day).length() == 1){
            dayString = "0" + Integer.toString(day);
        }
        endDate = dayString + "/"  + monthString + "/" + year;


        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -15);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        month = month + 1;
        monthString = Integer.toString(month);
        dayString = Integer.toString(day);

        if(Integer.toString(month).length() == 1){
            monthString = "0" + Integer.toString(month);
        }
        if(Integer.toString(day).length() == 1){
            dayString = "0" + Integer.toString(day);
        }
        initDate = dayString + "/"  + monthString + "/" + year;


        mDisplayDateInicio = (TextView) findViewById(R.id.tvDate);
        mDisplayDateFim = (TextView) findViewById(R.id.tvDate2);
        mDisplayDateInicio.setText(initDate);
        mDisplayDateFim.setText(endDate);

        verifyDatesAndCallFirebase();

        setDates();
    }

    public void initGraphs(ArrayList arrayList, int idGraph){


        GraphView graph = (GraphView) findViewById(idGraph);
        graph.removeAllSeries();

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
                new DataPoint(1, (Long)arrayList.get(0)),
                new DataPoint(2, (Long)arrayList.get(1)),
                new DataPoint(3, (Long)arrayList.get(2)),
                new DataPoint(4, (Long)arrayList.get(3)),
                new DataPoint(5, (Long)arrayList.get(4))
        });

        graph.addSeries(series);
        series.setAnimated(true);
        series.setSpacing(15);
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLACK);
        graph.getViewport().setMinY(0);


        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return getColorGraphs(data.getX());
            }
        });
    }

    public int getColorGraphs(Double data){
        if(data == 1.0){
            return Color.rgb(204,0, 0);
        }
        if(data == 2.0){
            return Color.rgb(255,128, 0);
        }
        if(data == 3.0){
            return Color.rgb(230,230, 0);
        }
        if(data == 4.0){
            return Color.rgb(102,204, 0);
        }
        if(data == 5.0){
            return Color.rgb(0,153, 0);
        }

        return Color.rgb(0,51, 255);
    }

    public void getFirebaseDatas(String pergunta, final int idGraph){
        System.out.println("pergunta " + pergunta);
        System.out.println(graphAtual);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("forte_ativo/pergunta_"+pergunta+"/");

        if(graphAtual == arrayGraphs.length){
            graphAtual = 1;
        }

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList arrayList = new ArrayList();
                Long a = new Long(0);
                arrayList.add(a);
                arrayList.add(a);
                arrayList.add(a);
                arrayList.add(a);
                arrayList.add(a);
                arrayList.add(a);
                arrayList.add(a);
                arrayList.add(a);

                for(DataSnapshot i:dataSnapshot.getChildren() ){
                    String date = "";
                    if(i.getKey().toString().length() == 8){
                         date = i.getKey().substring(0,2) + "/" +
                                i.getKey().substring(2,4) + "/" +
                                i.getKey().substring(4,8);
                    }


                    if((i.getValue() instanceof ArrayList || i.getValue() instanceof HashMap) && checkDateBetweenDates(initDate, endDate, date)){
                        int index = 0;
                        for(Object item: (ArrayList)i.getValue()){
                            if(item != null){
                                arrayList.set(index, (Long) item + (Long) arrayList.get(index));
                                index++;
                            }
                        }
                    }
                }

                initGraphs(arrayList, idGraph);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public void setDates(){
        /*DATEPICKER INÍCIO*/

        mDisplayDateInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year;
                int month;
                int day;
                String initDateOnlyNumber = initDate.replace("/","");
                if(initDateOnlyNumber.length() > 0){
                    day = Integer.parseInt(initDateOnlyNumber.substring(0,2));
                    month = Integer.parseInt(initDateOnlyNumber.substring(2,4))-1;
                    year = Integer.parseInt(initDateOnlyNumber.substring(4,8));
                }else{
                    Calendar cal = Calendar.getInstance();
                    year = cal.get(Calendar.YEAR);
                    month = cal.get(Calendar.MONTH);
                    day = cal.get(Calendar.DAY_OF_MONTH);
                }



                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerInicio,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDisplayDateFim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year;
                int month;
                int day;
                String endDateOnlyNumber = endDate.replace("/","");
                if(endDateOnlyNumber.length() > 0){
                    day = Integer.parseInt(endDateOnlyNumber.substring(0,2));
                    month = Integer.parseInt(endDateOnlyNumber.substring(2,4))-1;
                    year = Integer.parseInt(endDateOnlyNumber.substring(4,8));
                }else{
                    Calendar cal = Calendar.getInstance();
                    year = cal.get(Calendar.YEAR);
                    month = cal.get(Calendar.MONTH);
                    day = cal.get(Calendar.DAY_OF_MONTH);
                }

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListenerFim,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListenerInicio = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String monthString = Integer.toString(month);
                String dayString = Integer.toString(day);

                if(Integer.toString(month).length() == 1){
                    monthString = "0" + Integer.toString(month);
                }
                if(Integer.toString(day).length() == 1){
                    dayString = "0" + Integer.toString(day);
                }
                initDate = dayString + "/"  + monthString + "/" + year;
                mDisplayDateInicio.setText(initDate);

                verifyDatesAndCallFirebase();
            }
        };

        mDateSetListenerFim = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String monthString = Integer.toString(month);
                String dayString = Integer.toString(day);

                if(Integer.toString(month).length() == 1){
                    monthString = "0" + Integer.toString(month);
                }
                if(Integer.toString(day).length() == 1){
                    dayString = "0" + Integer.toString(day);
                }
                endDate = dayString + "/"  + monthString + "/" + year;
                mDisplayDateFim.setText(endDate);

                verifyDatesAndCallFirebase();
            }
        };
    }

    public Boolean verifyDates(String initDateParam, String endDateParam){
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Calendar initDate= new GregorianCalendar();
        Calendar endDate = new GregorianCalendar();
        try{
            initDate.setTime(format.parse(initDateParam));
            endDate.setTime(format.parse(endDateParam));
        }catch (Exception e){

        }

        if(initDate.getTimeInMillis() <= endDate.getTimeInMillis()){
            return true;
        }else{
            return false;
        }
    }

    public Boolean checkDateBetweenDates(String initDateParam, String endDateParam, String dateToCheckParam){
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Calendar initDate= new GregorianCalendar();
        Calendar endDate = new GregorianCalendar();
        Calendar dateToCheck = new GregorianCalendar();
        try{
            initDate.setTime(format.parse(initDateParam));
            endDate.setTime(format.parse(endDateParam));
            dateToCheck.setTime(format.parse(dateToCheckParam));
        }catch (Exception e){

        }



        if(initDate.getTimeInMillis() <= dateToCheck.getTimeInMillis()
                && endDate.getTimeInMillis() >= dateToCheck.getTimeInMillis()){
            return true;
        }else{
            return false;
        }
    }

    public Boolean verifyDatesHaveContent(){
        if(initDate.length() == 0 || endDate.length() == 0){
            return false;
        }else{
            return true;
        }
    }


    public void verifyDatesAndCallFirebase(){
        if(!verifyDatesHaveContent()){
            Context contexto = getApplicationContext();
            String texto = "Você deve selecionar as duas datas antes de continuar";
            int duracao = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(contexto, texto,duracao);
            toast.show();
            return;
        }

        if(verifyDates(initDate, endDate)){
            getFirebaseDatas("1", R.id.graph);
            getFirebaseDatas("2", R.id.graph2);
            getFirebaseDatas("3", R.id.graph3);
            getFirebaseDatas("4", R.id.graph4);
            getFirebaseDatas("5", R.id.graph5);
            getFirebaseDatas("6", R.id.graph6);
            getFirebaseDatas("7", R.id.graph7);
        }else{
            if(initDate != null && endDate !=null){
                Context contexto = getApplicationContext();
                String texto = "A primeira data deve ser menor que a segunda";
                int duracao = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(contexto, texto,duracao);
                toast.show();
            }
        }
    }


}
