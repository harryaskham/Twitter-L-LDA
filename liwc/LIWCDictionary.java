package uk.ac.cam.ha293.tweetlabel.liwc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import uk.ac.cam.ha293.tweetlabel.classify.NaiveBayes;
import uk.ac.cam.ha293.tweetlabel.twitter.SimpleProfile;
import uk.ac.cam.ha293.tweetlabel.twitter.SimpleTweet;
import uk.ac.cam.ha293.tweetlabel.types.Category;
import uk.ac.cam.ha293.tweetlabel.types.CategoryScore;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class LIWCDictionary implements Serializable {

	private static final long serialVersionUID = -1145009470341660362L;
	
	private Map<String,LIWCWord> words;
	private Set<Category> categories;
	private Map<String,Category> stringCategoryLookup;
	private LIWCTree lookupTree;
	private NaiveBayes naiveBayes;
	
	String[] categoryBlacklist = {"funct","pronoun","ppron","i","we","you","shehe","they","ipron","article","verb","auxverb","past","present","future","adverb","preps","conj","negate","quant","incl","excl","relativ","nonfl","filler","assent","space","affect","cogmech"};
	Set<String> categoryBlacklistSet = new HashSet<String>(Arrays.asList(categoryBlacklist));

	/*
	String[] categoryWhitelist = {"number","swear","social","family","friend","humans","posemo","negemo","anx","anger","sad","insight","cause","discrep","tentat","certain","inhib","percept","see","hear","feel","bio","body","health","sexual","ingest","motion","time","work","achieve","leisure","home","money","relig","death"};
	Set<String> categoryWhitelistSet = new HashSet<String>(Arrays.asList(categoryBlacklist));
	*/
	
	//Construct LIWC dictionary from .dic file
	public LIWCDictionary() {
		System.err.println("WARNING: If you are using this constructor, you're probably doing something wrong!");
		words = new LinkedHashMap<String,LIWCWord>();
		categories = new HashSet<Category>();
		stringCategoryLookup = new HashMap<String,Category>();
		createDictionaryFromFile("liwc/LIWC2007_English080730.dic");
		createLIWCTree();
		trainNaiveBayes();
		if(lookupTree == null) System.err.println("lookupTree created, but is NULL");
	}
	
	//Check for existing dictionary object, load it if so
	public static LIWCDictionary loadDictionaryFromFile(String path) {
		File dictionaryFile = new File(path);
		if(dictionaryFile.exists()) {
			LIWCDictionary dictionary = null;
			try {
				FileInputStream fileIn = new FileInputStream(path);
				ObjectInputStream objectIn = new ObjectInputStream(fileIn);
				dictionary = (LIWCDictionary)objectIn.readObject();
				if(dictionary != null) {
					System.out.println("Successfully constructed LIWC dictionary from "+path);
				}
			} catch (IOException e){
				System.out.println("Couldn't construct LIWC dictionary from "+path);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}			
			
			//SOLVED - hopefully: Can't serialize statics, DUH.
			//UPDATE: NOT SOLVED - weird!?
			//For some reason, the LIWCTree and the NB won't serialise properly, so create it here
			dictionary.createLIWCTree();
			dictionary.trainNaiveBayes();
			
			return dictionary; 
		} else {
			System.err.println("Couldn't find dictionary file, attempting to create anew");
			return new LIWCDictionary();
		}
	}
	
	//Serialise dictionary and store in file to avoid recreation
	public void saveDictionaryToFile(String path) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			System.out.println("Wrote dictionary to file "+path);
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't write dictionary to "+path);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't write dictionary to "+path);
			e.printStackTrace();			
		}
	}
	
	
	public void createDictionaryFromFile(String path) {
		File dictionaryFile = new File(path);
		if(dictionaryFile.exists()) {
			try {
				FileInputStream fileIn = new FileInputStream(path);
				BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));
				List<String> rawLIWC = new ArrayList<String>();
				String nextLine = "";
				while(true) {
					nextLine = buffer.readLine();
					if(nextLine == null) break;
					rawLIWC.add(nextLine);
				}
				System.out.println("Read in "+rawLIWC.size()+" lines");
				
				//Need to first iterate over each category, add to categories set
				//For each category, iterate over all words, add those with relevant number
				int i = 1;
				//iterate until we hit the end of the category list
				while(!rawLIWC.get(i).equals("%")) {
					String[] splitCategory = rawLIWC.get(i).split("\\s+"); //split on all whitespace
					int currentID = Integer.parseInt(splitCategory[0]);
					String currentTitle = splitCategory[1];
					Category currentCategory = new Category(currentTitle);	
					currentCategory.setLIWCID(currentID);
					
					int j = 66; //Start of word list
					//iterate until we hit end of word list
					while(j < rawLIWC.size()) {
						String[] splitWord = rawLIWC.get(j).split("\\s+");
						for(int k=1; k<splitWord.length; k++) {
							if(Integer.parseInt(splitWord[k]) == currentID) {
								currentCategory.addWord(splitWord[0]);
								
								//NOTE: now, we've added the word to the category
								//but here we can also try to create a new word object
								//and add this category to the word to avoid having to
								//iterate over it all again
								if(words.containsKey(splitWord[0])) {
									words.get(splitWord[0]).addCategory(currentCategory);
								} else {
									LIWCWord newWord = new LIWCWord(splitWord[0]);
									newWord.addCategory(currentCategory);
									words.put(splitWord[0], newWord);
								}							
								break;
							}
						}
						j++;
					}
					
					categories.add(currentCategory);
					stringCategoryLookup.put(currentCategory.getTitle(), currentCategory);
					i++;
				}
						
			} catch (IOException e) {
				System.err.println("Couldn't read from LIWC Dictionary file at "+path);
			}
		} else {
			System.err.println("Couldn't find dictionary file");
		}
		
		saveDictionaryToFile("liwc/dictionary.obj");
	}
	
	public void printCategories() {
		for(Category category : categories) {
			category.print();
		}
	}
	
	public void printWords() {
		for(LIWCWord word : words.values()) {
			word.print();
		}
	}	

	public List<CategoryScore> classifyTweet(SimpleTweet tweet) {
	
		//Initialise the categories into a HashMap with scores set to 0
		LinkedHashMap<Category,Integer> categoryScores = new LinkedHashMap<Category,Integer>();
		for(Category category : categories) {
			categoryScores.put(category, 0);
		}
				
		//Iterate over every tweet, converting to lowercase and removing all but apostrophes
		String stripped = Tools.LIWCStripTweet(tweet.getText());
		String[] split = stripped.split("\\s+");
			
		//Iterate over every word in the tweet and update category scores as necessary
		for(String word : split) {
			String lookupWord = lookupTree.lookup(word); //This gets the LIWC truncated representation of the lookup word, or NULL if not present

			if(lookupWord != null) { //If the word is present in the dictionary...
				//Iterate over every category for given word, incrementing scores in HashMap

				//Need to check if we have a * word, if so, add a *...
				String keyWord = lookupWord;
				if(!words.containsKey(keyWord)) keyWord = new String(keyWord+"*");

				Set<Category> cats = words.get(keyWord).getCategories();
				for(Category cat : cats) {
					Integer value = categoryScores.get(cat);
					categoryScores.put(cat, value+1);
				}
			}
		}			

		//Terrible hacky way of sorting the hashmap based on its values...
		categoryScores = sortLinkedHashMap(categoryScores);
		Iterator<Category> it = categoryScores.keySet().iterator();		

		List<CategoryScore> results = new ArrayList<CategoryScore>();
				
		//print results
		while(it.hasNext()) {		
			Category cat = it.next();
			if(categoryBlacklistSet.contains(cat.getTitle())) continue; //Don't include blacklist categories
			if(categoryScores.get(cat) == 0) continue; //Remove zero scoring categories
			//System.out.println(cat.getTitle()+": "+categoryScores.get(cat));
			results.add(new CategoryScore(cat,categoryScores.get(cat)));
		}
		
		return results;
	}
	
	public List<CategoryScore> classifyTweetNaiveBayes(SimpleTweet tweet) {	
		String text = Tools.LIWCStripTweet(tweet.getText());
		List<CategoryScore> results = naiveBayes.logClassify(text);
		//List<CategoryScore> results = naiveBayes.classify(text);
		for(Iterator<CategoryScore> it = results.iterator(); it.hasNext();) {
			CategoryScore score = it.next();
			if(categoryBlacklistSet.contains(score.getCategory().getTitle())) it.remove(); //Don't include blacklist categories
		}
		Collections.sort(results);
		Collections.reverse(results);
		return results;
	}
	
	public List<CategoryScore> classifyProfile(SimpleProfile profile) {
		System.out.println("Classifying profile using simple counts");
		
		//Initialise the categories into a HashMap with scores set to 0
		LinkedHashMap<Category,Integer> categoryScores = new LinkedHashMap<Category,Integer>();
		for(Category category : categories) {
			categoryScores.put(category, 0);
		}
				
		//Iterate over every tweet, converting to lowercase and removing all but apostrophes
		for(SimpleTweet tweet : profile.getTweets()) {
			String stripped = Tools.LIWCStripTweet(tweet.getText());
			String[] split = stripped.split("\\s+");
			
			//Iterate over every word in the tweet and update category scores as necessary
			for(String word : split) {
				String lookupWord = lookupTree.lookup(word); //This gets the LIWC truncated representation of the lookup word, or NULL if not present
								
				if(lookupWord != null) { //If the word is present in the dictionary...
					//Iterate over every category for given word, incrementing scores in HashMap
					
					//Need to check if we have a * word, if so, add a *...
					String keyWord = lookupWord;
					if(!words.containsKey(keyWord)) keyWord = new String(keyWord+"*");
					
					Set<Category> cats = words.get(keyWord).getCategories();
					for(Category cat : cats) {
						Integer value = categoryScores.get(cat);
						categoryScores.put(cat, value+1);
					}
				}
			}			
		}

		//Terrible hacky way of sorting the hashmap based on its values...
		categoryScores = sortLinkedHashMap(categoryScores);
		Iterator<Category> it = categoryScores.keySet().iterator();		

		List<CategoryScore> results = new ArrayList<CategoryScore>();
				
		//print results
		while(it.hasNext()) {		
			Category cat = it.next();
			//System.out.println(cat.getTitle()+": "+categoryScores.get(cat));
			if(categoryBlacklistSet.contains(cat.getTitle())) continue; //Don't include blacklist categories
			results.add(new CategoryScore(cat,categoryScores.get(cat)));
		}
		
		return results;
	}
	
	public List<CategoryScore> classifyProfileNaiveBayes(SimpleProfile profile) {
		System.out.println("Classifying profile using naive Bayes");
		
		Map<Category,Double> averageScores = new HashMap<Category,Double>();
		int averageCount = 0;
		for(SimpleTweet tweet : profile.getTweets()) {
			String text = Tools.LIWCStripTweet(tweet.getText());
			List<CategoryScore> scores = naiveBayes.logClassify(text);
			//List<CategoryScore> scores = naiveBayes.classify(text);

			for(Iterator<CategoryScore> it = scores.iterator(); it.hasNext();) {
				CategoryScore next = it.next();
				
				//Moving average calculation
				if(averageScores.containsKey(next.getCategory())) {
					averageScores.put(next.getCategory(), (((averageScores.get(next.getCategory())*averageCount)+next.getScore())/(averageCount+1)));
				} else {
					averageScores.put(next.getCategory(), next.getScore());
				}
				
				//System.out.println(next.getCategory().getTitle()+": new logP="+next.getLIWCLogP()+", new avg="+averageScores.get(next.getCategory()));
			}
			averageCount++;
		}
		
		List<CategoryScore> results = new ArrayList<CategoryScore>();
		for(Category category : averageScores.keySet()) {
			if(categoryBlacklistSet.contains(category.getTitle())) continue; //Don't include blacklist categories
			results.add(new CategoryScore(category,averageScores.get(category)));
		}
		Collections.sort(results);
		Collections.reverse(results);
		
		return results;
	}

	//Terrible hacky way of sorting the hashmap based on its values...
	public static LinkedHashMap<Category,Integer> sortLinkedHashMap(LinkedHashMap<Category,Integer> hashMap) {
		LinkedHashMap<Category,Integer> result = new LinkedHashMap<Category,Integer>();
		List<Category> keys = new ArrayList<Category>(hashMap.keySet());
		List<Integer> values = new ArrayList<Integer>(hashMap.values());
		TreeSet<Integer> sortedSet = new TreeSet<Integer>(values);
		Integer[] sortedArray = sortedSet.toArray(new Integer[1]); //Hack to get a new Integer array created due to overflow
		Arrays.sort(sortedArray,Collections.reverseOrder()); //Need descending order
		for(int i=0; i<sortedArray.length; i++) {
			result.put(keys.get(values.indexOf(sortedArray[i])), sortedArray[i]);
		}
		return result;
	}
	
	public boolean LIWCLookup(String word) {
		return ((lookupTree.lookup(word) != null) ? true : false);
	}
	
	public String LIWCVersionLookup(String word) {
		return lookupTree.lookup(word);
	}
	
	public void createLIWCTree() {
		if(words.keySet().isEmpty()) {
			System.err.println("No words in the keyset, cannot create the LIWC BST");
			return;
		}
		
		List<String> wordList = new ArrayList<String>(words.keySet());
		Collections.shuffle(wordList,new Random(0)); //Hacky - should permute the keys the same each time, as constructing the tree in different orders gives different classifications...
			
		for(String word : wordList) {
			if(lookupTree == null) {
				lookupTree = new LIWCTree(word);
			} else {
				lookupTree.insert(word);
			}
		}
	}
	
	//Note - erases existing naive bayes
	public void trainNaiveBayes() {
		System.out.println("Training Naive Bayes classifier");
		naiveBayes = new NaiveBayes(this);
		for(Category category : categories) {
			if(categoryBlacklistSet.contains(category.getTitle())) continue; //Don't train on blacklisted categories - should remove need for stopwords!
			for(String word : category.getWords()) {
				naiveBayes.trainLIWC(word, category);
			}
		}
	}
	
	public Category lookupCategoryByString(String category) {
		return stringCategoryLookup.get(category);
	}
	
	public Map<Long,List<CategoryScore>> getLIWCCategoryScores(long userID) {
		Map<Long,List<CategoryScore>> tweetScores = new HashMap<Long,List<CategoryScore>>();
		try {
			FileInputStream fileIn = new FileInputStream("classifications/liwc/count/"+userID+".csv");
			BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));
			String nextLine = "";
			buffer.readLine(); //skip past the CSV descriptor
			while(true) {
				nextLine = buffer.readLine();
				if(nextLine == null) break;
				String[] splitTweet = nextLine.split(",");
				long tweetID = Long.parseLong(splitTweet[0]);
				List<CategoryScore> categoryScores = new ArrayList<CategoryScore>();
				for(int i=1;i<splitTweet.length; i+=2) {
					CategoryScore categoryScore = new CategoryScore(stringCategoryLookup.get(splitTweet[i]),Double.parseDouble(splitTweet[i+1]));
					categoryScores.add(categoryScore);
				}
				tweetScores.put(tweetID, categoryScores);
			}
		} catch (IOException e){
			System.err.println("Couldn't read profile "+userID);
			return null;
		} 
		return tweetScores;
	}
	
	public Map<Long,List<CategoryScore>> getLIWCCategoryScoresNB(long userID) {
		Map<Long,List<CategoryScore>> tweetScores = new HashMap<Long,List<CategoryScore>>();
		try {
			FileInputStream fileIn = new FileInputStream("classifications/liwc/nb/"+userID+".csv");
			BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));
			String nextLine = "";
			buffer.readLine(); //skip past the CSV descriptor
			while(true) {
				nextLine = buffer.readLine();
				if(nextLine == null) break;
				String[] splitTweet = nextLine.split(",");
				long tweetID = Long.parseLong(splitTweet[0]);
				List<CategoryScore> categoryScores = new ArrayList<CategoryScore>();
				for(int i=1;i<splitTweet.length; i+=2) {
					CategoryScore categoryScore = new CategoryScore(stringCategoryLookup.get(splitTweet[i]),Double.parseDouble(splitTweet[i+1]));
					categoryScores.add(categoryScore);
				}
				tweetScores.put(tweetID, categoryScores);
			}
		} catch (IOException e){
			System.err.println("Couldn't read profile "+userID);
			return null;
		} 
		return tweetScores;
	}
		
}
