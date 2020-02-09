package com.example.kurzlistok;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity  {
    private HashMap<String, Double> aktualListok = new HashMap<String, Double>();
    private HashMap<String, Double> zobrazListok = new HashMap<String, Double>();
    SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd");
    String aktualVyber = "null";
    Date aktualDatum;
    Date zobrazDatum;
    DatePickerDialog datePickerDialog;
    SharedPreferences sharedPreferences;
    private RequestQueue mQueue;
    DatePickerDialog picker;
    EditText eText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        zobrazListok.put("CZK", 1.0);
        setDropdownMenu(zobrazListok);
        mQueue = Volley.newRequestQueue(this);
        jsonParse("2010-01-12");

        eText=(EditText) findViewById(R.id.editText1);
        eText.setInputType(InputType.TYPE_NULL);
        eText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                picker = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String datum = year + "-" + (monthOfYear) + "-" + dayOfMonth;
                                eText.setText(datum);
                                jsonParse(datum);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

    }
    private void jsonParse(String url) {
        //2010-01-13
        //latest
           url= "https://api.exchangeratesapi.io/"+url+"?base=CZK";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject rates = response.getJSONObject("rates");
                    Iterator keysToCopyIterator = rates.keys();
                    while(keysToCopyIterator.hasNext()) {
                        String key = (String) keysToCopyIterator.next();
                        aktualListok.put(key, Double.valueOf(rates.getString(key)));
                    }

                    try {
                        aktualDatum = dateFormat.parse(response.getString("date"));

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    setDropdownMenu(aktualListok);
                    TextView tw = findViewById(R.id.workingDay);
                    tw.setText(dateFormat.format(aktualDatum));

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error at games feed", Toast.LENGTH_SHORT).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error at games feed", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }


    public void setDropdownMenu(HashMap<String, Double> source){
        ArrayList<String> spinnerArray = new ArrayList<String>(source.keySet());
        Spinner dropdown = findViewById(R.id.spinner1);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,
                        spinnerArray);

        dropdown.setAdapter(spinnerArrayAdapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                try {
                    Toast.makeText(getApplicationContext(), (String) parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                    Double suma = aktualListok.get((String) parent.getItemAtPosition(position));
                    String sb = suma.toString() +" " + (String) parent.getItemAtPosition(position);
                    ((TextView)findViewById(R.id.hodnota)).setText(sb);
                }catch (Exception e){

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
