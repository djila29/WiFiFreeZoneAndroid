package com.example.wififreezone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.expample.model.Network;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

public class MainActivity extends Activity {
	
	ListView networksListView = null;
	Button refreshButton = null;
	
	ArrayAdapter<String> netoworkAdapter = null;
	View lastSelectedItem = null;
	
	List<Network> networksList = new ArrayList<Network>();
	List<String> networksStringList = new ArrayList<String>();
	
	WifiManager mainWifiObj = null;
	
	final Activity mainAct = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		networksListView = (ListView) findViewById(R.id.listView1);
		refreshButton = (Button) findViewById(R.id.refreshButton);
		
		mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		networksListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	for(int i = 0; i<networksListView.getCount(); i++) {
        			networksListView.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
        		}
            	
            	lastSelectedItem = view;
            	view.setBackgroundColor(Color.CYAN);
            }
        });
		
		
		
		final Runnable getAllNetworks = new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet hget = new HttpGet("http://192.168.1.123:8080/FreeZoneServices/api/networks/getAllNetworks");
					hget.setHeader("Content-Type", "application/json");
					hget.setHeader("Accept", "application/json");
					
					final HttpResponse response = client.execute(hget);
					
					if(response.getStatusLine().getStatusCode() == 200) {
						HttpEntity hentity = response.getEntity();
						
						if (hentity!=null) {
							
							String responseString = EntityUtils.toString(hentity);
							JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
							
							JsonArray jsonArray = jsonObject.getAsJsonArray("network");
							Gson gson = new GsonBuilder().create();
							Network[] networks = gson.fromJson(jsonArray, Network[].class);
							networksList = Arrays.asList(networks);
						}
					}
					else {
						mainAct.runOnUiThread(new Runnable() {
						    public void run() {
						        Toast.makeText(mainAct, "Error with getting networks. Status code: " + response.getStatusLine().getStatusCode(), Toast.LENGTH_SHORT).show();
						        
						    }
						});
					}
				} catch (final Exception e) {
					mainAct.runOnUiThread(new Runnable() {
					    public void run() {
					        Toast.makeText(mainAct, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					       
					    }
					});
					e.printStackTrace();
				}
			}
		};
		
		final Thread tNetworks = new Thread(getAllNetworks);
		tNetworks.start();
		
		refreshButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Thread tNetworks = new Thread(getAllNetworks);
				tNetworks.start();
				
				for(;;) {
					if(!tNetworks.isAlive()) {
						networksStringList.clear();
						for(int i = 0; i<networksList.size(); i++) {
							networksStringList.add(networksList.get(i).getSSID() + "\n" + networksList.get(i).getPassword());
						}
						
						final List<ScanResult> wifiavailable = mainWifiObj.getScanResults();
						
						for(int i = 0; i<wifiavailable.size(); i++) {
							networksStringList.add(wifiavailable.get(i).SSID + "\n" + wifiavailable.get(i).capabilities);
						}
						
						netoworkAdapter = new ArrayAdapter<String>(mainAct, R.layout.listview_item, networksStringList);
						
						networksListView.setAdapter(netoworkAdapter);
						break;
					}
				}
			}
		});
		
		for(;;) {
			if(!tNetworks.isAlive()) {
				networksStringList.clear();
				for(int i = 0; i<networksList.size(); i++) {
					networksStringList.add(networksList.get(i).getSSID() + "\n" + networksList.get(i).getPassword());
				}
				
				final List<ScanResult> wifiavailable = mainWifiObj.getScanResults();
				
				for(int i = 0; i<wifiavailable.size(); i++) {
					networksStringList.add(wifiavailable.get(i).SSID + "\n" + wifiavailable.get(i).capabilities);
				}
				
				netoworkAdapter = new ArrayAdapter<String>(mainAct, R.layout.listview_item, networksStringList);
				
				networksListView.setAdapter(netoworkAdapter);
				
				
				break;
			}
		}
	}
}
