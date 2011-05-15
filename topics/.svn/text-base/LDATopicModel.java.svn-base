/*
 * Based on the algorithm and formulae presented in "Parameter Estimation for Text Analysis, Gregor Heinrich 2008"
 */

package uk.ac.cam.ha293.tweetlabel.topics;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ha293.tweetlabel.types.CategoryScore;
import uk.ac.cam.ha293.tweetlabel.types.Corpus;
import uk.ac.cam.ha293.tweetlabel.types.Document;
import uk.ac.cam.ha293.tweetlabel.types.WordScore;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

import cc.mallet.topics.ParallelTopicModel;

public class LDATopicModel implements Serializable {

	private static final long serialVersionUID = 7094157759480964151L;
	
	private Corpus corpus;
	private boolean hasRun;
	private int[][] documents;
	private int numTopics;
	private int numIterations;
	private int numSamplingIterations;
	private int numTotalIterations;
	private int samplingLag;
	private Map<String,Integer> wordIDs;
	private ArrayList<String> idLookup;
	private ArrayList<Document> docIDLookup;
	private int numWords;
	private int numDocs;
	private int numStats;
	private double alpha;
	private double beta;
	private int[][] wordTopicAssignments;
	private int[][] docTopicAssignments;
	private int[] numWordsAssignedToTopic;
	private int[] numWordsInDocument;
	private double[][] thetaSum;
	private double[][] phiSum;
	private boolean phiSumNormalised;
	private int[][] topicAssignments; //z
	private boolean printEachIteration;
	
	public LDATopicModel(Corpus corpus, int numTopics, int numIterations, int numSamplingIterations, int samplingLag, double alpha, double beta) {
		this.corpus = corpus;
		this.numTopics = numTopics;
		this.numIterations = numIterations;
		this.numSamplingIterations = numSamplingIterations;
		this.samplingLag = samplingLag;
		this.alpha = alpha;
		this.beta = beta;
		printEachIteration = false;
		numTotalIterations = numIterations;
		if(samplingLag > 0) numTotalIterations += numSamplingIterations * samplingLag;
		else numTotalIterations += numSamplingIterations;
		init();
	}
	
	public void printEachIteration() {
		printEachIteration = true;
	}
	
	//Here, we could remove the least used words instead of giving them an ID - lengths would need consideration
	public void init() {
		hasRun = false;
		Set<Document> documentSet = corpus.getDocuments();
		numDocs = documentSet.size();	
		documents = new int[numDocs][];
		numWordsInDocument = new int[numDocs];
		wordIDs = new HashMap<String,Integer>();
		idLookup = new ArrayList<String>();
		docIDLookup = new ArrayList<Document>();
		int docID = 0;
		for(Document document : documentSet) {
			docIDLookup.add(document);
			String[] tokens = document.getDocumentString().split("\\s+");
			documents[docID] = new int[tokens.length];
			numWordsInDocument[docID] = tokens.length;
			for(int i=0; i<documents[docID].length; i++) {
				//Add the token's ID to the documents array
				int wordID;
				if(wordIDs.containsKey(tokens[i])) { 
					wordID = wordIDs.get(tokens[i]);
				} else {
					wordID = wordIDs.keySet().size();
					wordIDs.put(tokens[i], wordID);
					idLookup.add(tokens[i]);
				}
				documents[docID][i] = wordID; 
			}
			docID++;
		}
		numWords = wordIDs.keySet().size();

		//Now for random initial topic assignment
		wordTopicAssignments = new int[numWords][numTopics];
		docTopicAssignments = new int[numDocs][numTopics];
		numWordsAssignedToTopic = new int[numTopics];
		topicAssignments = new int[numDocs][];
		for(int m=0; m<documents.length; m++) {		
			topicAssignments[m] = new int[documents[m].length];
			for(int n=0; n<documents[m].length; n++) {
				//Generate a random topic and update arrays
				int topicID = (int)(Math.random()*numTopics);
				topicAssignments[m][n] = topicID;
				wordTopicAssignments[documents[m][n]][topicID]++;
				docTopicAssignments[m][topicID]++;
				numWordsAssignedToTopic[topicID]++;	
			}
		}
		
		//Initialise topic and word distributions for later		
		thetaSum = new double[numDocs][numTopics];
		phiSum = new double[numTopics][numWords];
		phiSumNormalised = false;
		numStats = 0;
	}
	
