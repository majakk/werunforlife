package com.android.wr41;


import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class wr41 extends Activity implements LocationListener{
    /* this class implements LocationListener, which listens for both
	 * changes in the location of the device and changes in the status
	 * of the GPS system.
	 * */

	static final String tag = "Main"; // for Log
	int debug = 1; // 0 = sharp version, 1 = half debug, 2 = full or terminal debug mode
	
	SharedPreferences app_preferences;
	SharedPreferences prefsPrivate;
	SharedPreferences.Editor editor;
	SharedPreferences.Editor prefsEditor;
	int programCounter;
	
	TextView txtQuest;
	TextView txtInfo;
	TextView txtInfo2;
	
	TextView txtStatus1;
	TextView txtStatus2;
	TextView txtStatus3;
	TextView txtStatus4;
	TextView txtStatus5;
	ImageView imgStatus4;
	
	ImageView imgQuest;
	
	LocationManager lm;
	StringBuilder sb;
	StringBuilder sb2;
	int noOfFixes = 0;
	double startLongitude;
	double startLatitude;
	double operatingLongitude;
	double operatingLatitude;
	float[] startDelta = new float[3];
	float[] operatingDelta = new float[3];
	float accDist = 0.0f;
	float totalDist = 0.0f;
	float stepping;
	
	int slumptal;
	
	long blood = 0;
	long sweat = 0;
	long tears = 0;
	long accBlood = 0;
	long accSweat = 0;
	long accTears = 0;
	
	long score;
	long totalScore;
	
	MediaPlayer playPick1;
	MediaPlayer playPick2;
	MediaPlayer playPick3;
	MediaPlayer playFindS;
	MediaPlayer playFindL;
	MediaPlayer playAdvance;
	
	quests myQuests;
	
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();

		setContentView(R.layout.main);
		
		TabHost tab_host = (TabHost) findViewById(R.id.edit_item_tab_host);
		tab_host.setup();
		
		TabSpec ts1 = tab_host.newTabSpec("TAB_MAIN");
		ts1.setIndicator("Main");
		ts1.setContent(R.id.main_tab);
		tab_host.addTab(ts1);

		TabSpec ts2 = tab_host.newTabSpec("TAB_QUEST");
		ts2.setIndicator("Quest");
		ts2.setContent(R.id.quest_tab);
		tab_host.addTab(ts2);

		TabSpec ts3 = tab_host.newTabSpec("TAB_STATUS");
		ts3.setIndicator("Stats");
		ts3.setContent(R.id.status_tab);
		tab_host.addTab(ts3);

		tab_host.setCurrentTab(0);
		
		if(debug == 0){
			stepping = 50.0f;	
		}
		else if(debug == 1){
			stepping = 25.0f;
		}
		else if(debug == 2){
			stepping = 0.0f;
		}
		
		// Get the app's shared preferences
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        programCounter = app_preferences.getInt("pcounter", 0);
        
        //A program counter
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putInt("pcounter", ++programCounter);
        editor.commit(); // Very important
		
        // App's private prefs
        SharedPreferences prefsPrivate = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        totalDist = prefsPrivate.getFloat("totalDist", 0.0f);
        totalScore = prefsPrivate.getLong("totalScore", 0);
        accBlood = prefsPrivate.getLong("accBlood", 0);
        accSweat = prefsPrivate.getLong("accSweat", 0);
        accTears = prefsPrivate.getLong("accTears", 0);
        myQuests = new quests(this, prefsPrivate.getInt("currentQuest", 1));
        
		playPick1 = MediaPlayer.create(this, R.raw.pick1);
		playPick1.setLooping(false); // Set looping
		playPick2 = MediaPlayer.create(this, R.raw.pick2);
		playPick2.setLooping(false); // Set looping
		playPick3 = MediaPlayer.create(this, R.raw.pick3);
		playPick3.setLooping(false); // Set looping
		playFindS = MediaPlayer.create(this, R.raw.findsmall);
		playFindS.setLooping(false); // Set looping
		playFindL = MediaPlayer.create(this, R.raw.findlarge);
		playFindL.setLooping(false); // Set looping
		playAdvance = MediaPlayer.create(this, R.raw.chimes3);
		playAdvance.setLooping(false); // Set looping

		txtInfo = (TextView) findViewById(R.id.textInfo);
		txtInfo2 = (TextView) findViewById(R.id.textInfo2);
		
		txtStatus1 = (TextView) findViewById(R.id.status1);
		txtStatus2 = (TextView) findViewById(R.id.status2);
		txtStatus3 = (TextView) findViewById(R.id.status3);
		txtStatus4 = (TextView) findViewById(R.id.status4);
		txtStatus5 = (TextView) findViewById(R.id.status5);
		
		imgStatus4 = (ImageView) findViewById(R.id.image4);
		
		txtQuest = (TextView) findViewById(R.id.textQuest);
		imgQuest = (ImageView) findViewById(R.id.imageQuest);

		//Display any saved resources
		sb2 = new StringBuilder(6);
		sb2.setLength(0);
		sb2.append(accBlood);
		txtStatus1.setText(sb2);
		
		sb2.setLength(0);
		sb2.append(accSweat);
		txtStatus2.setText(sb2);
		
		sb2.setLength(0);
		sb2.append(accTears);
		txtStatus3.setText(sb2);		
		
		//Display current quest
		txtQuest.setText(myQuests.getCurrentQuestText()); //Ok, make an update quest-tab function!!!
		imgQuest.setImageResource(myQuests.getCurrentQuestPicId());
		
		txtStatus5.setText("Walk " + Math.round(stepping) + "m to start...");
		
		/* the location manager is the most vital part it allows access
		 * to location and GPS status services */
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
	}

	@Override
	protected void onResume() {
		/*
		 * onResume is is always called after onStart, even if the app hasn't been
		 * paused
		 *
		 * add location listener and request updates every 1000ms or 10m
		 */
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5f, this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		/* GPS, as it turns out, consumes battery like crazy */
		lm.removeUpdates(this);
		super.onResume();
	}

	//@Override
	public void onLocationChanged(Location location) {
		Log.v(tag, "Location Changed");

		sb = new StringBuilder(512);
		sb2 = new StringBuilder(6);
		
		//player.stop();
		
		noOfFixes++;
		if(noOfFixes == 1){
			startLatitude = location.getLatitude();
			startLongitude = location.getLongitude();
			operatingLatitude = startLatitude;
			operatingLongitude = startLongitude;
		}
		Random generator = new Random(location.getTime());

		/*sb.append("Londitude: ");
		sb.append(location.getLongitude());
		sb.append('\n');

		sb.append("Latitude: ");
		sb.append(location.getLatitude());
		sb.append('\n');

		sb.append("Altitiude: ");
		sb.append(location.getAltitude());
		sb.append('\n');*/
		
		/*sb.append("Accuracy: ");
		sb.append(location.getAccuracy());
		sb.append('\n');*/

		/*sb.append("Timestamp: ");
		sb.append(location.getTime());
		sb.append('\n');*/
		
		Location.distanceBetween(startLatitude, startLongitude, location.getLatitude(), location.getLongitude(), startDelta);
		Location.distanceBetween(operatingLatitude, operatingLongitude, location.getLatitude(), location.getLongitude(), operatingDelta);
		
		blood = sweat = tears = 0;
				
		if(operatingDelta[0] >= stepping){ 
			accDist += operatingDelta[0];
			totalDist += operatingDelta[0];
						
			operatingLatitude = location.getLatitude();
			operatingLongitude = location.getLongitude();
			
			
			
			slumptal = Math.abs(generator.nextInt() % 21); // generating type of finding
			
			if(slumptal >= 0 && slumptal < 16){
				blood = Math.round(Math.pow(Math.abs(generator.nextDouble()), -0.5));
				accBlood += blood;
				score += blood;
				totalScore += blood;
				
				sb2.setLength(0);
				sb2.append(accBlood);
				txtStatus1.setText(sb2);
				
				sb2.setLength(0);
				sb2.append(blood);
				txtStatus4.setText(sb2);
				imgStatus4.setImageResource(R.drawable.redheart);
				
				playPick1.start(); //play different tunes for different sizes of findings. Also tweet about big findings!
			}	
			else if(slumptal >= 16 && slumptal < 20){
				sweat = Math.round(Math.pow(Math.abs(generator.nextDouble()), -0.5));
				accSweat += sweat;			
				score += (sweat * 5);
				totalScore += (sweat * 5);
				
				sb2.setLength(0);
				sb2.append(accSweat);
				txtStatus2.setText(sb2);
				
				sb2.setLength(0);
				sb2.append(sweat);
				txtStatus4.setText(sb2);
				imgStatus4.setImageResource(R.drawable.greenheart);
				
				playPick2.start(); 
			}
				
			else if(slumptal == 20){
				tears = Math.round(Math.pow(Math.abs(generator.nextDouble()), -0.5));
				accTears += tears;
				score += (tears * 25);
				totalScore += (tears * 25);
				
				sb2.setLength(0);
				sb2.append(accTears);
				txtStatus3.setText(sb2);
				
				sb2.setLength(0);
				sb2.append(tears);
				txtStatus4.setText(sb2);
				imgStatus4.setImageResource(R.drawable.blueheart);
				
				playPick3.start();
			}
			
		}
		
		//Small or large find?
		if(blood >= 10 || sweat >= 10 || tears >= 10)
			playFindS.start();
		
		if(blood >= 100 || sweat >= 100 || tears >= 100)
			playFindL.start();
		
	
		//Advancing Quests
		if(accBlood >= myQuests.getCurrentQuestCond()[0] && accSweat >= myQuests.getCurrentQuestCond()[1] && accTears >= myQuests.getCurrentQuestCond()[2] && myQuests.getCurrentQuest() < myQuests.getTotalQuests()){
			txtStatus5.setText("You saved " + myQuests.getCurrentQuestName() + "!");
			
			//reset all resources
			accBlood = 0; accSweat = 0; accTears = 0;
			txtStatus1.setText("0"); txtStatus2.setText("0"); txtStatus3.setText("0");
			
			//Display quest success
			txtStatus4.setText("*");
			imgStatus4.setImageResource(myQuests.getCurrentQuestPicId());
			
			//update score
			score += (myQuests.getCurrentQuest() * 100);
			totalScore += (myQuests.getCurrentQuest() * 100);
			
			//Goto next quest
			myQuests.setCurrentQuest(this, myQuests.getCurrentQuest() + 1);
			
			txtQuest.setText(myQuests.getCurrentQuestText()); //Ok, make an update quest-tab function!!!
			imgQuest.setImageResource(myQuests.getCurrentQuestPicId());
			playAdvance.start();
		}
		else{
			txtStatus5.setText("");
		}
		
		//Save private data to preferences
		SharedPreferences prefsPrivate = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefsPrivate.edit();
        prefsEditor.putFloat("totalDist", totalDist);
        prefsEditor.putLong("totalScore", totalScore);
        prefsEditor.putLong("accBlood", accBlood);
        prefsEditor.putLong("accSweat", accSweat);
        prefsEditor.putLong("accTears", accTears);
        prefsEditor.putInt("currentQuest", myQuests.getCurrentQuest());
        prefsEditor.commit(); // Very important
        
        /* display some of the data in the TxtInfo(s) */
		sb.append("No. of Searches:  ");
		sb.append(noOfFixes);
		sb.append('\n');
		sb.append('\n');
				
		sb.append("Distance moved: " + Math.round(accDist) + " m (Stepping " + Math.round(stepping) + " m)");
		sb.append('\n');
		
		sb.append("Current quest: " + myQuests.getCurrentQuest() + "/" + myQuests.getTotalQuests());
		sb.append('\n');
		
		sb.append("You have:   " + accBlood + "R " + accSweat + "G " + accTears + "B");
		sb.append('\n');
		
		sb.append("Conditions: " + myQuests.getCurrentQuestCond()[0] + "R " + myQuests.getCurrentQuestCond()[1] + "G " + myQuests.getCurrentQuestCond()[2] + "B");
		sb.append('\n');
		
		sb.append('\n');
		sb.append("SCORE: " + score);
		sb.append('\n');
		
				
		txtInfo.setText(sb.toString());
		
		sb.setLength(0);
		sb.append("Program started: ");
		sb.append(programCounter);
		sb.append('\n');
		
		sb.append("Distance moved: " + Math.round(totalDist) + " m");
		sb.append('\n');
		
		sb.append('\n');
		sb.append("TOTAL SCORE: " + totalScore);
		sb.append('\n');
		
		
		//txtInfo2.setTextColor(Color.GREEN);
		txtInfo2.setText(sb.toString());
		
		Log.v(tag, txtInfo.toString());
		Log.v(tag, sb.toString());
	}
	
	//@Override
	public void onProviderDisabled(String provider) {
		/* this is called if/when the GPS is disabled in settings */
		Log.v(tag, "Disabled");

		/* bring up the GPS settings */
		Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}

	//@Override
	public void onProviderEnabled(String provider) {
		Log.v(tag, "Enabled");
		Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();

	}

	//@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		/* This is called when the GPS status alters */
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			Log.v(tag, "Status Changed: Out of Service");
			//Toast.makeText(this, "Status Changed: Out of Service",
			//		Toast.LENGTH_SHORT).show();
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Log.v(tag, "Status Changed: Temporarily Unavailable");
			//Toast.makeText(this, "Status Changed: Temporarily Unavailable",
			//		Toast.LENGTH_SHORT).show();
			break;
		case LocationProvider.AVAILABLE:
			Log.v(tag, "Status Changed: Available");
			//Toast.makeText(this, "Status Changed: Available",
			//		Toast.LENGTH_SHORT).show();
			break;
		}
	}

	@Override
	protected void onStop() {
		/* may as well just finish since saving the state is not important for this toy app */
		wl.release();
		//Log.v(tag, "Released wlock??");
		playPick1.release();
		playPick2.release();
		playPick3.release();
		playFindS.release();
		playFindL.release();
		playAdvance.release();
		finish();
		super.onStop();
	}
	

}

