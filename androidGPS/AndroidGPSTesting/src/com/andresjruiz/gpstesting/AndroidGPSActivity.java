package com.andresjruiz.gpstesting;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
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
	
	private LocationManager location;
	private LocationListener locationListener;
	private GeomagneticField compass;
	
	private ProgressDialog dialog;
	
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
        
        //Set the intial waiting time, before getting a fix
        setWaiting();
        
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
		
                
        locationListener = new LocationListener(){
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
				setWaiting(); // Wait till we get a fix
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				//DO NOTHING
			}
        	
        };
        
        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    
    protected void onPause(){
    	//Stop application from using the GPS for updates while on
    	//the background
    	location.removeUpdates(locationListener);
    	super.onPause();
    }
    
    protected void onResume(){
    	//Restart the GPS
    	location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    	//Force into a waiting mode
    	setWaiting();
    	super.onResume();
    }
    
    protected void onStop(){
    	//Kill the GPS
    	location.removeUpdates(locationListener);
    	super.onStop();
    }
    
    /**
     * Sets the text for the location found by the GPS/Location
     * listeners. This function takes care of rounding the numbers
     * and scaling them down to reasonable numbers, not numbers 10+ digits.
     * 
     * Also handles the north/south, east/west appended to the text.
     * 
     * @param lat - Latitude
     * @param lon - Longitude
     * @param head - The Heading
     * @param sats - The Number of satelites active
     */
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
    	this.log.setText(tenDig.format(lon) + EorW);
    	this.head.setText(Float.toString(head));
    	this.sats.setText(Integer.toString(sats));
    	
    	//Hide the waiting dialog
    	if(dialog.isShowing()){
    		dialog.dismiss();
    	}
    }
    
    /**
     * Called when the GPS is found disabled.
     * This also kills the waiting dialog if its
     * still active.
     */
    public void setDisabledText(){
    	this.lat.setText("GPS DISABLED");
    	this.log.setText("GPS DISABLED");
    	this.head.setText("GPS DISABLED");
    	this.sats.setText("0");
    	//Hide the waiting dialog
    	if(dialog.isShowing()){
    		dialog.dismiss();
    	}
    }
    
    /**
     * Sets the application in a waiting state. This is called
     * when the application is waiting on a GPS Fix and
     * when the application is started/resumed to allow
     * for the GPS to get a fix.
     */
    public void setWaiting(){
    	
    	this.showDialog(0);
    	
    	this.lat.setText("");
    	this.log.setText("");
    	this.head.setText("");
    	this.sats.setText("0");
    }
    
    
    protected Dialog onCreateDialog(int id){
    	//Instantiate the dialog
    	dialog = new ProgressDialog(this);
		
    	//Setup the properties
		dialog.setTitle("Seaching");
		dialog.setMessage("Searching for a satelite fix...");
		//Make the progress bar a circle turning indefinately
		dialog.setIndeterminate(true);
		return dialog;
    }
}