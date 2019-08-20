package com.debug.barcodedetect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button scanButton;
    private int BARCODE_READER_REQUEST_CODE = 1;
    private SharedPreferences prefs;

    private float x1,x2,y1,y2;
    static final int MIN_DISTANCE = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getApplicationContext().getSharedPreferences("barcodedetect", MODE_PRIVATE);

        scanButton = (Button) findViewById(R.id.scan_barcode_button);

        scanButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivityForResult(
                                new Intent(MainActivity.this, BarcodeCaptureActivity.class),
                                BARCODE_READER_REQUEST_CODE
                        );
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("optionsmenu","oncreate");
        getMenuInflater().inflate(R.menu.history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("optionsmenu","onselect");
        switch (item.getItemId()) {
            case R.id.history:
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                MainActivity.this.startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                float deltaX = x2 - x1;
                float deltaY = y2 - y1;
                if (Math.abs(deltaX) > MIN_DISTANCE){
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    return true;
                }
                if (deltaY > MIN_DISTANCE){
                    prefs.edit().putBoolean("frontcam", true).apply();
                    startActivityForResult(
                            new Intent(MainActivity.this, BarcodeCaptureActivity.class),
                            BARCODE_READER_REQUEST_CODE
                    );
                    return true;
                }
                if (deltaY < -MIN_DISTANCE){
                    prefs.edit().putBoolean("frontcam", false).apply();
                    startActivityForResult(
                            new Intent(MainActivity.this, BarcodeCaptureActivity.class),
                            BARCODE_READER_REQUEST_CODE
                    );
                    return true;
                }

                break;
        }
        return super.onTouchEvent(event);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String barcode_result = data.getExtras().getString(BarcodeCaptureActivity.BarcodeObject);

                    Toast toast = Toast.makeText(getApplicationContext(), barcode_result, Toast.LENGTH_SHORT);
                    toast.show();
                    addCodeToHistory(barcode_result);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.no_barcode_captured),
                            Toast.LENGTH_SHORT);
                    toast.show();
                    Log.i(TAG, getString(R.string.no_barcode_captured));
                }
            } else
                Log.e(TAG, String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void addCodeToHistory(String s){
        String json = prefs.getString("history", null);
        JSONArray allOrders;
        try {
            JSONObject o = new JSONObject();
            o.put("code",s);
            if (json != null) {
                allOrders = new JSONArray(json);
            } else {
                allOrders = new JSONArray();
            }
            allOrders = allOrders.put(o);
            prefs.edit().putString("history", allOrders.toString()).apply();
            Log.d(TAG, allOrders.toString());
            Log.i(TAG, "registered "+s);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }


}
