package uk.ac.cam.ha293.tweetlabel.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.*;
import twitter4j.http.*;

public class Profiler {
	
	Set<Long> existingProfiles;
	Twitter twitter;
	String accessTokenFilename = "WEB-INF/tokens/accessToken.obj";
	AccessToken accessToken;
	private String consumerKey = "ovSLNjZKYbYu3kcrDyVuXg";
	private String consumerSecret = "YzfRt93vUrPvO7ZKHnqcNslrsy7oa5fIQTiY0ARno";
	
	public Profiler() {
		//Figure out which profiles we've already saved so we don't have to worry about them later
		existingProfiles = new HashSet<Long>();		
		
		//Either get a new authentication token, or load the persistent one
		registerOAuth();
	}
	
	public void findExistingProfileNames() {
		File profileDir = new File("profiles/simple");
		ArrayList<String> profiles = new ArrayList<String>(Arrays.asList(profileDir.list()));
		profiles.remove(".svn");
		if(profiles == null) return;
		//System.out.println("Existing Profiles: ");
		for(int i=0; i<profiles.size(); i++) { //Set to 1 to skip .svn...
			String username = profiles.get(i).split("\\.")[0];
			existingProfiles.add(Long.parseLong(username));
			//System.out.println(username);
		}
	}
	
	public Set<Long> getExistingProfileNames() {
		return existingProfiles;
	}
	
	@SuppressWarnings("deprecation")
	public void registerOAuth() {
		twitter = new TwitterFactory().getInstance();	
		twitter.setOAuthConsumer(consumerKey, consumerSecret);		
		
		try {
			FileInputStream fileIn = new FileInputStream(accessTokenFilename);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			accessToken = (AccessToken)objectIn.readObject();
			if(accessToken != null) {
				System.out.println("Successfully read accessToken");
				System.out.println(accessToken.getToken()+", "+accessToken.getTokenSecret());
				twitter.setOAuthAccessToken(accessToken);
				return;
			}
		} catch (IOException e){
			System.out.println("Couldn't read access token");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}		
		
	    RequestToken requestToken = null;
		try {
			requestToken = twitter.getOAuthRequestToken();
		} catch (TwitterException e1) {
			System.err.println("Couldn't get request token for OAuth");
			e1.printStackTrace();
			return;
		}
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    while (null == accessToken) {
	    	System.out.println("Open the following URL and grant access to your account:");
	    	System.out.println(requestToken.getAuthorizationURL());
	    	System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
	    	String pin;
	    	try{
				pin = br.readLine();
	    		if(pin.length() > 0){
	    			accessToken = twitter.getOAuthAccessToken(requestToken, pin);
	    		}else{
	    			accessToken = twitter.getOAuthAccessToken();
	    		}
	    	} catch (TwitterException te) {
	    		if(401 == te.getStatusCode()){
	    			System.out.println("Unable to get the access token.");
	    		}else{
	    			te.printStackTrace();
	    		}
	    		return;
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		return;
	    	}
	    }
	    
	    //persist to the accessToken for future reference.
	    try {
	    	FileOutputStream fileOut = new FileOutputStream(accessTokenFilename);
	    	ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
	    	objectOut.writeObject(accessToken);
	    	System.out.println("Access token aquired successfully");
	    } catch (IOException e) {
	    	System.err.println("Couldn't store access token");
	    	e.printStackTrace();
	    }
	}
	
	//Note: this can't update a profile
	//TODO: Create a version that synchronises profiles without redownloading
	public RawProfile getRawProfile(String username, int tweets) {
		
		//Download Profile as a list of Status objects
		List<Status> timeline = null;
		int statusesCount;
		try {
			User user = twitter.showUser(username);
			statusesCount = user.getStatusesCount();
			System.out.println(username+" has "+statusesCount+" tweets, attempting download...");
		} catch (TwitterException e) {
			System.err.println("Couldn't get "+username+"'s number of tweets");
			e.printStackTrace();
			return null;			
		}
		
		RawProfile rawProfile = new RawProfile(username);
		try {
			rawProfile.setUserID(twitter.showUser(username).getId());
		} catch (TwitterException e) {
			System.err.println("Couldn't get UID");
			e.printStackTrace();
		}
		
		int pageCount = 1;
		do {
			try {
				System.out.println("Attempting to obtain page "+pageCount+" of "+username+"'s tweets");
				//Paging paging = new Paging(pageCount,statusesCount);
				Paging paging = new Paging(pageCount,200); //200 is max but gives http error 502
				timeline = twitter.getUserTimeline(username, paging);
				if(timeline.isEmpty()) {
					break; //To avoid the case of the phantom missing tweet...
				}
				rawProfile.addTweets(timeline);
				if(rawProfile.getTweets().size() >= tweets) break;
				pageCount++;				
			} catch (TwitterException e) {
				System.err.println("Couldn't get "+username+"'s profile, retrying...");
				//e.printStackTrace();
			}
			System.out.println("Downloaded "+rawProfile.getTweets().size()+" tweets so far");
		} while(rawProfile.getTweets().size() != statusesCount);
		
		//rawProfile.print();
		
		//save raw profile to a profile file and CSV, as well as a simple profile and a simple CSV
		//rawProfile.save();
		//rawProfile.saveCSV();
		//rawProfile.saveAsSimpleProfile();
		
		return rawProfile;
	}	
		
