package com.andresjruiz.gpstesting;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.hardware.GeomagneticField;
import android.location.*;
import android.util.*;

public class AndroidGPSActivity extends Activity {
    /** Called when the activity is first created. */
	private TextView lat;
	private TextView log;
	private TextView head; 
	private TextView sats;
	
	LocationManager location;
	GeomagneticField compass;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        lat = (TextView) findViewById(R.id.lat);
        log = (TextView) findViewById(R.id.log);
        head = (TextView) findViewById(R.id.head);
        sats = (TextView) findViewById(R.id.sats);
        
        //Get the location information
        location  = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        setWaiting();//Setup the text for showing the values
        
        //The status of the gps, get a new instance from location
        final GpsStatus gps = location.getGpsStatus(null);
        
        //GPS Listener so that gps actually gets feed information
        //just a small annoyance
        GpsStatus.Listener GpsStatus = new GpsStatus.Listener() {
			
			@Override
			public void onGpsStatusChanged(int event) {
				//DO NOTHING
			}
		};
		
		//Hook up a listener so that we can actually have information on gps
		location.addGpsStatusListener(GpsStatus);
		
                
        LocationListener locationListener = new LocationListener(){
			@Override
			public void onLocationChanged(Location newLocation) {
				//Get all the values for the information
				double lat = newLocation.getLatitude();
				double log = newLocation.getLongitude();
				float head = newLocation.getBearing();
				Iterable<GpsSatellite> satelites = location.getGpsStatus(gps).getSatellites();
				int sats = 0;
				int totalSats = 0;//Keep track of total num of satelites for logging
				
				
				for(GpsSatellite i : satelites){
					totalSats++;
					if(i.usedInFix()){
						sats++;
					}
				}
				
				//Debugging information
				Log.w("androidGPS", "Sats: " + Integer.toString(sats) +  " Total Sats: " + Integer.toString(totalSats));

				//Set the text information
				setLocation(lat, log, head, sats);
				
				compass = new GeomagneticField(
				         Double.valueOf(newLocation.getLatitude()).floatValue(),
				         Double.valueOf(newLocation.getLongitude()).floatValue(),
				         Double.valueOf(newLocation.getAltitude()).floatValue(),
				         System.currentTimeMillis()
				      );

			}

			@Override
			public void onProviderDisabled(String provider) {
				setDisabledText();//Set the text when the GPS is disabled
			}

			@Override
			public void onProviderEnabled(String provider) {
				setWaiting(); //Set a nice waiting text on the fields
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				//DO NOTHING
			}
        	
        };
        
        location.requestLocationUpdates(location.GPS_PROVIDER, 0, 0, locationListener);
    }
    
    public void setLocation(double lat, double lon, float head, int sats){
    	//Do Some Formatting for the numbers
    	DecimalFormat tenDig = new DecimalFormat("##.######");
    	
    	//Character that will hold if the position of N or S
    	char NorS;
    	
    	if(lat > 0){
    		NorS = 'N'; //We are north of the equator
    	} else{
    		lat = lat * -1;
    		NorS = 'S';
    	}
    	
    	//Character that will hold if the position if E or W
    	char EorW;
    	
    	if(lon > 0){
    		EorW = 'E';
    	} else{
    		lon = lon * -1;
    		EorW = 'W';
    	}
    	
    	this.lat.setText(tenDig.format(lat) + NorS);
    	this.log.setText(tenDig.format(lon));
    	this.head.setText(Float.toString(head));
    	this.sats.setText(Integer.toString(sats));
    }
    
    public void setDisabledText(){
    	this.lat.setText("GPS DISABLED");
    	this.log.setText("GPS DISABLED");
    	this.head.setText("GPS DISABLED");
    	this.sats.setText("0");
    }
    
    public void setWaiting(){
    	this.lat.setText("Searching...");
    	this.log.setText("Searching...");
    	this.head.setText("Searching...");
    	this.sats.setText("0");
    }
}