package com.srijan.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout RLHome;
    private ProgressBar loadingPB;
    private TextView cityNameTextView,temperatureTv,conditionTV;
    private TextInputEditText cityNameEditText;
    private ImageView backIV,iconIV,searchIcon;
    private RecyclerView weatherRV;

    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;

    private static final int PERMISSION_CODE=1;

    private String cityName;

    private LineChart lineChart;

    private ArrayList<Entry> dataVal=new ArrayList<Entry>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        weatherRV.setAdapter(weatherRVAdapter);
    }

    private void init(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        RLHome=findViewById(R.id.RLHome);
        loadingPB=findViewById(R.id.PBLoading);
        cityNameTextView=findViewById(R.id.citynametextview);
        temperatureTv=findViewById(R.id.temperatureTextView);
        conditionTV=findViewById(R.id.conditionTextView);
        cityNameEditText=findViewById(R.id.editcityname);
        backIV=findViewById(R.id.backIV);
        iconIV=findViewById(R.id.iconimageview);
        searchIcon=findViewById(R.id.IVSearch);
        weatherRV=findViewById(R.id.weatherrecyclerview);
        weatherRVModelArrayList=new ArrayList<>();
        weatherRVAdapter=new WeatherRVAdapter(this,weatherRVModelArrayList);

        lineChart=findViewById(R.id.linechart);


//
//        getWeatherInfo(cityName);

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city=cityNameEditText.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTextView.setText(city);
                    getWeatherInfo(city);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted...", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please provide the permission...", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getWeatherInfo(String cityName){
        String url="https://api.weatherapi.com/v1/forecast.json?key=1db170ef9f974014bf785858210912 &q="+cityName+"&days=7&aqi=no&alerts=no";
        cityNameTextView.setText(cityName);
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                RLHome.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();
                dataVal.clear();
                try {
                    String temperature=response.getJSONObject("current").getString("temp_c");
                    temperatureTv.setText(temperature);

                    int isDay=response.getJSONObject("current").getInt("is_day");
                    String condition=response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon=response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if (isDay==1){
                        //day
                        Picasso.get().load(R.drawable.day).into(backIV);
                    }else{
                        //night
                        Picasso.get().load(R.drawable.night).into(backIV);
                    }

                    JSONObject forecastObj=response.getJSONObject("forecast");
                    JSONObject forecastObject=forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hoursArray=forecastObject.getJSONArray("hour");
                    for (int i=0;i< hoursArray.length();i++){
                        JSONObject hourObj= hoursArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String temperatureC=hourObj.getString("temp_c");
                        String img=hourObj.getJSONObject("condition").getString("icon");
                        String wind=hourObj.getString("wind_kph");
                        Float temp=Float.valueOf(temperatureC);

//                        dataVal.add(new Entry(i,temp));
                        weatherRVModelArrayList.add(new WeatherRVModel(time,temperatureC,img,wind));
                        weatherRVAdapter.notifyDataSetChanged();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Enter valid city name", Toast.LENGTH_SHORT).show();
                Log.d("errorInResponse", "onErrorResponse: "+error);
            }
        });

        for (int i=0;i<weatherRVModelArrayList.size();i++){
            dataVal.add(new Entry(i,Float.parseFloat(weatherRVModelArrayList.get(i).getTemperature())));
        }
        requestQueue.add(jsonObjectRequest);
        lineGraphSet();
    }

    private void lineGraphSet() {
        if (dataVal.size()>0) {
            LineDataSet lineDataSet = new LineDataSet(dataVal, "Today's Weather");
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet);
            LineData lineData = new LineData(dataSets);

            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillColor(Color.GREEN);
            lineDataSet.setColor(Color.GREEN);
            lineChart.setVisibleYRangeMaximum(50f, YAxis.AxisDependency.LEFT);

            lineChart.setBackgroundColor(Color.WHITE);

            lineChart.setTouchEnabled(false);

            lineChart.setData(lineData);
            lineChart.notifyDataSetChanged();
            for (int i=0;i<dataVal.size();i++){
                Log.d("data value "+i, "lineGraphSet: "+dataVal.get(i));
            }
        }
        else {
            Log.d("data value", "lineGraphSet: empty data set");
        }
    }


}