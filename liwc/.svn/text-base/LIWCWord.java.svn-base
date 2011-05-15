package uk.ac.cam.ha293.tweetlabel.liwc;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import uk.ac.cam.ha293.tweetlabel.types.Category;

public class LIWCWord implements Serializable, Comparable<LIWCWord> {

	private static final long serialVersionUID = 1226473677648649283L;

	private String word;
	private Set<Category> categories;
	
	public LIWCWord(String word) {
		this.word = word;
		categories = new HashSet<Category>();
	}
	
	public void addCategory(Category category) {
		categories.add(category);
	}
	
	public String getWord() {
		return word;
	}
	
	public Set<Category> getCategories() {
		return categories;
	}

	public int compareTo(LIWCWord newLIWCWord) {
		if(word.equals(newLIWCWord.getWord())) {
			return 0;
		} else {
			return 1;
		}
	}
	
	public void print() {
		System.out.println("Word: "+word);
		for(Category category : categories) {
			System.out.print(category.getTitle()+" ");
		}
		System.out.println();
	}
	
}
