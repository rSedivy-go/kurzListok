package com.example.kurzlistok;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class EditValueActivity extends AppCompatActivity {
    String mena;
    Double hodnota;
    TextView twMena;
    EditText etHodnota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_value);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        twMena = (TextView) findViewById(R.id.twMena);
        etHodnota = (EditText)  findViewById(R.id.etHodnota);
        Intent mIntent = getIntent();
        mena = mIntent.getStringExtra("mena");
        twMena.setText(mena);
        try {
            hodnota = Double.valueOf(mIntent.getStringExtra("hodnota"));
        } catch (Exception e) {
            hodnota = 0.0;
        }
        etHodnota.setText(hodnota.toString());
        Button button = (Button) findViewById(R.id.buttonNastaHodnotu);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    hodnota = Double.valueOf(etHodnota.getText().toString());
                    Toast.makeText(getApplicationContext(), "Uložené", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Chyba v hodnote", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void returnCall(){
        Intent intent = new Intent();
        intent.putExtra("mena", mena);
        intent.putExtra("hodnota", hodnota.toString());
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
