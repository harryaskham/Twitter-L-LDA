package uk.ac.cam.ha293.tweetlabel.twitter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.Status;
import uk.ac.cam.ha293.tweetlabel.types.Document;

public class RawProfile implements Serializable {

	private static final long serialVersionUID = 1203485541696998554L;
	
	private String username;
	private long userID;
	private List<Status> tweets;
	
	public RawProfile(String username) {
		this.username = username;
		this.userID = -1;
		tweets = new ArrayList<Status>();
	}
	
	public void setUserID(long userID) {
		this.userID = userID;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void addTweet(Status tweet) {
		tweets.add(tweet);
	}
	
	public void addTweets(List<Status> tweetList) {
		tweets.addAll(tweetList);
	}
	
	public List<Status> getTweets() {
		return tweets;
	}
	
	public void print() {
		System.out.println("Twitter Profile for "+username);
		for(Status status : tweets) {
			System.out.println(status.getText());
		}
	}
	
	public void save() {
		try {
			String profileFilename = "profiles/raw/"+username+".profile";
			FileOutputStream fileOut = new FileOutputStream(profileFilename);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			objectOut.close();
			System.out.println("Downloaded and saved profile for "+username);
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save profile for "+username);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't save profile for "+username);
			e.printStackTrace();			
		}
	}
	
	public void saveCSV() {
		if(userID == -1) {
			System.err.println("Cannot save as CSV profile, no UID set for RawProfile");
			return;
		}
		try {
			String profileFilename = "profiles/csv/"+userID+".csv";
			FileOutputStream fileOut = new FileOutputStream(profileFilename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			writeOut.println("\"userid\",\"tweetid\",\"text\"");
			for(Status status : tweets) {
				writeOut.print(userID+",");
				writeOut.print(status.getId()+",");
				writeOut.print("\""+status.getText()+"\"");
				writeOut.println();
			}
			writeOut.close();
			System.out.println("Saved CSV profile for "+username);
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save CSV profile for "+username);
			e.printStackTrace();
		} 
	}

	public void saveAsSimpleProfile() {
		if(userID == -1) {
			System.err.println("Cannot save as simple profile, no UID set for RawProfile");
			return;
		}
		SimpleProfile simple = new SimpleProfile(userID);
		for(Status tweet : tweets) {
			SimpleTweet simpleTweet = new SimpleTweet(userID,tweet.getId(),tweet.getText());
			simple.addTweet(simpleTweet);
		}
		simple.save();
	}
	
	public SimpleProfile asSimpleProfile() {
		if(userID == -1) {
			System.err.println("Cannot convert to simple profile, no UID set for RawProfile");
			return null;
		}
		SimpleProfile simple = new SimpleProfile(userID);
		for(Status tweet : tweets) {
			SimpleTweet simpleTweet = new SimpleTweet(userID,tweet.getId(),tweet.getText());
			simple.addTweet(simpleTweet);
		}
		return simple;
	}
	
	public Set<Document> asDocumentSet() {
		Set<Document> documents = new HashSet<Document>();
		for(Status tweet : tweets) {
			Document document = new Document(tweet.getText());
			documents.add(document);
		}
		return documents;
	}
}
