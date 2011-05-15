package uk.ac.cam.ha293.tweetlabel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.cam.ha293.tweetlabel.classify.AlchemyClassification;
import uk.ac.cam.ha293.tweetlabel.classify.AlchemyClassifier;
import uk.ac.cam.ha293.tweetlabel.classify.FullAlchemyClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullCalaisClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullTextwiseClassification;
import uk.ac.cam.ha293.tweetlabel.eval.Pearson;
import uk.ac.cam.ha293.tweetlabel.eval.SimilarityMatrix;
import uk.ac.cam.ha293.tweetlabel.eval.SpearmanRank;
import uk.ac.cam.ha293.tweetlabel.topics.FullLLDAClassification;
import uk.ac.cam.ha293.tweetlabel.topics.FullSVMClassification;
import uk.ac.cam.ha293.tweetlabel.topics.LDATopicModel;
import uk.ac.cam.ha293.tweetlabel.topics.LLDATopicModel;
import uk.ac.cam.ha293.tweetlabel.twitter.Profiler;
import uk.ac.cam.ha293.tweetlabel.twitter.SimpleProfile;
import uk.ac.cam.ha293.tweetlabel.types.Corpus;
import uk.ac.cam.ha293.tweetlabel.types.WordScore;

public class Tools {
	
	private static Set<Character> blacklist;
	private static Set<Character> whitespaceBlacklist;
	private static List<Long> synchronisedUserIDList;
	private static boolean stem;
	
	public static void init() {
		initBlacklist();
		synchronisedUserIDList = Collections.synchronizedList(getCSVUserIDs()); //Allows concurrency to work...
		configure(true);
	}
	
	public static void configure(boolean stem) {
		Tools.stem = stem;
	}
	
	public static void initBlacklist() {
		blacklist = new HashSet<Character>();
		whitespaceBlacklist = new HashSet<Character>();
		Character[] blackChars = {'\'','’'};
		Character[] whitespaceBlackChars = {'.',',','\"',';',':','!','£','$','%','^','&','*','(',')','+','=','?','<','>','/','\\','|','{','}','[',']','?','~','`','€','¬','¦','-','_','ã','©'};
		for(Character character : blackChars) {
			blacklist.add(character);
		}
		for(Character character : whitespaceBlackChars) {
			whitespaceBlacklist.add(character);
		}
	}
	
	//Take tweet, remove most punctuation and leave as sequence of words, #tags and @mentions with whitespace breaks
	public static String stripTweet(String original) {
		//Can't believe I forgot to lowercase it! facepalm
		String originalLC = original.toLowerCase();

		//THIS BREAKS THE URLCHECKER
		/*
		//for double-words not split on whitespace
		String originalWhitespaced = "";
		for(char c : originalLC.toCharArray()) {
			if(whitespaceBlacklist.contains(c)) {
				originalWhitespaced += " ";
			} else {
				originalWhitespaced += c;
			}
		}
		*/
		
		String[] split = originalLC.split("\\s+");
		//We do want to keep retweets - they contain semantically interesting information
		/*
		//Remove retweets
		//TODO: Maybe we want to keep retweets!?
		if(split[0].equals("RT")) return null;
		*/
		String tempResult = "";
		for(String tokenWithPunctuation : split) {
			//Remove links
			//TODO: Maybe we want to look at links!			
			if(tokenWithPunctuation.length() >= 4 && (tokenWithPunctuation.substring(0, 3).equals("www") || tokenWithPunctuation.substring(0, 4).equals("http"))) {
				continue;
			}
			
			//Remove usernames
			//TODO: Maybe we want to look at usernames!
			if(tokenWithPunctuation.charAt(0) == '@') {
				continue;
			}
			
			String token = removePunctuationWord(tokenWithPunctuation); //obviously need to check for URLS first!!!
			String stemmedToken = null;
			if(stem) stemmedToken = Stopwords.stemString(token);
			if(token.equals("")) {
				continue;
			}
			if(token.equals("rt")) {
				continue;
			}
			


			/* Redundant after removePunctuationFromWord
			//Remove the hash from hashtags
			if(tokenWithPunctuation.charAt(0) == '#') {
				token = token.subSequence(1, token.length()).toString(); //Now it's subejct to below checks
			}
			*/
			
			//Remove links
			//TODO: Maybe we want to look at links!			
			if(token.length() >= 4 && (token.substring(0, 3).equals("www") || token.substring(0, 4).equals("http"))) {
				continue;
			}
					
			//Check for stopwords before AND after punctuation tampering
			if(Stopwords.isStopword(token)) {
				continue;
			}
			
			if(stem && (Stopwords.isStemmedStopword(token) || Stopwords.isStemmedStopword(stemmedToken))) {
				continue;
			}
										
			//Everything's fine (...) so keep the token
			//tempResult += token+" ";
			if(stem) {
				tempResult += stemmedToken+" ";
			} else {
				tempResult += token+" ";
			}
		}

		String result = null;
		//check for stopwords again
		if(stem) result = Stopwords.removeStemmedStopWords(tempResult);
		else result = Stopwords.removeStopWords(tempResult);
		return result;
	}
	
