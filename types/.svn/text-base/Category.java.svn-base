package uk.ac.cam.ha293.tweetlabel.types;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Category implements Serializable {

	private static final long serialVersionUID = -3033274268834147218L;
	private String title;
	private Set<String> words;
	private int LIWCID;
		
	public Category(String title) {
		this.title = title;
		words = new HashSet<String>();
		LIWCID = -1;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Set<String> getWords() {
		return words;
	}
	
	public void addWord(String word) {
		words.add(word);
	}
	
	public int getLIWCID() {
		return LIWCID;
	}
	
	public void setLIWCID(int LIWCID) {
		this.LIWCID = LIWCID;
	}
	
	public void print() {
		System.out.println("Category: "+title);
		System.out.println("Words: ");
		for(String word : words) {
			System.out.print(word+" ");
		}
		System.out.println();
	}
}
