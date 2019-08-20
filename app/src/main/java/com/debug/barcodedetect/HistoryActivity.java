package com.debug.barcodedetect;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private SharedPreferences prefs;
    private String history;
    private List<HashMap<String, String>> dataList;
    private ListAdapter myAdapter;
    private ListView myListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        prefs = getApplicationContext().getSharedPreferences("barcodedetect", MODE_PRIVATE);
        history = prefs.getString("history",null);
        myListView = (ListView) findViewById(R.id.history_list);
        myListView.setEmptyView(findViewById(R.id.list_empty));

        populateHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("optionsmenu","oncreate");
        getMenuInflater().inflate(R.menu.history_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("optionsmenu","onselect");
        switch (item.getItemId()) {
            case R.id.history_clear:
                clearOrdersFromPref();
                break;
        }
        return true;
    }

    private void populateHistory(){
        String[] codes = getOrdersFromPref();
        final List<HashMap<String, String>> dataList;
        dataList = new ArrayList<>();
        for (int i = 0; i < codes.length; i++) {
            HashMap<String, String> element = new HashMap<>();
            element.put("text1", codes[i]);
//            element.put("text2", "");
            dataList.add(element);
        }
        myAdapter = new SimpleAdapter(HistoryActivity.this, dataList, android.R.layout.simple_list_item_1, new String[]{"text1"}, new int[]{android.R.id.text1});
        myListView.setAdapter(myAdapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // if url, open in browser

                String potentialUrl = dataList.get(i).get("text1");
                if (Patterns.WEB_URL.matcher(potentialUrl).matches()){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(potentialUrl));
                    startActivity(intent);
                }
            }
        });
        myListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // copy to clipboard

                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(HistoryActivity.CLIPBOARD_SERVICE);
                ClipData clipData;

                String txtcopy = dataList.get(i).get("text1");
                clipData = ClipData.newPlainText("text",txtcopy);
                clipboardManager.setPrimaryClip(clipData);

                return true;
            }
        });
    }

    private void clearOrdersFromPref(){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("barcodedetect", MODE_PRIVATE);
        prefs.edit().putString("history", null).apply();
        myAdapter = new SimpleAdapter(HistoryActivity.this, new ArrayList<HashMap<String, String>>(), android.R.layout.simple_list_item_1, new String[]{"text1"}, new int[]{android.R.id.text1});
        myListView.setAdapter(myAdapter);
    }

    private String[] getOrdersFromPref() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("barcodedetect", MODE_PRIVATE);
        String json = prefs.getString("history", null);
        String[] urls = new String[0];
        if (json != null) {
            Log.d(TAG,json);
            try {
                JSONArray allOrders = new JSONArray(json);
                urls = new String[allOrders.length()];
                for (int i = 0; i < allOrders.length(); i++) {
                    JSONObject oneOrder = allOrders.getJSONObject(i);
                    String code = oneOrder.getString("code");
                    Log.i(TAG, code);
                    urls[i] = code;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

}
