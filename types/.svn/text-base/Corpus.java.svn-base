package uk.ac.cam.ha293.tweetlabel.types;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.cam.ha293.tweetlabel.twitter.Profiler;
import uk.ac.cam.ha293.tweetlabel.twitter.SimpleProfile;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class Corpus implements Serializable {

	private static final long serialVersionUID = 2305142967278892458L;

	private Set<Document> documents;
	private String topicType;
	
	public Corpus() {
		documents = new HashSet<Document>();
		topicType = null;
	}
	
	public Corpus(Set<Document> documents) {
		this.documents = documents;
		topicType = null;
	}
	
	public Corpus(String topicType) {
		documents = new HashSet<Document>();
		this.topicType = topicType;
	}
	
	public Corpus(Set<Document> documents, String topicType) {
		this.documents = documents;
		this.topicType = topicType;
	}
	
	public Set<Document> getDocuments() {
		return documents;
	}
	
	public String getTopicType() {
		return topicType;
	}
	
	public List<Document> asDocumentList() {
		return new ArrayList<Document>(documents);
	}
	
	public void addDocument(Document document) {
		documents.add(document);
	}

    public void print() {
    	System.out.println("Printing a Corpus - Documents:");
    	for(Document document : documents) {
    		document.print();
    		System.out.println();
    	}
    }
    
    public void removeLeastCommonWords(double percentage, double numDocThreshold) {
    	Set<Document> newDocuments = new HashSet<Document>();
    	
    	//Discover the frequencies of each word
    	Map<String,Integer> frequencyCounts = new HashMap<String,Integer>();
    	Map<String,Integer> numDocsContaining = new HashMap<String,Integer>();
    	for(Document doc : documents) {
    		Set<String> wordsFoundInDoc = new HashSet<String>();
    		for(String word : doc.getDocumentString().split("\\s+")) {
    			if(frequencyCounts.containsKey(word)) {
    				frequencyCounts.put(word, frequencyCounts.get(word)+1);
    			} else {
    				frequencyCounts.put(word, 1);
    			}
    			
    			//If it's the first time we've seen the word in this doc, increase doc-containing count
    			if(!wordsFoundInDoc.contains(word)) {
    				wordsFoundInDoc.add(word);
        			if(numDocsContaining.containsKey(word)) {
        				numDocsContaining.put(word, numDocsContaining.get(word)+1);
        			} else {
        				numDocsContaining.put(word, 1);
        			}
    			}
    		}
    	}
    	
    	//Sort them and make a set of the least-used words
    	frequencyCounts = Tools.sortMapByValue(frequencyCounts);
    	numDocsContaining = Tools.sortMapByValue(numDocsContaining);
    	System.out.println("Initial vocab size = "+frequencyCounts.keySet().size());
    	int numToRemove = (int)((frequencyCounts.keySet().size()*percentage) / 100.0);
    	System.out.println("Removing the "+numToRemove+" least-used words");
    	int count = 0;
    	Set<String> toRemove = new HashSet<String>();
    	for(String word : frequencyCounts.keySet()) {
    		if(count >= numToRemove) break;
    		toRemove.add(word);
    		//System.out.println("Added "+word+" to the least-common set");
    		count++;
    	}
    	
    	System.out.println("Removing words that appear in "+numDocThreshold+" documents or fewer");
    	int numDocCount=0;
    	for(String word : frequencyCounts.keySet()) {
    		if(numDocsContaining.get(word) > numDocThreshold) break;
    		toRemove.add(word);
    		numDocCount++;
    		//System.out.println("Added "+word+" to the too-few-documents set");
    	}
    	System.out.println(numDocCount+" such profiles");

    	//Create a new document set and remove these words from it
    	for(Document doc : documents) {
    		Document newDoc = new Document();
;    		newDoc.setId(doc.getId());
    		newDoc.setTopics(doc.getTopics());
    		String newDocumentString = "";
    		for(String word : doc.getDocumentString().split("\\s+")) {
    			if(toRemove.contains(word)) continue;
    			else newDocumentString += (word+" ");
    		}
    		//if(newDocumentString.isEmpty()) continue; //allowing empty documents...
    		newDoc.setDocumentString(newDocumentString);
    		newDocuments.add(newDoc);
    	}
    	
    	//Finally, update the document set pointer
    	documents = newDocuments;
    }
    
    public static Corpus getFullProfileCorpus(String topicType, int reduction) {
    	Corpus corpus = new Corpus(topicType);
    	int count = 0;
    	for(Long userID : Tools.getCSVUserIDs()) {
    		if(count % 50 == 0) System.out.println("Added "+count+" profiles to Corpus");
    		count++;
    		SimpleProfile profile = Profiler.loadCSVProfile(userID);
    		profile.reduceBy(reduction);
    		corpus.addDocument(profile.asDocument(topicType));
    	}
    	return corpus;	
    }
    
    public static Corpus getFullProfileCorpus(String topicType) {
    	Corpus corpus = new Corpus(topicType);
    	int count = 0;
    	for(Long userID : Tools.getCSVUserIDs()) {
    		if(count % 50 == 0) System.out.println("Added "+count+" profiles to Corpus");
    		count++;
    		SimpleProfile profile = Profiler.loadCSVProfile(userID);
    		corpus.addDocument(profile.asDocument(topicType));
    	}
    	return corpus;
    }
    
    public static Corpus getFullProfileCorpus() {
    	return getFullProfileCorpus(null);
    }
    
    public void save(String name) {
		try {
			String filename;
			if(topicType != null) {
				filename = "corpora/"+topicType+"/"+name+".corpus";	
			} else {
				filename = "corpora/"+name+".corpus";
			}
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			objectOut.close();
			System.out.println("Saved corpus "+name);
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save corpus "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't save corpus "+name);
			e.printStackTrace();			
		}    	
    }
    
    public static Corpus load(String name) {
		try {
			String filename = "corpora/"+name+".corpus";
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			Corpus corpus = (Corpus)objectIn.readObject();
			objectIn.close();
			System.out.println("Loaded corpus "+name);
			return corpus;
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't load corpus "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't load corpus "+name);
			e.printStackTrace();			
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't load corpus "+name);
			e.printStackTrace();			
		}
		return null;
    }
    
    public static Corpus loadLabelled(String topicType,String name) {
		try {
			String filename = "corpora/"+topicType+"/"+name+".corpus";
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			Corpus corpus = (Corpus)objectIn.readObject();
			objectIn.close();
			System.out.println("Loaded corpus "+name);
			return corpus;
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't load corpus "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't load corpus "+name);
			e.printStackTrace();			
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't load corpus "+name);
			e.printStackTrace();			
		}
		return null;
    }
    
    public int size() {
    	return documents.size();
    }
    
    public Corpus[] split(Set<Long> ids1, Set<Long> ids2) {
    	Corpus c[] = new Corpus[2];
    	c[0] = new Corpus(topicType);
    	c[1] = new Corpus(topicType);
    	for(Document document : documents) {
    		if(ids1.contains(document.getId())) {
    			c[0].addDocument(document);
    		} else if(ids2.contains(document.getId())) {
    			c[1].addDocument(document);
    		}
    	}
    	return c;
    }
    
    public Corpus randomlyRemove(double fraction) {
    	if(fraction > 1.0) return this;
    	int toKeep = (int)(documents.size()*(1.0-fraction));
    	Corpus newCorpus = new Corpus(topicType);
    	List<Document> oldDocumentsList = new ArrayList<Document>(documents);
    	Collections.shuffle(oldDocumentsList);
    	for(int i=0; i<toKeep; i++) {
        	newCorpus.addDocument(oldDocumentsList.get(i));
    	}
    	return newCorpus;
    }
	
}
