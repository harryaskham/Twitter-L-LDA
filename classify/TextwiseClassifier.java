package uk.ac.cam.ha293.tweetlabel.classify;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.w3c.dom.NodeList;

import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class TextwiseClassifier {
	
	private static String[] keys;
	private static int keyIndex;
	private static int requestCount;
	private static String requestURL;

	public TextwiseClassifier() {
		init(); //NOTE: needs to be called from elsewhere too
	}
	
	public static void init() {
		keys = new String[7];
		keys[0] = "t6sqta0i";
		keys[1] = "kjk1r0lh";
		keys[2] = "i3x9rte7";
		keys[3] = "fb4dt0gk";
		keys[4] = "gfgkiiv5";
		keys[5] = "w1jv7fli";
		keys[6] = "p3ssii6p";
		keyIndex = 1;
		requestCount = 0;
		updateRequestURL();
	}
	
	//NOTE: Either need to add content=URL+ENCODED+STRING or uri=someURI to this for a valid request
	public static void updateRequestURL() {
		requestURL = "http://api.semantichacker.com/" + keys[keyIndex] + "/category?filter=text&format=xml&showLabels=true&nCategories=1&useShortLabels=false&";
		//requestURL = "http://api.semantichacker.com/" + keys[keyIndex] + "/category?filter=text&format=xml&showLabels=true&nCategories=1&useShortLabels=true&";
	}
			
	public static TextwiseClassification classify(String text, boolean isURL) {
		updateRequestCount();
		String requestURLSnapshot;
		try {
			if(isURL) {
				requestURLSnapshot = new String(requestURL) + "uri=" + URLEncoder.encode(text, "UTF-8");
			} else {
				requestURLSnapshot = new String(requestURL) + "content=" + URLEncoder.encode(text, "UTF-8");
				if(requestURLSnapshot.length()>1000) return new TextwiseClassification();
			}
			URL url = new URL(requestURLSnapshot);

			URLConnection urlConnection = url.openConnection();
			BufferedInputStream buffer = new BufferedInputStream(urlConnection.getInputStream());
			StringBuilder builder = new StringBuilder();
			int byteRead;
			while((byteRead = buffer.read()) != -1) {
				builder.append((char)byteRead);
			}
			buffer.close();
			String xmlString = builder.toString();
			org.w3c.dom.Document doc = Tools.xmlStringToDocument(xmlString);
			
			//Check for errors in the XML
			String message;
			try {
				message = doc.getElementsByTagName("message").item(0).getAttributes().getNamedItem("string").getTextContent();
				if(message.equals("Over hour limit")) {
					System.err.println("Hourly limit reached");
					updateKey();
					return classify(text,isURL);
				} else if(message.equals("Over minute limit")) {
					System.err.println("Minute(!?) limit reached");
					updateKey();
					return classify(text,isURL);
				} else if(message.equals("Over load limit")) {
					System.err.println("Load limit reached");
					updateKey();
					Thread.sleep(500);
					return classify(text,isURL);
				} else if(message.equals("Invalid token")) {
					System.err.println("Invalid token used");
					updateKey();
					return classify(text,isURL);
				} else if(message.equals("Failure Fetching Content")) {
					System.err.println("Couldn't fetch content, skipping");
					updateKey();
					return new TextwiseClassification();
				} else if(message.equals("Invalid URI")) {
					System.err.println(text+" is an invalid URI");
					return new TextwiseClassification();
				} else {
					System.err.println("Unknown error: "+message);
					updateKey();
					return classify(text,isURL);
				}
			} catch(NullPointerException e) {
				//This is fine - just means there's no message.
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			NodeList categories = doc.getElementsByTagName("category");
			
			TextwiseClassification result = new TextwiseClassification();
			
			for(int i=0; i<categories.getLength(); i++) {
				String category = categories.item(i).getAttributes().getNamedItem("label").getTextContent();
				String score = categories.item(i).getAttributes().getNamedItem("weight").getTextContent();
				result.add(category, Double.parseDouble(score));
			}
			return result;
		} catch (UnsupportedEncodingException e) {
			System.err.println("Couldn't URL-Encode the text");
		} catch (MalformedURLException e) {
			System.err.println("URL-Encoded request is badly formed");
		} catch (IOException e) {
			System.err.println("Couldn't connect to URL");
			updateKey();
			return classify(text,isURL);
		} catch (NumberFormatException e) {
			System.err.println("Couldn't format the score as a Double");
		}
		return new TextwiseClassification();
	}
		
	private static void updateRequestCount() {
		requestCount++;
		if(requestCount % 50 == 0) {
			System.out.println("Textwise Requests Made: "+requestCount);
		}
	}
	
	private static boolean updateKey() {
		System.out.println("Trying another Textwise key...");
		keyIndex++;
		if(keyIndex >= keys.length) keyIndex = 0;
		updateRequestURL();
		System.out.println("Using key "+keyIndex);
		return true;
	}
}
