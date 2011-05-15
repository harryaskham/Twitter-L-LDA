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

public class FullLLDAClassification {

	private long userID;
	private Map<String,Double> classifications; 
	
	public FullLLDAClassification(String topicType, double alpha, long userID) {
		this.userID = userID;
		//Construct the path to the classification file
		String path = "classifications/llda/"+topicType+"/1000-100-"+alpha;
		path += "/"+userID+".csv";
		
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
	}
	
	public FullLLDAClassification(String topicType, double alpha, boolean fewerProfiles, int reduction, long userID) {
		this.userID = userID;
		//Construct the path to the classification file
		String path = "";
		if(fewerProfiles) path = "classifications/fewerprofiles/"+reduction+"/llda/"+topicType+"/1000-100-"+alpha;
		else path = "classifications/fewertweets/"+reduction+"/llda/"+topicType+"/1000-100-"+alpha;
		path += "/"+userID+".csv";
		
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
	
	public double jsDivergence(FullLLDAClassification c) {
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
	
	public double jsDivergence(FullAlchemyClassification c) {
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
	
	public double jsDivergence(FullCalaisClassification c) {
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
