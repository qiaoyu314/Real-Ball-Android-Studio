package com.realBall;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
/**
 * Class for getting weather info 
 * @author Yu, Marty and Lingchen
 *
 */
public class Weather {

	private static boolean hasWeather = false;
	private static String dir;
    private static String velocity;
	/**
	 * constructor. Pass the zip code or city inputed by user and use Google API to get weather info 
	 * @param zipcodeOrCity
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	Weather(String zipcodeOrCity) throws ParserConfigurationException, SAXException,
			MalformedURLException, IOException {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		XMLReader reader = sp.getXMLReader();

		MyHandler mh = new MyHandler();
		reader.setContentHandler(mh);
		reader.parse(new InputSource(new URL(
				"http://www.google.com/ig/api?weather=" + zipcodeOrCity).openStream()));
		//set hasWeaher to true
		allow();
		//set the dir and velocity
		dir = mh.get_dir();
		velocity = mh.get_velocity();

	}
	//getter
	public static boolean hasWeather(){
		return hasWeather;
	}
	public static String get_dir(){
    	return dir;
    }
    public static String get_velocity(){
    	return velocity;
    }
    public static void allow (){
    	hasWeather = true;
    }
    public static void prohibit (){
    	hasWeather = false;
    }
}
