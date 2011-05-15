package uk.ac.cam.ha293.tweetlabel.topics;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ha293.tweetlabel.classify.FullAlchemyClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullCalaisClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullTextwiseClassification;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class FullSVMClassification {
	private long userID;
	private Map<String,Double> classifications; 
	
	public FullSVMClassification(String topicType, long userID) {
		this.userID = userID;
		//Construct the path to the classification file
		String path = "classifications/svm/"+topicType+"/"+userID+".csv";
		
		classifications = new HashMap<String,Double>();
		
		try {
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			br.readLine(); //Skp the CSV descriptor
			while ((strLine = br.readLine()) != null)   {
				//no quotes, damn
				int splitIndex = strLine.lastIndexOf(",");
				String cat = strLine.substring(0,splitIndex);
				double score = Double.parseDouble(strLine.substring(splitIndex+1,strLine.length()));
				classifications.put(cat,score);
			}
			in.close();
		} catch (Exception e) {
			//System.err.println("Error: " + e.getMessage());
		}
		classifications = Tools.sortMapByValueDesc(classifications);
		
		//normalisation - make it so that after correction, lowest topic will be 0.0
		//is this fair?
		//could only normalise positive values...
		double magSum = 0.0;
		for(String topic : classifications.keySet()) {
			magSum += Math.abs(classifications.get(topic));
		}
		double min = Double.MAX_VALUE;
		for(String topic : classifications.keySet()) {
			double newScore = classifications.get(topic)/magSum;
			if(newScore<min) min = newScore;
			classifications.put(topic, newScore);
		}
		if(min<0) {
			for(String topic : classifications.keySet()) {
				classifications.put(topic, classifications.get(topic)+Math.abs(min));
			}
		}
	}
	
	public void print() {
		for(String cat : classifications.keySet()) {
			System.out.println(cat+": "+classifications.get(cat));
		}
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

	public double cosineSimilarity(FullSVMClassification c) {
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
	
	public double cosineSimilarity(FullLLDAClassification c) {
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
	
	//horrible hacky duplicates coming up
	public double cosineSimilarity(FullAlchemyClassification c) {
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
	
	public double cosineSimilarity(FullCalaisClassification c) {
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
	
	public double cosineSimilarity(FullTextwiseClassification c) {
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
	//end of hacky duplications
	
}
