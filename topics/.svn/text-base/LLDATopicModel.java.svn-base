package uk.ac.cam.ha293.tweetlabel.topics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ha293.tweetlabel.twitter.SimpleProfile;
import uk.ac.cam.ha293.tweetlabel.types.Corpus;
import uk.ac.cam.ha293.tweetlabel.types.Document;
import uk.ac.cam.ha293.tweetlabel.types.WordScore;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class LLDATopicModel implements Serializable{

	private static final long serialVersionUID = 6251334729243904933L;
	
	String topicType;
	private Corpus corpus;
	private boolean hasRun;
	private int[][] documents;
	private int numTopics;
	private int numIterations;
	private int numSamplingIterations;
	private int numTotalIterations;
	private int samplingLag;
	private Map<String,Integer> wordIDs;
	private Map<String,Integer> topicIDs;
	private ArrayList<String> idLookup;
	private ArrayList<String> topicLookup;
	private ArrayList<Document> docIDLookup;
	private ArrayList<ArrayList<Integer>> docLabels;
	private int numWords;
	private int numDocs;
	private int numStats;
	private double alpha;
	private double beta;
	private int[][] wordTopicAssignments;
	private int[][] docTopicAssignments;
	private int[] numWordsAssignedToTopic;
	private int[] numWordsInDocument;
	private int[] numTopicsInDocument;
	private double[][] thetaSum;
	private double[][] phiSum;
	private boolean phiSumNormalised;
	private int[][] topicAssignments; //z
	private boolean printEachIteration;
	
	private int threadNum; //hacky hacky hacky
	public LLDATopicModel(Corpus corpus, int numIterations, int numSamplingIterations, int samplingLag, double alpha, double beta, int threadNum) {
		this.threadNum = threadNum;
		topicType = corpus.getTopicType();
		this.corpus = corpus;
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
	
	public LLDATopicModel(Corpus corpus, int numIterations, int numSamplingIterations, int samplingLag, double alpha, double beta) {
		topicType = corpus.getTopicType();
		this.corpus = corpus;
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
		
		//Remove all no-topic documents to avoid breaking LLDA - use an iterator to avoid exceptions when removing
		for(Iterator<Document> iter = documentSet.iterator(); iter.hasNext();) {
			Document document = iter.next();
			if(document.getTopics().isEmpty()) {
				iter.remove();
			}
		}
		
		numDocs = documentSet.size();	
		System.out.println("THREAD "+threadNum+": numDocs = "+numDocs);
		documents = new int[numDocs][];
		numWordsInDocument = new int[numDocs];
		numTopicsInDocument = new int[numDocs];
		wordIDs = new HashMap<String,Integer>();
		topicIDs = new HashMap<String,Integer>();
		idLookup = new ArrayList<String>();
		topicLookup = new ArrayList<String>();
		docIDLookup = new ArrayList<Document>();
		docLabels = new ArrayList<ArrayList<Integer>>();
		int docID = 0;
		for(Document document : documentSet) {
			docIDLookup.add(document);
			ArrayList<Integer> labels = new ArrayList<Integer>();
			for(String topic : document.getTopics()) {
				int topicID;
				if(topicIDs.containsKey(topic)) {
					topicID = topicIDs.get(topic); 
				} else {
					topicID = topicIDs.keySet().size();
					topicIDs.put(topic, topicID);
					topicLookup.add(topic);
				}
				labels.add(topicID);
			}
			docLabels.add(labels); //In correct position docID...
			numTopicsInDocument[docID] = document.getTopics().size();
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
		numTopics = topicIDs.keySet().size();
		
		//Now for random initial topic assignment
		wordTopicAssignments = new int[numWords][numTopics];
		docTopicAssignments = new int[numDocs][numTopics];
		numWordsAssignedToTopic = new int[numTopics];
		topicAssignments = new int[numDocs][];
		for(int m=0; m<documents.length; m++) {		
			topicAssignments[m] = new int[documents[m].length];
			for(int n=0; n<documents[m].length; n++) {
				//Generate a random topic and update arrays
				//NOTE: This is now constrained to only those topics in the document
				int topicIDIndex = (int)(Math.random()*numTopicsInDocument[m]);
				int topicID = -1;
				int labelCount = 0;
				for(Integer k : docLabels.get(m)) {
					if(labelCount == topicIDIndex) {
						topicID = k;
						break;
					} else {
						labelCount++;
					}
				}
				if(topicID == -1) {
					System.out.println("Something went wrong when choosing a random topic from the document's topic set - or, no topics");
					System.out.println(numTopicsInDocument[m]);
				}
				
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
		
		System.out.println("LLDA Topic Model successfully initialised");
	}
	
	public void runGibbsSampling() {	
		hasRun = true;
		System.out.println("Starting Gibbs sampling");
		System.out.println("Documents: "+numDocs+" docs");
		System.out.println("Words: "+numWords+" unique words");
		System.out.println("Topics: "+numTopics+" topics");
		for(int i=0; i<numTotalIterations; i++) {
			//if(i % 50 == 0) System.out.println("Starting iteration "+i);
			System.out.println("Starting iteration "+i);
			for(int m=0; m<topicAssignments.length; m++) { //m is document index
				for(int n=0; n<topicAssignments[m].length; n++) { //n is word index
					//Get an updated topic sample for this word
					//topicAssignments[m][n] = sampleTopicExperimental(m, n);
					topicAssignments[m][n] = sampleTopic(m, n);
					
				}
			}
			
			if(i >= numIterations && (samplingLag == 0 || i % samplingLag == 0)) {
				updatePhiThetaSums();
				
				if(printEachIteration) {
					iterationPrintTopics(10);
				}
			}
		}
	}
	
	public void runCVGibbsSampling(int startDoc, int endDoc) {	
		hasRun = true;
		int segmentSize = endDoc-startDoc+1;
		
		numDocs -= segmentSize;
		//need to get an updated numWords
		Set<Integer> tempWordSet = new HashSet<Integer>();
		for(int m=0; m<numDocs; m++) {
			if(m >=startDoc && m <= endDoc) {
				continue;
			}
			for(int n=0; n<numWordsInDocument[m]; n++) {
				tempWordSet.add(documents[m][n]);
			}
		}
		numWords = tempWordSet.size();
		
		System.out.println("Starting CV Gibbs sampling");
		System.out.println("Documents: "+numDocs+" docs");
		System.out.println("Words: "+numWords+" unique words");
		System.out.println("Topics: "+numTopics+" topics");
		System.out.println("CV Segment: "+segmentSize+" docs");

		
		for(int i=0; i<numIterations; i++) {
			//if(i % 50 == 0) System.out.println("Starting iteration "+i);
			System.out.println("Starting iteration "+i);
			for(int m=0; m<topicAssignments.length; m++) { //m is document index
				if(m >= startDoc && m <= endDoc) {
					//this is one of the segment docs - abort
					continue;
				}
				//otherwise, normal LLDA sampling please
				for(int n=0; n<topicAssignments[m].length; n++) { //n is word index
					//Get an updated topic sample for this word
					topicAssignments[m][n] = sampleTopic(m, n);
				}
			}
		}
		
		for(int m=startDoc; m<=endDoc; m++) {
			thetaSum[m] = getUnseenDocTheta(m);
		}
		
		saveOut(startDoc, endDoc, 0);
	}	
	
	private void saveOut(int startDoc, int endDoc, int reduction) {
		String dir = "";
		double dReduction = reduction/10.0;
		if(reduction == 0) {
			dir = "classifications/llda/"+topicType+"/"+numIterations+"-"+numSamplingIterations+"-"+alpha;
		} else if(reduction < 0) {
			dir = "classifications/fewertweets/"+(reduction*-1)+"/llda/"+topicType+"/"+numIterations+"-"+numSamplingIterations+"-"+alpha;
		} else {
			dir = "classifications/fewerprofiles/"+reduction+"/llda/"+topicType+"/"+numIterations+"-"+numSamplingIterations+"-"+alpha;
		}
		java.io.File dirFile = new java.io.File(dir);
		if(!dirFile.exists()) System.out.println(dirFile.mkdirs());
		
		for(int m=startDoc; m<=endDoc; m++) {
			long userID = docIDLookup.get(m).getId();
			try {
				FileOutputStream fileOut;
				fileOut = new FileOutputStream(dir+"/"+userID+".csv");
				PrintWriter writeOut = new PrintWriter(fileOut);
				writeOut.println("\"topic\",\"probability\"");
				for(int k=0; k<numTopics; k++) {
					writeOut.println(topicLookup.get(k)+","+thetaSum[m][k]);
				}
				writeOut.close();	
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Successfully saved LLDA classifications for "+topicType+" "+startDoc+"-"+endDoc);
	}
	
	public void runQuickCVGibbsSampling(int reduction) {
		hasRun = true;
		
		int segmentSize = numDocs/10; //emulates CV
		numDocs -= segmentSize;
		//need to get an updated numWords
		numWords *= 0.9;
		
		System.out.println("Starting CV Gibbs sampling");
		System.out.println("Documents: "+numDocs+" docs");
		System.out.println("Words: "+numWords+" unique words");
		System.out.println("Topics: "+numTopics+" topics");
		System.out.println("CV Segment: "+segmentSize+" docs");

		for(int i=0; i<numIterations; i++) {
			System.out.println("THREAD "+threadNum+": "+"Starting iteration "+i);
			for(int m=0; m<topicAssignments.length; m++) { //m is document index
				for(int n=0; n<topicAssignments[m].length; n++) { //n is word index
					//Get an updated topic sample for this word
					topicAssignments[m][n] = sampleTopic(m, n);
				}
			}
		}
		
		for(int m=0; m<topicAssignments.length; m++) {
			System.out.println("THREAD "+threadNum+": "+"Inferring for document "+m);
			thetaSum[m] = getUnseenDocTheta(m);
		}
		
		//take the values back to normal
		numDocs += segmentSize;
		numWords /= 0.9;
		saveOut(0, numDocs-1, reduction);
	}
	
	//Sample a new topic from the multinomial topic distribution
	private int sampleTopic(int m, int n) {
		//Removes this iteration's topicAssignment from the counting variables
		int topicID = topicAssignments[m][n]; //Get the currently assigned topic
		wordTopicAssignments[documents[m][n]][topicID]--; //Decrement the topic count for the current word
		docTopicAssignments[m][topicID]--; //Decrement the topic count for the current document
		numWordsAssignedToTopic[topicID]--; //Decrement the number of words the current topic has
		numWordsInDocument[m]--;
		
		//THIS BIT PUTS THE L IN LLDA - by restricting topics to only those in doc m, we get LLDA
		//Cumulative multinomial sampling
		Map<Integer,Double> p = new HashMap<Integer,Double>();
		for(int k : docLabels.get(m)) {
			double sample = (wordTopicAssignments[documents[m][n]][k] + beta) / (numWordsAssignedToTopic[k] + numWords * beta) * (docTopicAssignments[m][k] + alpha) / (numWordsInDocument[m] + numTopics * alpha);
			p.put(k, sample);
		}
		
		//Cumulative part - could be combined into above part cleverly
		double pSum = 0;
		for(int k : p.keySet()) {
			pSum += p.get(k);
		}

		//Sampling part - scaled because we haven't normalised
		double topicThreshold = Math.random() * pSum;
		double accumulator = 0.0; //used to see if the threshold lies in this mmultinomial segment
		int sampledTopicID = -1;
		for(int topic : p.keySet()) {
			accumulator += p.get(topic);
			if(topicThreshold < accumulator) {
				sampledTopicID = topic;
				break;
			}
		}
		
		//Maybe a fix needed by faulty double arithmetic
		if(sampledTopicID == -1) {
			System.err.println("Couldn't sample a topic, scaled sample failed");
		}
		
		//Finally, increment the relevant count variables
		wordTopicAssignments[documents[m][n]][sampledTopicID]++;
		docTopicAssignments[m][sampledTopicID]++;
		numWordsAssignedToTopic[sampledTopicID]++;
		numWordsInDocument[m]++;
		
		return sampledTopicID;
	}
	
	//like sampleTopic, but doesn't sample from one segment
	private int sampleTopicCV(int m, int n) {
		//Removes this iteration's topicAssignment from the counting variables
		int topicID = topicAssignments[m][n]; //Get the currently assigned topic
		wordTopicAssignments[documents[m][n]][topicID]--; //Decrement the topic count for the current word
		docTopicAssignments[m][topicID]--; //Decrement the topic count for the current document
		numWordsAssignedToTopic[topicID]--; //Decrement the number of words the current topic has
		numWordsInDocument[m]--;
		
		//Note: we allow sampling from everything now...
		//Cumulative multinomial sampling
		Map<Integer,Double> p = new HashMap<Integer,Double>();
		for(int k=0; k<numTopics; k++) {
			double sample = (wordTopicAssignments[documents[m][n]][k] + beta) / (numWordsAssignedToTopic[k] + numWords * beta) * (docTopicAssignments[m][k] + alpha) / (numWordsInDocument[m] + numTopics * alpha);
			p.put(k, sample);
		}
		
		//Cumulative part - could be combined into above part cleverly
		double pSum = 0;
		for(int k : p.keySet()) {
			pSum += p.get(k);
		}

		//Sampling part - scaled because we haven't normalised
		double topicThreshold = Math.random() * pSum;
		double accumulator = 0.0; //used to see if the threshold lies in this mmultinomial segment
		int sampledTopicID = -1;
		for(int topic : p.keySet()) {
			accumulator += p.get(topic);
			if(topicThreshold < accumulator) {
				sampledTopicID = topic;
				break;
			}
		}
		
		//Maybe a fix needed by faulty double arithmetic
		if(sampledTopicID == -1) {
			System.err.println("Couldn't sample a topic, scaled sample failed");
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
	
	//used for unseen inference
	private double[] getUnseenDocTheta(int m) {
		double[] topicDistribution = new double[numTopics];
		
		//model is complete, so we only have to sample over the unseen document
		for(int i=0; i<numSamplingIterations; i++) {
			//iterate over all words in the document
			for(int n=0; n<numWordsInDocument[m]; n++) {
				topicAssignments[m][n] = sampleTopicCV(m,n);
			}
			
			//now take a sample
			for(int topicID=0; topicID<numTopics; topicID++) {
				topicDistribution[topicID] += (docTopicAssignments[m][topicID] + alpha) / (numWordsInDocument[m] + numTopics * alpha);
			}
		}
		
		//now to normalise over samples
		for(int topicID=0; topicID<numTopics; topicID++) {
			topicDistribution[topicID] /= numSamplingIterations;
		}
		
		return topicDistribution;
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
	
	public void printTopicsNew(int topWords) {
		if(!hasRun) {
			System.err.println("Gibbs sampler has not yet run");
			return;
		}
		
		double[][] phi = normalisePhiSum();
		for(int topicID=0; topicID<numTopics; topicID++) {
			double[] wordProbs = phi[topicID];
			
			List<WordScore> wordScores = new LinkedList<WordScore>();
			for(int wordID=0; wordID<numWords; wordID++) {
				wordScores.add(new WordScore(idLookup.get(wordID), wordProbs[wordID]));
			}
			Collections.sort(wordScores);
			Collections.reverse(wordScores);
			
			System.out.print("Topic "+topicLookup.get(topicID)+": ");
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
				wordScores.add(new WordScore(topicLookup.get(topicID), topicProbs[topicID]));
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
			System.out.println("Topic "+topicLookup.get(k)+":");
			List<WordScore> words = topics.get(k);
			for(int n=0; n<topWords; n++) {
				if(n == numWords) break; //incase topWords is huge or Corpus is tiny...
				System.out.println(words.get(n).getWord()+" = "+words.get(n).getScore());
			}
			try {System.in.read();} catch (IOException e) {}
		}
	}
	
	private void iterationPrintTopics(int topWords) {
		List<List<WordScore>> topics = getTopics();
		for(int k=0; k<numTopics; k++) {
			System.out.print("Topic "+topicLookup.get(k)+": ");
			List<WordScore> words = topics.get(k);
			for(int n=0; n<topWords; n++) {
				if(n == numWords) break; //incase topWords is huge or Corpus is tiny...
				System.out.print(words.get(n).getWord()+"(");
				Tools.dpPrint(words.get(n).getScore(),3);
				System.out.print(") ");
			}
			System.out.println();
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
			System.out.print("Document "+d+" (uid: "+docIDLookup.get(d).getId()+"): ");
			for(int n=0; n<documents[d].length; n++) {
				System.out.print(idLookup.get(documents[d][n])+"["+topicLookup.get(topicAssignments[d][n])+"] ");
			}
			System.out.println();
			
			System.out.print("Initial topic set: {");
			for(int n=0; n<docLabels.get(d).size(); n++) {	
				System.out.print(topicLookup.get(docLabels.get(d).get(n)));
				if(docLabels.get(d).size() > 1 && n < docLabels.get(d).size()-1) System.out.print(", ");
			}
			System.out.println("}");
			
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
			String filename = "models/llda/"+topicType+"/"+name+".model";
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			objectOut.close();
			System.out.println("Saved LLDA topic model "+name);
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save LLDA topic model "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't save LLDA topic model "+name);
			e.printStackTrace();			
		}    	
    }
    
    public static LLDATopicModel load(String topicType, String name) {
		try {
			String filename = "models/llda/"+topicType+"/"+name+".model";
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			LLDATopicModel model = (LLDATopicModel)objectIn.readObject();
			objectIn.close();
			System.out.println("Loaded LLDA topic model "+name);
			return model;
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't load LLDA topic model "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't load LLDA topic model "+name);
			e.printStackTrace();			
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't load LLDA topic model "+name);
			e.printStackTrace();			
		}
		return null;
    }
    
    public static LLDATopicModel loadFromPath(String topicType, String path) {
		try {
			String filename = path;
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			LLDATopicModel model = (LLDATopicModel)objectIn.readObject();
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
    
    public long getDocIDFromIndex(int m) {
    	return docIDLookup.get(m).getId();
    }
    
    public Map<String,Integer> getVocab() {
    	return wordIDs;
    }
    
    public ArrayList<String> getTopicsIDList() {
    	return topicLookup;
    }

    public Map<String,Double> inferTopicDistribution(SimpleProfile sp, int burnIn, int sampling, double alpha, double beta) {
    	//Get FV from SP
    	Document d = sp.asDocument();
		String[] tokens = d.getDocumentString().split("\\s+");
		ArrayList<Integer> fv = new ArrayList<Integer>();
		int numExistingWords = 0;
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
    
    public LightweightLLDA asLightweightLLDA() {
    	LightweightLLDA lllda = new LightweightLLDA(wordIDs, topicLookup, numTopics, numWords, wordTopicAssignments, numWordsAssignedToTopic);
    	return lllda;
    }
    
    public void printStats() {
    	System.out.println("num words = "+wordIDs.size()+" and "+numWords);
		System.out.println("num topics = "+topicLookup.size()+" and "+numTopics);
    }
}