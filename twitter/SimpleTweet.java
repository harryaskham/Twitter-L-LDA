package uk.ac.cam.ha293.tweetlabel.twitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.ha293.tweetlabel.types.Document;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class SimpleTweet implements Serializable {

	private static final long serialVersionUID = 6122082972723545508L;
	
	private long tweetID;
	private long userID;
	private String text;
	private List<String> urls;
	private List<String> hashtags;
	
	public SimpleTweet(long userID, long tweetID, String text) {
		this.userID = userID;
		this.tweetID = tweetID;
		this.text = text;
		urls = new ArrayList<String>();
		hashtags = new ArrayList<String>();
		mineText(text);
	}
	
	public long getTweetID() {
		return tweetID;
	}
	
	public long getUserID() {
		return userID;
	}
	
	public String getText() {
		return text;
	}
	
	public void print() {
		System.out.println(userID+","+tweetID+","+"\""+text+"\"");
	}
	
	public void printStripped() {
		String strippedData = Tools.stripTweet(text);
		if(strippedData == null) return;
		System.out.println(userID+","+tweetID+","+"\""+strippedData+"\"");
	}
	
	public Document asDocument() {
		return new Document(text);
	}
	
	public void mineText(String text) {
		String[] split = text.split("\\s+");
		for(String token : split) {
			
			//Save out the urls
			if(token.startsWith("http") || token.startsWith("Http") || token.startsWith("www")) {
				urls.add(token);
			}
			
			//Save out the hashtags - NOTE maybe want to 
			if(token.startsWith("#")) {
				hashtags.add(token.substring(1));
			}
		}
	}
	
	public boolean hasUrls() {
		return !urls.isEmpty();
	}
	
	public boolean hasHashtags() {
		return !hashtags.isEmpty();
	}
	
	public List<String> getUrls() {
		return urls;
	}
	
	public List<String> getHashtags() {
		return hashtags;
	}
}
