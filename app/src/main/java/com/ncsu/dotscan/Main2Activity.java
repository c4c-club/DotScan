package com.ncsu.dotscan;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

public class Main2Activity extends AppCompatActivity {

    private int count = 0;
    private TextView mTvChambers;
    private EditText mEtVolume;
    private EditText mEtTotal;
    private TextView mTvDna;
    private Button mBtnAnalyze;

    private String volume;
    private String total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Retrieve number of dots scanned from previous activity
        count = getIntent().getExtras().getInt("count");

        // Set Positive Chambers TextView, displaying number of positive chambers
        mTvChambers = findViewById(R.id.tv_positive_chambers);
        mTvChambers.setText("Positive Chambers: " + count);

        // Set up the Analyze Button
        mBtnAnalyze = findViewById(R.id.btn_result);
        mBtnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get volume
                mEtVolume = findViewById(R.id.et_volume);
                volume = mEtVolume.getText().toString();
                //Get total number of Chambers
                mEtTotal = findViewById(R.id.et_total);
                total = mEtTotal.getText().toString();

                mTvDna = findViewById(R.id.tv_dna);

                if(volume.equals("")||total.equals("")){
                    mTvDna.setText("Error: Insufficient Input");
                    mTvDna.setTextColor(Color.RED);
                }else{
                    mTvDna.setTextColor(Color.BLACK);
                    // Calculation
                    // Format the result to have 3 decimal digits
                    DecimalFormat formatval = new DecimalFormat("##.###");

                    // Get values entered by the user
                    double volume2 = Double.valueOf(volume);
                    double Total_Number_of_Chamber = Double.valueOf(total);

                    // Perform calculation and print result
                    double c_parameter = (-2000000)/volume2;
                    //double natural_log = Math.log(1-(pos_chamber/Total_Number_of_Chamber));
                    double natural_log = Math.log(1-(3802/Total_Number_of_Chamber));
                    mTvDna.setText(formatval.format((c_parameter * natural_log))+" copies/μL");
                }
            }
        });

    }
}