	public static String stripTweetVerbose(String original) {
		
		System.out.println("Stripping: "+original);
		
		//Can't believe I forgot to lowercase it! facepalm
		String originalLC = original.toLowerCase();
			
		String[] split = originalLC.split("\\s+");
		
		//We do want to keep retweets - they contain semantically interesting information
		/*
		//Remove retweets
		//TODO: Maybe we want to keep retweets!?
		if(split[0].equals("RT")) return null;
		*/
				
		String tempResult = "";
		for(String tokenWithPunctuation : split) {
			String token = removePunctuationWord(tokenWithPunctuation);
			
			String stemmedToken = null;
			if(stem) stemmedToken = Stopwords.stemString(token);
			
			System.out.println("Considering token "+token);
			
			if(token.equals("")) {
				System.out.println("Empty token");
				continue; // redundant also
			}
			
			if(token.equals("rt")) {
				System.out.println("How is rt slipping past? We just caught one");
				continue; //redundant with length check
			}
			
			//Remove the hash from hashtags
			//TODO: we DEFINITELY want to keep hashtags semantically separate from words!!! TODO TODO TODO
			if(tokenWithPunctuation.charAt(0) == '#') {
				System.out.println("Found a hashtag, keepin the word");
				token = token.subSequence(1, token.length()).toString(); //Now it's subejct to below checks
			}
					
			//Check for stopwords before AND after punctuation tampering
			if(Stopwords.isStopword(token)) {
				System.out.println("It's a stopword, boom");
				continue;
			}
			
			if(stem && (Stopwords.isStemmedStopword(token) || Stopwords.isStemmedStopword(stemmedToken))) {
				System.out.println("It's a stemmed stopword, boom");
				continue;
			}
			
			//Remove links
			//TODO: Maybe we want to look at links!			
			if(token.length() >= 4 && (token.substring(0, 3).equals("www") || token.substring(0, 4).equals("http"))) {
				System.out.println("It's a link - why are these getting through!?");
				continue;
			}
			
			//Remove usernames
			//TODO: Maybe we want to look at usernames!
			if(tokenWithPunctuation.charAt(0) == '@') {
				System.out.println("Found a username, skipping it");
				continue;
			}
					
			//Everything's fine (...) so keep the token
			//tempResult += token+" ";
			if(stem) {
				System.out.println("Adding the stemmed token "+stemmedToken);
				tempResult += stemmedToken+" ";
			} else {
				System.out.println("Adding the token "+token);
				tempResult += token+" ";
			}
		}
		
		System.out.println("Current result is: "+tempResult);

		String result = null;
		
		//check for stopwords again
		if(stem) result = Stopwords.removeStemmedStopWords(tempResult);
		else result = Stopwords.removeStopWords(tempResult);
		
		System.out.println("After final stopword removal, result is: "+result);
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String removePunctuation(String original) {
		String result = "";
		char[] chars = original.toCharArray();
		for(char character : chars) {
			//Check if we need to add whitespace due to a banned char
			if(whitespaceBlacklist.contains(character)) {
				result += " ";
			} //Now check if our char is a non-whitespace banned one (mostly just apostrophes) 
			else if(!blacklist.contains(character)) {
				result += character;
			} 
		}
		return result;
	}
	
	
	//Don't have to add in teh spaces if it's just an individual word - can do it like this!
	public static String removePunctuationWord(String original) {
		String result = "";
		char[] chars = original.toCharArray();
		for(char character : chars) {
			if(character >= 'a' && character <= 'z') {
				result += character;
			}
		}
		return result;
	}
	
	//Strip a tweet down so that it might be used with the LIWC dictionary
	public static String LIWCStripTweet(String original) {
		String result = "";
		char[] chars = original.toCharArray();
		for(char character : chars) {
			//Check if we need to add whitespace due to a banned char
			if(whitespaceBlacklist.contains(character)) {
				result += " ";
			} //Now check if our char is a non-whitespace banned one (mostly just apostrophes) 
			else {
				result += character;
			}
		}
		return result.toLowerCase();		
	}
	
	//TODO: maybe we should just store a list of dataset userIDs somewhere...
	public static List<Long> getCSVUserIDs() {
		List<Long> userIDs = new ArrayList<Long>();
		File csvProfileDirectory = new File("profiles/csv");
		for(File profileFileName : csvProfileDirectory.listFiles()) {
			//swap files need to be ignored
			if(profileFileName.getName().startsWith(".")) {
				continue;
			}
			
			String[] split = profileFileName.getName().split("\\.");
			userIDs.add(Long.parseLong(split[0]));
		}
		return userIDs;
	}
	
	public static void alchemyClassificationRoutine() {
		System.out.println("Beginning complete Alchemy classification...");
		Profiler profiler = new Profiler();
		while(!synchronisedUserIDList.isEmpty()) {
			long currentID = synchronisedUserIDList.remove(0);
			SimpleProfile profile = profiler.loadCSVProfile(currentID);
			if(!profile.classifyAlchemy()) {
				System.err.println("Failed to classify profile properly, probably reached the daily limit");
				return;
			}
		}
		System.out.println("Classification thread complete");
	}
	
	//Fancy!
	public static void parallelAlchemyClassificationRoutine(int numThreads) {
		System.out.println("Beginning complete Alchemy classification... IN PARALLEL");
		synchronisedUserIDList = Collections.synchronizedList(getCSVUserIDs());
		for(int i=0; i<numThreads; i++) {
			new Thread() {
				public void run() {
					alchemyClassificationRoutine();
				}
			}.start();
		}
	}
	
	public static void liwcClassificationRoutine() {
		System.out.println("Beginning complete LIWC classification...");
		while(!synchronisedUserIDList.isEmpty()) {
			long currentID = synchronisedUserIDList.remove(0);
			SimpleProfile profile = Profiler.loadCSVProfile(currentID);
			if(!profile.classifyLIWC()) {
				System.err.println("Failed to classify profile "+currentID+" properly using the LIWC");
				return;
			}
		}
	}
	
	public static void parallelLIWCClassificationRoutine(int numThreads) {
		System.out.println("Beginning complete LIWC classification... IN PARALLEL");
		synchronisedUserIDList = Collections.synchronizedList(getCSVUserIDs());
		for(int i=0; i<numThreads; i++) {
			new Thread() {
				public void run() {
					liwcClassificationRoutine();
				}
			}.start();
		}
	}
	
	public static void calaisClassificationRoutine() {
		System.out.println("Beginning complete Calais classification...");
		while(!synchronisedUserIDList.isEmpty()) {
			long currentID = synchronisedUserIDList.remove(0);
			SimpleProfile profile = Profiler.loadCSVProfile(currentID);
			if(!profile.classifyCalais()) {
				System.err.println("Failed to classify profile "+currentID+" properly using Calais");
				return;
			}
		}
	}
	
	public static void parallelCalaisClassificationRoutine(int numThreads) {
		System.out.println("Beginning complete Calais classification... IN PARALLEL");
		synchronisedUserIDList = Collections.synchronizedList(getCSVUserIDs());
		for(int i=0; i<numThreads; i++) {
			new Thread() {
				public void run() {
					calaisClassificationRoutine();
				}
			}.start();
		}
	}
	
	public static void textwiseClassificationRoutine() {
		System.out.println("Beginning complete Textwise classification...");
		Profiler profiler = new Profiler();
		while(!synchronisedUserIDList.isEmpty()) {
			long currentID = synchronisedUserIDList.remove(0);
			SimpleProfile profile = profiler.loadCSVProfile(currentID);
			if(!profile.classifyTextwise()) {
				System.err.println("Failed to classify profile "+currentID+" properly using Textwise");
				return;
			}
		}
	}
	
	public static void parallelTextwiseClassificationRoutine(int numThreads) {
		System.out.println("Beginning complete Calais classification... IN PARALLEL");
		synchronisedUserIDList = Collections.synchronizedList(getCSVUserIDs());
		for(int i=0; i<numThreads; i++) {
			new Thread() {
				public void run() {
					textwiseClassificationRoutine();
				}
			}.start();
		}
	}
	
	public static void properTextwiseClassificationRoutine() {
		System.out.println("Beginning complete Textwise classification...");
		Profiler profiler = new Profiler();
		while(!synchronisedUserIDList.isEmpty()) {
			long currentID = synchronisedUserIDList.remove(0);
			SimpleProfile profile = profiler.loadCSVProfile(currentID);
			if(!profile.classifyTextwiseProper()) {
				System.err.println("Failed to classify profile "+currentID+" properly using Textwise");
				return;
			}
		}
	}
	
	public static void parallelProperTextwiseClassificationRoutine(int numThreads) {
		System.out.println("Beginning complete Calais classification... IN PARALLEL");
		synchronisedUserIDList = Collections.synchronizedList(getCSVUserIDs());
		for(int i=0; i<numThreads; i++) {
			new Thread() {
				public void run() {
					properTextwiseClassificationRoutine();
				}
			}.start();
		}
	}
	
	public static void ldaClassificationRoutine(int numTopics, int burn, int sample, boolean stem, double alpha, boolean reduceProfiles, int reduction) {
		//Make appropriately named directories
		String description = numTopics+"-"+burn+"-"+sample+"-"+alpha;
		String dirName = "classifications/lda/"+description;
		if(reduceProfiles) dirName = "classifications/fewerprofiles/"+reduction+"/lda/"+description;
		else dirName = "classifications/fewertweets/"+reduction+"/lda/"+description;
		new File(dirName).mkdirs();
		
		//Configure and run LDA
		Tools.configure(stem);

		//TODO: Flexibility in Corpus choice - make automatic?
		Corpus corpus = null;
		if(stem) corpus = Corpus.load("allprofiles-stemmed");
		else corpus = Corpus.load("allprofiles-unstemmed");
		
		//Check for model existence
		LDATopicModel lda = null;
		if(new File("models/lda/"+description+".model").exists()) {
			System.out.println("Found LDA model "+description);
			lda = LDATopicModel.load(description);
		} else {
			System.out.println("Couldn't find LDA model "+description+", creating new one");
			lda = new LDATopicModel(corpus,numTopics,burn,sample,0,alpha,0.01);
			lda.runGibbsSampling();
			lda.save(description);
		}
		
		try {
			//Get the document topic distributions and store these
			List<List<WordScore>> docTopics = lda.getDocuments();
			int docID = 0;
			for(List<WordScore> document : docTopics) {
				Long userID = lda.getDocIDFromIndex(docID);
				FileOutputStream fileOut = new FileOutputStream(dirName+"/"+userID+".csv");
				PrintWriter writeOut = new PrintWriter(fileOut);
				writeOut.println("\"topic\",\"probability\"");
				for(WordScore topic : document) {
					writeOut.println(topic.getWord()+","+topic.getScore());
				}
				writeOut.close();
				docID++;
			}
			
			
			//NOTE: We are saving these for now. However, we always have a saved model
			//and we can get these attributes from the model
			
			//should also save the topic-word distributions
			//okay, so we should definitely serialize topics and vocab
			Map<String,Integer> vocab = lda.getVocab();
			double[][] topics = lda.getTopicsUnsorted();
			
			//Save topics
			FileOutputStream topicsFileOut = new FileOutputStream(dirName+"/TOPICS.obj");
			ObjectOutputStream topicsObjectOut = new ObjectOutputStream(topicsFileOut);
			topicsObjectOut.writeObject(topics);
			topicsObjectOut.close();
			
			//Save vocab
			FileOutputStream vocabFileOut = new FileOutputStream(dirName+"/VOCAB.obj");
			ObjectOutputStream vocabObjectOut = new ObjectOutputStream(vocabFileOut);
			vocabObjectOut.writeObject(vocab);
			vocabObjectOut.close();
			
			System.out.println("Saved LDA Classifications, phew");
			
		} catch (IOException e) {
			System.out.println("Error in saving LDA classifications");
		}
	}
	
	/*
	public static void ldaAllAlphas() {
		//double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		double[] alphas = {0.75,1.0,1.25,1.5,1.75,2.0};
		for(double alpha : alphas) {
			Tools.ldaClassificationRoutine(50,1000,100,false,alpha);
		}
	}
	*/
	
	//THIS ISN'T GOING TO WORK
	//need to crossvalidate in LLDA itself...
	public static void lldaClassificationRoutine(String topicType, int burn, int sample, int lag, boolean stem) {
		//Make appropriately named directories
		String description = burn+"-"+sample+"-"+lag;
		if(stem) description += "-stemmed";
		String dirName = "classifications/llda/"+topicType+"/"+description;
		new File(dirName).mkdir();
		
		//Configure and run LDA
		Tools.configure(stem);

		//TODO: Flexibility in Corpus choice - make automatic?
		Corpus corpus = null;
		if(stem) corpus = Corpus.loadLabelled(topicType, "allprofiles-stemmed");
		else corpus = Corpus.loadLabelled(topicType, "allprofiles-unstemmed");
		
		//Check for model existence
		LLDATopicModel llda = null;
		if(new File("models/llda/"+topicType+"/"+description+".model").exists()) {
			System.out.println("Found LLDA model "+description);
			llda = LLDATopicModel.load(topicType,description);
		} else {
			System.out.println("Couldn't find LLDA model "+description+", creating new one");
			llda = new LLDATopicModel(corpus,burn,sample,lag,1,0.01);
			llda.runGibbsSampling();
			llda.save(description);
		}
		
		try {
			//Get the document topic distributions and store these
			List<List<WordScore>> docTopics = llda.getDocuments();
			int docID = 0;
			for(List<WordScore> document : docTopics) {
				Long userID = llda.getDocIDFromIndex(docID);
				FileOutputStream fileOut = new FileOutputStream(dirName+"/"+userID+".csv");
				PrintWriter writeOut = new PrintWriter(fileOut);
				writeOut.println("\"topic\",\"probability\"");
				for(WordScore topic : document) {
					writeOut.println(topic.getWord()+","+topic.getScore());
				}
				writeOut.close();
				docID++;
			}
			
			
			//NOTE: We are saving these for now. However, we always have a saved model
			//and we can get these attributes from the model
			
			//should also save the topic-word distributions
			//okay, so we should definitely serialize topics and vocab
			Map<String,Integer> vocab = llda.getVocab();
			double[][] topics = llda.getTopicsUnsorted();
			ArrayList<String> topicIDs = llda.getTopicsIDList();
			
			//Save topics
			FileOutputStream topicsFileOut = new FileOutputStream(dirName+"/TOPICS.obj");
			ObjectOutputStream topicsObjectOut = new ObjectOutputStream(topicsFileOut);
			topicsObjectOut.writeObject(topics);
			topicsObjectOut.close();
			
			//Save vocab
			FileOutputStream vocabFileOut = new FileOutputStream(dirName+"/VOCAB.obj");
			ObjectOutputStream vocabObjectOut = new ObjectOutputStream(vocabFileOut);
			vocabObjectOut.writeObject(vocab);
			vocabObjectOut.close();
			
			//Also need to save topic ID mappings in list form
			FileOutputStream topicIDFileOut = new FileOutputStream(dirName+"/TOPICIDS.obj");
			ObjectOutputStream topicIDObjectOut = new ObjectOutputStream(topicIDFileOut);
			topicIDObjectOut.writeObject(topicIDs);
			topicIDObjectOut.close();
			
			System.out.println("Saved LLDA Classifications, phew");
			
		} catch (IOException e) {
			System.out.println("Error in saving LLDA classifications");
		}
	}
		
	public static Map sortMapByValue(Map map) {
	     List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	              .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	} 
	
	public static Map sortMapByValueDesc(Map map) {
	     List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	              .compareTo(((Map.Entry) (o1)).getValue());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	} 
	
	public static void prettyPrintDocument(org.w3c.dom.Document doc) {
		System.out.println(doc.getTextContent());
		NodeList children = doc.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			prettyPrintNode(children.item(i));
		}
	}
	
