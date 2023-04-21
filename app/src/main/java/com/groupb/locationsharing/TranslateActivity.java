package com.groupb.locationsharing;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.detectlanguage.DetectLanguage;
import com.detectlanguage.Result;
import com.detectlanguage.errors.APIError;
import com.google.firebase.database.tubesock.WebSocket;


public class TranslateActivity extends AppCompatActivity {
    ImageView translateButton, icon;
    TextView toText, languageFrom, languageTo;
    EditText fromText;
    String translated;
    Spinner spinnerTo;
    private Handler handler = new Handler();
    private Runnable translationRunnable;

    String apiKey = "25158ff602380332d1d456163bf31e52";

    public static boolean isVietnamese(String input) {
        // Danh sách các ký tự tiếng Việt trong bảng mã Unicode
        final String vietnameseCharacters = "àằèềìòồùừáắéếíóốúứýãẵẽễĩõỗũữỹảẳẻểỉỏổủửỷạặẹệịọộụựỵăĕĭŏŭơưôêđ";
        String upperCase = input.toLowerCase(Locale.ROOT);
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

    class LanguageItem {
        ArrayList<String> language;
        ArrayList<String> isoCode;

        LanguageItem() {
            language = new ArrayList<>();
            isoCode = new ArrayList<>();
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale locale : locales) {
                String[] iso2 = locale.getISOLanguages();
                String code = locale.getISO3Language();
                String name = locale.getDisplayLanguage();
                if (!"".equals(code) && !"".equals(name)) {
                    if (name.equals("world")) continue;
                    if (language.contains(name)) continue;
                    language.add(name);
                    isoCode.add(code.substring(0, 2).toLowerCase());
                }
            }
        }

        public ArrayList<String> getLanguage() {
            return language;
        }

        public void setLanguage(ArrayList<String> language) {
            this.language = language;
        }

        public ArrayList<String> getIsoCode() {
            return isoCode;
        }

        public void setIsoCode(ArrayList<String> isoCode) {
            this.isoCode = isoCode;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggtranslate);
        fromText = findViewById(R.id.fromText);
        toText = findViewById(R.id.toText);
        icon = findViewById(R.id.icon);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        DetectLanguage.apiKey = apiKey;

        final String[] from = {""};
        final String[] to = {""};

        LanguageItem languageItem = new LanguageItem();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languageItem.getLanguage());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTo = findViewById(R.id.languageToSpinner);
        spinnerTo.setAdapter(adapter);

        languageFrom = findViewById(R.id.languageFrom);
        languageTo = findViewById(R.id.languageTo);

        fromText.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable detectLanguageRunnable = new Runnable() {
                @Override
                public void run() {
                    List<Result> results = null;
                    try {
                        results = DetectLanguage.detect(fromText.getText().toString());
                    } catch (APIError e) {
                        e.printStackTrace();
                    }
                    
                    if (results != null && !results.isEmpty()) {
                        Result result = results.get(0);
                        from[0] = result.language;
                        String language = languageItem.getLanguage().get(languageItem.getIsoCode().indexOf(from[0]));
                        languageFrom.setText("Auto Detect: " + language);
                        Log.e(TAG, from[0]);
                    } else {
                        Log.e(TAG, "No language detected");
                    }
                }
            };

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                handler.removeCallbacks(detectLanguageRunnable);

                handler.postDelayed(detectLanguageRunnable, 500);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                spinnerTo.setSelection(position); // Update Spinner selection
                languageTo.setText(selectedLanguage);
                to[0] = languageItem.getIsoCode().get(position);
                Log.e(TAG, to[0]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when no item is selected
            }
        });

        fromText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(translationRunnable);

                // Define a new translation runnable
                translationRunnable = new Runnable() {
                    @Override
                    public void run() {
                        String input = fromText.getText().toString();
                        try {
                            translated = translate(from[0], to[0], input);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        toText.setText(translated);
                    }
                };

                // Post the translation runnable with a delay of 300 milliseconds
                handler.postDelayed(translationRunnable, 1000);
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
