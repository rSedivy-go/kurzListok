package com.example.kurzlistok;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private HashMap<String, Double> savedExchangeRate = new HashMap<String, Double>();
    private HashMap<String, Double> showExchangeRate = new HashMap<String, Double>();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd");
    TextView tvResult;
    TextView tvCurrencyFrom;
    String selectedCurrency = "CZK";
    String actualCurrencyFrom = "CZK";
    String actualCurrencyTo = "CZK";
    Date dateActual;
    Date dateShowing;
    SharedPreferences sharedPreferences;
    private RequestQueue mQueue;
    DatePickerDialog picker;
    EditText etDate;
    Context context;
    boolean toCZK = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showExchangeRate.put("CZK", 1.0);
        try {
            savedExchangeRate = loadMap();
        } catch (Exception e) {
            savedExchangeRate = showExchangeRate;
        }
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String date = sharedPreferences.getString("datum", "null");
            dateShowing = dateActual = simpleDateFormat.parse(date);
            ((TextView) findViewById(R.id.tvWorkingDay)).setText(simpleDateFormat.format(dateShowing));
        } catch (Exception e) {
        }

        setDropdownMenu(showExchangeRate);
        mQueue = Volley.newRequestQueue(this);

        context = getApplicationContext();
        etDate = (EditText) findViewById(R.id.etDate);
        etDate.setInputType(InputType.TYPE_NULL);
        etDate.setOnClickListener(new View.OnClickListener() {
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
                                etDate.setText(datum);
                                apiHandle(datum);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiHandle("latest");
                etDate.setText("");
            }
        });

        ((ImageButton) findViewById(R.id.ibEdit)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditValueActivity.class);
                intent.putExtra("mena", selectedCurrency);
                intent.putExtra("hodnota", showExchangeRate.get(selectedCurrency).toString());
                startActivityForResult(intent, 666);
            }
        });

        tvResult = (TextView) findViewById(R.id.tvResult);
        tvCurrencyFrom = (TextView) findViewById(R.id.tvCurrencyFrom);
        ((EditText) findViewById(R.id.etInputValue)).addTextChangedListener(new TextWatcher() {

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
                        tvResult.setText("Vyskytla sa chyba");
                    }
            }
        });

        apiHandle("latest");
    }

    @Override
    protected void onStop() {
        mQueue.getCache().clear();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("datum", simpleDateFormat.format(dateActual));
        editor.commit();
        saveMap(savedExchangeRate);
        super.onStop();
    }

    private void calc() {
        String b = ((EditText) findViewById(R.id.etInputValue)).getText().toString();
        calc(Double.parseDouble(b));
    }

    private void calc(Double s) {
        try {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String value;
            if (toCZK) {
                value = String.valueOf(decimalFormat.format(s / showExchangeRate.get(selectedCurrency)));
            } else {
                value = String.valueOf(decimalFormat.format(s * showExchangeRate.get(selectedCurrency)));
            }
            value = value + " " + actualCurrencyTo;
            tvCurrencyFrom.setText(actualCurrencyFrom);
            tvResult.setText(value);
        } catch (Exception e) {
            tvResult.setText("Error");
        }
    }

    private void apiHandle(String url) {
        /*
        url in format -> latest or 2010-01-13
         */
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://api.exchangeratesapi.io/" + url + "?base=CZK",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject rates = response.getJSONObject("rates");
                    Iterator keysToCopyIterator = rates.keys();
                    showExchangeRate.clear();
                    while (keysToCopyIterator.hasNext()) {
                        String key = (String) keysToCopyIterator.next();
                        showExchangeRate.put(key, Double.valueOf(rates.getString(key)));
                    }
                    try {
                        dateShowing = simpleDateFormat.parse(response.getString("date"));
                        if (dateActual == null || dateActual.before(dateShowing)) {
                            dateActual = dateShowing;
                            savedExchangeRate = showExchangeRate;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    setDropdownMenu(showExchangeRate);
                    selectedCurrency = showExchangeRate.keySet().toArray()[0].toString();
                    ((TextView) findViewById(R.id.tvWorkingDay)).setText(simpleDateFormat.format(dateShowing));
                    if (toCZK) {
                        actualCurrencyTo = "CZK";
                        actualCurrencyFrom = selectedCurrency;
                    } else {
                        actualCurrencyTo = selectedCurrency;
                        actualCurrencyFrom = "CZK";
                    }
                    calc();
                    Toast.makeText(getApplicationContext(), "Kurzové lístky prevzaté", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Chyba pri preberaní lístkov", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Chyba pri preberaní lístkov", Toast.LENGTH_SHORT).show();
                showExchangeRate = savedExchangeRate;
                setDropdownMenu(showExchangeRate);
                selectedCurrency = showExchangeRate.keySet().toArray()[0].toString();
                try {
                    ((TextView) findViewById(R.id.tvWorkingDay)).setText(simpleDateFormat.format(dateShowing));
                } catch (Exception e) {
                }
                if (toCZK) {
                    actualCurrencyTo = "CZK";
                    actualCurrencyFrom = selectedCurrency;
                } else {
                    actualCurrencyTo = selectedCurrency;
                    actualCurrencyFrom = "CZK";
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
            case R.id.rbFromCZK:
                if (checked) {
                    actualCurrencyFrom = "CZK";
                    tvCurrencyFrom.setText(actualCurrencyFrom);
                    actualCurrencyTo = selectedCurrency;
                    ((RadioButton) findViewById(R.id.rbToCZK)).setChecked(false);
                    toCZK = false;
                    calc();
                }
                break;
            case R.id.rbToCZK:
                if (checked) {
                    actualCurrencyFrom = selectedCurrency;
                    tvCurrencyFrom.setText(actualCurrencyFrom);
                    actualCurrencyTo = "CZK";
                    ((RadioButton) findViewById(R.id.rbFromCZK)).setChecked(false);
                    toCZK = true;
                    calc();
                }

                break;
        }
    }

    public void setDropdownMenu(HashMap<String, Double> source) {
        ArrayList<String> spinnerArray = new ArrayList<String>(source.keySet());
        Spinner spinnerDropdown = findViewById(R.id.spinner1);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        spinnerArray);

        spinnerDropdown.setAdapter(spinnerArrayAdapter);
        spinnerDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                try {
                    selectedCurrency = (String) parent.getItemAtPosition(position);
                    Double value = showExchangeRate.get(selectedCurrency);
                    String sb = value.toString() + " " + selectedCurrency;
                    ((TextView) findViewById(R.id.tvValue)).setText(sb);
                    if (toCZK) {
                        actualCurrencyTo = "CZK";
                        actualCurrencyFrom = selectedCurrency;
                    } else {
                        actualCurrencyTo = selectedCurrency;
                        actualCurrencyFrom = "CZK";
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == 666) {
            try {
                Double orig = showExchangeRate.get(data.getStringExtra("mena"));
                Double navrat = Double.valueOf(data.getStringExtra("hodnota"));
                Double zmena = navrat - orig;
                showExchangeRate.put(data.getStringExtra("mena"), showExchangeRate.get(data.getStringExtra("mena")) + zmena);

                String sb = showExchangeRate.get(data.getStringExtra("mena")).toString() + " " + data.getStringExtra("mena");
                ((TextView) findViewById(R.id.tvValue)).setText(sb);
                calc();
                int i = 0;
            } catch (Exception e) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            startActivityForResult(new Intent(getApplicationContext(), AboutActivity.class), 1);
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
