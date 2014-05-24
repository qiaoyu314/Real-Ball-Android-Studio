package com.realBall;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class for parsing XML using SAX
 * @author Yu, Marty and Lingchen
 *
 */
public class MyHandler extends DefaultHandler{
    
    private boolean inForcast;
    private String dir;
    private String velocity;




    public void XmlHandler() {
        
 
        inForcast = false;
        
    }
    /**
     * When reaching the start tag
     */
	@Override
	public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        
        String tagName = localName.length() != 0 ? localName : qName;
        tagName = tagName.toLowerCase();
        
        if(tagName.equals("current_conditions")) {
            
            inForcast = true;
            
        }
        //get velocity and direction
        if(inForcast) {
            
            if(tagName.equals("wind_condition")) {                
                this.dir = (attributes.getValue("data").split(" ")[1]);
                this.velocity = (attributes.getValue("data").split(" ")[3]);
            }
        }
        
    }
    /**
     * when reaching the end tag
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        String tagName = localName.length() != 0 ? localName : qName;
        tagName = tagName.toLowerCase();
        
        if(tagName.equals("forecast_conditions")) {
            inForcast = false;
        }
    }
    
    public String get_dir(){
    	return this.dir;
    }
    public String get_velocity(){
    	return this.velocity;
    }

}

	
	


