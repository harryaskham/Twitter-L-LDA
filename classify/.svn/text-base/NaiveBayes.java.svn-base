package uk.ac.cam.ha293.tweetlabel.classify;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cam.ha293.tweetlabel.liwc.LIWCDictionary;
import uk.ac.cam.ha293.tweetlabel.types.Category;
import uk.ac.cam.ha293.tweetlabel.types.CategoryScore;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

//NOTE: This class gets hairy as it performs naive bayesian classification compatible with the LIWC's ___* based word format
//NOTE: Use LIWCDictionary.lookupLIWCVersion(word) to get LIWC* form!
public class NaiveBayes implements Serializable {

	private static final long serialVersionUID = -6242147857055689677L;
	
	Map<Category,Map<String,Integer>> categories;
	Map<Category,Integer> frequencyCounts;
	Map<String,Map<Category,Integer>> words;
	double totalNumberOfWordsTrained;
	LIWCDictionary liwc;
	
	public NaiveBayes(LIWCDictionary liwc) {
		categories = new HashMap<Category,Map<String,Integer>>();
		frequencyCounts = new HashMap<Category,Integer>();
		words = new HashMap<String,Map<Category,Integer>>();
		totalNumberOfWordsTrained = 0.0;
		this.liwc = liwc;
	}
	
	public void addCategory(Category category) {
		categories.put(category, new HashMap<String,Integer>());
		frequencyCounts.put(category,new Integer(0));
	}
	
	//Note - since we're getting LIWC* form, we can train using LIWC* form! simple naive bayes works now. thank god.
	//Also - each category is only going to have at most a count of 1 for any given word... how does this affect the maths?
	//Maybe NB and training from a dictionary don't mix so well. Will test TODO
	//Also, I guess we could use this to train a NB model from a twitter profile, which would make use of all the frequency stuff...
	public void trainLIWC(String document, Category category) {
		String stripped = Tools.LIWCStripTweet(document);
		String[] split = stripped.split("\\s+");
		if(!categories.containsKey(category)) {
			categories.put(category, new HashMap<String,Integer>());
		}
		if(!frequencyCounts.containsKey(category)) {
			frequencyCounts.put(category, new Integer(0));
		}
		Map<String,Integer> wordMapping = categories.get(category);
		for(String word : split) {		
			String liwcVersion = liwc.LIWCVersionLookup(word);
			if(liwcVersion == null) continue;
			
			if(!words.containsKey(liwcVersion)) {
				words.put(liwcVersion, new HashMap<Category,Integer>());
			}
			Map<Category,Integer> categoryMapping = words.get(liwcVersion);
			
			//If we already have the word stored for this category, increment its count
			//Otherwise, add it in as 1
			if(wordMapping.containsKey(liwcVersion)) {
				wordMapping.put(liwcVersion, new Integer(wordMapping.get(liwcVersion)+1));
			} else {
				wordMapping.put(liwcVersion, new Integer(1));
			}
			
			//If we already have the category stored for this word, increment its count
			//Otherwise, add it in as 1
			if(categoryMapping.containsKey(category)) {
				categoryMapping.put(category, new Integer(categoryMapping.get(category)+1));
			} else {
				categoryMapping.put(category, new Integer(1));
			}	
			
			//For easy probability calculation later on - also store individual frequencies for each category
			//To avoid having to sum later on every time
			totalNumberOfWordsTrained++;
			frequencyCounts.put(category,new Integer(frequencyCounts.get(category)+1));
		}
	}
	
	public List<CategoryScore> logClassify(String document) {
		List<CategoryScore> categoryScores = new ArrayList<CategoryScore>();
		for(Category category : categories.keySet()) {
			double logP = logPOfCategoryGivenDocument(category, Tools.LIWCStripTweet(document));
			categoryScores.add(new CategoryScore(category,logP));			
		}
		return categoryScores;
	}
	
	public List<CategoryScore> classify(String document) {
		List<CategoryScore> categoryScores = new ArrayList<CategoryScore>();
		for(Category category : categories.keySet()) {
			double p = pOfCategoryGivenDocument(category, Tools.LIWCStripTweet(document));
			categoryScores.add(new CategoryScore(category,p));			
		}
		return categoryScores;
	}	
	
	public double logPOfCategoryGivenDocument(Category category, String document) {
		double p = 0.0;
		String[] split = document.split("\\s+");
		for(String token : split) {
			//We add because we're dealing with logs - this would be a multiple product, normally
			String liwcVersion = liwc.LIWCVersionLookup(token);
			if(liwcVersion == null) continue;
			p += logPOfWordGivenCategory(liwcVersion, category);
		}
		p += logPOfCategory(category);
		return p;
	}
	
	public double pOfCategoryGivenDocument(Category category, String document) {
		double p = 1.0;
		String[] split = document.split("\\s+");
		for(String token : split) {
			String liwcVersion = liwc.LIWCVersionLookup(token);
			if(liwcVersion == null) continue;
			p *= pOfWordGivenCategory(liwcVersion, category);
		}
		p *= pOfCategory(category);
		return p;
	}	
	
	public double logPOfCategory(Category category) {
		double p = Math.log((double)(frequencyCounts.get(category))/totalNumberOfWordsTrained);
		return p;
	}
	
	public double pOfCategory(Category category) {
		double p = ((double)(frequencyCounts.get(category))/totalNumberOfWordsTrained);
		return p;
	}
	
	public double logPOfWordGivenCategory(String word, Category category) {
		double tiny = Math.log(0.0000000001);

		if(!words.containsKey(word) || !words.get(word).containsKey(category)) {
			return tiny; //Maybe we want to output 0 in the cases where we have not seen the word before...
		}
		
		double p = Math.log((double)(words.get(word).get(category)) / (double)frequencyCounts.get(category));
		if(p == 0.0) return tiny;
		else return p;
	}
	
	public double pOfWordGivenCategory(String word, Category category) {
		double tiny = 0.0000000001;

		if(!words.containsKey(word) || !words.get(word).containsKey(category)) {
			return tiny; //Maybe we want to output 0 in the cases where we have not seen the word before...
		}
		
		double p = (double)(words.get(word).get(category)) / (double)frequencyCounts.get(category);
		if(p == 0.0) return tiny;
		else return p;
	}
	
	public void print() {
		for(Category category : categories.keySet()) {
			System.out.println("Category: "+category.getTitle());
			for(String word : categories.get(category).keySet()) {
				System.out.print(word+" ");
			}
			System.out.println();
		}
	}
	
	public void tests() {
		System.out.println("Running tests on the Naive Bayesian Classifier");
		
		double categorySum = 0.0;
		for(Category category : categories.keySet()) {
			double prob = pOfCategory(category);
			double logProb = logPOfCategory(category);
			categorySum += prob;
			System.out.println("P("+category.getTitle()+") = "+prob+", log(P("+category.getTitle()+")) = "+logProb);
		}
		System.out.println("Sum = "+categorySum);
		
		for(Category category : categories.keySet()) {
			double wordSum = 0.0;
			for(String word : categories.get(category).keySet()) {
				double prob = pOfWordGivenCategory(word,category);
				double logProb = logPOfWordGivenCategory(word,category);
				wordSum += prob;
				System.out.println("P("+word+"|"+category.getTitle()+") = "+prob+", log(P("+word+"|"+category.getTitle()+")) = "+logProb);	
			}
			System.out.println("Sum = "+wordSum);
		}
	}
}
