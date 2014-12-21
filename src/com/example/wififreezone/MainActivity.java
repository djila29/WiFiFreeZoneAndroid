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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

public class MainActivity extends Activity {
	
	ListView networksListView = null;
	Button refreshButton = null;
	Button connectButton = null;
	
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
		connectButton = (Button) findViewById(R.id.connectButton);
		
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
					HttpGet hget = new HttpGet("http://192.168.1.100:8080/FreeZoneServices/api/networks/getAllNetworks");
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
		
		connectButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder alert = new AlertDialog.Builder(mainAct);				

				// Set an EditText view to get user input 
				final EditText input = new EditText(mainAct);
				alert.setView(input);
				

            	TextView textview1 = (TextView) lastSelectedItem.findViewById(R.id.rowTextView);
            	String [] networkDetails = textview1.getText().toString().split("\n");
				final String SSID = networkDetails[0];
				
				alert.setTitle("Connect to " + SSID);
				alert.setMessage("Enter password:");
					
				alert.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String password = input.getText().toString();
				 	// Do something with value!
				  	WifiConfiguration tmpConf = new WifiConfiguration();
					tmpConf.SSID = "\"".concat(SSID).concat("\"");
					tmpConf.status = WifiConfiguration.Status.DISABLED;
					tmpConf.priority = 40;
					tmpConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
					tmpConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
					tmpConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
					tmpConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
					tmpConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
					tmpConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
					tmpConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
					tmpConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
					tmpConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
					 
					tmpConf.preSharedKey = "\"".concat(password).concat("\"");
					
					int netID = mainWifiObj.addNetwork(tmpConf);
					if (netID != -1) {
						 // success, can call wfMgr.enableNetwork(networkId, true) to connect
						mainWifiObj.enableNetwork(netID, true);
					} else {
						mainAct.runOnUiThread(new Runnable() {
						    public void run() {
						        Toast.makeText(mainAct, "Error while connecting to network", Toast.LENGTH_SHORT).show();
						        
						    }
						});

					}
				  }
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});

				alert.show();
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
