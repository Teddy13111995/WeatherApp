package com.srijan.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //1. Add dependency in build.gradle(Module) for volley(implementation 'com.android.volley:volley:1.1.1')
    // and picasso(implementation 'com.squareup.picasso:picasso:2.71828')
    //2. Add color scheme for recyclerview
    //3.Update main activity xml file
    //4. Declare the variables used in the xml file.
    //5. Initialize the variables.
    //6. Create Weather Recycler view model
    //7. Create weather recycler view adapter
    //8. Initialise the weatherRvmodelarray list and weatherRV adapter.
    //

    private ScrollView RLHome;
    private ProgressBar loadingPB;
    private TextView cityNameTextView,temperatureTv,conditionTV;
    private TextInputEditText cityNameEditText;
    private ImageView backIV,iconIV,searchIcon;
    private RecyclerView weatherRV;

    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;

    private LocationManager locationManager;
    private int PERMISSION_CODE=1;

    private String cityName;

    private GraphView graph;
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
        graph=findViewById(R.id.graphview);
        LineGraphSeries<DataPoint> series=new LineGraphSeries<DataPoint>();
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }


        getWeatherInfo(cityName);

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

        requestQueue.add(jsonObjectRequest);
    }
}