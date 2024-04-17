package com.example.rey;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.rey.CustomerLoginActivity;
import com.example.rey.DriverLoginActivity;

import java.sql.Driver;

public class MainActivity extends AppCompatActivity {

    private Button mDriver;
    private Button mCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mDriver = (Button)  findViewById(R.id.driver);
            mCustomer= (Button) findViewById(R.id.customer);

        }catch (NullPointerException exception){
            exception.printStackTrace();
        }


        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {  Intent intent = new Intent (MainActivity.this, DriverLoginActivity.class);

                startActivity(intent);
                finish();

                return;

            }
        });

        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();

                return;

            }
        });
    }
}