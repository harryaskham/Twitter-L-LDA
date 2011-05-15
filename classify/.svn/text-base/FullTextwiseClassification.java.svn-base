package uk.ac.cam.ha293.tweetlabel.classify;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ha293.tweetlabel.topics.FullLDAClassification;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class FullTextwiseClassification {
	
	private long userID;
	private Map<String,Double> classifications;
	
	public FullTextwiseClassification(long userID, boolean proper) {
		this.userID = userID;
		classifications = new HashMap<String,Double>();
		
		try {
			FileInputStream fstream;
			if(proper) fstream = new FileInputStream("classifications/textwiseproper/"+userID+".csv");
			else fstream = new FileInputStream("classifications/textwise/"+userID+".csv");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			br.readLine(); //Skp the CSV descriptor
			while ((strLine = br.readLine()) != null)   {
				//typical, some categories have commas in them!
				String[] initSplit = strLine.split("\"");
				String cat = initSplit[1];
				String[] split = initSplit[2].split(",");
				
				double score = 0.0;
					if(!proper) {
					//tweet scores
					if(Integer.parseInt(split[2]) != 0) {
						score += Double.parseDouble(split[1]) / Double.parseDouble(split[2]);
					}
					//url scores
					if(Integer.parseInt(split[4]) != 0) {
						score += Double.parseDouble(split[3]) / Double.parseDouble(split[4]);
					}
				} else {
					score = Double.parseDouble(split[1])/Double.parseDouble(split[2]);
				}
				
				if(proper) cat = cat.substring(0,cat.indexOf("/")); //take only the textwise root
				classifications.put(cat,score);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			System.err.println(userID);
		}
		
		//need to normalise the classifcations, durr
		double scoreSum = 0.0;
		for(String cat : classifications.keySet()) {
			scoreSum += classifications.get(cat);
		}
		for(String cat : classifications.keySet()) {
			classifications.put(cat, classifications.get(cat)/scoreSum);
		}
		
		classifications = Tools.sortMapByValueDesc(classifications);
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
	
	public double jsDivergence(FullTextwiseClassification c) {
		Set<String> catSet = new HashSet<String>();
		catSet.addAll(classifications.keySet());
		catSet.addAll(c.getCategorySet());
		Map<String,Double> M = new HashMap<String,Double>();
		for(String cat : catSet) {
			if(this.hasCategory(cat) && c.hasCategory(cat)) {
				M.put(cat, (this.getScore(cat)+c.getScore(cat))/2.0);
			}
		}
		double d1 = 0.0;
		for(String cat : M.keySet()) {
			if(this.getCategorySet().contains(cat) ) {
				d1 += this.getScore(cat) * Math.log(this.getScore(cat)/M.get(cat));
			}
		}	
		double d2 = 0.0;
		for(String cat : M.keySet()) {
			if(c.getCategorySet().contains(cat)) {
				d1 += c.getScore(cat) * Math.log(c.getScore(cat)/M.get(cat));
			}
		}		
		double score = d1/2.0 + d2/2.0;
		return score;
	}
	
}
