package uk.ac.cam.ha293.tweetlabel.eval;

public class SimilarityPair implements Comparable<SimilarityPair> {
	
	private long uid1;
	private long uid2;
	private Double similarity;
	
	public SimilarityPair(SimilarityMatrix sm, long uid1, long uid2) {
		this.uid1 = uid1;
		this.uid2 = uid2;
		similarity = sm.getSimilarity(uid1, uid2);
	}
	
	public long uid1() {
		return uid1;
	}
	
	public long uid2() {
		return uid2;
	}
	
	public double similarity() {
		return similarity;
	}

	@Override
	public int compareTo(SimilarityPair sp) {
		//can be used to detect existence in a list, also to sort
		if(similarity == sp.similarity()) {
			return 0;
		} else if(similarity < sp.similarity()) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public boolean equals(Object o) {
		if(o instanceof SimilarityPair) {
			SimilarityPair sp = (SimilarityPair)o;
			if((uid1 == sp.uid1() && uid2 == sp.uid2()) || (uid1 == sp.uid2() && uid2 == sp.uid1())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		int hash = (int)(uid1 + uid2);
		return hash;
	}
	
	public void print() {
		System.out.println(uid1+","+uid2+":"+similarity);
	}

}
