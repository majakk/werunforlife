package com.android.wr41;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
//import android.view.Menu;
import android.widget.TextView;

public class splash extends Activity {

	// ===========================================================
	// Fields
	// ===========================================================
	static final String tag = "Splash"; // for Log

	private final int SPLASH_DISPLAY_LENGHT = 3000;
	TextView introTxt;
	StringBuilder sb;
	// ===========================================================
	// "Constructors"
	// ===========================================================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.intro);
		
		introTxt = (TextView) findViewById(R.id.introText);
		sb = new StringBuilder(512);
		
		//sb.append('\n');
		//sb.append('\n');
		//sb.append("Presented by:");
		//sb.append('\n');
		sb.append("Studio Coldstream");
		sb.append('\n');
		sb.append('\n');
		sb.append("Disclaimer - PreAlfa test release, please report bugs or email comments to:");
		sb.append('\n');
		sb.append("coldstream.solutions@gmail.com");
		sb.append('\n');
		sb.append('\n');
		sb.append(getString(R.string.version));
		
		introTxt.setText(sb.toString());
		
		/* New Handler to start the Menu-Activity 
		 * and close this Splash-Screen after some seconds.*/
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				/* Create an Intent that will start the Menu-Activity. */
				Intent mainIntent = new Intent(splash.this,wr41.class);
				splash.this.startActivity(mainIntent);
				splash.this.finish();
			}
		}, SPLASH_DISPLAY_LENGHT);
	}
}