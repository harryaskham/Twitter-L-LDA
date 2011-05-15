package uk.ac.cam.ha293.tweetlabel.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.cam.ha293.tweetlabel.util.Tools;

public class Pearson {

	public static double pmcc(SimilarityMatrix sm1, SimilarityMatrix sm2) {
		if(sm1.dimension() != sm2.dimension()) {
			System.err.println("Similarity Matrices not of same dimension");
			return -1.0;
		}
		List<Long> uids = Tools.getCSVUserIDs();
		
		//create baseline similarity pairs
		List<SimilarityPair> sps1 = new ArrayList<SimilarityPair>();
		List<SimilarityPair> sps2 = new ArrayList<SimilarityPair>();
		for(int uid1=0; uid1<uids.size(); uid1++) {
			for(int uid2=uid1; uid2<uids.size();uid2++) {
				SimilarityPair sp1 = new SimilarityPair(sm1,sm1.lookupID(uid1),sm2.lookupID(uid2));
				SimilarityPair sp2 = new SimilarityPair(sm2,sm1.lookupID(uid1),sm2.lookupID(uid2));
				if(sp1 == null || sp2 == null) {
					//no categories for one of them
					continue;
				} else {
					sps1.add(sp1);
					sps2.add(sp2);
				}
			}
		}

		//pmcc correlation
		int n = sps1.size();
		double sum1 = 0.0;
		double sum2 = 0.0;
		for(int i=0; i<n; i++) {
			sum1 += sps1.get(i).similarity();
			sum2 += sps2.get(i).similarity();
		}
		double mean1 = sum1/n;
		double mean2 = sum2/n;
		double sumMinusMean1 = 0.0;
		double sumMinusMean2 = 0.0;
		for(int i=0; i<n; i++) {
			sumMinusMean1 += Math.pow(sps1.get(i).similarity()-mean1,2.0);
			sumMinusMean2 += Math.pow(sps2.get(i).similarity()-mean2,2.0);
		}
		double sd1 = Math.sqrt(sumMinusMean1/(n-1));
		double sd2 = Math.sqrt(sumMinusMean2/(n-1));
		double pmccSum = 0.0;
		for(int i=0; i<n; i++) {
			pmccSum += ((sps1.get(i).similarity()-mean1)/sd1)*((sps2.get(i).similarity()-mean2)/sd2);
		}
		double pmcc = pmccSum/(n-1);
		
		return pmcc;
	}
	
}