	public static void prettyPrintNode(Node node) {
		System.out.println(node.getTextContent());
		NodeList children = node.getChildNodes();
		for(int i=0; i<children.getLength(); i++) {
			prettyPrintNode(children.item(i));
		}
	}
	
	public static org.w3c.dom.Document xmlStringToDocument(String xmlString) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
			StringReader reader = new StringReader(xmlString);
			InputSource inputSource = new InputSource(reader);
			org.w3c.dom.Document parsedXML = documentBuilder.parse(inputSource);
			return parsedXML;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void dpPrint(double x, int dp) {
		System.out.format("%."+dp+"f", x);	
	}
	
	public static void lldaStuff() {
		//Corpus corpus = Corpus.loadLabelled("alchemy","allprofiles-unstemmed-alchemy-top3");
		//Corpus corpus = Corpus.loadLabelled("calais","allprofiles-unstemmed-calais-top3");
		Corpus corpus = Corpus.loadLabelled("textwise","allprofiles-unstemmed-textwise-top3");
		corpus.removeLeastCommonWords(10,1);
		Set<Double> alphaSet = new HashSet<Double>();
		alphaSet.add(0.25);
		alphaSet.add(0.5);
		alphaSet.add(0.75);
		alphaSet.add(1.00);
		alphaSet.add(1.25);
		alphaSet.add(1.5);
		alphaSet.add(1.75);
		alphaSet.add(2.00);
		for(Double alpha : alphaSet) {
			LLDATopicModel llda = new LLDATopicModel(corpus,1000,100,0,alpha,0.01);
			llda.runQuickCVGibbsSampling(0);
		}
	}
	
