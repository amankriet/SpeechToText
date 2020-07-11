package com.naruto.speechtotext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mVoiceInputTv, tvFromPlane, tvToPlane, tvDepartureDate, tvReturnDate;
    private ImageView mSpeakBtn;

    private final String REQUEST_URL = "https://leova-api22.appspot.com/endpoint";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVoiceInputTv = findViewById(R.id.tv_voice_response);
        mSpeakBtn = findViewById(R.id.iv_mic);
        tvFromPlane = findViewById(R.id.tv_from_plane);
        tvToPlane = findViewById(R.id.tv_to_plane);
        tvDepartureDate = findViewById(R.id.tv_departure_date);
        tvReturnDate = findViewById(R.id.tv_return_date);


        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, a.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result != null) {
                        mVoiceInputTv.setText(result.get(0));

                        getEndpointResponse(result.get(0));
                    } else {
                        Toast.makeText(this, "NullPointerException on voice receive", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }

        }
    }

    private void getEndpointResponse(String s) {

        HashMap<String, String> params = new HashMap<>();
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("tzo", -330);
            requestJson.put("q", s);
            requestJson.put("channel", "channel");
            requestJson.put("api_key", "free-api-trial");
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        params.put("tzo", "330");
//        params.put("q", s);
//        params.put("channel", "channel");
//        params.put("api_key", "free-api-trial");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, REQUEST_URL, requestJson, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject trips = response.getJSONArray("trips").getJSONObject(0);

                            tvDepartureDate.setText(trips.getString("date1"));
                            tvFromPlane.setText(trips.getString("from_city_name"));
                            tvToPlane.setText(trips.getString("to_city_name"));
                            tvReturnDate.setText(trips.getString("date2"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();

                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

// Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}
