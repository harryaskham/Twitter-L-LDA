package uk.ac.cam.ha293.tweetlabel.eval;

import java.io.IOException;
import java.util.List;

import uk.ac.cam.ha293.tweetlabel.util.Tools;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import jsc.correlation.KendallCorrelation;
import jsc.correlation.LinearCorrelation;
import jsc.correlation.SpearmanCorrelation;
import jsc.datastructures.PairedData;

public class SpearmanRank {
	
	public static double srcc(SimilarityMatrix sm1, SimilarityMatrix sm2) {
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
		
		//sort both pairing lists
		Collections.sort(sps1);
		Collections.sort(sps2);
		Collections.reverse(sps1);
		Collections.reverse(sps2);
		
		//For efficiency: need a map of pairs to ranks
		//this makes it O(n), rather than O(n^2)
		Map<SimilarityPair,Integer> rankMap = new HashMap<SimilarityPair,Integer>();
		for(int i=0; i<sps1.size(); i++) {
			//lose the 0-sims, they lend nothing
			if(sps1.get(i).similarity()>0.0) {
				rankMap.put(sps1.get(i),i);
			}
		}
		
		double n = rankMap.keySet().size();
		double rhoInc = 6.0 / (n*(n*n-1));
		double rho = 1.0;

		for(int i=0; i<sps2.size(); i++) {
			//if(i%10000==0) System.out.println("i="+i+", rho="+rho);
			if(rankMap.containsKey(sps2.get(i))) {
				int diff = rankMap.get(sps2.get(i))-i;
				rho -= rhoInc*diff*diff;
				//System.out.println("Pair "+sps2.get(i).uid1()+","+sps2.get(i).uid2()+":"+sps1.get(i).similarity()+" at rank "+i+" appears at rank "+(diff+i)+" in other list");
			}
		}
		
		return rho;
	}
	
	public static double jscSRCC(SimilarityMatrix sm1, SimilarityMatrix sm2) {
		if(sm1.dimension() != sm2.dimension()) {
			System.err.println("Similarity Matrices not of same dimension");
			return -1.0;
		}
		List<Long> uids = Tools.getCSVUserIDs();
		
		int numPairings = (sm1.dimension()*(sm1.dimension()+1)/2);
		double[] pairedDataX = new double[numPairings];
		double[] pairedDataY = new double[numPairings];
		int pairingIndex = 0;
		for(int i=0; i<uids.size(); i++) {
			for(int j=i; j<uids.size(); j++) {
				pairedDataX[pairingIndex] = sm1.getID(uids.get(i), uids.get(j));
				pairedDataY[pairingIndex] = sm2.getID(uids.get(i), uids.get(j));
				pairingIndex++;
			}
		}
		PairedData pairedData = new PairedData(pairedDataX,pairedDataY);
		//System.out.println("now running sc");
		//SpearmanCorrelation sc = new SpearmanCorrelation(pairedData);
		//return sc.getR();
		return LinearCorrelation.correlationCoeff(pairedData);
		
		/*
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
		
		//sort both pairing lists
		Collections.sort(sps1);
		Collections.sort(sps2);
		Collections.reverse(sps1);
		Collections.reverse(sps2);
		
		Map<SimilarityPair,Double> rankMap1 = new HashMap<SimilarityPair,Double>();
		Map<SimilarityPair,Double> rankMap2 = new HashMap<SimilarityPair,Double>();
		//rank first list
		for(int i=0; i<sps1.size(); i++) {
			double similarity = sps1.get(i).similarity();
			
			//check for tied values
			int j = i+1;
			int count = 0;
			while(j < sps1.size() && sps1.get(j).similarity()==similarity) {
				count++;
				j++;
			}
			
			if(count == 0) {
				rankMap1.put(sps1.get(i),(double)i);
			} else {
				//work out the average rank, give it to all tied values
				double newRank = (i+j)/2.0;
				for(int k=i; k<j; k++) {
					rankMap1.put(sps1.get(k), newRank);
				}
				i=j-1;
			}
		}
		//rank second list
		for(int i=0; i<sps2.size(); i++) {
			double similarity = sps2.get(i).similarity();
			
			//check for tied values
			int j = i+1;
			int count = 0;
			while(j < sps2.size() && sps2.get(j).similarity()==similarity) {
				count++;
				j++;
			}
			
			if(count == 0) {
				rankMap2.put(sps2.get(i),(double)i);
			} else {
				//work out the average rank, give it to all tied values
				double newRank = (i+j)/2.0;
				for(int k=i; k<j; k++) {
					rankMap2.put(sps2.get(k), newRank);
				}
				i=j-1;
			}
		}
		*/
	}

}