	public static void similarityStuff() {
		String[] topicTypes = {"alchemy","calais","textwiseproper"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		
		for(double alpha : alphas) {
			SimilarityMatrix lda = SimilarityMatrix.load("lda-50-1000-100-"+alpha);
			for(String topicType : topicTypes) {
				SimilarityMatrix llda = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
				System.out.println(topicType+"\t"+alpha+"\t"+SpearmanRank.srcc(lda,llda));
			}
		}

		
		
		/*
		//API baselines, LLDA tests
		for(String topicType : topicTypes) {
			SimilarityMatrix baseline = SimilarityMatrix.load(topicType);
			for(double alpha : alphas) {
				SimilarityMatrix llda = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
				System.out.println(topicType+","+alpha+","+SpearmanRank.srcc(baseline,llda));
			}
		}
		*/
		
		/*
		//LDA baseline against API baselines
		for(String topicType : topicTypes) {
			SimilarityMatrix baseline = SimilarityMatrix.load(topicType);
			for(double alpha : alphas) {
				SimilarityMatrix lda = SimilarityMatrix.load("lda-50-1000-100-"+alpha);
				System.out.println(topicType+"/lda,"+alpha+","+SpearmanRank.srcc(baseline,lda));
			}
		}
		*/
		
		/*
		//LDA baseline, LLDA tests
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				SimilarityMatrix lda = SimilarityMatrix.load("lda-50-1000-100-"+alpha);
				SimilarityMatrix llda = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
				System.out.println(topicType+"/lda,"+alpha+","+SpearmanRank.srcc(lda,llda));
			}
		}
		*/
		
		/*
		//LDA baseline, LLDA tests - lda alpha fixed at 1.0
		for(String topicType : topicTypes) {
			SimilarityMatrix lda = SimilarityMatrix.load("lda-50-1000-100-1.0");
			for(double alpha : alphas) {
				SimilarityMatrix llda = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
				System.out.println(topicType+"/lda,"+alpha+","+SpearmanRank.srcc(lda,llda));
			}
		}	
		*/
		
		/*
		//LIWC stuff - forgot about this...
		SimilarityMatrix liwc = SimilarityMatrix.load("liwc");
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				SimilarityMatrix llda = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
				System.out.println(topicType+"/liwc,"+alpha+","+SpearmanRank.srcc(liwc,llda));
			}
		}
		*/
		/*
		//SVM stuff
		SimilarityMatrix alchemy = SimilarityMatrix.load("alchemy");
		SimilarityMatrix calais = SimilarityMatrix.load("calais");
		SimilarityMatrix svmAlchemy = SimilarityMatrix.load("svm-alchemy");
		SimilarityMatrix svmCalais = SimilarityMatrix.load("svm-calais");
		
		SimilarityMatrix textwise = SimilarityMatrix.load("textwiseproper");
		SimilarityMatrix svmTextwise = SimilarityMatrix.load("svm-textwise");
		//System.out.println("Alchemy Baseline vs SVM Spearman: "+SpearmanRank.srcc(alchemy, svmAlchemy));
		//System.out.println("Alchemy Baseline vs SVM Pearson: "+Pearson.pmcc(alchemy, svmAlchemy));
		//System.out.println("Calais Baseline vs SVM Spearman: "+SpearmanRank.srcc(calais, svmCalais));
		//System.out.println("Calais Baseline vs SVM Pearson: "+Pearson.pmcc(calais, svmCalais));
		System.out.println("Textwise Baseline vs SVM Spearman: "+SpearmanRank.srcc(textwise, svmTextwise));
		System.out.println("Textwise Baseline vs SVM Pearson: "+Pearson.pmcc(textwise, svmTextwise));
		*/
	}
	
	public static void pmccSimilarityStuff() {
		String[] topicTypes = {"alchemy","calais","textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};

		/*
		//API baselines, LLDA tests
		for(String topicType : topicTypes) {
			SimilarityMatrix baseline = SimilarityMatrix.load(topicType);
			for(double alpha : alphas) {
				SimilarityMatrix llda = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
				System.out.println(topicType+","+alpha+","+Pearson.pmcc(baseline,llda));
			}
		}
		*/
		
		/*
		//LDA baseline against API baselines
		for(String topicType : topicTypes) {
			SimilarityMatrix baseline = SimilarityMatrix.load(topicType);
			for(double alpha : alphas) {
				SimilarityMatrix lda = SimilarityMatrix.load("lda-50-1000-100-"+alpha);
				System.out.println(topicType+"/lda,"+alpha+","+Pearson.pmcc(baseline,lda));
			}
		}
		*/
		
		/*
		//LDA baseline, LLDA tests
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				SimilarityMatrix lda = SimilarityMatrix.load("lda-50-1000-100-"+alpha);
				SimilarityMatrix llda = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
				System.out.println(topicType+"/lda,"+alpha+","+Pearson.pmcc(lda,llda));
			}
		}
		*/
		
		/*
		//LDA baseline, LLDA tests - lda alpha fixed at 1.0
		for(String topicType : topicTypes) {
			SimilarityMatrix lda = SimilarityMatrix.load("lda-50-1000-100-1.0");
			for(double alpha : alphas) {
				SimilarityMatrix llda = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
				System.out.println(topicType+"/lda,"+alpha+","+Pearson.pmcc(lda,llda));
			}
		}	
		*/	
		
		/*
		//LIWC stuff - forgot about this...
		SimilarityMatrix liwc = SimilarityMatrix.load("liwc");
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				SimilarityMatrix llda = SimilarityMatrix.load("llda-"+topicType+"-"+alpha);
				System.out.println(topicType+"/liwc,"+alpha+","+Pearson.pmcc(liwc,llda));
			}
		}
		*/
		
	}
	
	public static String[] getTopics(String topicType) {
		if(topicType.equals("alchemy")) {
			String[] topics = {"law_crime","arts_entertainment","culture_politics","religion","sports","science_technology","computer_internet","business","health","recreation","gaming","weather"};
			return topics;
		} else if(topicType.equals("calais")) {
			String[] topics = {"Politics","Social Issues","Health_Medical_Pharma","Environment","Education","Law_Crime","Hospitality_Recreation","Religion_Belief","Technology_Internet","Human Interest","Entertainment_Culture","Sports","War_Conflict","Weather","Labor","Business_Finance","Disaster_Accident"};
			return topics;
		} else if(topicType.equals("textwise") || topicType.equals("textwiseproper")) {
			String[] topics = {"Computers","Arts","Business","Society","Health","Recreation","Home","Science","Sports","Reference","Games"};
			return topics;
		} else {
			return null;
		}
	}
	
	public static void parallelLLDA() {
		//multithreading test
		int threadNum=1;
		double[] als = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(double al : als) {
			System.out.println("THREAD "+threadNum+": Starting up");
			final int fThread=threadNum;
			threadNum++;
			final double alph=al;
			Thread thread = new Thread(){
				public void run() {
					System.out.println("THREAD: "+"Running for alpha="+alph);
					Corpus corpus = Corpus.loadLabelled("textwiseproper", "allprofiles-unstemmed-textwiseproper-top3");
					LLDATopicModel llda = new LLDATopicModel(corpus,1000,100,0,alph,0.01,fThread);
					llda.runQuickCVGibbsSampling(0);
				}
			};
			thread.start();
		}
		System.out.println("All threads started");
	}
	
	public static void theBigOne() {
		System.out.println("CALLING THE BIG ONE");
		int threadNum=1;
		for(int reduction = 0; reduction <= 0; reduction ++) {
			System.out.println("THREAD "+threadNum+": Starting up");
			final int iReduction = reduction;
			final double fReduction = reduction/10.0;
			String[] topicTypes = {"alchemy","calais","textwiseproper"};
			for(String topicType : topicTypes) {
				Corpus corpus = Corpus.loadLabelled(topicType, "allprofiles-unstemmed-"+topicType+"-top3");
				final Corpus fCorpus = corpus.randomlyRemove(fReduction);
				System.out.println(fCorpus.size());
				final int fThread=threadNum;
				threadNum++;
				Thread thread = new Thread(){
					public void run() {
						double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
						for(double alpha : alphas) {
							System.out.println("THREAD "+fThread+": Running for alpha="+alpha);
							LLDATopicModel llda = new LLDATopicModel(fCorpus,1000,100,0,alpha,0.01,fThread);
							llda.runQuickCVGibbsSampling(iReduction);
						}
					}
				};
				thread.start();
			}

		}
		System.out.println("All threads started");
	}
	
	public static void theBiggerOne() {
		double[] reductions = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9};
		int threadNum=1;
		for(int reduction=1; reduction <=9; reduction++) {
			System.out.println("THREAD "+threadNum+": Starting up");
			final int iReduction = reduction;
			final double fReduction = reductions[reduction-1];
			String[] topicTypes = {"textwiseproper"};
			for(String topicType : topicTypes) {
				final Corpus fCorpus = Corpus.loadLabelled(topicType, "allprofiles-unstemmed-"+fReduction+"-tweets-top3");
				final int fThread=threadNum;
				threadNum++;
				Thread thread = new Thread(){
					public void run() {
						double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
						for(double alpha : alphas) {
							System.out.println("THREAD "+fThread+": Running for alpha="+alpha);
							LLDATopicModel llda = new LLDATopicModel(fCorpus,1000,100,0,alpha,0.01,fThread);
							llda.runQuickCVGibbsSampling(-1*iReduction); //sooo hacky
						}
					}
				};
				thread.start();
			}

		}
		System.out.println("All threads started");
	}
	
	//not wokring properly
	//also, not enough memory to multithread - WHY!?
	public static void theLDAOne() {
		double[] reductions = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9};
		int threadNum=1;
		for(int reduction=1; reduction <=9; reduction++) {
			System.out.println("THREAD "+threadNum+": Starting up");
			final int iReduction = reduction;
			final double fReduction = reductions[reduction-1];
			//final Corpus fCorpus = Corpus.loadLabelled(topicType, "allprofiles-unstemmed-"+fReduction+"-tweets-top3");
			final Corpus fCorpus = Corpus.load("allprofiles-unstemmed");
			fCorpus.randomlyRemove(fReduction);
			final int fThread=threadNum;
			threadNum++;
			Thread thread = new Thread(){
				public void run() {
					double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
					for(double alpha : alphas) {
						System.out.println("THREAD "+fThread+": Running for alpha="+alpha);
						Tools.ldaClassificationRoutine(50,1000,100,false,alpha,true,iReduction);
					}
				}
			};
			thread.start();
		}
		System.out.println("All threads started");
	}
	
