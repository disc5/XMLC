package Learner;

import java.util.PriorityQueue;
import java.util.Properties;

import Data.AVPair;
import Data.AVTable;
import Data.ComparablePair;
import Learner.step.StepFunction;

public class ConstantLearner extends AbstractLearner {

	public ConstantLearner(Properties properties, StepFunction stepfunction) {
		super(properties, stepfunction);
		// TODO Auto-generated constructor stub
	}

	public int getPrediction(AVPair[] x, int label){
		return 0;
	}
	
	// naive implementation checking all labels
	public PriorityQueue<ComparablePair> getPositiveLabelsAndPosteriors(AVPair[] x) {
		PriorityQueue<ComparablePair> positiveLabels = new PriorityQueue<>();
		return positiveLabels;
	}

	
	@Override
	public void allocateClassifiers(AVTable data) {
		// TODO Auto-generated method stub		
		this.m = data.m;
		this.d = data.d;
	}

	@Override
	public void train(AVTable data) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getPosteriors(AVPair[] x, int label) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void savemodel(String fname) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadmodel(String fname) {
		// TODO Auto-generated method stub

	}

}