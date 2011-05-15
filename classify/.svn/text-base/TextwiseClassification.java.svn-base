package uk.ac.cam.ha293.tweetlabel.classify;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextwiseClassification {
	
	private Map<String,Double> categoryScores;
	
	public TextwiseClassification() {
		categoryScores = new HashMap<String,Double>();
	}
	
	public Set<String> getCategories() {
		return categoryScores.keySet();
	}
	
	public Map<String,Double> getCategoryScores() {
		return categoryScores;
	}
	
	public double lookupScore(String category) {
		return categoryScores.get(category);
	}
	
	public void add(String category, double score) {
		categoryScores.put(category, score);
	}
	
	public void print() {
		for(String cat : categoryScores.keySet()) {
			System.out.println(cat+": "+categoryScores.get(cat));
		}
	}
}