	//this one gets profiles or tweets removed vs similarity to baseline
	public static void dataGatherer() {
		double[] alphas = {1.25,1.5,1.75,2.0};
		String[] topicTypes = {"alchemy","calais","textwiseproper"};
		List<Long> uids = Tools.getCSVUserIDs();
		for(double alpha : alphas) {
			for(int reduction = 1; reduction <= 9; reduction++) {
				for(String topicType : topicTypes) {
					//get average cosine similarity to baseline
					double cosineSum = 0.0;
					int cosineCount = 0;
					for(Long uid : uids) {
						if(topicType.equals("alchemy")) {
							FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
							FullLLDAClassification llda = new FullLLDAClassification(topicType,alpha,false,reduction,uid);
							double sim = llda.cosineSimilarity(baseline);
							cosineSum += sim;
							cosineCount++;
						} else if(topicType.equals("calais")) {
							FullCalaisClassification baseline = new FullCalaisClassification(uid);
							FullLLDAClassification llda = new FullLLDAClassification(topicType,alpha,false,reduction,uid);
							double sim = llda.cosineSimilarity(baseline);
							cosineSum += sim;
							cosineCount++;
						} else if(topicType.equals("textwiseproper")) {
							FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
							FullLLDAClassification llda = new FullLLDAClassification(topicType,alpha,false,reduction,uid);
							double sim = llda.cosineSimilarity(baseline);
							cosineSum += sim;
							cosineCount++;
						}
					}
					double avgCosine = cosineSum/cosineCount;
					System.out.println(alpha+","+reduction+","+topicType+","+avgCosine);
				}
			}
		}
	}
	
