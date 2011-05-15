package uk.ac.cam.ha293.tweetlabel.classify;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CalaisClassification {
	
	private Map<String,Double> categoryScores;
	
	public CalaisClassification() {
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
}
