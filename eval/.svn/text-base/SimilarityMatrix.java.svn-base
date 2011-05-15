package uk.ac.cam.ha293.tweetlabel.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cam.ha293.tweetlabel.classify.FullAlchemyClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullCalaisClassification;
import uk.ac.cam.ha293.tweetlabel.classify.FullTextwiseClassification;
import uk.ac.cam.ha293.tweetlabel.liwc.FullLIWCClassification;
import uk.ac.cam.ha293.tweetlabel.topics.FullLDAClassification;
import uk.ac.cam.ha293.tweetlabel.topics.FullLLDAClassification;
import uk.ac.cam.ha293.tweetlabel.topics.FullSVMClassification;
import uk.ac.cam.ha293.tweetlabel.types.Corpus;
import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class SimilarityMatrix implements Serializable {

	private static final long serialVersionUID = 8293810632053185977L;
	
	private double[][] sim;
	private long[] userIDLookup;
	private Map<Long,Integer> indexLookup;
	private int d; //dimensions
	private boolean verbose = false;
	
	public SimilarityMatrix() {
		if(verbose) System.out.println("Creating similarity matrix");
		d = 2506;
		sim = new double[d][d];
		userIDLookup = new long[d];
		indexLookup = new HashMap<Long,Integer>();
		fillLookups();
	}
	
	public SimilarityMatrix(int d) {
		if(verbose) System.out.println("Creating similarity matrix");
		this.d = d;
		sim = new double[d][d];
		userIDLookup = new long[d];
		indexLookup = new HashMap<Long,Integer>();
		fillLookups();
	}
	
	public Double getSimilarity(long uid1, long uid2) {
		try {
			int index1 = indexLookup.get(uid1);
			int index2 = indexLookup.get(uid2);
			return sim[index1][index2];
		} catch (NullPointerException e) {
			//Occurs when a uid had no LLDA topics - so no classifications - so no indexLookup
			return null;
		}
	}
	
	public void fillLookups() {
		if(verbose) System.out.println("Filling the lookup tables");
		int indexCount = 0;
		for(long id : Tools.getCSVUserIDs()) {
			userIDLookup[indexCount] = id;
			indexLookup.put(id, indexCount);
			indexCount++;
		}
	}
	
	public int dimension() {
		return d;
	}
	
	public long lookupID(int index) {
		return userIDLookup[index];
	}

	public void fillAlchemy() {
		//get clasifications
		System.out.println("Filling from Alchemy classifications");
		FullAlchemyClassification[] classifications = new FullAlchemyClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullAlchemyClassification(id); 
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].cosineSimilarity(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillAlchemyJS() {
		//get clasifications
		System.out.println("Filling from Alchemy classifications");
		FullAlchemyClassification[] classifications = new FullAlchemyClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullAlchemyClassification(id); 
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].jsDivergence(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillCalais() {
		//get clasifications
		System.out.println("Filling from OpenCalais classifications");
		FullCalaisClassification[] classifications = new FullCalaisClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullCalaisClassification(id); 
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].cosineSimilarity(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillCalaisJS() {
		//get clasifications
		System.out.println("Filling from OpenCalais classifications");
		FullCalaisClassification[] classifications = new FullCalaisClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullCalaisClassification(id); 
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].jsDivergence(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillTextwise() {
		//get clasifications
		System.out.println("Filling from Textwise classifications");
		FullTextwiseClassification[] classifications = new FullTextwiseClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullTextwiseClassification(id,true); 
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].cosineSimilarity(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}	
	
	public void fillTextwiseJS() {
		//get clasifications
		System.out.println("Filling from Textwise classifications");
		FullTextwiseClassification[] classifications = new FullTextwiseClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullTextwiseClassification(id,true); 
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].jsDivergence(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}	
	
	public void fillLIWC(boolean naiveBayes) {
		System.out.println("Filling from LIWC classifications, NB="+naiveBayes);
		FullLIWCClassification[] classifications = new FullLIWCClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullLIWCClassification(naiveBayes,id);
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) {
				Double cos = classifications[m].cosineSimilarity(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillLLDA(String topicType, double alpha) {
		System.out.println("Filling from LLDA-inferred "+topicType+" classifications");
		FullLLDAClassification[] classifications = new FullLLDAClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullLLDAClassification(topicType,alpha,id);
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].cosineSimilarity(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillLLDA(String topicType, double alpha, boolean fewerProfiles, int reduction) {
		//System.out.println("Filling from LLDA-inferred "+topicType+" classifications");
		FullLLDAClassification[] classifications = new FullLLDAClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullLLDAClassification(topicType,alpha,fewerProfiles,reduction,id);
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			//System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].cosineSimilarity(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillKLLDA(String topicType, double alpha, int k) {
		//System.out.println("Filling from LLDA-inferred "+topicType+" classifications");
		FullLLDAClassification[] classifications = new FullLLDAClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullLLDAClassification(topicType,alpha,id);
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			//System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = CosineManager.cosineKSimilarity(classifications[m],classifications[n],k);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillLLDAJS(String topicType, double alpha) {
		System.out.println("Filling from LLDA-inferred "+topicType+" classifications");
		FullLLDAClassification[] classifications = new FullLLDAClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullLLDAClassification(topicType,alpha,id);
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].jsDivergence(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillSVM(String topicType) {
		System.out.println("Filling from SVM "+topicType+" classifications");
		FullSVMClassification[] classifications = new FullSVMClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullSVMClassification(topicType,id);
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].cosineSimilarity(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public static void lldaMatrixCreation() {
		//String[] topicTypes = {"alchemy","calais","textwise"};
		String[] topicTypes = {"textwise"};
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(String topicType : topicTypes) {
			for(double alpha : alphas) {
				System.out.println("Creating SM for "+topicType+" "+alpha);
				SimilarityMatrix sm = new SimilarityMatrix(2506);
				sm.fillLLDA(topicType, alpha);
				sm.save("llda-"+topicType+"-"+alpha);
			}
		}
	}
	
	public static void ldaMatrixCreation() {
		double[] alphas = {0.25,0.5,0.75,1.0,1.25,1.5,1.75,2.0};
		for(double alpha : alphas) {
			System.out.println("Creating SM for lda "+alpha);
			SimilarityMatrix sm = new SimilarityMatrix(2506);
			sm.fillLDAAndSave(50,1000,100,alpha);
		}
	}
	
	public void fillLDAAndSave(int numTopics, int burn, int sample, double alpha) {
		//get clasifications
		System.out.println("Filling from LDA classifications");
		FullLDAClassification[] classifications = new FullLDAClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullLDAClassification(id,numTopics,burn,sample,alpha); 
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].cosineSimilarity(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
		
		//save with params
		String saveString = "lda-"+numTopics+"-"+burn+"-"+sample+"-"+alpha;
		save(saveString);
	}
	
	public void fillLDAJS(int numTopics, int burn, int sample, double alpha) {
		//get clasifications
		System.out.println("Filling from LDA classifications");
		FullLDAClassification[] classifications = new FullLDAClassification[d];
		for(long id : Tools.getCSVUserIDs()) {
			classifications[indexLookup.get(id)] = new FullLDAClassification(id,numTopics,burn,sample,alpha); 
		}
		
		//cosine similarities!
		for(int m=0; m<d; m++) {
			System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = classifications[m].jsDivergence(classifications[n]);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void fillRestricted(boolean baseline, String topicType, int topTopics, double alpha) {
		List<Map<String,Double>> classifications = new ArrayList<Map<String,Double>>();
		if(baseline) {
			if(topicType.equals("alchemy")) {
				for(long id : Tools.getCSVUserIDs()) {
					FullAlchemyClassification c = new FullAlchemyClassification(id);
					Map<String,Double> classification = new HashMap<String,Double>();
					int topicCount = 0;
					for(String topic : c.getCategorySet()) {
						if(topicCount == topTopics) break;
						classification.put(topic, c.getScore(topic));
						topicCount++;
					}
					classifications.add(classification);
				}
			} else if(topicType.equals("calais")) {
				for(long id : Tools.getCSVUserIDs()) {
					FullCalaisClassification c = new FullCalaisClassification(id);
					Map<String,Double> classification = new HashMap<String,Double>();
					int topicCount = 0;
					for(String topic : c.getCategorySet()) {
						if(topicCount == topTopics) break;
						if(topic.equals("Other")) continue;
						classification.put(topic, c.getScore(topic));
						topicCount++;
					}
					classifications.add(classification);
				}
			} else if(topicType.equals("textwise")) {
				for(long id : Tools.getCSVUserIDs()) {
					FullTextwiseClassification c = new FullTextwiseClassification(id,true);
					Map<String,Double> classification = new HashMap<String,Double>();
					int topicCount = 0;
					for(String topic : c.getCategorySet()) {
						if(topicCount == topTopics) break;
						classification.put(topic, c.getScore(topic));
						topicCount++;
					}
					classifications.add(classification);
				}
			} 
		} else {
			for(long id : Tools.getCSVUserIDs()) {
				FullLLDAClassification c = new FullLLDAClassification(topicType,alpha,id);
				Map<String,Double> classification = new HashMap<String,Double>();
				int topicCount = 0;
				for(String topic : c.getCategorySet()) {
					if(topicCount == topTopics) break;
					if(topic.equals("Other")) continue;
					classification.put(topic, c.getScore(topic));
					topicCount++;
				}
				classifications.add(classification);
			}
		}
		
		for(int m=0; m<d; m++) {
			//System.out.println("On row "+m);
			for(int n=m; n<d; n++) { //no point working eveyrthing out twice!
				Double cos = CosineManager.cosineSimilarity(classifications.get(m), classifications.get(n));
				//System.out.println("Similarity Found: "+cos);
				sim[m][n] = cos;
				sim[n][m] = cos;
			}
		}
	}
	
	public void print() {
		System.out.println("Printing similarity matrix");
		for(int m=0; m<d; m++) {
			for(int n=0; n<d; n++) {
				Tools.dpPrint(sim[m][n], 2);
				System.out.print(" ");
			}
			System.out.println();
		}
	}
	
    public void save(String name) {
		try {
			String filename = "smatrices/"+name+".smatrix";
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			objectOut.close();
			System.out.println("Saved smatrix "+name);
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save smatrix "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't save smatrix "+name);
			e.printStackTrace();			
		}    	
    }
    
    public static SimilarityMatrix load(String name) {
		try {
			String filename = "smatrices/"+name+".smatrix";
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			SimilarityMatrix smatrix = (SimilarityMatrix)objectIn.readObject();
			objectIn.close();
			//System.out.println("Loaded smatrix "+name);
			return smatrix;
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't load smatrix "+name);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't load smatrix "+name);
			e.printStackTrace();			
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't load smatrix "+name);
			e.printStackTrace();			
		}
		return null;
    }
    
    public double getID(long m, long n) {
    	return sim[indexLookup.get(m)][indexLookup.get(n)];
    }
    
    public double getIndex(int m, int n) {
    	return sim[m][n];
    }

}
