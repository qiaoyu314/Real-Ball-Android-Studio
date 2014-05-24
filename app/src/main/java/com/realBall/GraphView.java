package com.realBall;


import java.util.List;
import com.jjoe64.graphview.*;
import com.jjoe64.graphview.GraphView.*;
import com.jjoe64.graphview.GraphViewSeries.*;
import android.content.*;
import android.graphics.Color;
import android.view.View;

/**
 * Created by Vinky on 5/23/14.
 * Use GraphView to create a beautiful graph for velocity-time and acceleration-time graph
 */
public class GraphView {

    LineGraphView graphView;

    GraphView(Context context){

        graphView = new LineGraphView(context,"");

    }

    public void setData(List<Float> velocity, List<Float> acceleration){
        //build data
        int size = velocity.size();
        GraphViewData[] vData = new com.jjoe64.graphview.GraphView.GraphViewData[size];
        GraphViewData[] aData = new com.jjoe64.graphview.GraphView.GraphViewData[size];
        for(int i=0;i<size;i++){
            vData[i] = new GraphViewData(i, velocity.get(i)*100);
            aData[i] = new GraphViewData(i, acceleration.get(i)*100);
        }

        //build line
        GraphViewSeries.GraphViewSeriesStyle blue = new GraphViewSeriesStyle();
        blue.color = Color.BLUE;
        GraphViewSeries.GraphViewSeriesStyle red = new GraphViewSeriesStyle();
        red.color = Color.RED;
        GraphViewSeries vLine = new GraphViewSeries("Velocity-Time",blue, vData);
        GraphViewSeries aLine = new GraphViewSeries("Acceleration-Time", red, aData);

        //draw
        graphView.addSeries(vLine);
        graphView.addSeries(aLine);
        //graphView.setViewPort();
        graphView.setScalable(false);

        //label
        //graphView.setVerticalLabels(new String[]{""});
        //graphView.setHorizontalLabels(new String[]{"Time"});

        //legend
        graphView.setShowLegend(true);
        graphView.setLegendAlign(LegendAlign.BOTTOM);
        graphView.setLegendWidth(200);

    }

    public View getView(){
        return graphView;
    }
}
