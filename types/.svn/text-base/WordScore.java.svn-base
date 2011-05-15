package uk.ac.cam.ha293.tweetlabel.types;

import java.io.Serializable;


public class WordScore implements Serializable, Comparable<WordScore> {

	private static final long serialVersionUID = -8651433241587027607L;
	
	private String word;
	private double score;
	
	public WordScore(String word, double score) {
		this.word = word;
		this.score = score;
	}
	
	public String getWord() {
		return word;
	}
	
	public double getScore() {
		return score;
	}

	public int compareTo(WordScore newScore) {
		if(score < newScore.getScore()) return -1;
		if(score > newScore.getScore()) return 1;
		return 0;
	}

}