	public static RawProfile loadRawProfile(String username) {
		RawProfile profile = null;
		try {
			FileInputStream fileIn = new FileInputStream("profiles/raw/"+username+".profile");
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			profile = (RawProfile)objectIn.readObject();
			if(profile != null) {
				System.out.println("Successfully read profile "+username);
			}
		} catch (IOException e){
			System.err.println("Couldn't read profile "+username);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}			
		return profile;
	}
	
	public SimpleProfile loadSimpleProfile(String username) {
		try {
			return loadSimpleProfile(twitter.showUser(username).getId());
		} catch (TwitterException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public SimpleProfile loadSimpleProfileWeb(String username) {
		try {
			return loadSimpleProfileWeb(twitter.showUser(username).getId());
		} catch (TwitterException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static SimpleProfile loadSimpleProfile(long userID) {
		SimpleProfile profile = new SimpleProfile(userID);
		try {
			FileInputStream fileIn = new FileInputStream("profiles/simple/"+userID+".sprofile");
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			profile = (SimpleProfile)objectIn.readObject();
			if(profile != null) {
				System.out.println("Successfully read simple profile "+userID);
			}
		} catch (IOException e){
			System.err.println("Couldn't read simple profile "+userID);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}			
		return profile;
	}
	
	public static SimpleProfile loadSimpleProfileWeb(long userID) {
		SimpleProfile profile = new SimpleProfile(userID);
		try {
			FileInputStream fileIn = new FileInputStream("WEB-INF/profiles/"+userID+".sprofile");
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			profile = (SimpleProfile)objectIn.readObject();
			if(profile != null) {
				System.out.println("Successfully read simple profile "+userID);
			}
		} catch (IOException e){
			System.err.println("Couldn't read simple profile "+userID);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}			
		return profile;
	}
	
	public SimpleProfile loadCSVProfile(String username) {
		try {
			return loadCSVProfile(twitter.showUser(username).getId());
		} catch (TwitterException e) {
			e.printStackTrace();
			return null;
		}
	}	
	
	public static SimpleProfile loadCSVProfile(long userID) {
		SimpleProfile profile = new SimpleProfile(userID);
		try {
			FileInputStream fileIn = new FileInputStream("profiles/csv/"+userID+".csv");
			BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));
			String nextLine = "";
			buffer.readLine(); //skip past the CSV descriptor
			while(true) {
				nextLine = buffer.readLine();
				if(nextLine == null) break;
				String[] splitTweet = nextLine.split(",");
				long tweetID = Long.parseLong(splitTweet[1]);
				String text = nextLine.substring(nextLine.indexOf("\"")+1,nextLine.length()-1); //to avoid comma tweeting errors
				SimpleTweet newTweet = new SimpleTweet(userID,tweetID,text);
				profile.addTweet(newTweet);
			}
		} catch (IOException e){
			System.err.println("Couldn't read profile "+userID);
			return null;
		} 
		return profile;
	}
	
	public void parseCSVDataset() {
		try {
			FileInputStream fileIn = new FileInputStream("dataset/users_with_location.csv");
			BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));
			String nextLine = "";
			buffer.readLine(); //skip past the CSV descriptor
			SimpleProfile currentProfile = null;
			int lineCount = 1;
			while(true) {
				
				lineCount++;
				if(lineCount % 1000 == 0) System.out.println("Currently on line "+lineCount);
				
				nextLine = buffer.readLine();
				//If nextLine is null, we still have to save the final profile!
				if(nextLine == null) {
					System.out.println("Profile "+currentProfile.getUserID()+" is the final profile, saving:");
					currentProfile.save();
					currentProfile.saveCSV();		
					break;
				}
				
				String[] splitTweet = nextLine.split(",");
				long currentTweetUserID = Long.parseLong(splitTweet[0]);
				long currentTweetID = Long.parseLong(splitTweet[1]);
				String currentTweetText = nextLine.substring(nextLine.indexOf("\"")+1,nextLine.length()-1);  //to avoid comma tweeting errors
				
				//Creating a SimpleTweet out of the CSV entry
				SimpleTweet currentTweet = new SimpleTweet(currentTweetUserID,currentTweetID,currentTweetText);
				
				//If we are at the first profile in the list, simply create it, add the tweet and continue
				if(currentProfile == null) {
					System.out.println("Found first profile, ID: "+currentTweetUserID);
					currentProfile = new SimpleProfile(currentTweetUserID);
					currentProfile.addTweet(currentTweet);
					continue;
				}
				
				//Else, check if we've hit a new user - if so, we need to save the simple profile and make a new one
				if(currentProfile.getUserID() != currentTweetUserID) {
					System.out.println("Profile "+currentProfile.getUserID()+" finished, starting on "+currentTweetUserID);
					currentProfile.save();
					currentProfile.saveCSV();
					currentProfile = new SimpleProfile(currentTweetUserID);
					currentProfile.addTweet(currentTweet);
					continue;
				}
				
				//Else, we're simply on the same user's next tweet - so add it and carry on.
				currentProfile.addTweet(currentTweet);
			}
		} catch(IOException e) {
			System.err.println("Couldn't open dataset");
			e.printStackTrace();
		}

	}
}