	public static void kFinder(String topicType, double alpha) {
		System.out.println("Finding optimal K for "+topicType+" "+alpha);
		String[] topics = Tools.getTopics(topicType);
		int maxK = topics.length;
		for(int k=1; k<maxK; k++) {
			int totalCount = 0;
			int correctCount = 0;
			for(Long uid : Tools.getCSVUserIDs()) {
				//System.out.println(totalCount);
				totalCount++;
				Set<String> lldaTopicSet = new HashSet<String>();
				Set<String> baselineTopicSet = new HashSet<String>();
				String modTopic = topicType;
				if(modTopic.equals("textwise")) modTopic = "textwiseproper";
				FullLLDAClassification llda = new FullLLDAClassification(modTopic,alpha,uid);
				int kCount=0;
				for(String topic : llda.getCategorySet()) {
					if(kCount == k) break;
					kCount++;
					lldaTopicSet.add(topic);
				}
				if(topicType.equals("alchemy")) {
					FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
					kCount=0;
					for(String topic : baseline.getCategorySet()) {
						if(kCount == k) break;
						kCount++;
						baselineTopicSet.add(topic);
					}
				} else if(topicType.equals("calais")) {
					FullCalaisClassification baseline = new FullCalaisClassification(uid);
					kCount=0;
					for(String topic : baseline.getCategorySet()) {
						if(kCount == k) break;
						if(topic.equals("Other")) continue;
						kCount++;
						baselineTopicSet.add(topic);
					}
				} else if(topicType.equals("textwise")) {
					FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
					kCount=0;
					for(String topic : baseline.getCategorySet()) {
						if(kCount == k) break;
						kCount++;
						baselineTopicSet.add(topic);
					}
				}

				if(lldaTopicSet.equals(baselineTopicSet)) {
					correctCount++;
				}
			}
			double correctFraction = (double)correctCount/(double)totalCount;
			System.out.println(correctFraction);
		}
	}
	
