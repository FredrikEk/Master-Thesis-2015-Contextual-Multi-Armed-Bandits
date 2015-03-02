package com.mapr.stats.bandit;

import java.util.ArrayList;
import java.util.Random;

import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

public class ContextualBandit {

	private ArrayList<ContextualBayesArm> banditArms;
	private static Random gen = RandomUtils.getRandom();

	public ContextualBandit(ArrayList<ContextualBayesArm> arms) {
		this.banditArms = arms;
	}
	
	public ContextualBandit(int numberOfArms, int numberOfFeatures) {
		banditArms = new ArrayList<ContextualBayesArm>();
		
		for(int i = 0; i < numberOfArms; i++) {
			double[] test = new double[numberOfFeatures];
			for(int j = 0; j < numberOfFeatures; j++) {
				test[j] = gen.nextDouble();
			}
			Vector vec = new DenseVector(test);
			ContextualBayesArm cba = new ContextualBayesArm(i, vec);
			banditArms.add(cba);
		}
		
		/*
		double[] test = new double[numberOfFeatures];
		for(int j = 0; j < numberOfFeatures; j++) {
			test[j] = 1.0;
		}
		Vector vec = new DenseVector(test);
		ContextualBayesArm cba = new ContextualBayesArm(0, vec);
		banditArms.add(cba);
		*/
		/*
		double[] test = new double[]{0.8985382466066163,0.6771109989185896,0.6377995558345793,0.4217317937040622,0.024219869354243473,0.3767818042028521};
		Vector vec = new DenseVector(test);
		ContextualBayesArm cba = new ContextualBayesArm(0, vec);
		banditArms.add(cba);
		*/
	}
	
	public ContextualBayesArm getArm() {
		ContextualBayesArm bestArm = null;
		double bestScore = -1.0;
		for(ContextualBayesArm cba : banditArms) {
			double sample = cba.sample();
			if(sample > bestScore) {
				bestScore = sample;
				bestArm = cba;
			}
		}
		return bestArm;
	}
	
	public ArrayList<ContextualBayesArm> getAllArms() {
		return banditArms;
	}
}
