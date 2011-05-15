package uk.ac.cam.ha293.tweetlabel.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;

import uk.ac.cam.ha293.tweetlabel.classify.FullAlchemyClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullCalaisClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullTextwiseClassification;
import uk.ac.cam.ha293.tweetlabel.topics.FullLLDAClassification;
import uk.ac.cam.ha293.tweetlabel.topics.FullSVMClassification;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

//note: this class needs to handle both inferred vs baseline similarity,
//and also needs to be able to determine which topics are most similar.
public class CosineManager {

	public static void similarityReport(String topicType, double alpha) {
		List<Long> uids = Tools.getCSVUserIDs();
		double cosineSum = 0.0;
		int cosineCount = 0;
		double squareSum = 0.0;
		for(Long uid : uids) {
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,uid);
				double sim = inferred.cosineSimilarity(baseline);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("calais")) {
				FullCalaisClassification baseline = new FullCalaisClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,uid);
				double sim = inferred.cosineSimilarity(baseline);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
				FullLLDAClassification inferred = new FullLLDAClassification("textwiseproper",alpha,uid);
				double sim = inferred.cosineSimilarity(baseline);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			}
			//System.out.println("UID:"+uid+", CS:"+sim);
			//if(cosineCount % 100 == 0) System.out.println(cosineCount);
		}
		double sd = Math.sqrt((1.0/cosineCount)*squareSum - Math.pow((cosineSum/cosineCount),2));
		double avg = cosineSum/cosineCount;
		System.out.println(topicType+"\t"+alpha+"\t"+avg+"\t"+sd);
	}
	
	public static void kSimilarityReport(String topicType, double alpha, int k) {
		List<Long> uids = Tools.getCSVUserIDs();
		double cosineSum = 0.0;
		int cosineCount = 0;
		double squareSum = 0.0;
		for(Long uid : uids) {
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,uid);
				double sim = cosineKSimilarity(baseline,inferred,k);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("calais")) {
				FullCalaisClassification baseline = new FullCalaisClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,uid);
				double sim = cosineKSimilarity(baseline,inferred,k);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
				FullLLDAClassification inferred = new FullLLDAClassification("textwiseproper",alpha,uid);
				double sim = cosineKSimilarity(baseline,inferred,k);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			}
			//System.out.println("UID:"+uid+", CS:"+sim);
			//if(cosineCount % 100 == 0) System.out.println(cosineCount);
		}
		double sd = Math.sqrt((1.0/cosineCount)*squareSum - Math.pow((cosineSum/cosineCount),2));
		double avg = cosineSum/cosineCount;
		System.out.println(topicType+"\t"+alpha+"\t"+avg+"\t"+sd);
	}
	
	public static void similarityReport(String topicType, double alpha, boolean fewerProfiles, int reduction) {
		List<Long> uids = Tools.getCSVUserIDs();
		double cosineSum = 0.0;
		int cosineCount = 0;
		double squareSum = 0.0;
		for(Long uid : uids) {
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,fewerProfiles,reduction,uid);
				if(inferred.getCategorySet().isEmpty()) continue;
				double sim = inferred.cosineSimilarity(baseline);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("calais")) {
				FullCalaisClassification baseline = new FullCalaisClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,fewerProfiles,reduction,uid);
				if(inferred.getCategorySet().isEmpty()) continue;
				double sim = inferred.cosineSimilarity(baseline);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
				FullLLDAClassification inferred = new FullLLDAClassification("textwiseproper",alpha,fewerProfiles,reduction,uid);
				if(inferred.getCategorySet().isEmpty()) continue;
				double sim = inferred.cosineSimilarity(baseline);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			}
			//System.out.println("UID:"+uid+", CS:"+sim);
			//if(cosineCount % 100 == 0) System.out.println(cosineCount);
		}
		double sd = Math.sqrt((1.0/cosineCount)*squareSum - Math.pow((cosineSum/cosineCount),2));
		double avg = cosineSum/cosineCount;
		System.out.println(topicType+"\t"+alpha+"\t"+avg+"\t"+sd);
	}
	
	public static void kSimilarityReport(String topicType, double alpha, boolean fewerProfiles, int reduction, int k) {
		List<Long> uids = Tools.getCSVUserIDs();
		double cosineSum = 0.0;
		int cosineCount = 0;
		double squareSum = 0.0;
		for(Long uid : uids) {
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,fewerProfiles,reduction,uid);
				if(inferred.getCategorySet().isEmpty()) continue;
				double sim = cosineKSimilarity(baseline,inferred,k);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("calais")) {
				FullCalaisClassification baseline = new FullCalaisClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,fewerProfiles,reduction,uid);
				if(inferred.getCategorySet().isEmpty()) continue;
				double sim = cosineKSimilarity(baseline,inferred,k);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
				FullLLDAClassification inferred = new FullLLDAClassification("textwiseproper",alpha,fewerProfiles,reduction,uid);
				if(inferred.getCategorySet().isEmpty()) continue;
				double sim = cosineKSimilarity(baseline,inferred,k);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			}
			//System.out.println("UID:"+uid+", CS:"+sim);
			//if(cosineCount % 100 == 0) System.out.println(cosineCount);
		}
		double sd = Math.sqrt((1.0/cosineCount)*squareSum - Math.pow((cosineSum/cosineCount),2));
		double avg = cosineSum/cosineCount;
		System.out.println(topicType+"\t"+alpha+"\t"+avg+"\t"+sd);
	}
	
	public static void fullSimilarityReport() {
		String[] topicTypes = {"alchemy","calais","textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				similarityReport(topicType,alpha);
			}
		}
	}
	
	public static void fullKSimilarityReport(int k) {
		String[] topicTypes = {"alchemy","calais","textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				kSimilarityReport(topicType,alpha,k);
			}
		}
	}
	
	public static void svmSimilarityReport(String topicType) {
		double cosineSum = 0.0;
		int cosineCount = 0;
		double squareSum = 0.0;
		for(Long uid : Tools.getCSVUserIDs()) {
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification fac = new FullAlchemyClassification(uid);
				FullSVMClassification fsm = new FullSVMClassification(topicType,uid);
				double sim = fsm.cosineSimilarity(fac);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("calais")) {
				FullCalaisClassification fcc = new FullCalaisClassification(uid);
				FullSVMClassification fsm = new FullSVMClassification(topicType,uid);
				double sim = fsm.cosineSimilarity(fcc);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification ftc = new FullTextwiseClassification(uid,true);
				FullSVMClassification fsm = new FullSVMClassification(topicType,uid);
				double sim = fsm.cosineSimilarity(ftc);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			}
		}
		double sd = Math.sqrt((1.0/cosineCount)*squareSum - Math.pow((cosineSum/cosineCount),2));
		double avg = cosineSum/cosineCount;
		System.out.println(topicType+"\t"+avg+"\t"+sd);
	}
	
	public static void topKSVMSimilarityReport(String topicType, int k) {
		double cosineSum = 0.0;
		int cosineCount = 0;
		double squareSum = 0.0;
		for(Long uid : Tools.getCSVUserIDs()) {
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification fac = new FullAlchemyClassification(uid);
				FullSVMClassification fsm = new FullSVMClassification(topicType,uid);
				double sim = cosineKSimilarity(fac,fsm,k);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("calais")) {
				FullCalaisClassification fcc = new FullCalaisClassification(uid);
				FullSVMClassification fsm = new FullSVMClassification(topicType,uid);
				double sim = cosineKSimilarity(fcc,fsm,k);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification ftc = new FullTextwiseClassification(uid,true);
				FullSVMClassification fsm = new FullSVMClassification(topicType,uid);
				double sim = cosineKSimilarity(ftc,fsm,k);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			}
		}
		double sd = Math.sqrt((1.0/cosineCount)*squareSum - Math.pow((cosineSum/cosineCount),2));
		double avg = cosineSum/cosineCount;
		System.out.println(topicType+"\t"+avg+"\t"+sd);
	}
	
	public static void topicAnalysis(String topicType, double alpha) {
		List<Long> uids = Tools.getCSVUserIDs();
		Map<String,Set<Long>> profileSets = new HashMap<String,Set<Long>>();
		Map<Long,String> userSetLookup = new HashMap<Long,String>();
		//firstly create the baseline profile sets
		//IMPORTANT: profiles with no classifications aren't included in sets!
		for(Long uid : uids) {
			String topTopic = "";
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification c = new FullAlchemyClassification(uid);
				if(c.getCategorySet().size()==0) continue;
				topTopic = c.getCategorySet().toArray(new String[0])[0];
			} else if(topicType.equals("calais")) {
				FullCalaisClassification c = new FullCalaisClassification(uid);
				if(c.getCategorySet().size()==0) continue;
				topTopic = c.getCategorySet().toArray(new String[0])[0];
			} else if(topicType.equals("textwiseproper")) {
				FullTextwiseClassification c = new FullTextwiseClassification(uid,true);
				if(c.getCategorySet().size()==0) continue;
				topTopic = c.getCategorySet().toArray(new String[0])[0];
			}
			if(profileSets.containsKey(topTopic)) {
				profileSets.get(topTopic).add(uid);
			} else {
				Set<Long> newSet = new HashSet<Long>();
				newSet.add(uid);
				profileSets.put(topTopic,newSet);
			}
			userSetLookup.put(uid, topTopic);
		}
		
		//System.out.println("Created top-topic profile sets, "+profileSets.size()+" sets in total");
		
		//then look up the inferred topics for each profile and compare it to the sets
		Map<String,Double> averageTopicCosinesSum = new HashMap<String,Double>();
		Map<String,Integer> averageTopicCosinesCount = new HashMap<String,Integer>();
		Map<String,Integer> correctTopTopicClassifications = new HashMap<String,Integer>();
		Map<Long,Double> similarities = CosineManager.loadCosineSimilarities(topicType, alpha);
		for(Long uid : uids) {
			//work out cosine similarity, contribute to average
			double sim = similarities.get(uid);
			String setName = userSetLookup.get(uid);
			if(averageTopicCosinesSum.containsKey(setName)) {
				averageTopicCosinesSum.put(setName, averageTopicCosinesSum.get(setName)+sim);
				averageTopicCosinesCount.put(setName, averageTopicCosinesCount.get(setName)+1);
			} else {
				averageTopicCosinesSum.put(setName, sim);
				averageTopicCosinesCount.put(setName, 1);
			}
			
			//work out top-topic, does it match?
			FullLLDAClassification llda = new FullLLDAClassification(topicType,alpha,uid);
			if(llda.getCategorySet().size()==0) continue;
			String topTopic = llda.getCategorySet().toArray(new String[0])[0];
			if(topTopic.equals(userSetLookup.get(uid))) {
				//managed to correctly infer the most prominent topic
				if(correctTopTopicClassifications.containsKey(topTopic)) {
					correctTopTopicClassifications.put(topTopic, correctTopTopicClassifications.get(topTopic)+1);
				} else {
					correctTopTopicClassifications.put(topTopic, 1);
				}
			}
		}
		
		/*
		//normalise and sort hashmaps for topic average cosines
		for(String topic : averageTopicCosinesSum.keySet()) {
			averageTopicCosinesSum.put(topic, averageTopicCosinesSum.get(topic)/averageTopicCosinesCount.get(topic));
		}
		Map<String,Double> averageTopicCosines = Tools.sortMapByValueDesc(averageTopicCosinesSum);
		System.out.println("Average cosine similarities per top-topic profile for "+topicType+" "+alpha);
		for(String topic : averageTopicCosines.keySet()) {
			System.out.println(topic+","+averageTopicCosines.get(topic));
		}
		System.out.println();
		*/

		//normalise and sort hashmaps for top-topic assignments
		Map<String,Double> correctTopTopicProportions = new HashMap<String,Double>();
		for(String topic : correctTopTopicClassifications.keySet()) {
			//change the map so that it stores (# of correct classifications for topic / total number of profiles in topic)
			correctTopTopicProportions.put(topic, (double)correctTopTopicClassifications.get(topic)/profileSets.get(topic).size());
		}
		correctTopTopicProportions = Tools.sortMapByValueDesc(correctTopTopicProportions);
		System.out.println("% of profiles with top-topic correctly inferred for "+topicType+" "+alpha);
		/*
		for(String topic : correctTopTopicProportions.keySet()) {
			System.out.println(topic+","+correctTopTopicProportions.get(topic));
		}*/
		for(String topic : correctTopTopicProportions.keySet()) {
			System.out.println(topic);
		}
		for(String topic : correctTopTopicProportions.keySet()) {
			System.out.println(correctTopTopicProportions.get(topic));
		}
	}
	
	public static void fullTopicAnalysis() {
		String[] topicTypes = {"alchemy","calais","textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				topicAnalysis(topicType,alpha);
			}
		}
	}
	
	public static void createCosineSimilarities() {
		//String[] topicTypes = {"alchemy","calais","textwise"};
		String[] topicTypes = {"textwiseproper"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		List<Long> uids = Tools.getCSVUserIDs();
		try {
			for(String topicType : topicTypes) {
				for(double alpha : alphas) {
					System.out.println("Saving cosine sims for "+topicType+" "+alpha);
					FileOutputStream fileOut = new FileOutputStream("cosinesims/"+topicType+"-"+alpha+".csv");
					PrintWriter writeOut = new PrintWriter(fileOut);
					writeOut.println("\"uid\",\"similarity\"");
					if(topicType.equals("alchemy")) {
						for(long uid : uids) {
							FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
							FullLLDAClassification llda = new FullLLDAClassification(topicType,alpha,uid);
							writeOut.println(uid+","+llda.cosineSimilarity(baseline));
						}
					} else if(topicType.equals("calais")) {
						for(long uid : uids) {
							FullCalaisClassification baseline = new FullCalaisClassification(uid);
							FullLLDAClassification llda = new FullLLDAClassification(topicType,alpha,uid);
							writeOut.println(uid+","+llda.cosineSimilarity(baseline));
						}
					} else if(topicType.equals("textwise")) {
						for(long uid : uids) {
							FullTextwiseClassification baseline = new FullTextwiseClassification(uid,false);
							FullLLDAClassification llda = new FullLLDAClassification(topicType,alpha,uid);
							writeOut.println(uid+","+llda.cosineSimilarity(baseline));
						}
					}else if(topicType.equals("textwiseproper")) {
						for(long uid : uids) {
							FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
							FullLLDAClassification llda = new FullLLDAClassification(topicType,alpha,uid);
							writeOut.println(uid+","+llda.cosineSimilarity(baseline));
						}
					}
					writeOut.close();
					fileOut.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Map<Long,Double> loadCosineSimilarities(String topicType, double alpha) {
		Map<Long,Double> result = new HashMap<Long,Double>();
		String nextLine = "";
		String[] split = new String[2];
		try {
			FileInputStream fileIn = new FileInputStream("cosinesims/"+topicType+"-"+alpha+".csv");
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
	
	public static double cosineSimilarity(Map<String,Double> v1, Map<String,Double> v2) {
		double sum = 0.0;
		for(String cat : v1.keySet()) {
			sum += (v1.get(cat)*v1.get(cat));
		}
		double v1mag = Math.sqrt(sum);
		sum = 0.0;
		for(String cat : v2.keySet()) {
			sum += (v2.get(cat)*v2.get(cat));
		}
		double v2mag = Math.sqrt(sum);
		
		Set<String> catSet = new HashSet<String>();
		catSet.addAll(v1.keySet());
		catSet.addAll(v2.keySet());
		
		double score = 0.0;
		for(String cat : catSet) {
			if(v1.containsKey(cat) && v2.containsKey(cat)) {
				score += (v1.get(cat) * v2.get(cat));
			}
		}
		
		Double magnitudes = (v1mag * v2mag);
		score /= magnitudes;
		if(Double.isNaN(score)) {
			return 0.0; //NaN caused by zero vectors ie no classifications!
		}
		else return score;
	}
	
	public static void jsSimilarityReport(String topicType, double alpha) {
		//System.out.println("JS Divergences to Baseline for "+topicType+" "+alpha+":");
		List<Long> uids = Tools.getCSVUserIDs();
		double cosineSum = 0.0;
		double squareSum = 0.0;
		int cosineCount = 0;
		for(Long uid : uids) {
			//System.out.println(cosineCount);
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification baseline = new FullAlchemyClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,uid);
				double sim = inferred.jsDivergence(baseline);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("calais")) {
				FullCalaisClassification baseline = new FullCalaisClassification(uid);
				FullLLDAClassification inferred = new FullLLDAClassification(topicType,alpha,uid);
				double sim = inferred.jsDivergence(baseline);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification baseline = new FullTextwiseClassification(uid,true);
				FullLLDAClassification inferred = new FullLLDAClassification("textwiseproper",alpha,uid);
				double sim = inferred.jsDivergence(baseline);
				cosineSum += sim;
				squareSum += sim*sim;
				cosineCount++;
			}
			//System.out.println("UID:"+uid+", CS:"+sim);
			//if(cosineCount % 100 == 0) System.out.println(cosineCount);
		}
		double sd = Math.sqrt((1.0/cosineCount)*squareSum - Math.pow((cosineSum/cosineCount),2));
		System.out.println(topicType+","+alpha+","+cosineSum/cosineCount+","+sd);
	}
	
	public static void fullJSSimilarityReport() {
		String[] topicTypes = {"alchemy","calais","textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				jsSimilarityReport(topicType,alpha);
			}
		}
	}
	
	public static void precisionRecallReport(String topicType, double alpha, boolean svm) {
		Map<String,Set<Long>> gtTopicSets = new HashMap<String,Set<Long>>();
		Map<String,Set<Long>> lldaTopicSets = new HashMap<String,Set<Long>>();
		for(String topic : Tools.getTopics(topicType)) {
			gtTopicSets.put(topic, new HashSet<Long>());
			lldaTopicSets.put(topic, new HashSet<Long>());
		}
		Set<Long> noClassifications = new HashSet<Long>();
		for(Long uid : Tools.getCSVUserIDs()) {
			String topTopic = "";
			if(topicType.equals("alchemy")) {
				FullAlchemyClassification c = new FullAlchemyClassification(uid);
				if(c.getCategorySet().size()==0) {
					noClassifications.add(uid);
					continue;
				}
				topTopic = c.getCategorySet().toArray(new String[1])[0];
			} else if(topicType.equals("calais")) {
				FullCalaisClassification c = new FullCalaisClassification(uid);
				if(c.getCategorySet().size()==0) {
					noClassifications.add(uid);
					continue;
				}
				topTopic = c.getCategorySet().toArray(new String[1])[0];
				if(topTopic.equals("Other") && c.getCategorySet().size() < 2) {
					noClassifications.add(uid);
					continue;
				} else if(topTopic.equals("Other")) {
					topTopic = c.getCategorySet().toArray(new String[1])[1];
				}
			} else if(topicType.equals("textwise")) {
				FullTextwiseClassification c = new FullTextwiseClassification(uid,true);
				if(c.getCategorySet().size()==0) {
					noClassifications.add(uid);
					continue;
				}
				topTopic = c.getCategorySet().toArray(new String[1])[0];
			}
			if(svm) {
				FullSVMClassification svmClassification = new FullSVMClassification(topicType,uid);
				String topSVMTopic = svmClassification.getCategorySet().toArray(new String[1])[0];
				gtTopicSets.get(topTopic).add(uid);
				lldaTopicSets.get(topSVMTopic).add(uid);
			} else {
				FullLLDAClassification llda = new FullLLDAClassification(topicType,alpha,uid);
				if(topicType.equals("textwise")) llda = new FullLLDAClassification("textwiseproper",alpha,uid); 
				String topLLDATopic = llda.getCategorySet().toArray(new String[1])[0];
				gtTopicSets.get(topTopic).add(uid);
				lldaTopicSets.get(topLLDATopic).add(uid);
			}
		}
		
		Map<String,Double> topicPrecisions = new HashMap<String,Double>();
		Map<String,Double> topicRecalls = new HashMap<String,Double>();
		Map<String,Double> topicFs = new HashMap<String,Double>();
		for(String topic : gtTopicSets.keySet()) {
			Set<Long> gtSet = gtTopicSets.get(topic);
			Set<Long> lldaSet = lldaTopicSets.get(topic);
			Set<Long> intersection = new HashSet<Long>(gtSet);
			intersection.retainAll(lldaSet);
			//System.out.println(topic+", intersection: "+intersection.size()+", retrieved: "+lldaSet.size()+", relevant: "+gtSet.size());
			double precision = (double)intersection.size() / lldaSet.size();
			double recall = (double)intersection.size() / gtSet.size();
			double f = (2*precision*recall)/(precision+recall);
			topicPrecisions.put(topic, precision);
			topicRecalls.put(topic, recall);
			topicFs.put(topic, f);
		}
		
		topicFs = Tools.sortMapByValueDesc(topicFs);
		System.out.println("TOPIC RESULTS FOR "+topicType+" alpha "+alpha);
		System.out.println("topic\tprecision\trecall\tF");
		for(String topic : topicFs.keySet()) {
			System.out.println(topic+"\t"+topicPrecisions.get(topic)+"\t"+topicRecalls.get(topic)+"\t"+topicFs.get(topic));
		}
	}
	
	public static void fullPR(boolean svm) {
		String[] topicTypes = {"textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				precisionRecallReport(topicType,alpha,svm);
			}
		}
	}
	
	public static double cosineKSimilarity(FullAlchemyClassification c1, FullSVMClassification c2, int k) {
		Set<String> catSet = new HashSet<String>();
		String[] topics1 = c1.getCategorySet().toArray(new String[1]);
		String[] topics2 = c2.getCategorySet().toArray(new String[1]);
		for(int i=0;i<k;i++) {
			if(i<topics1.length) catSet.add(topics1[i]);
			if(i<topics2.length) catSet.add(topics2[i]);
		}
		
		double mag1 = 0.0;
		double mag2 = 0.0;
		double score = 0.0;
		for(String cat : catSet) {
			if(c1.hasCategory(cat) && c2.hasCategory(cat)) {
				score += (c1.getScore(cat) * c2.getScore(cat));
				mag1 += c1.getScore(cat)*c1.getScore(cat);
				mag2 += c2.getScore(cat)*c2.getScore(cat);
			}
		}
		mag1 = Math.sqrt(mag1);
		mag2 = Math.sqrt(mag2);

		//normalise by magnitudes
		Double magnitudes = (mag1 * c2.magnitude());
		score /= magnitudes;
		if(Double.isNaN(score)) {
			return 0.0; //NaN caused by zero vectors ie no classifications!
		}
		else return score;
	}
	
	public static double cosineKSimilarity(FullCalaisClassification c1, FullSVMClassification c2, int k) {
		Set<String> catSet = new HashSet<String>();
		String[] topics1 = c1.getCategorySet().toArray(new String[1]);
		String[] topics2 = c2.getCategorySet().toArray(new String[1]);
		for(int i=0;i<k;i++) {
			if(i<topics1.length) catSet.add(topics1[i]);
			if(i<topics2.length) catSet.add(topics2[i]);
		}
		
		double mag1 = 0.0;
		double mag2 = 0.0;
		double score = 0.0;
		for(String cat : catSet) {
			if(c1.hasCategory(cat) && c2.hasCategory(cat)) {
				score += (c1.getScore(cat) * c2.getScore(cat));
				mag1 += c1.getScore(cat)*c1.getScore(cat);
				mag2 += c2.getScore(cat)*c2.getScore(cat);
			}
		}
		mag1 = Math.sqrt(mag1);
		mag2 = Math.sqrt(mag2);

		//normalise by magnitudes
		Double magnitudes = (mag1 * c2.magnitude());
		score /= magnitudes;
		if(Double.isNaN(score)) {
			return 0.0; //NaN caused by zero vectors ie no classifications!
		}
		else return score;
	}
	
	public static double cosineKSimilarity(FullTextwiseClassification c1, FullSVMClassification c2, int k) {
		Set<String> catSet = new HashSet<String>();
		String[] topics1 = c1.getCategorySet().toArray(new String[1]);
		String[] topics2 = c2.getCategorySet().toArray(new String[1]);
		for(int i=0;i<k;i++) {
			if(i<topics1.length) catSet.add(topics1[i]);
			if(i<topics2.length) catSet.add(topics2[i]);
		}
		
		double mag1 = 0.0;
		double mag2 = 0.0;
		double score = 0.0;
		for(String cat : catSet) {
			if(c1.hasCategory(cat) && c2.hasCategory(cat)) {
				score += (c1.getScore(cat) * c2.getScore(cat));
				mag1 += c1.getScore(cat)*c1.getScore(cat);
				mag2 += c2.getScore(cat)*c2.getScore(cat);
			}
		}
		mag1 = Math.sqrt(mag1);
		mag2 = Math.sqrt(mag2);

		//normalise by magnitudes
		Double magnitudes = (mag1 * c2.magnitude());
		score /= magnitudes;
		if(Double.isNaN(score)) {
			return 0.0; //NaN caused by zero vectors ie no classifications!
		}
		else return score;
	}
	
	public static double cosineKSimilarity(FullAlchemyClassification c1, FullLLDAClassification c2, int k) {
		Set<String> catSet = new HashSet<String>();
		String[] topics1 = c1.getCategorySet().toArray(new String[1]);
		String[] topics2 = c2.getCategorySet().toArray(new String[1]);
		for(int i=0;i<k;i++) {
			if(i<topics1.length) catSet.add(topics1[i]);
			if(i<topics2.length) catSet.add(topics2[i]);
		}
		
		double mag1 = 0.0;
		double mag2 = 0.0;
		double score = 0.0;
		for(String cat : catSet) {
			if(c1.hasCategory(cat) && c2.hasCategory(cat)) {
				score += (c1.getScore(cat) * c2.getScore(cat));
				mag1 += c1.getScore(cat)*c1.getScore(cat);
				mag2 += c2.getScore(cat)*c2.getScore(cat);
			}
		}
		mag1 = Math.sqrt(mag1);
		mag2 = Math.sqrt(mag2);

		//normalise by magnitudes
		Double magnitudes = (mag1 * mag2);
		score /= magnitudes;
		if(Double.isNaN(score)) {
			return 0.0; //NaN caused by zero vectors ie no classifications!
		}
		else return score;
	}
	
	public static double cosineKSimilarity(FullCalaisClassification c1, FullLLDAClassification c2, int k) {
		Set<String> catSet = new HashSet<String>();
		String[] topics1 = c1.getCategorySet().toArray(new String[1]);
		String[] topics2 = c2.getCategorySet().toArray(new String[1]);
		for(int i=0;i<k;i++) {
			if(i<topics1.length) catSet.add(topics1[i]);
			if(i<topics2.length) catSet.add(topics2[i]);
		}
		
		double mag1 = 0.0;
		double mag2 = 0.0;
		double score = 0.0;
		for(String cat : catSet) {
			if(c1.hasCategory(cat) && c2.hasCategory(cat)) {
				score += (c1.getScore(cat) * c2.getScore(cat));
				mag1 += c1.getScore(cat)*c1.getScore(cat);
				mag2 += c2.getScore(cat)*c2.getScore(cat);
			}
		}
		mag1 = Math.sqrt(mag1);
		mag2 = Math.sqrt(mag2);

		//normalise by magnitudes
		Double magnitudes = (mag1 * mag2);
		score /= magnitudes;
		if(Double.isNaN(score)) {
			return 0.0; //NaN caused by zero vectors ie no classifications!
		}
		else return score;
	}
	
	public static double cosineKSimilarity(FullTextwiseClassification c1, FullLLDAClassification c2, int k) {
		Set<String> catSet = new HashSet<String>();
		String[] topics1 = c1.getCategorySet().toArray(new String[1]);
		String[] topics2 = c2.getCategorySet().toArray(new String[1]);
		for(int i=0;i<k;i++) {
			if(i<topics1.length) catSet.add(topics1[i]);
			if(i<topics2.length) catSet.add(topics2[i]);
		}
		
		double mag1 = 0.0;
		double mag2 = 0.0;
		double score = 0.0;
		for(String cat : catSet) {
			if(c1.hasCategory(cat) && c2.hasCategory(cat)) {
				score += (c1.getScore(cat) * c2.getScore(cat));
				mag1 += c1.getScore(cat)*c1.getScore(cat);
				mag2 += c2.getScore(cat)*c2.getScore(cat);
			}
		}
		mag1 = Math.sqrt(mag1);
		mag2 = Math.sqrt(mag2);

		//normalise by magnitudes
		Double magnitudes = (mag1 * mag2);
		score /= magnitudes;
		if(Double.isNaN(score)) {
			return 0.0; //NaN caused by zero vectors ie no classifications!
		}
		else return score;
	}
	
	public static double cosineKSimilarity(FullLLDAClassification c1, FullLLDAClassification c2, int k) {
		Set<String> catSet = new HashSet<String>();
		String[] topics1 = c1.getCategorySet().toArray(new String[1]);
		String[] topics2 = c2.getCategorySet().toArray(new String[1]);
		for(int i=0;i<k;i++) {
			if(i<topics1.length) catSet.add(topics1[i]);
			if(i<topics2.length) catSet.add(topics2[i]);
		}
		
		double mag1 = 0.0;
		double mag2 = 0.0;
		double score = 0.0;
		for(String cat : catSet) {
			if(c1.hasCategory(cat) && c2.hasCategory(cat)) {
				score += (c1.getScore(cat) * c2.getScore(cat));
				mag1 += c1.getScore(cat)*c1.getScore(cat);
				mag2 += c2.getScore(cat)*c2.getScore(cat);
			}
		}
		mag1 = Math.sqrt(mag1);
		mag2 = Math.sqrt(mag2);

		//normalise by magnitudes
		Double magnitudes = (mag1 * mag2);
		score /= magnitudes;
		if(Double.isNaN(score)) {
			return 0.0; //NaN caused by zero vectors ie no classifications!
		}
		else return score;
	}
}