	public static void bigKFinder() {
		String[] topicTypes = {"alchemy","calais","textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(double alpha : alphas) {
			for(String topicType : topicTypes) {
				kFinder(topicType,alpha);
			}
		}
	}
	
	public static void svmKFinder(String topicType) {
		System.out.println("Finding optimal K for svm "+topicType);
		String[] topics = Tools.getTopics(topicType);
		int maxK = topics.length;
		for(int k=1; k<maxK; k++) {
			int totalCount = 0;
			int correctCount = 0;
			for(Long uid : Tools.getCSVUserIDs()) {
				//System.out.println(totalCount);
				totalCount++;
				Set<String> svmTopicSet = new HashSet<String>();
				Set<String> baselineTopicSet = new HashSet<String>();
				FullSVMClassification svm = new FullSVMClassification(topicType,uid);
				int kCount=0;
				for(String topic : svm.getCategorySet()) {
					if(kCount == k) break;
					kCount++;
					svmTopicSet.add(topic);
					System.out.println("Adding topic "+topic+" "+svm.getScore(topic));
				}
				if(topicType.equals("alchemy")) {
					FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
					kCount=0;
					for(String topic : baseline.getCategorySet()) {
						if(kCount == k) break;
						kCount++;
						baselineTopicSet.add(topic);
					}
				} else if(topicType.equals("calais")) {
					FullCalaisClassification baseline = new FullCalaisClassification(uid);
					kCount=0;
					for(String topic : baseline.getCategorySet()) {
						if(kCount == k) break;
						if(topic.equals("Other")) continue;
						kCount++;
						baselineTopicSet.add(topic);
					}
				} else if(topicType.equals("textwise")) {
					FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
					kCount=0;
					for(String topic : baseline.getCategorySet()) {
						if(kCount == k) break;
						kCount++;
						baselineTopicSet.add(topic);
					}
				}
				System.out.print("svm topic set: ");
				printSet(svmTopicSet);
				System.out.print("baseline topic set: ");
				printSet(baselineTopicSet);
				
				try {
					System.in.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(svmTopicSet.equals(baselineTopicSet)) {
					correctCount++;
					//System.out.println((double)correctCount/(double)totalCount);
				}
			}
			double correctFraction = (double)correctCount/(double)totalCount;
			System.out.println(correctFraction);
		}
	}

	public static void printSet(Set x) {
		System.out.print("{");
		int count=0;
		for(Object o : x) {
			count++;
			if(count==x.size()) System.out.print(o);
			else System.out.print(o+",");
		}
		System.out.print("}");
		System.out.println();
	}
	
	public static void discoverBaselineTopicProportions(String topicType) {
		System.out.println("Baseline topic proportions for "+topicType);
		Map<String,Integer> topicCounts = new HashMap<String,Integer>();
		int count = 0;
		for(String topic : Tools.getTopics(topicType)) {
			topicCounts.put(topic, 0);
		}
		for(Long uid : Tools.getCSVUserIDs()) {
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification c = new FullAlchemyClassification(uid);
				if(c.getCategorySet().size() == 0) continue;
				String topTopic = c.getCategorySet().toArray(new String[1])[0];
				topicCounts.put(topTopic,topicCounts.get(topTopic)+1);
			} else if(topicType.equals("calais")) {
				FullCalaisClassification c = new FullCalaisClassification(uid);
				if(c.getCategorySet().size() == 0) continue;
				String topTopic = c.getCategorySet().toArray(new String[1])[0];
				if(topTopic.equals("Other") && c.getCategorySet().size() > 1)  topTopic = c.getCategorySet().toArray(new String[1])[1];
				else if(topTopic.equals("Other")) continue;
				topicCounts.put(topTopic,topicCounts.get(topTopic)+1);
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification c = new FullTextwiseClassification(uid,true);
				if(c.getCategorySet().size() == 0) continue;
				String topTopic = c.getCategorySet().toArray(new String[1])[0];
				topicCounts.put(topTopic,topicCounts.get(topTopic)+1);
			}
			count++;
		}
		double sum = 0.0;
		Map<String,Double> topicProportions = new HashMap<String,Double>();
		for(String topic : topicCounts.keySet()) {
			topicProportions.put(topic, topicCounts.get(topic)/(double)count);
			sum += topicCounts.get(topic)/(double)count;
		}
		topicProportions = Tools.sortMapByValueDesc(topicProportions);
		for(String topic : topicProportions.keySet()) {
			System.out.println(topic);
		}
		for(String topic : topicProportions.keySet()) {
			System.out.println(topicProportions.get(topic));
		}
		System.out.println(sum);
	}
	
	public static void srccKFinder(String topicType, double alpha) {
		System.out.println("Finding optimal SRCC K for "+topicType+" "+alpha);
		String[] topics = Tools.getTopics(topicType);
		int maxK = topics.length;
		for(int k=1; k<=maxK; k++) {
			SimilarityMatrix baseline = new SimilarityMatrix(2506);
			baseline.fillRestricted(true, topicType, k, 0);
			SimilarityMatrix llda = new SimilarityMatrix(2506);
			llda.fillRestricted(false, topicType, k, alpha);
			System.out.println(alpha+","+topicType+","+k+","+SpearmanRank.jscSRCC(baseline, llda));
		}
	}
	
	public static void bigSRCCKFinder() {
		String[] topicTypes = {"alchemy","calais","textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(double alpha : alphas) {
			for(String topicType : topicTypes) {
				srccKFinder(topicType,alpha);
			}
		}
	}
	
	public static void inferredTopTopicProportions(String topicType, double alpha) {
		Map<String,Double> proportions = new HashMap<String,Double>();
		for(String topic : Tools.getTopics(topicType)) {
			proportions.put(topic, 0.0);
		}
		for(Long uid : Tools.getCSVUserIDs()) {
			FullLLDAClassification c = new FullLLDAClassification(topicType,alpha,uid);
			if(c.getCategorySet().size()==0) continue;
			String topTopic = c.getCategorySet().toArray(new String[1])[0];
			proportions.put(topTopic,proportions.get(topTopic)+1.0);
		}
		double sum = 0.0;
		for(String topic : proportions.keySet()) {
			sum += proportions.get(topic);
		}
		for(String topic : proportions.keySet()) {
			proportions.put(topic, proportions.get(topic)/sum);
		}
		proportions = Tools.sortMapByValueDesc(proportions);
		for(String topic : proportions.keySet()) {
			System.out.println(topic+"\t"+proportions.get(topic));
		}
	}
	
