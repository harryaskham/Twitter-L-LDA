package uk.ac.cam.ha293.tweetlabel.topics;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scala.actors.threadpool.Arrays;
import uk.ac.cam.ha293.tweetlabel.classify.FullAlchemyClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullCalaisClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullTextwiseClassification;
import uk.ac.cam.ha293.tweetlabel.eval.SVMTest;
import uk.ac.cam.ha293.tweetlabel.types.Corpus;
import uk.ac.cam.ha293.tweetlabel.types.Document;
import uk.ac.cam.ha293.tweetlabel.types.Pair;
import uk.ac.cam.ha293.tweetlabel.util.Tools;
import jnisvmlight.*;

public class SVMTopicModel implements Serializable{
	
	private static final long serialVersionUID = -2519797518060230902L;
	private Corpus corpus;
	private int[][] documents;
	private int numDocs;
	private int numTopics;
	private Map<String,Integer> wordIDs;
	private Map<String,Integer> topicIDs;
	private ArrayList<String> idLookup;
	private ArrayList<Document> docIDLookup;
	private ArrayList<String> topicIDLookup;
	private ArrayList<String> topTopics;
	private Map<String,Set<Integer>> topicIDSets;
	private ArrayList<Map<Integer,Integer>> featureCountsArray;
	private SVMLightModel[] svmModels;
	private long kernelType;
	private long kernelParam;
	private double c;
	private boolean verbose = false;
	private String topicType;
	
	public SVMTopicModel(Corpus corpus, String topicType, long kernelType, long kernelParam, double c) {
		System.out.println("Creating one-vs-all SVM models...");	
		//creating feature vectors and token lookups
		this.topicType = topicType;
		this.corpus = corpus;
		Set<Document> documentSet = corpus.getDocuments();
		numDocs = documentSet.size();	
		documents = new int[numDocs][];
		wordIDs = new HashMap<String,Integer>();
		topicIDs = new HashMap<String,Integer>();
		idLookup = new ArrayList<String>();
		docIDLookup = new ArrayList<Document>();
		topicIDSets = new HashMap<String,Set<Integer>>();
		this.kernelType = kernelType;
		this.kernelParam = kernelParam;
		this.c = c;
		int docID = 0;
		for(Document document : documentSet) {
			docIDLookup.add(document);
			String[] tokens = document.getDocumentString().split("\\s+");
			documents[docID] = new int[tokens.length];
			for(int i=0; i<documents[docID].length; i++) {
				//Add the token's ID to the documents array
				int wordID;
				if(wordIDs.containsKey(tokens[i])) { 
					wordID = wordIDs.get(tokens[i]);
				} else {
					wordID = wordIDs.keySet().size();
					wordIDs.put(tokens[i], wordID);
					idLookup.add(tokens[i]);
					if(verbose) System.out.println("Assigned word "+tokens[i]+" to id "+wordID);
				}
				documents[docID][i] = wordID; 
			}
			docID++;
		}
		
		int topicID = 0;
		topicIDLookup = new ArrayList<String>();
		for(String topic : Tools.getTopics(topicType)) {
			topicIDs.put(topic,topicID);
			if(verbose) System.out.println("Assigned topic "+topic+" to id "+topicID);
			topicID++;
			topicIDLookup.add(topic);
		}
		numTopics = topicIDs.size();
		svmModels = new SVMLightModel[numTopics];
		
		//Now need to work out which documents are positive for which SVMs
		//need a mapping between documents and toptopics
		topTopics = new ArrayList<String>();
		for(int i=0; i<numDocs; i++) {
			String topTopic = "";
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification cl = new FullAlchemyClassification(docIDLookup.get(i).getId());
				if(cl.getCategorySet().size()==0) topTopic="NO_TOP_TOPIC";
				else topTopic = cl.getCategorySet().toArray(new String[0])[0];
			} else if(topicType.equals("calais")) {
				FullCalaisClassification cl = new FullCalaisClassification(docIDLookup.get(i).getId());
				if(cl.getCategorySet().size()==0) topTopic="NO_TOP_TOPIC";
				else topTopic = cl.getCategorySet().toArray(new String[0])[0];
				//Required because of Calais' stupid classification system
				if(topTopic.equals("Other")) {
					if(cl.getCategorySet().size()==1) topTopic="NO_TOP_TOPIC";
					else topTopic = cl.getCategorySet().toArray(new String[0])[1]; 
				}
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification cl = new FullTextwiseClassification(docIDLookup.get(i).getId(),true);
				if(cl.getCategorySet().size()==0) topTopic="NO_TOP_TOPIC";
				else topTopic = cl.getCategorySet().toArray(new String[0])[0];
			}
			if(verbose) System.out.println("Document "+i+" found to have top topic "+topTopic+", id "+topicIDs.get(topTopic));
			topTopics.add(topTopic);
			
			//add this id to the topTopics map
			if(topicIDSets.containsKey(topTopics)) {
				topicIDSets.get(topTopic).add(i);
			} else {
				Set<Integer> newSet = new HashSet<Integer>();
				newSet.add(i);
				topicIDSets.put(topTopic, newSet);
			}
		}
		
