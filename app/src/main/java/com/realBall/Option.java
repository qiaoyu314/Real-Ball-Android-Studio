package com.realBall;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.realBall.R;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;
/**
 * Activity for option.
 * User can turn on/off music and control the switch of weather info
 * @author Yu, Marty and Lingchen
 *
 */
public class Option extends Activity{
	
	private ToggleButton tbMusic, tbWeather;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.option);
		tbMusic = (ToggleButton) findViewById(R.id.tbMusic);
		tbWeather = (ToggleButton) findViewById(R.id.tbWeather);
		
		//check if the music is playing. if it's playing, set the checkview
		if(MenuList.mp.isPlaying()){
			tbMusic.setChecked(true);
		}
		else{
			tbMusic.setChecked(false);
		}
		//check if already got weather
		if(Weather.hasWeather()){
			tbWeather.setChecked(true);
		}
		else{
			tbWeather.setChecked(false);
		}
		//set the check change listener
		tbMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(!isChecked)
					//if false, pause the music, else, start it.
					MenuList.mp.pause();
				else
					MenuList.mp.start();
			}
		});
		
		tbWeather.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				//
				if(isChecked){
					//if true, check the network first.
					if(hasNetworkConnection()){
						//if there's network, pop up a dialog to input zipcode 
						//and create a weahter object to get weather information
						        final EditText etZip = new EditText(Option.this);
								new AlertDialog.Builder(
										Option.this)
										.setTitle("Please input your city name or zipcode")
										.setIcon(
												android.R.drawable.ic_dialog_info)
										.setView(etZip)
										.setPositiveButton(
												"OK",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {
														try {
															Weather myWeather = new Weather(
																	etZip.getText()
																			.toString());
														} catch (MalformedURLException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														} catch (ParserConfigurationException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														} catch (SAXException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														} catch (IOException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														} finally {
															if (Weather
																	.get_dir() == null) {
																new AlertDialog.Builder(
																		Option.this)
																		.setTitle(
																				"Check your network and input!")
																		.setIcon(
																				android.R.drawable.ic_dialog_alert)
																		.setNegativeButton(
																				"OK",
																				new DialogInterface.OnClickListener() {
																					public void onClick(
																							DialogInterface dialog,
																							int id) {
																						dialog.cancel();
																					}
																				})
																		.show();
																tbWeather
																		.setChecked(false);
															}
															dialog.cancel();
														}

													}

												})
										.setNegativeButton(
												"cancel",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														dialog.cancel();
													}
												})
										.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							                dialog.cancel();
							                tbWeather.setChecked(false);
							           }
							       }).create()
								   .show();
					}
					else{
						//if there's no network, pop up a dialog to warn user
						new AlertDialog.Builder(Option.this)
						.setTitle("No access to the Internet!")
						.setMessage("To get the weather information, you need get your phone connect the Internet first.")
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setNegativeButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
						.show();
						tbWeather.setChecked(false);
					}
				}
				else{
					//if the weather is canceled, prohibit the weather
					Weather.prohibit();
				}
					
					
			}
		});
	}
	
	//This method is used for checking the network's availability
	private boolean hasNetworkConnection(){ 
		ConnectivityManager connectivityManager = 
		(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
		boolean isConnected = true; 
		boolean isWifiAvailable = networkInfo.isAvailable(); 
		boolean isWifiConnected = networkInfo.isConnected(); 
		networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isMobileAvailable = networkInfo.isAvailable(); 
		boolean isMobileConnnected = networkInfo.isConnected();
		isConnected = (isMobileAvailable&&isMobileConnnected)|| (isWifiAvailable&&isWifiConnected);
		return(isConnected);
	}

}
