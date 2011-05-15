package uk.ac.cam.ha293.tweetlabel.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import uk.ac.cam.ha293.tweetlabel.twitter.SimpleProfile;
import uk.ac.cam.ha293.tweetlabel.twitter.SimpleTweet;
import uk.ac.cam.ha293.tweetlabel.types.Document;

public class AssociatedPress {
	
	private String datasetPath;
	private HashSet<String> documents;

	public AssociatedPress() {
		datasetPath = "dataset/ap/ap.txt";
		documents = new HashSet<String>();
		try {
			FileInputStream fileIn = new FileInputStream(datasetPath);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));
			while(true) {
				String nextLine = buffer.readLine();
				if(nextLine == null || nextLine.equals(" ")) break;
				if(nextLine.charAt(0) == '<' || nextLine.charAt(1) == '<') continue;
				documents.add(nextLine);
			}
		} catch (IOException e){
			System.err.println("An error occured");
		} 
	}
	
	public SimpleProfile asSimpleProfile() {
		SimpleProfile profile = new SimpleProfile(0);
		for(String document : documents) {
			profile.addTweet(new SimpleTweet(0,0,document));
		}
		return profile;
	}
}
