package uk.ac.cam.ha293.tweetlabel.liwc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class FullLIWCClassification {
	
	private long userID;
	private Map<String,Double> classifications;
	
	public FullLIWCClassification(boolean naiveBayes, long userID) {
		this.userID = userID;
		classifications = new HashMap<String,Double>();
		
		try {
			FileInputStream fstream = new FileInputStream("classifications/liwc/"+userID+".csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			br.readLine(); //Skp the CSV descriptor
			while ((strLine = br.readLine()) != null)   {
				String[] split = strLine.split(",");
				String cat = split[0];
				double score = 0.0;
				if(!naiveBayes) {
					score = Double.parseDouble(split[1]);
				} else {
					score = Double.parseDouble(split[2]);
				}
				classifications.put(cat,score);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		
		//need to normalise the alchemy classifcations, durr
		if(!naiveBayes) {
			double scoreSum = 0.0;
			for(String cat : classifications.keySet()) {
				scoreSum += classifications.get(cat);
			}
			for(String cat : classifications.keySet()) {
				classifications.put(cat, classifications.get(cat)/scoreSum);
			}
		} else {
			//convert back from log scores
			double scoreSum = 0.0;
			for(String cat : classifications.keySet()) {
				classifications.put(cat, Math.pow(Math.E,classifications.get(cat)));
				scoreSum += classifications.get(cat);
			}
			//now normalise to sum to 1
			for(String cat : classifications.keySet()) {
				classifications.put(cat,classifications.get(cat)*(1/scoreSum));
			}
		}
		
		classifications = Tools.sortMapByValueDesc(classifications);
	}
	
	public void print() {
		double sum = 0.0;
		for(String cat : classifications.keySet()) {
			System.out.println(cat+": "+classifications.get(cat));
			sum += classifications.get(cat);
		}
		System.out.println(sum);
	}
	
	public Set<String> getCategorySet() {
		return classifications.keySet();
	}
	
	public boolean hasCategory(String cat) {
		if(classifications.keySet().contains(cat)) return true;
		else return false;
	}
	
	public double getScore(String cat) {
		return classifications.get(cat);
	}
	
	public double magnitude() {
		double sum = 0.0;
		for(String cat : classifications.keySet()) {
			sum += (classifications.get(cat)*classifications.get(cat));
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	public double cosineSimilarity(FullLIWCClassification c) {
		Set<String> catSet = new HashSet<String>();
		catSet.addAll(classifications.keySet());
		catSet.addAll(c.getCategorySet());
		
		double score = 0.0;
		for(String cat : catSet) {
			if(this.hasCategory(cat) && c.hasCategory(cat)) {
				score += (this.getScore(cat) * c.getScore(cat));
			}
		}
		
		//normalise by magnitudes
		Double magnitudes = (this.magnitude() * c.magnitude());
		score /= magnitudes;
		if(Double.isNaN(score)) {
			return 0.0; //NaN caused by zero vectors ie no classifications!
		}
		else return score;
	}
	
	
}
