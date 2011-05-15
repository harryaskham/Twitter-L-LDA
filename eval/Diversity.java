package uk.ac.cam.ha293.tweetlabel.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import uk.ac.cam.ha293.tweetlabel.classify.FullAlchemyClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullCalaisClassification;
import uk.ac.cam.ha293.tweetlabel.topics.FullLDAClassification;
import uk.ac.cam.ha293.tweetlabel.topics.FullLLDAClassification;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class Diversity {
	
	private static Set<Double> diversitySet(String topicType, long uid) {
		Set<Double> valueSet = new HashSet<Double>();
		if(topicType.equals("alchemy")) {
			FullAlchemyClassification c = new FullAlchemyClassification(uid);
			for(String cat : c.getCategorySet()) {
				valueSet.add(c.getScore(cat));
			}
		} else if(topicType.equals("calais")) {
			FullCalaisClassification c = new FullCalaisClassification(uid);
			for(String cat : c.getCategorySet()) {
				valueSet.add(c.getScore(cat));
			}
		} else if(topicType.equals("textwise")) {
			FullCalaisClassification c = new FullCalaisClassification(uid);
			for(String cat : c.getCategorySet()) {
				valueSet.add(c.getScore(cat));
			}
		}
		return valueSet;
	}
	
	public static Set<Double> diversitySet(String topicType, double alpha, long uid) {
		Set<Double> valueSet = new HashSet<Double>();
		if(topicType.equals("lda")) {
			FullLDAClassification c = new FullLDAClassification(uid,1000,100,0,alpha);
			for(String cat : c.getCategorySet()) {
				valueSet.add(c.getScore(cat));
			}
		} else if(topicType.equals("alchemy")) {
			FullLLDAClassification c = new FullLLDAClassification("alchemy",alpha,uid);
			for(String cat : c.getCategorySet()) {
				valueSet.add(c.getScore(cat));
			}
		} else if(topicType.equals("calais")) {
			FullLLDAClassification c = new FullLLDAClassification("calais",alpha,uid);
			for(String cat : c.getCategorySet()) {
				valueSet.add(c.getScore(cat));
			}
		} else if(topicType.equals("textwise")) {
			FullLLDAClassification c = new FullLLDAClassification("textwise",alpha,uid);
			for(String cat : c.getCategorySet()) {
				valueSet.add(c.getScore(cat));
			}
		}
		return valueSet;
	}
	
	public static double simpson(String topicType, long uid) {
		return simpson(diversitySet(topicType, uid));
	}
	
	public static double simpson(String topicType, double alpha, long uid) {
		return simpson(diversitySet(topicType,alpha,uid));
	}
	
	public static double simpson(Set<Double> values) {
		double result = 0.0;
		for(Double value : values) {
			result += value*value;
		}
		return result;
	}
	
	public static double shannon(String topicType, long uid) {
		return shannon(diversitySet(topicType, uid));
	}
	
	public static double shannon(String topicType, double alpha, long uid) {
		return shannon(diversitySet(topicType,alpha,uid));
	}
	
	public static double shannon(Set<Double> values) {
		double sum = 0.0;
		for(Double value : values) {
			sum += value * Math.log(value);
		}
		return -1.0*sum;
	}
	
	//saves baseline and inferred API - no LDA
	public static void saveDiversities() {
		String[] topicTypes = {"alchemy","calais","textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		try {
			for(String topicType : topicTypes) {
				System.out.println("Diversities for "+topicType);
				//get the baselines
				String fileName = "diversities/baseline-"+topicType;
				FileOutputStream simpsonFileOut = new FileOutputStream(fileName+"-simpson.csv");
				FileOutputStream shannonFileOut = new FileOutputStream(fileName+"-shannon.csv");
				PrintWriter simpsonWriteOut = new PrintWriter(simpsonFileOut);
				PrintWriter shannonWriteOut = new PrintWriter(shannonFileOut);
				simpsonWriteOut.println("\"uid\",\"diversity\"");
				for(long uid : Tools.getCSVUserIDs()) {
					simpsonWriteOut.println(uid+","+Diversity.simpson(topicType, uid));
				}
				simpsonWriteOut.close();
				shannonWriteOut.println("\"uid\",\"diversity\"");
				for(long uid : Tools.getCSVUserIDs()) {
					shannonWriteOut.println(uid+","+Diversity.shannon(topicType, uid));
				}
				shannonWriteOut.close();
				for(double alpha : alphas) {
					System.out.println("Simpson Diversities for alpha "+alpha);
					fileName = "diversities/llda-"+topicType+"-"+alpha;
					FileOutputStream lldaSimpsonFileOut = new FileOutputStream(fileName+"-simpson.csv");
					PrintWriter lldaSimpsonWriteOut = new PrintWriter(lldaSimpsonFileOut);
					lldaSimpsonWriteOut.println("\"uid\",\"diversity\"");
					for(long uid : Tools.getCSVUserIDs()) {
						lldaSimpsonWriteOut.println(uid+","+Diversity.simpson(topicType, alpha, uid));
					}
					lldaSimpsonWriteOut.close();
				}
				for(double alpha : alphas) {
					System.out.println("Shannon Diversities for alpha "+alpha);
					fileName = "diversities/llda-"+topicType+"-"+alpha;
					FileOutputStream lldaShannonFileOut = new FileOutputStream(fileName+"-shannon.csv");
					PrintWriter lldaShannonWriteOut = new PrintWriter(lldaShannonFileOut);
					lldaShannonWriteOut.println("\"uid\",\"diversity\"");
					for(long uid : Tools.getCSVUserIDs()) {
						lldaShannonWriteOut.println(uid+","+Diversity.shannon(topicType, alpha, uid));
					}
					lldaShannonWriteOut.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Map<Long,Double> loadDiversities(String file) {
		Map<Long,Double> result = new HashMap<Long,Double>();
		String nextLine = "";
		String[] split = new String[2];
		try {
			FileInputStream fileIn = new FileInputStream(file+".csv");
			BufferedReader buffer = new BufferedReader(new InputStreamReader(fileIn));
			buffer.readLine(); //skip past the CSV descriptor
			while(true) {
				nextLine = buffer.readLine();
				//If nextLine is null, we still have to save the final profile!
				if(nextLine == null) {
					break;
				}
				split = nextLine.split(",");
				result.put(Long.parseLong(split[0]), Double.parseDouble(split[1]));
			}
		} catch (Exception e) {
			System.out.println(nextLine);
			System.out.println(split[0]);
			e.printStackTrace();
		}
		return result;
	}
	
	public static Map<Long,Double> loadDiversities(String topicType, String diversityType) {
		String file = "diversities/baseline-"+topicType+"-"+diversityType;
		return loadDiversities(file);
	}
	
	public static Map<Long,Double> loadDiversities(String topicType, double alpha, String diversityType) {
		String file = "diversities/llda-"+topicType+"-"+alpha+"-"+diversityType;
		return loadDiversities(file);
	}

}
