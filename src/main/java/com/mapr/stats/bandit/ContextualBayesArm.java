package com.mapr.stats.bandit;

import org.apache.mahout.math.Vector;

import com.mapr.stats.random.BetaDistribution;

public class ContextualBayesArm {
	
	private static double decayFactor = 1.0d;
	
    private final BetaDistribution rand;
	private final Vector features;
	private double alpha;
	private double beta;
	private int numberOfTries;
	private int numberOfBuys;
	private int armNumber;
	
	public ContextualBayesArm(int armNumber, Vector features) {
		this(armNumber, features, 1.0, 1.0);
	}
	
	public ContextualBayesArm(int armNumber, Vector features, double alpha_0, double beta_0) {
		this.armNumber = armNumber;
		this.features = features;
		this.rand = new BetaDistribution(alpha_0, beta_0);
		this.alpha = alpha_0;
		this.beta = beta_0;
		this.numberOfTries = 0;
		this.numberOfBuys = 0;
	}
	
	public ContextualBayesArm(ContextualBayesArm cba) {
		this.armNumber 		= cba.armNumber;
		this.alpha			= 1.0;
		this.beta			= 1.0;
		this.features		= cba.features.clone();
		this.rand 			= new BetaDistribution(alpha, beta);
		this.numberOfBuys 	= 0;
		this.numberOfTries	= 0;
		
	}
	
	public double sample() {
		return this.rand.nextDouble(alpha, beta);
	}
	
	public void train(boolean success) {
		alpha *= decayFactor;
		beta *= decayFactor;
		alpha += success ? 1 : 0;
		beta++;
		numberOfBuys += success ? 1 : 0;
		numberOfTries++;
	}
	
	public Vector getContext() {
		return features;
	}
	
	public double getAlpha() {
		return alpha;
	}
	
	public double getBeta() {
		return beta;
	}
	
	public int getArmNumber() {
		return armNumber;
	}
	
	public int getNumberOfTries() {
		return numberOfTries;
	}

	public void setNumberOfTries(int numberOfTries) {
		this.numberOfTries = numberOfTries;
	}

	public int getNumberOfBuys() {
		return numberOfBuys;
	}

	public void setNumberOfBuys(int numberOfBuys) {
		this.numberOfBuys = numberOfBuys;
	}

	public ContextualBayesArm copy() {
		return new ContextualBayesArm(this);
	}
}
