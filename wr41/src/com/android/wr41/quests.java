package com.android.wr41;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;


public class quests{
	private static final String TAG = "questsXML";
	private int currentQuest;
	private int totalQuests;
	private int currentQuestPicId;
	private int[] currentQuestCond;
	private String currentQuestName;
	private String currentQuestText;
	
	public quests (Context context, int cq){
		currentQuestCond = new int[3];
		setCurrentQuest(context, cq);
	}
	
	public void setCurrentQuest(Context context, int cq){
		currentQuest = cq;
		retrieveQuestData(context, currentQuest);
	}
	
	public int getCurrentQuest(){
		return currentQuest;
	}
	
	public int getTotalQuests(){
		return totalQuests;
	}
	
	public int getCurrentQuestPicId(){
		return currentQuestPicId;
	}
	
	public int[] getCurrentQuestCond(){
		return currentQuestCond;
	}
		
	public String getCurrentQuestName(){
		return currentQuestName;
	}
	
	public String getCurrentQuestText(){
		return currentQuestText;
	}
	
	private void retrieveQuestData(Context context, int cq){
		// Get the Android-specific compiled XML parser.
		totalQuests = 0; //reset counter
		
			
		try{
			XmlResourceParser xrp = context.getResources().getXml(R.xml.quests);
			//Log.v(TAG, "HIT 2");
			while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
				
				if (xrp.getEventType() == XmlResourceParser.START_TAG) {
					String s = xrp.getName();
					
					if (s.equals("questtag")) {
						totalQuests++;
						if(currentQuest == totalQuests){
							// Get the resource id; this will be retrieved as a 
							// resolved hex value.
							currentQuestPicId = xrp.getAttributeResourceValue(null, "id", 0);
							currentQuestName = xrp.getAttributeValue(null, "namestr");
							currentQuestCond[0] = xrp.getAttributeIntValue(null, "redheart", 0);
							currentQuestCond[1] = xrp.getAttributeIntValue(null, "greenheart", 0);
							currentQuestCond[2] = xrp.getAttributeIntValue(null, "blueheart", 0);
						}
					} else if (s.equals("questtext")) {
						// Get the element tag name here; the value will be 
						// gotten on the next TEXT event.
						//tv.append("Tag " + s + " has value ");
					}
				} else if (xrp.getEventType() == XmlResourceParser.END_TAG) {
					;
				} else if (xrp.getEventType() == XmlResourceParser.TEXT) { 
					// Get our value from the plaintag element. 
					// Since this is a value and not an
					// attribute, we retrieve it with the 
					// generic .getText().
					if(currentQuest == totalQuests){
						currentQuestText = xrp.getText();
					}
				}
				xrp.next();
			}
			xrp.close();
			Log.v(TAG, "totq " + totalQuests);
			
		} catch (XmlPullParserException xppe) {
		
		                        Log.e(TAG, "Failure of .getEventType or .next, probably bad file format");
		
		                        xppe.toString();
		
		} catch (IOException ioe) {
		
		                        Log.e(TAG, "Unable to read resource file");
		
		                        ioe.printStackTrace();
		
		}
	
	}
	
}
