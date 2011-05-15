package uk.ac.cam.ha293.tweetlabel.types;

import java.io.Serializable;
import java.util.Set;

public class Document implements Serializable {

	private static final long serialVersionUID = -3396519504581827961L;
	
	private long id; //ie twitter user id! duh
	private String documentString;
	private Set<String> topics;
	
	public Document() {
		documentString = new String();
		topics = null;
		id = -1;
	}
	
	public Document(String documentString) {
		this.documentString = documentString;
		topics = null;
	}
	
	public Document(String documentString, long id) {
		this.documentString = documentString;
		this.id = id;
		topics = null;
	}
	
	public Document(String documentString, long id, Set<String> topics) {
		this.documentString = documentString;
		this.id = id;
		this.topics = topics;
	}
	
	public String getDocumentString() {
		return documentString;
	}
	
	public Set<String> getTopics() {
		return topics;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setTopics(Set<String> topics) {
		this.topics = topics;
	}
	
	public void setDocumentString(String documentString) {
		this.documentString = documentString;
	}
	
	//TODO: A method for returning a word count vector based on an alphabet Map maybe?
		
	public void print() {
		System.out.println(documentString);
	}
	
}
