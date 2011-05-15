package uk.ac.cam.ha293.tweetlabel.classify;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.clearforest.calais.common.CalaisJavaIf;
import com.clearforest.calais.common.StringUtils;

public class CalaisClassifier {

	private static String[] keys;
	private static int keyIndex;
	private static int requestCount;
	private static CalaisJavaIf calais;

	public CalaisClassifier() {
		init(); //NOTE: needs to be called from elsewhere too
	}
	
	public static void init() {
		keys = new String[13];
		keys[0] = "3xw5bayrkykgeseyydwxw4re";
		keys[1] = "amn8753bkhfhxf3mth3yv6ev";
		keys[2] = "sg2dn8yfy4uavasmnzehaffg";
		keys[3] = "v7kg9y8p388q3wjd4arsgtw2";
		keys[4] = "bkuf9tnx9xsb6hgjf4f2x845";
		keys[5] = "tnvpbfb94mxgvg9ycaygkqak";
		keys[6] = "6umcpjh6ufp6v3th6zw6yfgs";
		keys[7] = "zcuu786ap89euazm88b77kzm";
		keys[8] = "54dr8xsuy6z72m5p3x6dt5nq";
		keys[9] = "hk3nwmjzf5pg8qt79sac9yth";
		keys[10] = "jt5pyvr9f428f3qr4rsyh6dg";
		keys[11] = "sg87qnkr8c5jjw233ezejqwh";
		keys[12] = "fkrrhm7w8asp3dchddzvzzs3";
		keyIndex = 0;
		requestCount = 8600; //roughly where it left off
		calais = new CalaisJavaIf(keys[keyIndex]);
	}
			
	//Discards a lot of tweets - maybe better to amalgamate some?
	public static CalaisClassification classifyText(String text) {
		
		if(text.length() < 100) {
			//System.err.println("Tweet is <100 chars - too short for Calais");
			return null;
		}
		
		updateRequestCount();
		
		//Hacky key rotation scheme to hopefully bypass the time and rate limits...
		keyIndex++;
		if(keyIndex >= keys.length) keyIndex = 0;
		calais = new CalaisJavaIf(keys[keyIndex]);
				
		CalaisClassification result = new CalaisClassification();		
		String xmlResponse = StringUtils.unescapeHTML(calais.callEnlighten(text));
					
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		org.w3c.dom.Document parsedXML = null;
		try {
			documentBuilder = dbf.newDocumentBuilder();
			
			StringReader reader = new StringReader(xmlResponse);
			InputSource inputSource = new InputSource(reader);
			parsedXML = documentBuilder.parse(inputSource);
					
			//System.out.println(text);
			
			for(int i=0; i<3; i++) {
				String pairedString = null;;
				try {
					pairedString = parsedXML.getChildNodes().item(0).getChildNodes().item(2).getChildNodes().item(3+i).getTextContent();
				} catch(NullPointerException e) {
					//System.err.println("NullPointerException found, scores cannot be present in the XML");
					break;
				}
				if(!pairedString.startsWith("Calais")) {
					break;
				}
				pairedString = pairedString.substring(6);
				int scoreIndex = pairedString.indexOf("0.");
				if(pairedString.equals("Other")) {
					result.add("Other", 1.0);
					break;
				}
				
				//"Foolproof" check to make sure we do actually have a category String...
				if(scoreIndex == -1) {
					break;
				}
							
				String category = pairedString.substring(0, scoreIndex);
				String score = pairedString.substring(scoreIndex);
				try {
					result.add(category, Double.parseDouble(score));
				} catch(NumberFormatException e) {
					System.err.println("Got an NFE - "+category+", "+score);
				}
			}		
			
			return result;
			
		} catch (IOException e) {
			System.err.println("Couldn't parse the XML response - IOException");
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't parse the XML response - Parser Config Error");
		} catch (SAXException e) {
			System.err.println("Couldn't parse the XML response - SAX Error. Retrying...");
			
			//experimental retry
			return classifyText(text);
		} 
		return null;
	}
	
	private static void updateRequestCount() {
		requestCount++;
		if(requestCount % 50 == 0) {
			System.out.println("Calais Requests Made: "+requestCount);
			
			if(requestCount % 49900 == 0) {
				updateKey();
			}
		}
	}
	
	private static boolean updateKey() {
		if(keyIndex < keys.length - 1) {
			System.out.println("Trying another Calais key...");
			keyIndex++;
			calais = new CalaisJavaIf(keys[keyIndex]);
			System.out.println("Using key "+keyIndex);
			return true;
		} else {
			System.out.println("Out of Calais keys to try");
			return false;
		}
	}
	
}
