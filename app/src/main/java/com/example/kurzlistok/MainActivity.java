package com.example.kurzlistok;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private HashMap<String, Double> aktualSavedListok = new HashMap<String, Double>();
    private HashMap<String, Double> zobrazListok = new HashMap<String, Double>();
    SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd");
    TextView textViewVysledok;
    TextView textViewMenaZ;
    String zvolenaMena = "CZK";
    String aktualMenaZ = "CZK";
    String aktualMenaDo = "CZK";
    Date aktualDatum;
    Date zobrazDatum;
    SharedPreferences sharedPreferences;
    private RequestQueue mQueue;
    DatePickerDialog picker;
    EditText eText;
    Context context;
    boolean doCZK = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        zobrazListok.put("CZK", 1.0);
        try {
            aktualSavedListok = loadMap();

        } catch (Exception e) {
            aktualSavedListok = zobrazListok;
        }
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String datum = sharedPreferences.getString("datum", "null");
            zobrazDatum = aktualDatum = dateFormat.parse(datum);
            ((TextView) findViewById(R.id.workingDay)).setText(dateFormat.format(zobrazDatum));
        } catch (Exception e) {
        }


        setDropdownMenu(zobrazListok);
        mQueue = Volley.newRequestQueue(this);
        InputStream in;

        context = getApplicationContext();
        eText = (EditText) findViewById(R.id.editText1);
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
                                String datum = year + "-" +
                                        (monthOfYear + 1 < 10 ?
                                                String.format(Locale.getDefault(), "%02d", monthOfYear + 1) :
                                                monthOfYear + 1)
                                        + "-" +
                                        (dayOfMonth < 10 ?
                                                String.format(Locale.getDefault(), "%02d", dayOfMonth) :
                                                dayOfMonth);
                                eText.setText(datum);
                                jsonParse(datum);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jsonParse("latest");
                eText.setText("");
            }
        });
        EditText field1 = (EditText) findViewById(R.id.editTextHodnota);
        textViewVysledok = (TextView) findViewById(R.id.vysledok);
        textViewMenaZ = (TextView) findViewById(R.id.menaZ);
        field1.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0)
                    try {
                        calc(Double.parseDouble(s.toString()));
                    } catch (Exception e) {
                        textViewVysledok.setText("Vyskytla sa chyba");
                    }
            }
        });

        jsonParse("latest");
    }

    @Override
    protected void onStop() {
        mQueue.getCache().clear();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("datum", dateFormat.format(aktualDatum));
        editor.commit();
        saveMap(aktualSavedListok);
        super.onStop();
    }

    private void calc() {
        String b = ((EditText) findViewById(R.id.editTextHodnota)).getText().toString();
        calc(Double.parseDouble(b));
    }

    private void calc(Double s) {
        try {
            DecimalFormat df2 = new DecimalFormat("#.##");
            String hodnota;
            if (doCZK) {
                hodnota = String.valueOf(df2.format(s / zobrazListok.get(zvolenaMena)));
                hodnota = hodnota + " " + aktualMenaDo;
            } else {
                hodnota = String.valueOf(df2.format(s * zobrazListok.get(zvolenaMena)));
                hodnota = hodnota + " " + aktualMenaDo;
            }
            textViewMenaZ.setText(aktualMenaZ);
            textViewVysledok.setText(hodnota);

        } catch (Exception e) {
            textViewVysledok.setText("Error");
        }

    }

    private void jsonParse(String url) {
        //2010-01-13
        //latest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://api.exchangeratesapi.io/" + url + "?base=CZK",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject rates = response.getJSONObject("rates");
                    Iterator keysToCopyIterator = rates.keys();
                    zobrazListok.clear();
                    while (keysToCopyIterator.hasNext()) {
                        String key = (String) keysToCopyIterator.next();
                        zobrazListok.put(key, Double.valueOf(rates.getString(key)));
                    }
                    try {
                        zobrazDatum = dateFormat.parse(response.getString("date"));
                        if (aktualDatum == null || aktualDatum.before(zobrazDatum)) {
                            aktualDatum = zobrazDatum;
                            aktualSavedListok = zobrazListok;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    setDropdownMenu(zobrazListok);
                    zvolenaMena = zobrazListok.keySet().toArray()[0].toString();
                    TextView tw = findViewById(R.id.workingDay);
                    tw.setText(dateFormat.format(zobrazDatum));
                    if (doCZK) {
                        aktualMenaDo = "CZK";
                        aktualMenaZ = zvolenaMena;
                    } else {
                        aktualMenaDo = zvolenaMena;
                        aktualMenaZ = "CZK";
                    }
                    calc();
                    Toast.makeText(getApplicationContext(), "loaded", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error at games feed", Toast.LENGTH_SHORT).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error at games feed", Toast.LENGTH_SHORT).show();
                zobrazListok = aktualSavedListok;
                setDropdownMenu(zobrazListok);
                zvolenaMena = zobrazListok.keySet().toArray()[0].toString();
                try {
                    ((TextView) findViewById(R.id.workingDay)).setText(dateFormat.format(zobrazDatum));
                } catch (Exception e) {
                }
                int i = 0;
                if (doCZK) {
                    aktualMenaDo = "CZK";
                    aktualMenaZ = zvolenaMena;
                } else {
                    aktualMenaDo = zvolenaMena;
                    aktualMenaZ = "CZK";
                }
                calc();
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.zCZK:
                if (checked) {
                    aktualMenaZ = "CZK";
                    textViewMenaZ.setText(aktualMenaZ);
                    aktualMenaDo = zvolenaMena;
                    ((RadioButton) findViewById(R.id.doCZK)).setChecked(false);
                    doCZK = false;
                    calc();
                }
                break;
            case R.id.doCZK:
                if (checked) {
                    aktualMenaZ = zvolenaMena;
                    textViewMenaZ.setText(aktualMenaZ);
                    aktualMenaDo = "CZK";
                    ((RadioButton) findViewById(R.id.zCZK)).setChecked(false);
                    doCZK = true;
                    calc();
                }

                break;
        }
    }

    public void setDropdownMenu(HashMap<String, Double> source) {
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
                    zvolenaMena = (String) parent.getItemAtPosition(position);
                    Double suma = zobrazListok.get(zvolenaMena);
                    String sb = suma.toString() + " " + zvolenaMena;
                    ((TextView) findViewById(R.id.hodnota)).setText(sb);
                    if (doCZK) {
                        aktualMenaDo = "CZK";
                        aktualMenaZ = zvolenaMena;
                    } else {
                        aktualMenaDo = zvolenaMena;
                        aktualMenaZ = "CZK";
                    }
                    calc();
                } catch (Exception e) {

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

    private void saveMap(Map<String, Double> inputMap) {
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("MyVariables", Context.MODE_PRIVATE);
        if (pSharedPref != null) {
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove("My_map").commit();
            editor.putString("My_map", jsonString);
            editor.commit();
        }
    }

    private HashMap<String, Double> loadMap() {
        HashMap<String, Double> outputMap = new HashMap<String, Double>();
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("MyVariables", Context.MODE_PRIVATE);
        try {
            if (pSharedPref != null) {
                String jsonString = pSharedPref.getString("My_map", (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    Double value = (Double) jsonObject.get(key);
                    outputMap.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputMap;
    }

}
