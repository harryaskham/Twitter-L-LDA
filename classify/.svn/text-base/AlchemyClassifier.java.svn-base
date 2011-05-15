package uk.ac.cam.ha293.tweetlabel.classify;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

import uk.ac.cam.ha293.tweetlabel.util.Tools;


import com.alchemyapi.api.*;

public class AlchemyClassifier {
	
	private static String[] keys;
	private static int keyIndex;
	private static AlchemyAPI alchemyAPI;
	private static int requestCount;

	public AlchemyClassifier() {
		init(); //NOTE: needs to be called from elsewhere too
	}
	
	public static void init() {
		keys = new String[35];
		keys[0] = "c497c0fc6dd00dd6fb13a7cb29c3311709d8c344";
		keys[1] = "9dcb52bec38f96f4d455467453858e44cbeccdaf";
		keys[2] = "5821b26a00557cf60289b03cb1be2257dc3cf6eb";
		keys[3] = "8aa54e2962747f2653cd1d848a63d2a463e4d41f";
		keys[4] = "c0b97edd81b8f4bef113f8d78b4c2755768746b6";
		keys[5] = "f45c15726375f4fa78644d75dbc2eef2e3528d87";
		keys[6] = "c3811e9bf08a209569d1c335e576e8b4ac0df0bc";
		keys[7] = "358855bc94a0fbb2a7fd5ac574099cc574a63932";
		keys[8] = "73173512d654109b54694292fc87fdf51998d408";
		keys[9] = "2225a47a67142a25643ac926bd6e79a0b6250dd1";
		keys[10] = "1601aaa431917f4e3ba62c7a3e1ba6afd3190d10";
		keys[11] = "b6e21a7ded427e2d9bf0f080edadb7cd0e0f31fe";
		keys[12] = "31b8d6989ec914b4b36f72eff8df6ac20ee37263";
		keys[13] = "5eb99cbc7756802962c891b7843f4f6160037c31";
		keys[14] = "8d442ee0195e3e5343a97e1d4c2b478bd91b8899";
		keys[15] = "02e3f0ab5fd43d85178ed501e9917227c24a1be7";
		keys[16] = "e4f32b39fa8ca88ef12f488895797695a9c6f902";
		keys[17] = "a10efc9508044afdbd3415341e1dfb12ef27fc92";
		keys[18] = "f809b9795a4761084f22e07b339e293c5556d308";
		keys[19] = "b0470e00a30d6e9fbf94716baca9c4174d2b9f12";
		keys[20] = "e7f90d7158b2168479ae944af07f56bad6bc2247";
		keys[21] = "88bd58ed3df9d77eff384092e62d0788363242cd";
		keys[22] = "3254e7332a9262a475236563b9984eb8b18fc97e";
		keys[23] = "4af5c0b33f491ea00499b8dfae827c8eaa3ed128";
		keys[24] = "15a9800bd191aad9bce2e65b7040eee74c36a110";
		keys[25] = "51c2e1771680357dbd34bb8352bda62a6b661316";
		keys[26] = "e21a8e0e597e5b9d3278e9d361080909481d20c9";
		keys[27] = "371ac1ba6dc6b6809a4c2bb15bc37b78879d2714";
		keys[27] = "81d9efc046063f6508acaf5052083c2188605817";
		keys[28] = "b44c8b62149e0d27edc51e7df2feddd397178145";
		keys[29] = "7431ba2ecc7703222a09dd1d86642837af5697eb";
		keys[30] = "f80d64cc89038a918f42e95c02b6f7fe6b9308c1";
		keys[31] = "ca3a625d7c429ace4b8033d4cc9ec3b08acaa878";
		keys[32] = "eaab19db33996b5404a9be6ba00bc5e8c3851561";
		keys[33] = "97b981de050c18582c39e37f9340f4c57e2e1500";
		keys[34] = "084385760b5c9b55f8c8c91b26d5195ba3bfa6dd";
		
		
		keyIndex = 0;
		alchemyAPI = AlchemyAPI.GetInstanceFromString(keys[keyIndex]);
		requestCount = 0;
	}
	
	//Either returns a valid AlchemyClassification, or it returns an empty one. Never null...
	public static AlchemyClassification classifyURL(String url) {
		updateRequestCount();
		try {
			org.w3c.dom.Document d = alchemyAPI.URLGetCategory(url);
			String category = d.getChildNodes().item(0).getChildNodes().item(9).getTextContent();
			double score = Double.parseDouble(d.getChildNodes().item(0).getChildNodes().item(11).getTextContent());
			if(category.isEmpty() || category == null) {
				return new AlchemyClassification();
			} else {
				return new AlchemyClassification(category,score);
			}
		} catch(NullPointerException e) {
			System.err.println("NullPointerException trying to classify a URL:");
			System.err.println(url);
		} catch (IllegalArgumentException e) {
			//URL not well formatted - might be a truncated retweet. This is fine.		
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			//Page not HTML - image or something. Or - daily limit reached!!!
			if(e.getMessage().contains("daily-transaction-limit-exceeded")) {
				System.err.println("Transaction limit alert");
				/*
				if(updateKey()) {
					return classifyText(text);
				} else {
					return null; 
				}
				*/
				return null;
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return new AlchemyClassification();
	}
	
	public static AlchemyClassification classifyText(String text) {
		updateRequestCount();
		try {
			org.w3c.dom.Document d = alchemyAPI.TextGetCategory(text);	
			//Tools.prettyPrintDocument(d);
			String category = d.getChildNodes().item(0).getChildNodes().item(9).getTextContent();
			double score = Double.parseDouble(d.getChildNodes().item(0).getChildNodes().item(11).getTextContent());
			if(category == null || category.isEmpty()) {
				return new AlchemyClassification();
			} else {
				return new AlchemyClassification(category,score);
			}
		} catch(NullPointerException e) {
			System.err.println("NullPointerException trying to classify some text:");
			System.err.println(text);
		} catch (IllegalArgumentException e) {
			//text not well formatted - might be an empty tweet? This is (probably) fine.		
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			//Page not HTML - image or something. Or - daily limit reached!!!
			if(e.getMessage().contains("daily-transaction-limit-exceeded")) {
				System.err.println("Transaction limit alert");
				if(updateKey()) {
					return classifyText(text);
				} else {
					return null; 
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return new AlchemyClassification();
	}
	
	private static void updateRequestCount() {
		requestCount++;
		if(requestCount % 50 == 0) {
			//System.out.println("Alchemy Requests Made: "+requestCount);
			
			
			//Experimental key-change -it works !!!
			if(requestCount % 29900 == 0) {
				updateKey();
			}
		}
	}
	
	private static boolean updateKey() {
		if(keyIndex < keys.length - 1) {
			System.out.println("Trying another alchemy key...");
			keyIndex++;
			System.out.println("Using key "+keyIndex);
			alchemyAPI = AlchemyAPI.GetInstanceFromString(keys[keyIndex]);
			return true;
		} else {
			System.out.println("Out of alchemy keys to try");
			keyIndex=0;
			return true;
			//return false;
		}
	}
	
}