		featureCountsArray = new ArrayList<Map<Integer,Integer>>();
		//create the mappings from features to counts for each document;
		for(int n=0; n<numDocs; n++) {
			//create a map, token IDs to token counts
			Map<Integer,Integer> featureCounts = new HashMap<Integer,Integer>();
			for(Integer m=0; m<documents[n].length; m++) {
				int word = documents[n][m];
				if(featureCounts.containsKey(word)) {
					featureCounts.put(word, featureCounts.get(word)+1);
				} else {
					featureCounts.put(word, 1);
				}
			}
			featureCountsArray.add(featureCounts);
		}
		
		//now generate K svms, each with K feature vectors - +1 for the topic it's on about, and -1 for all others
		//NOTE: this isn't CV - need to remove the relevant documents at the start of the constructor for CV
		for(int k=0; k<numTopics; k++) {
			System.out.println("Training the +1 SVM for "+topicIDLookup.get(k));
			SVMLightInterface svmLightInterface = new SVMLightInterface();
			TrainingParameters trainingParameters = new TrainingParameters();
			trainingParameters.getLearningParameters().verbosity = 1;
			
			//Set SVM parameters proper
			trainingParameters.getKernelParameters().kernel_type = kernelType;
			trainingParameters.getKernelParameters().coef_lin = kernelParam;
			trainingParameters.getKernelParameters().coef_const = kernelParam;
			trainingParameters.getKernelParameters().poly_degree = kernelParam;
			trainingParameters.getKernelParameters().rbf_gamma = kernelParam;
			trainingParameters.getLearningParameters().svm_c = c;
			
			LabeledFeatureVector[] trainingData = new LabeledFeatureVector[numDocs];
			for(int i=0; i<numDocs; i++) {
				Map<Integer,Integer> featureCounts = featureCountsArray.get(i);
				int uniqueTerms = featureCounts.size();
				int[] dims = new int[uniqueTerms];
				double[] vals = new double[uniqueTerms];
			    Integer[] presentFeatures = featureCounts.keySet().toArray(new Integer[1]);
			    Arrays.sort(presentFeatures);
			    for(int j=0; j<presentFeatures.length; j++) {
			    	dims[j] = presentFeatures[j]+1; //NOTE: avoids 0-feature error. remember this hack!
			    	vals[j] = featureCounts.get(presentFeatures[j]);
			    }
				
			    
			    if(topTopics.get(i).equals(topicIDLookup.get(k))) {
			    	//we're currently dealing with a document whose top topic corresponds to K - positive classification
			    	if(verbose) System.out.println("Document "+i+" has topTopic "+topTopics.get(i)+", matches "+k+" so training positively");
					trainingData[i] = new LabeledFeatureVector(+1,dims,vals);			    	
			    } else {
			    	if(verbose) System.out.println("Document "+i+" has topTopic "+topTopics.get(i)+", doesnt match "+k+" so training negatively");
					trainingData[i] = new LabeledFeatureVector(-1,dims,vals);			    	
			    }

				trainingData[i].normalizeL2();
			}

			System.out.println("Preamble complete, now training model");
			svmModels[k] = svmLightInterface.trainModel(trainingData,trainingParameters);
		}

	}
	
	public Map<Long,Pair<String,Double>> classify(Corpus c) {
		Map<Long,Pair<String,Double>> results = new HashMap<Long,Pair<String,Double>>();
		//convert each document to a feature vector
		Set<Document> documentSet = c.getDocuments();
		for(Document document : documentSet) {
			System.out.println("Classifying document "+document.getId());
			Map<Integer,Integer> featureCounts = new HashMap<Integer,Integer>();
			String[] split = document.getDocumentString().split("\\s+");
			for(String token : split) {
				if(wordIDs.containsKey(token)) {
					int wordID = wordIDs.get(token);
					if(featureCounts.containsKey(wordID)) {
						featureCounts.put(wordID,featureCounts.get(wordID)+1);
					} else {
						featureCounts.put(wordID,1);
					}
				}
				//otherwise, we haven't seen the word in training, so we can ignore it?
				//TODO: maybe add smoothing
			}
			int[] dims = new int[featureCounts.size()];
			double[] vals = new double[featureCounts.size()];
			Integer[] features = featureCounts.keySet().toArray(new Integer[1]);
			Arrays.sort(features);
			for(int i=0; i<features.length; i++) {
				dims[i]=features[i]+1; //phew, remembered the hack!
				vals[i]=featureCounts.get(features[i]);
			}
			FeatureVector fv = new FeatureVector(dims,vals);
			
			double highestScore = Double.NEGATIVE_INFINITY;
			int bestTopic = -1;
			//Now classify the feature vector using each of the K svm
			
			for(int k=0; k<svmModels.length; k++) {
				if(verbose) System.out.println("Classifying with SVM for "+topicIDLookup.get(k));
				double classification = svmModels[k].classify(fv);
				if(classification > highestScore) {
					highestScore = classification;
					bestTopic = k;
				}
				if(verbose) System.out.println("Classification = "+classification);
			}
			if(verbose) System.out.println("best topic fit found to be "+bestTopic+" "+topicIDLookup.get(bestTopic));
			results.put(document.getId(), new Pair<String,Double>(topicIDLookup.get(bestTopic),highestScore));
		}
		
		return results;
	}
	
	public Map<Long,Map<String,Double>> classifyFull(Corpus c) {
		Map<Long,Map<String,Double>> results = new HashMap<Long,Map<String,Double>>();
		//convert each document to a feature vector
		Set<Document> documentSet = c.getDocuments();
		for(Document document : documentSet) {
			System.out.println("Classifying document "+document.getId());
			Map<Integer,Integer> featureCounts = new HashMap<Integer,Integer>();
			String[] split = document.getDocumentString().split("\\s+");
			for(String token : split) {
				if(wordIDs.containsKey(token)) {
					int wordID = wordIDs.get(token);
					if(featureCounts.containsKey(wordID)) {
						featureCounts.put(wordID,featureCounts.get(wordID)+1);
					} else {
						featureCounts.put(wordID,1);
					}
				}
				//otherwise, we haven't seen the word in training, so we can ignore it?
				//TODO: maybe add smoothing
			}
			int[] dims = new int[featureCounts.size()];
			double[] vals = new double[featureCounts.size()];
			Integer[] features = featureCounts.keySet().toArray(new Integer[1]);
			Arrays.sort(features);
			for(int i=0; i<features.length; i++) {
				dims[i]=features[i]+1; //phew, remembered the hack!
				vals[i]=featureCounts.get(features[i]);
			}
			FeatureVector fv = new FeatureVector(dims,vals);
			
			results.put(document.getId(), new HashMap<String,Double>());
			//Now classify the feature vector using each of the K svm
			for(int k=0; k<svmModels.length; k++) {
				if(verbose) System.out.println("Classifying with SVM for "+topicIDLookup.get(k));
				double classification = svmModels[k].classify(fv);
				results.get(document.getId()).put(topicIDLookup.get(k), classification);
				if(verbose) System.out.println("Classification = "+classification);
			}
		}
		
		return results;
	}
	
	public static void runCVInference(Corpus corpus, String topicType, long kernelType, long kernelParam, double c) {
		Long[] uids = Tools.getCSVUserIDs().toArray(new Long[1]);
		//Create segments
		int[] segments = {0,251,502,753,1004,1255,1506,1756,2006,2256,2506};
		try {
			FileOutputStream fileOut = new FileOutputStream("classifications/svm/"+topicType+".csv");
			PrintWriter writeOut = new PrintWriter(fileOut);
			writeOut.println("\"uid\",\"topTopic\",\"score\"");
			for(int segment=0; segment<segments.length-1; segment++) {
				System.out.println("Cross validation: Dealing with profiles "+segments[segment]+" to "+segments[segment+1]);
				//need to generate two corpora, one training, one testing
				Set<Long> testingSet = new HashSet<Long>();
				Set<Long> trainingSet = new HashSet<Long>();
				for(int i=0; i<uids.length; i++) {
					if(i>=segments[segment] && i < segments[segment+1]) {
						testingSet.add(uids[i]);
					} else {
						trainingSet.add(uids[i]);
					}
				}
				Corpus[] corpusSplit = corpus.split(trainingSet, testingSet);
				Corpus training = corpusSplit[0];
				Corpus testing = corpusSplit[1];
				System.out.println(training.size()+" profiles in the training set");
				System.out.println(testing.size()+" profiles in the testing set");
				System.out.println("Training SVM...");
				SVMTopicModel svm = new SVMTopicModel(training,topicType,kernelType,kernelParam,c);
				System.out.println("SVM Trained");
				
				//now use svm to infer topics for corpus testing
				System.out.println("Inferring for testing set...");
				Map<Long,Pair<String,Double>> classifications = svm.classify(testing);
				System.out.println("Inference procedure completed");
				for(Long uid : classifications.keySet()) {
					System.out.println(uid+","+classifications.get(uid).item1()+","+classifications.get(uid).item2());
					writeOut.println(uid+","+classifications.get(uid).item1()+","+classifications.get(uid).item2());
				}
				System.out.println("Done outputting results for segment");
			}
			writeOut.close();
			fileOut.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
	}
	
	public static void runCVInferenceFull(Corpus corpus, String topicType, long kernelType, long kernelParam, double c) {
		Long[] uids = Tools.getCSVUserIDs().toArray(new Long[1]);
		//Create segments
		int[] segments = {0,251,502,753,1004,1255,1506,1756,2006,2256,2506};
		try {
			for(int segment=0; segment<segments.length-1; segment++) {
				System.out.println("Cross validation: Dealing with profiles "+segments[segment]+" to "+segments[segment+1]);
				//need to generate two corpora, one training, one testing
				Set<Long> testingSet = new HashSet<Long>();
				Set<Long> trainingSet = new HashSet<Long>();
				for(int i=0; i<uids.length; i++) {
					if(i>=segments[segment] && i < segments[segment+1]) {
						testingSet.add(uids[i]);
					} else {
						trainingSet.add(uids[i]);
					}
				}
				Corpus[] corpusSplit = corpus.split(trainingSet, testingSet);
				Corpus training = corpusSplit[0];
				Corpus testing = corpusSplit[1];
				System.out.println(training.size()+" profiles in the training set");
				System.out.println(testing.size()+" profiles in the testing set");
				System.out.println("Training SVM...");
				SVMTopicModel svm = new SVMTopicModel(training,topicType,kernelType,kernelParam,c);
				System.out.println("SVM Trained");
				
				//now use svm to infer topics for corpus testing
				System.out.println("Inferring for testing set...");
				Map<Long,Map<String,Double>> classifications = svm.classifyFull(testing);
				System.out.println("Inference procedure completed");
				for(Long uid : classifications.keySet()) {
					FileOutputStream fileOut = new FileOutputStream("classifications/svm/"+topicType+"/"+uid+".csv");
					PrintWriter writeOut = new PrintWriter(fileOut);
					writeOut.println("\"topic\",\"score\"");
					for(String topic : classifications.get(uid).keySet()) {
						writeOut.println(topic+","+classifications.get(uid).get(topic));
					}
					writeOut.close();
					fileOut.close();
				}
				System.out.println("Done outputting results for segment");
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void cFinder(Corpus corpus, String topicType, long kernelType, long kernelParam, double cLow, double cHigh, double cInc) {
		for(double c = cLow; c<=cHigh; c+= cInc) {
			SVMTopicModel.runCVInferenceFullStdOut(corpus,topicType,kernelType,kernelParam,c);
		}
	}
	
	public static void kernelFinder(Corpus corpus, String topicType, long kernelParam, double c) {
		long[] kernels = {KernelParam.LINEAR, KernelParam.POLYNOMIAL, KernelParam.RBF, KernelParam.SIGMOID};
		for(long kernel : kernels) {
			SVMTopicModel.runCVInferenceFullStdOut(corpus,topicType,kernel,kernelParam,c);
		}
	}
	
	public static void runCVInferenceFullStdOut(Corpus corpus, String topicType, long kernelType, long kernelParam, double c) {
		Long[] uids = Tools.getCSVUserIDs().toArray(new Long[1]);
		//Create segments
		int[] segments = {0,251,502,753,1004,1255,1506,1756,2006,2256,2506};
		try {
			for(int segment=0; segment<segments.length-1; segment++) {
				System.out.println("Cross validation: Dealing with profiles "+segments[segment]+" to "+segments[segment+1]);
				//need to generate two corpora, one training, one testing
				Set<Long> testingSet = new HashSet<Long>();
				Set<Long> trainingSet = new HashSet<Long>();
				for(int i=0; i<uids.length; i++) {
					if(i>=segments[segment] && i < segments[segment+1]) {
						testingSet.add(uids[i]);
					} else {
						trainingSet.add(uids[i]);
					}
				}
				Corpus[] corpusSplit = corpus.split(trainingSet, testingSet);
				Corpus training = corpusSplit[0];
				Corpus testing = corpusSplit[1];
				System.out.println(training.size()+" profiles in the training set");
				System.out.println(testing.size()+" profiles in the testing set");
				System.out.println("Training SVM...");
				SVMTopicModel svm = new SVMTopicModel(training,topicType,kernelType,kernelParam,c);
				System.out.println("SVM Trained");
				
				//now use svm to infer topics for corpus testing
				System.out.println("Inferring for testing set...");
				Map<Long,Map<String,Double>> classifications = svm.classifyFull(testing);
				System.out.println("Inference procedure completed");
				for(Long uid : classifications.keySet()) {
					System.out.println("\"topic\",\"score\"");
					for(String topic : classifications.get(uid).keySet()) {
						System.out.println(topic+","+classifications.get(uid).get(topic));
					}
				}
				System.out.println("Done outputting results for segment");
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}
