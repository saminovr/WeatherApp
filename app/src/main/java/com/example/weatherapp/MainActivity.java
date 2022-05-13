package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private EditText user_field;
    private Button main_btn;
    private TextView result_info;
    private TextView preResult_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user_field = findViewById(R.id.user_field);
        main_btn = findViewById(R.id.main_btn);
        result_info = findViewById(R.id.result_info);
        preResult_info = findViewById(R.id.preResult_info);

        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user_field.getText().toString().trim().equals("")) {
                    Toast.makeText(MainActivity.this, R.string.error_text, Toast.LENGTH_LONG).show();
                    result_info.setText("");
                }
                else {
                    String cityName = user_field.getText().toString().trim();
                    final String key = "624c8dfda3ddb79f08b3186caa1234bd";
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" +
                            key + "&units=metric&lang=ru";
                    System.out.println(url);
                    result_info.setText("");



                    if (hasConnection(MainActivity.this))
                        new GetURLData().execute(url);
                    else Toast.makeText(MainActivity.this, R.string.connection_error_text, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private class GetURLData extends AsyncTask <String, String, String> {

        protected void onPreExecute () {
            super.onPreExecute();
            preResult_info.setText("Подождите...");


        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
                try {
                    URL url = new URL(strings[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();
                    String line = "";

                    while ((line = reader.readLine()) != null)
                        buffer.append(line).append("\n");

                    return buffer.toString();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null)
                        connection.disconnect();
                    try {
                        if (reader != null)
                            reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute (String result) {
            if (result != null) {
                try {
                    JSONObject obj = new JSONObject(result);
                    int temperature = (int) Math.round(obj.getJSONObject("main").getDouble("temp"));
                    int temperatureFeelsLike = (int) Math.round(obj.getJSONObject("main").getDouble("feels_like"));
                    float windSpeed = (float) Math.round(obj.getJSONObject("wind").getDouble("speed") * 10) / 10;
                    String description = obj.getJSONArray("weather").getJSONObject(0).getString("description");

                    preResult_info.setText(description.toUpperCase());

                    result_info.setText("\nТемпература: " + temperature + " °С\n" +
                            "Ощущается как: " + temperatureFeelsLike + " °С\n" +
                            "Ветер: " + windSpeed + " м/с, " +
                            windDirection(obj.getJSONObject("wind").getInt("deg"))
                    );

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                preResult_info.setText("");
                Toast.makeText(MainActivity.this, R.string.parse_error_text, Toast.LENGTH_SHORT).show();
            }
        }

        private String windDirection (int degrees) {
            int roundedDegrees = Math.round(degrees/45)*45;
            if (roundedDegrees == 360)
                roundedDegrees -= 360;
            String direction = null;
            switch (roundedDegrees) {
                case 0:
                    direction = "С ⬇";
                    break;
                case 45:
                    direction = "С-В ↙";
                    break;
                case 90:
                    direction = "В ⬅";
                    break;
                case 135:
                    direction = "Ю-В ↖";
                    break;
                case 180:
                    direction = "Ю ⬆";
                    break;
                case 225:
                    direction = "Ю-З ↗";
                    break;
                case 270:
                    direction = "З ➡";
                    break;
                case 315:
                    direction = "С-З ↘";
                    break;

            }
            return direction;
        }
    }


    private boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        return false;
    }
}