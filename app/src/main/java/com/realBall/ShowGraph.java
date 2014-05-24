package com.realBall;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.content.pm.ActivityInfo;

/**
 * Activity for showing the last graph
 * The graph is based on the last game the user played
 * @author Yu, Marty and Lingchen
 *
 */
public class ShowGraph extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        GraphView graph = new GraphView(this);
        graph.setData(LevelOne.velocityList,LevelOne.accelerationList);
        setContentView(graph.getView());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		startActivity(new Intent("com.realBall.MENULIST"));
	}
	
	
	

}
