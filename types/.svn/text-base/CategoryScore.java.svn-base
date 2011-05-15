package uk.ac.cam.ha293.tweetlabel.types;

import java.io.Serializable;


public class CategoryScore implements Serializable, Comparable<CategoryScore> {

	private static final long serialVersionUID = -8651433241587027607L;
	
	private Category category;
	private double score;
	
	public CategoryScore(Category category, double score) {
		this.category = category;
		this.score = score;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public double getScore() {
		return score;
	}

	public int compareTo(CategoryScore newScore) {
		if(score < newScore.getScore()) return -1;
		if(score > newScore.getScore()) return 1;
		return 0;
	}

}
