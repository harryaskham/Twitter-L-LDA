package uk.ac.cam.ha293.tweetlabel.topics;

import java.io.FileInputStream;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.ac.cam.ha293.tweetlabel.twitter.SimpleProfile;
import uk.ac.cam.ha293.tweetlabel.twitter.SimpleTweet;
import uk.ac.cam.ha293.tweetlabel.types.Document;
import uk.ac.cam.ha293.tweetlabel.util.Tools;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class LightweightLLDA implements Serializable {
	
	private static final long serialVersionUID = -7178136504829779391L;
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	transient private Map<String, Integer> wordIDs;
	@Persistent
	private ArrayList<String> topicLookup;
	@Persistent
	private int numTopics;
	@Persistent
	private int numWords;
	@Persistent
	private int[][] wordTopicAssignments;
	@Persistent
	private int[] numWordsAssignedToTopic;
	
	public LightweightLLDA(Map<String,Integer> wordIDs, ArrayList<String> topicLookup, int numTopics, int numWords, int[][] wordTopicAssignments, int[] numWordsAssignedToTopic) {
		this.wordIDs = wordIDs;
		this.topicLookup = topicLookup;
		this.numTopics = numTopics;
		this.numWords = numWords;
		this.wordTopicAssignments = wordTopicAssignments;
		this.numWordsAssignedToTopic = numWordsAssignedToTopic;
	}

	public Map<String,Double> inferTopicDistribution(SimpleProfile sp, int burnIn, int sampling, double alpha, double beta) {
    	//Get FV from SP
    	Document d = sp.asDocument();
		String[] tokens = d.getDocumentString().split("\\s+");
		ArrayList<Integer> fv = new ArrayList<Integer>();

		for(int i=0; i<tokens.length; i++) {
			if(wordIDs.containsKey(tokens[i])) { 
				fv.add(wordIDs.get(tokens[i]));
			}
		}
		
		//Run Gibbs Sampler again
		int[] z = new int[fv.size()];
		int[] zCounts = new int[numTopics];
		for(int n=0; n<z.length; n++) {
			//Random topic assignments
			z[n] = (int)(Math.random()*numTopics);
			zCounts[z[n]]++;
		}
		double[] thetam = new double[numTopics];
		for(int i=0; i<burnIn + sampling; i++) {
			System.out.print(".");
			for(int n=0; n<fv.size(); n++) {
				numWordsAssignedToTopic[z[n]]--;
				wordTopicAssignments[fv.get(n)][z[n]]--;
				//Cumulative multinomial sampling
				double[] p = new double[numTopics];
				for(int k=0; k<numTopics; k++) {
						p[k] = (wordTopicAssignments[fv.get(n)][k] + beta) / (numWordsAssignedToTopic[k] + numWords * beta) * (zCounts[k] + alpha) / (fv.size() + numTopics * alpha);
				}
				
				//Cumulative part - could be combined into above part cleverly
				for(int k=1; k<numTopics; k++) {
					p[k] += p[k-1];
				}
				
				//Sampling part - scaled because we haven't normalised
				double topicThreshold = Math.random() * p[numTopics-1];
				int sampledTopicID = 0;
				for(sampledTopicID=0; sampledTopicID<numTopics; sampledTopicID++) {
					if(topicThreshold < p[sampledTopicID]) {
						break;
					}
				}
				
				//Maybe a fix needed by faulty double arithmetic
				if(sampledTopicID >= numTopics) {
					sampledTopicID = numTopics-1;
				}
				
				zCounts[z[n]]--;
				z[n] = sampledTopicID;
				numWordsAssignedToTopic[z[n]]++;
				wordTopicAssignments[fv.get(n)][z[n]]++;
				zCounts[z[n]]++;
			}
			
			if(i >= burnIn) {
				for(int topicID=0; topicID<numTopics; topicID++) {
					thetam[topicID] += (zCounts[topicID] + alpha) / (fv.size() + numTopics * alpha);
				}
			}
		}
		System.out.println();
		//normalise theta and store
		Map<String,Double> results = new HashMap<String,Double>();
		for(int k=0; k<numTopics; k++) {
			thetam[k] /= sampling;
			results.put(topicLookup.get(k),thetam[k]);
		}
		
		return Tools.sortMapByValueDesc(results);
    }
	
    public void save(String name) {
		try {
			String filename = "models/lightweight/"+name+".model";
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			objectOut.close();
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save LLDA topic model "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't save LLDA topic model "+name);
			e.printStackTrace();			
		}    	
    }
    
    public static LightweightLLDA load(String path) {
		try {
			String filename = path;
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			LightweightLLDA model = (LightweightLLDA)objectIn.readObject();
			objectIn.close();
			System.out.println("Loaded LLDA topic model "+path);
			return model;
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't load LLDA topic model "+path);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't load LLDA topic model "+path);
			e.printStackTrace();			
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't load LLDA topic model "+path);
			e.printStackTrace();			
		}
		return null;
    }
    
    public void loadWordIDs(String path) {
		try {
			String filename = path;
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			wordIDs = (Map<String,Integer>)objectIn.readObject();
			objectIn.close();
			System.out.println("Loaded wordIDs");
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't load wordIDS");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't load wordIDS");
			e.printStackTrace();			
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't load wordIDS");
			e.printStackTrace();			
		}
    }
    
    public void printStats() {
    	System.out.println("num words = "+wordIDs.size()+" and "+numWords);
		System.out.println("num topics = "+topicLookup.size()+" and "+numTopics);
    }
    
    public void saveWordIDs(String name) {
		try {
			String filename = "models/lightweight/"+name+".wordids";
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(wordIDs);
			objectOut.close();
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save wordIDs");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't save wordIDs");
			e.printStackTrace();			
		}    	
    }
    
    public void helpMe() {
    	System.out.print("{");
    	for(int n=0; n<wordTopicAssignments.length; n++) {
    		System.out.print("{");
    		for(int k=0; k<wordTopicAssignments[n].length; k++) {
    			System.out.print(wordTopicAssignments[n][k]);
    			if(k < wordTopicAssignments[n].length-1) System.out.print(",");
    		}
    		System.out.print("}");
    		if(n < wordTopicAssignments.length - 1) System.out.print(",");
    	}
    }
    
    public void saveCSVComponents() {
    	//save wordTopicAssignments
		try {
	    	//save wordTopicAssignments
			{String filename = "calaisWordTopicAssignments.csv";
			FileOutputStream fileOut = new FileOutputStream(filename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			for(int v = 0; v<wordTopicAssignments.length; v++) {
				for(int k=0; k<wordTopicAssignments[v].length; k++) {
					writeOut.print(wordTopicAssignments[v][k]);
					if(k<wordTopicAssignments[v].length-1) writeOut.print(",");
				}
				writeOut.print("\n");
			}
			writeOut.close();
			fileOut.close();}
			
			//save wordIDs
			wordIDs = Tools.sortMapByValue(wordIDs);
			{String filename = "alchemyWordIDs.csv";
			FileOutputStream fileOut = new FileOutputStream(filename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			for(String word : wordIDs.keySet()) {
				writeOut.print(wordIDs.get(word)+","+word+"\n");
			}
			writeOut.close();
			fileOut.close();}
			
			//save wordIDs
			{String filename = "alchemyTopics.csv";
			FileOutputStream fileOut = new FileOutputStream(filename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			for(int i=0; i<topicLookup.size(); i++) {
				writeOut.print(i+","+topicLookup.get(i)+"\n");
			}
			writeOut.close();
			fileOut.close();}
			
			//save numWordsAssignedToTopic
			{String filename = "alchemyNumWordsAssignedToTopic.csv";
			FileOutputStream fileOut = new FileOutputStream(filename);
			PrintWriter writeOut = new PrintWriter(fileOut);
			for(int i=0; i<numWordsAssignedToTopic.length; i++) {
				writeOut.print(i+","+numWordsAssignedToTopic[i]+"\n");
			}
			writeOut.close();
			fileOut.close();}		
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void readOut() {
    	System.out.println(numWords+","+numTopics);
    }

}
