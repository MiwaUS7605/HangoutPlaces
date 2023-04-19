package com.groupb.locationsharing;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;


public class TranslateActivity extends AppCompatActivity {
    ImageView translateButton, icon;
    TextView toText;
    EditText fromText;
    String translated;

    public static boolean isVietnamese(String input) {
        // Danh sách các ký tự tiếng Việt trong bảng mã Unicode
        final String vietnameseCharacters = "ĂẮẰẤẾẶẲẨÊẾỀỂỆƠÓỐỒỐỚỢỞỜỤỨỪỰỬÍỐỚỜỢỞÚỨỪỰỬÝĐ";
        String upperCase = input.toUpperCase(Locale.ROOT);
        for (char c : upperCase.toCharArray()) {
            // Kiểm tra xem ký tự c có nằm trong danh sách ký tự tiếng Việt không
            if (vietnameseCharacters.contains(Character.toString(c))) {
                return true;
            }
        }
        return false;
    }

    public static String translate(String langFrom, String langTo, String text) throws IOException {
        // INSERT YOU URL HERE
        String urlStr = "https://script.google.com/macros/s/AKfycbxM5RTr1vtx3e5HvzsWjPIhR9M46ok16FG0V6mjmajg2oPSvZ0duHq2mEXuB1fXnPiIoQ/exec" +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        String responseString = "";
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlStr);
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
            responseString = stringBuilder.toString();
        } catch (IOException e) {
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
        return responseString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggtranslate);
        translateButton = findViewById(R.id.transButton);
        fromText = findViewById(R.id.fromText);
        toText = findViewById(R.id.toText);
        icon = findViewById(R.id.icon);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = fromText.getText().toString();
                try {
                    if (isVietnamese(input)) {
                        translated = translate("vi", "en", input);
                    } else {
                        translated = translate("en", "vi", input);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                toText.setText(translated);
            }
        });
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
