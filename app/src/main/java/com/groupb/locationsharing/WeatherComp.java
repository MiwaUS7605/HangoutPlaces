package com.groupb.locationsharing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WeatherComp extends AppCompatActivity {

    private TextView weatherStatus;
    private Button getWeatherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_weather);

        // Allow network operations to be performed on the main thread (for demo purposes only)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1", "Weather notification channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        weatherStatus = findViewById(R.id.weather_status);

        getWeatherButton = findViewById(R.id.get_weather_button);
        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = "New York"; // Replace with the desired location
                String apiKey = "310352215638c3395568cef64ac062e2"; // Replace with your OpenWeatherMap API key

                // Build the API URL with the location and API key
                String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + location + "&appid=" + apiKey;

                // Create an HTTP connection and request object
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    URL url = new URL(apiUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(10000 /* milliseconds */);
                    connection.setConnectTimeout(15000 /* milliseconds */);

                    // Connect to the API and get the response
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode != 200) {
                        throw new IOException("Response code: " + responseCode);
                    }
                    inputStream = connection.getInputStream();

                    // Parse the JSON response and extract the weather information
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String jsonData = stringBuilder.toString();
                    JSONObject json = new JSONObject(jsonData);
                    JSONArray weatherArray = json.getJSONArray("weather");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    final String weatherDescription = weatherObject.getString("description");
                    JSONObject mainObject = json.getJSONObject("main");
                    final double temperature = mainObject.getDouble("temp");

                    // Update the UI with the weather information
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Push notification
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(WeatherComp.this, "1")
                                    .setSmallIcon(R.drawable.ic_app)
                                    .setContentTitle("Weather")
                                    .setContentText("Weather: " + weatherDescription + "\nTemperature: " + temperature)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(WeatherComp.this);
                            if (ActivityCompat.checkSelfPermission(WeatherComp.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                ActivityCompat.requestPermissions(WeatherComp.this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
                                return;
                            }
                            managerCompat.notify(1, builder.build());

                            //Update weather information
                            weatherStatus.setText("Weather: " + weatherDescription + "\nTemperature: " + temperature);
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    // Close the input stream and disconnect the connection
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }
}
