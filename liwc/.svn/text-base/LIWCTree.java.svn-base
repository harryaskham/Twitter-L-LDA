package uk.ac.cam.ha293.tweetlabel.liwc;

import java.io.IOException;
import java.io.Serializable;

public class LIWCTree implements Serializable {

	private static final long serialVersionUID = 5000836694401289673L;
	
	private String word;
	private LIWCTree left;
	private LIWCTree right;
	
	public LIWCTree(String word) {
		this.word = word;
		left = null;
		right = null;
	}
	
	public String word() {
		return word;
	}
	
	public LIWCTree left() {
		return left;
	}
	
	public LIWCTree right() {
		return right;
	}
	
	public void insert(String word) {
		if(this.word.equals(word)) return;

		
		if(stringLTE(word, this.word)) {
			if(left == null) {
				left = new LIWCTree(word);
			} else {			
				left.insert(word);
			}
		} else {
			if(right == null) {				
				right = new LIWCTree(word);
			} else {	
				right.insert(word);
			}
		}
	}
	
	public String lookup(String word) {
		if(word.contains(stripStars(this.word))) return this.word;
		
		if(stringLTE(word,this.word)) {
			if(left == null) {
				return null;
			} else {
				return left.lookup(word);
			}
		} else {
			if(right == null) {
				return null;
			} else {
				return right.lookup(word);
			}
		}
	}
	
	public String stripStars(String word) {
		return word.replaceAll("[*]", "");		
	}
	
	public static boolean stringLTE(String word1, String word2) {
		char[] w1 = word1.toCharArray();
		char[] w2 = word2.toCharArray();
		int w1length = word1.length();
		int w2length = word2.length();
		int biglength = w1length;
		if(w2length < w1length) biglength = w2length;
		for(int i=0; i<biglength; i++) {
			if(w1[i] < w2[i]) return true;
			else if(w1[i] > w2[i]) return false;
			else continue;
		}
		
		if(w1length < w2length) return true;
		else return false;
	}
	
	public void print() {
		System.out.print("(");
		left.print();
		System.out.print(")"+word+"(");
		right.print();
		System.out.print(")");
	}

}