	public void runGibbsSampling() {	
		hasRun = true;
		System.out.println("Starting Gibbs sampling");
		System.out.println("Documents: "+numDocs+" docs");
		System.out.println("Words: "+numWords+" unique words");
		System.out.println("Topics: "+numTopics+" topics");
		System.out.println("Iterations: "+numTotalIterations);
		
		for(int i=0; i<numTotalIterations; i++) {
			//if(i % 50 == 0) 
			System.out.println("Starting iteration "+i);
			for(int m=0; m<topicAssignments.length; m++) { //m is document index
				for(int n=0; n<topicAssignments[m].length; n++) { //n is word index
					//Get an updated topic sample for this word
					topicAssignments[m][n] = sampleTopic(m, n);
				}
			}
					
			if(i >= numIterations && (samplingLag == 0 || i % samplingLag == 0)) {
				updatePhiThetaSums();
				
				if(printEachIteration) {
					if(i % 20 == 0)	iterationPrintTopicsNew(5);
				}
				
				//try{System.in.read();}catch(IOException e){}
			}
		}
	}
	
	//Sample a new topic from the multinomial topic distribution
	private int sampleTopic(int m, int n) {
		//Removes this iteration's topicAssignment from the counting variables
		int topicID = topicAssignments[m][n]; //Get the currently assigned topic
		wordTopicAssignments[documents[m][n]][topicID]--; //Decrement the topic count for the current word
		docTopicAssignments[m][topicID]--; //Decrement the topic count for the current document
		numWordsAssignedToTopic[topicID]--; //Decrement the number of words the current topic has
		numWordsInDocument[m]--;
		
		//Cumulative multinomial sampling
		double[] p = new double[numTopics];
		for(int k=0; k<numTopics; k++) {
			p[k] = (wordTopicAssignments[documents[m][n]][k] + beta) / (numWordsAssignedToTopic[k] + numWords * beta) * (docTopicAssignments[m][k] + alpha) / (numWordsInDocument[m] + numTopics * alpha);
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
		
		//Finally, increment the relevant count variables
		wordTopicAssignments[documents[m][n]][sampledTopicID]++;
		docTopicAssignments[m][sampledTopicID]++;
		numWordsAssignedToTopic[sampledTopicID]++;
		numWordsInDocument[m]++;
		
		return sampledTopicID;
	}
	
	//By eqs 82 and 83 of paper mentioned in topmost comment
	private void updatePhiThetaSums() {
		
		for(int docID=0; docID<numDocs; docID++) {
			for(int topicID=0; topicID<numTopics; topicID++) {
				thetaSum[docID][topicID] += (docTopicAssignments[docID][topicID] + alpha) / (numWordsInDocument[docID] + numTopics * alpha);
			}
		}
		
		for(int topicID=0; topicID<numTopics; topicID++) {
			for(int wordID=0; wordID<numWords; wordID++) {
				phiSum[topicID][wordID] += (wordTopicAssignments[wordID][topicID] + beta) / (numWordsAssignedToTopic[topicID] + numWords * beta) ;
			}
		}
		numStats++; //Used if we want to take the mean of many stats
	}
	
	private double[][] getTheta() {
		double[][] theta = new double[numDocs][numTopics];
		for(int docID=0; docID<numDocs; docID++) {
			for(int topicID=0; topicID<numTopics; topicID++) {
				if(numStats > 0) theta[docID][topicID] = thetaSum[docID][topicID] / numStats; 
				else theta[docID][topicID] = (docTopicAssignments[docID][topicID] + alpha) / (numWordsInDocument[docID] + numTopics * alpha);
			}
		}
		return theta;
	}
	
	private List<Map<Integer,Double>> getThetaMap() {
		List<Map<Integer,Double>> docList = new LinkedList<Map<Integer,Double>>();
		for(int docID=0; docID<numDocs; docID++) {
			Map<Integer,Double> topicMap = new HashMap<Integer,Double>();
			for(int topicID=0; topicID<numTopics; topicID++) {
				topicMap.put(topicID, thetaSum[docID][topicID] / numStats);  
			}
			docList.add(Tools.sortMapByValue(topicMap));
		}
		return docList;
	}
	
	private double[][] getPhi() {
		double[][] phi = new double[numTopics][numWords];
		for(int topicID=0; topicID<numTopics; topicID++) {
			for(int wordID=0; wordID<numWords; wordID++) {
				if(numStats > 0) phi[topicID][wordID] = phiSum[topicID][wordID] / numStats; 
				else phi[topicID][wordID] += (wordTopicAssignments[wordID][topicID] + beta) / (numWordsAssignedToTopic[topicID] + numWords * beta) ;
			}
		}
		return phi;
	}
	
	private List<Map<Integer,Double>> getPhiMap() {
		List<Map<Integer,Double>> topicList = new LinkedList<Map<Integer,Double>>();
		for(int topicID=0; topicID<numTopics; topicID++) {
			Map<Integer,Double> wordMap = new HashMap<Integer,Double>();
			for(int wordID=0; wordID<numWords; wordID++) {
				wordMap.put(wordID, phiSum[topicID][wordID] / numStats);  
			}
			topicList.add(Tools.sortMapByValue(wordMap));
		}
		return topicList;
	}
	
	private double[][] normalisePhiSum() {
		if(phiSumNormalised) return phiSum;
		phiSumNormalised = true;
		for(int topicID=0; topicID<numTopics; topicID++) {
			for(int wordID=0; wordID<numWords; wordID++) {
				phiSum[topicID][wordID] /= numStats; 
			}
		}
		return phiSum;
	}
	
	public List<List<WordScore>> getTopics() {
		if(!hasRun) {
			System.err.println("Gibbs sampler has not yet run");
			return null;
		}
		
		List<List<WordScore>> topics = new LinkedList<List<WordScore>>();
		double[][] phi = getPhi();
		for(int topicID=0; topicID<numTopics; topicID++) {
			double[] wordProbs = phi[topicID];
			List<WordScore> wordScores = new LinkedList<WordScore>();
			for(int wordID=0; wordID<numWords; wordID++) {
				wordScores.add(new WordScore(idLookup.get(wordID), wordProbs[wordID]));
			}
			Collections.sort(wordScores);
			Collections.reverse(wordScores);
			topics.add(wordScores);
		}
		
		return topics;
	}
		
	public List<Map<Integer,Double>> getTopicsNew() {
		if(!hasRun) {
			System.err.println("Gibbs sampler has not yet run");
			return null;
		}
		
		List<Map<Integer,Double>> topics = new ArrayList<Map<Integer,Double>>();
		double[][] phi = normalisePhiSum();
		for(int topicID=0; topicID<numTopics; topicID++) {
			double[] wordProbs = phi[topicID];
			Map<Integer,Double> wordScores = new HashMap<Integer,Double>();
			for(int wordID=0; wordID<numWords; wordID++) {
				wordScores.put(wordID, wordProbs[wordID]);
			}
			Tools.sortMapByValueDesc(wordScores);
			topics.add(wordScores);
		}
		return topics;
	}
	
	public double[][] getTopicsUnsorted() {
		if(!hasRun) {
			System.err.println("Gibbs sampler has not yet run");
			return null;
		}
		
		double[][] phi = normalisePhiSum();
		return phi; //Hmmm...
	}
	
	public List<List<WordScore>> getDocuments() {
		if(!hasRun) {
			System.err.println("Gibbs sampler has not yet run");
			return null;
		}
		
		List<List<WordScore>> topics = new LinkedList<List<WordScore>>();
		double[][] theta = getTheta();
		for(int docID=0; docID<numDocs; docID++) {
			double[] topicProbs = theta[docID];
			List<WordScore> wordScores = new LinkedList<WordScore>();
			for(int topicID=0; topicID<numTopics; topicID++) {
				wordScores.add(new WordScore("Topic"+topicID, topicProbs[topicID]));
			}
			Collections.sort(wordScores);
			Collections.reverse(wordScores);
			topics.add(wordScores);
		}
		
		return topics;
	}
	
	public void printTopics(int topWords) {
		if(!hasRun) {
			System.err.println("Gibbs sampler has not yet run");
			return;
		}
		
		List<List<WordScore>> topics = getTopics();
		for(int k=0; k<numTopics; k++) {
			System.out.print("Topic "+k+":");
			List<WordScore> words = topics.get(k);
			for(int n=0; n<topWords; n++) {
				if(n == numWords) break; //incase topWords is huge or Corpus is tiny...
				//System.out.println(words.get(n).getWord()+" = "+words.get(n).getScore());
				System.out.print(words.get(n).getWord()+" ");
			}
			System.out.println();
		}
	}
	
	//Note - this is destructive as it calls normalisePhiSum
	public void printTopicsNew(int topWords) {
		if(!hasRun) {
			System.err.println("Gibbs sampler has not yet run");
			return;
		}
		
		double[][] phi;
		if(phiSumNormalised) phi = phiSum;
		else phi = normalisePhiSum();
		for(int topicID=0; topicID<numTopics; topicID++) {
			double[] wordProbs = phi[topicID];
			
			List<WordScore> wordScores = new LinkedList<WordScore>();
			for(int wordID=0; wordID<numWords; wordID++) {
				wordScores.add(new WordScore(idLookup.get(wordID), wordProbs[wordID]));
			}
			Collections.sort(wordScores);
			Collections.reverse(wordScores);
			
			System.out.print("Topic "+topicID+": ");
			int count=0;
			for(WordScore score : wordScores) {
				if(count==topWords) break;
				System.out.print(score.getWord()+" ");
				count++;
			}
			System.out.println();
		}
	}
	
	public boolean isPhiSumNormalised() {
		return phiSumNormalised;
	}
	
	private void iterationPrintTopics(int topWords) {
		List<List<WordScore>> topics = getTopics();
		for(int k=0; k<numTopics; k++) {
			System.out.print("Topic "+k+": ");
			List<WordScore> words = topics.get(k);
			for(int n=0; n<topWords; n++) {
				if(n == numWords) break; //incase topWords is huge or Corpus is tiny...
				System.out.print(words.get(n).getWord()+" ");
			}
			System.out.println();
		}
	}
	
	private void iterationPrintTopicsNew(int topWords) {
		List<Map<Integer,Double>> phiMap = getPhiMap();
		int topicID = 0;
		for(Map<Integer,Double> words : phiMap) {
			System.out.print("Topic "+topicID+": ");
			int count = 0;
			for(int wordID : words.keySet()) {
				if(count == topWords) break;
				System.out.print(idLookup.get(wordID)+" ");
				count++;
			}
			System.out.println();
			topicID++;
		}
	}
	
	public void printDocuments(int topTopics) {
		if(!hasRun) {
			System.err.println("Gibbs sampler has not yet run");
			return;
		}
		
		List<List<WordScore>> docs = getDocuments();
		for(int d=0; d<numDocs; d++) {
			System.out.println("Document "+d+":");
			List<WordScore> topics = docs.get(d);
			for(int n=0; n<topTopics; n++) {
				if(n == numWords) break;
				System.out.println(topics.get(n).getWord()+" = "+topics.get(n).getScore());
			}
			try {System.in.read();} catch (IOException e) {}
		}
	}
	
	public void printDocumentsVerbose(int topTopics) {
		if(!hasRun) {
			System.err.println("Gibbs sampler has not yet run");
			return;
		}
		
		List<List<WordScore>> docs = getDocuments();
		for(int d=0; d<numDocs; d++) {
			List<WordScore> topics = docs.get(d);
			System.out.print("Document "+d+": ");
			for(int n=0; n<documents[d].length; n++) {
				System.out.print(idLookup.get(documents[d][n])+"["+topicAssignments[d][n]+"] ");
			}
			System.out.println();
			for(int n=0; n<topTopics; n++) {
				if(n == numWords) break;
				System.out.println(topics.get(n).getWord()+" = "+topics.get(n).getScore());
			}
			try {System.in.read();} catch (IOException e) {}
		}
	}
	
	public void print() {
		//TODO
	}

    public void save(String name) {
		try {
			String filename = "models/lda/"+name+".model";
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			objectOut.close();
			System.out.println("Saved LDA topic model "+name);
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save LDA topic model "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't save LDA topic model "+name);
			e.printStackTrace();			
		}    	
    }
    
    public static LDATopicModel load(String name) {
		try {
			String filename = "models/lda/"+name+".model";
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			LDATopicModel model = (LDATopicModel)objectIn.readObject();
			objectIn.close();
			System.out.println("Loaded LDA topic model "+name);
			return model;
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't load LDA topic model "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't load LDA topic model "+name);
			e.printStackTrace();			
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't load LDA topic model "+name);
			e.printStackTrace();			
		}
		return null;
    }
    
    public long getDocIDFromIndex(int m) {
    	return docIDLookup.get(m).getId();
    }
    
    public Map<String,Integer> getVocab() {
    	return wordIDs;
    }
	
}