	public static void danieleDataset() {
		AlchemyClassifier.init();
		try {
			int startFromHere = 19198;
			FileInputStream fin = new FileInputStream("dataset/egotweets.txt");
			BufferedReader read = new BufferedReader(new InputStreamReader(fin));
			//FileOutputStream fout = new FileOutputStream("dataset/egoalchemy.txt");
			//PrintWriter write = new PrintWriter(fout);
			String nextLine = read.readLine();
			//String writeOut = "(ego)userid\ttweetid\tclassification\tscore\n";
			int count = 1;
			while(nextLine != null) {
				nextLine = read.readLine();
				if(count < startFromHere) {count++;continue;}
				String[] split = nextLine.split("\t");
				long uid = Long.parseLong(split[0]);
				BigInteger tid = new BigInteger(split[1]);
				String tweet = split[2];
				AlchemyClassification c = AlchemyClassifier.classifyText(tweet);
				while(c == null) {
					c = AlchemyClassifier.classifyText(tweet);
					
				}
				System.out.println(count+"\t"+uid+"\t"+tid+"\t"+c.getCategory()+"\t"+c.getScore());
				//writeOut += uid+"\t"+tid+"\t"+c.getCategory()+"\t"+c.getScore()+"\n";
				count++;
			}
			//write.print(writeOut);
			//write.close();
			//fout.close();
			read.close();
			fin.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static void reductionAccuracy(String topicType, double alpha, boolean fewerProfiles, int reduction, int k) {
		String[] topics = Tools.getTopics(topicType);
		int totalCount = 0;
		int correctCount = 0;
		for(Long uid : Tools.getCSVUserIDs()) {
			//System.out.println(totalCount);
			Set<String> lldaTopicSet = new HashSet<String>();
			Set<String> baselineTopicSet = new HashSet<String>();
			String modTopic = topicType;
			if(modTopic.equals("textwise")) modTopic = "textwiseproper";
			FullLLDAClassification llda = new FullLLDAClassification(modTopic,alpha,fewerProfiles,reduction,uid);
			if(llda.getCategorySet().isEmpty()) continue;
			totalCount++;
			int kCount=0;
			for(String topic : llda.getCategorySet()) {
				if(kCount == k) break;
				kCount++;
				lldaTopicSet.add(topic);
			}
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
				kCount=0;
				for(String topic : baseline.getCategorySet()) {
					if(kCount == k) break;
					kCount++;
					baselineTopicSet.add(topic);
				}
			} else if(topicType.equals("calais")) {
				FullCalaisClassification baseline = new FullCalaisClassification(uid);
				kCount=0;
				for(String topic : baseline.getCategorySet()) {
					if(kCount == k) break;
					if(topic.equals("Other")) continue;
					kCount++;
					baselineTopicSet.add(topic);
				}
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
				kCount=0;
				for(String topic : baseline.getCategorySet()) {
					if(kCount == k) break;
					kCount++;
					baselineTopicSet.add(topic);
				}
			}

			if(lldaTopicSet.equals(baselineTopicSet)) {
				correctCount++;
			}
		}
		double correctFraction = (double)correctCount/(double)totalCount;
		System.out.println(topicType+"\t"+alpha+"\t"+correctFraction);
	}

	public static void locations() {
		try {
			String[] apis = {"alchemy","calais","textwiseproper"};
			Map<Long,String> alchemyClassifications = new HashMap<Long,String>();
			Map<Long,String> calaisClassifications = new HashMap<Long,String>();
			Map<Long,String> textwiseClassifications = new HashMap<Long,String>();
			for(String api : apis) {
				for(Long uid : Tools.getCSVUserIDs()) {
					System.out.print(uid+",");
					String path = "classifications/llda/"+api+"/1000-100-1.0/"+uid+".csv";
					if(!(new File(path).exists())) continue;
					FileInputStream fileIn = new FileInputStream(path);
					BufferedReader read = new BufferedReader(new InputStreamReader(fileIn));
					String nextLine = read.readLine();
					nextLine = read.readLine();
					Map<String,Double> scores = new HashMap<String,Double>();
					while(nextLine != null) {
						String[] split = nextLine.split(",");
						scores.put(split[0], Double.parseDouble(split[1]));
						nextLine = read.readLine();
					}
					scores = Tools.sortMapByValueDesc(scores);
					
					String topTopic = (String)scores.keySet().toArray()[0];
					if(api.equals("alchemy")) {
						alchemyClassifications.put(uid,topTopic);
					} else if(api.equals("calais")) {
						calaisClassifications.put(uid,topTopic);
					} else if(api.equals("textwiseproper")) {
						textwiseClassifications.put(uid,topTopic);
					} 
					System.out.println(topTopic+","+scores.get(topTopic));
				}
			}
			
			String path = "dataset/uids_locations.txt";
			FileInputStream fileIn = new FileInputStream(path);
			BufferedReader read = new BufferedReader(new InputStreamReader(fileIn));
			String nextLine = read.readLine();
			nextLine = read.readLine();
			while(nextLine != null) {
				String[] split = nextLine.split("\t");
				String alchemyTopic = alchemyClassifications.containsKey(Long.parseLong(split[0])) ? alchemyClassifications.get(Long.parseLong(split[0])) : "";
				String calaisTopic = calaisClassifications.containsKey(Long.parseLong(split[0])) ? calaisClassifications.get(Long.parseLong(split[0])) : "";
				String textwiseTopic = textwiseClassifications.containsKey(Long.parseLong(split[0])) ? textwiseClassifications.get(Long.parseLong(split[0])) : "";
				System.out.println(split[0]+","+split[2]+","+split[3]+","+alchemyTopic+","+calaisTopic+","+textwiseTopic);
				nextLine = read.readLine();
			}
		} catch (IOException e) {
			
		}
	}
}
