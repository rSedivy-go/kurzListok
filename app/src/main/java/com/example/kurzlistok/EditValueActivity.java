package com.example.kurzlistok;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditValueActivity extends AppCompatActivity {
    String currency;
    Double value;
    TextView tvCurrency;
    EditText etValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_value);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        tvCurrency = (TextView) findViewById(R.id.tvCurrency);
        etValue = (EditText)  findViewById(R.id.etValue);
        Intent mIntent = getIntent();
        currency = mIntent.getStringExtra("mena");
        tvCurrency.setText(currency);
        try {
            value = Double.valueOf(mIntent.getStringExtra("hodnota"));
        } catch (Exception e) {
            value = 0.0;
        }
        etValue.setText(value.toString());
        ((Button) findViewById(R.id.bSetValue)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    value = Double.valueOf(etValue.getText().toString());
                    Toast.makeText(getApplicationContext(), "Uložené", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Chyba v hodnote", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void returnCall(){
        Intent intent = new Intent();
        intent.putExtra("mena", currency);
        intent.putExtra("hodnota", value.toString());
        setResult(666, intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        returnCall();
    }
    @Override
    public boolean onSupportNavigateUp() {
        returnCall();
        return true;
    }
}
