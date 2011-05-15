package uk.ac.cam.ha293.tweetlabel.types;

public class Pair<T1,T2> {

	private T1 item1;
	private T2 item2;
	
	public Pair(T1 item1, T2 item2) {
		this.item1 = item1;
		this.item2 = item2;
	}
	
	public T1 item1() {
		return item1;
	}
	
	public T2 item2() {
		return item2;
	}
	
}
