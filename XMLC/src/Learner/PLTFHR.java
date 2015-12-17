package Learner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Random;
import java.util.TreeSet;

import org.apache.commons.math3.analysis.function.Sigmoid;

import Data.AVPair;
import Data.AVTable;
import Data.ComparablePair;
import Data.EstimatePair;
import Data.NodeComparatorPLT;
import Data.NodePLT;
import Learner.step.StepFunction;
import jsat.linear.DenseVector;
import preprocessing.MurmurHasher;
import preprocessing.UniversalHasher;
import threshold.ThresholdTuning;
import util.MasterSeed;

public class PLTFHR extends PLTFH {
	protected int[] Tarray = null;	
	protected double[] scalararray = null;
	
	//protected int t = 0;
	//protected double innerThreshold = 0.15;

	//protected double[] scalars = null;
	
	public PLTFHR(Properties properties, StepFunction stepfunction) {
		super(properties, stepfunction);

		System.out.println("#####################################################" );
		System.out.println("#### Leraner: PLTFTHR" );

		//this.innerThreshold = Double.parseDouble(this.properties.getProperty("IThreshold", "0.15") );
		//System.out.println("#### Inner node threshold : " + this.innerThreshold );
		System.out.println("#####################################################" );
	}

	@Override
	public void allocateClassifiers(AVTable data) {
		super.allocateClassifiers(data);

		this.Tarray = new int[this.t];
		this.scalararray = new double[this.t];
		Arrays.fill(this.Tarray, 1);
		Arrays.fill(this.scalararray, 1.0);
		
		//System.out.println( "Done." );
	}
	
		
	@Override
	public void train(AVTable data) {
		
		
				
		for (int ep = 0; ep < this.epochs; ep++) {

			System.out.println("#############--> BEGIN of Epoch: " + (ep + 1) + " (" + this.epochs + ")" );
			// random permutation
			ArrayList<Integer> indirectIdx = this.shuffleIndex();
			
			
			for (int i = 0; i < traindata.n; i++) {
				int currIdx = indirectIdx.get(i);

				HashSet<Integer> positiveTreeIndices = new HashSet<Integer>();
				HashSet<Integer> negativeTreeIndices = new HashSet<Integer>();

				for (int j = 0; j < traindata.y[currIdx].length; j++) {

					int treeIndex = traindata.y[currIdx][j] + traindata.m - 1;
					positiveTreeIndices.add(treeIndex);

					while(treeIndex > 0) {

						treeIndex = (int) Math.floor((treeIndex - 1)/2);
						positiveTreeIndices.add(treeIndex);

					}
				}

				if(positiveTreeIndices.size() == 0) {

					negativeTreeIndices.add(0);

				} else {

					PriorityQueue<Integer> queue = new PriorityQueue<Integer>();
					queue.add(0);

					while(!queue.isEmpty()) {

						int node = queue.poll();
						int leftchild = 2 * node + 1;
						int rightchild = 2 * node + 2;

						Boolean left = false, right = false;

						if(positiveTreeIndices.contains(leftchild)) {
							queue.add(leftchild);
							left = true;
						}

						if(positiveTreeIndices.contains(rightchild)) {
							queue.add(rightchild);
							right = true;
						}

						if(left == true && right == false) {
							negativeTreeIndices.add(rightchild);
						}

						if(left == false && right == true) {
							negativeTreeIndices.add(leftchild);
						}

					}
				}

				//System.out.println("Negative tree indices: " + negativeTreeIndices.toString());


				for(int j:positiveTreeIndices) {

					double posterior = getPartialPosteriors(traindata.x[currIdx],j);
					double inc = -(1.0 - posterior); 

					updatedPosteriors(currIdx, j, inc);
				}

				for(int j:negativeTreeIndices) {

					if(j >= this.t) System.out.println("ALARM");

					double posterior = getPartialPosteriors(traindata.x[currIdx],j);
					double inc = -(0.0 - posterior); 
					
					updatedPosteriors(currIdx, j, inc);
				}

		

				if ((i % 100000) == 0) {
					System.out.println( "\t --> Epoch: " + (ep+1) + " (" + this.epochs + ")" + "\tSample: "+ i +" (" + data.n + ")" );
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					System.out.println("\t\t" + dateFormat.format(date));
					//System.out.println("Weight: " + this.w[0].get(0) );
					System.out.println("Scalar: " + this.scalararray[0]);
				}
			}

			System.out.println("--> END of Epoch: " + (ep + 1) + " (" + this.epochs + ")" );
		}
		
		int zeroW = 0;
		double sumW = 0;
		int maxNonZero = 0;
		int index = 0;
		for(double weight : w) {
			if(weight == 0) zeroW++;
			else maxNonZero = index;
			sumW += weight;
			index++;
		}
		System.out.println("Hash weights (lenght, zeros, nonzeros, ratio, sumW, last nonzero): " + w.length + ", " + zeroW + ", " + (w.length - zeroW) + ", " + (double) (w.length - zeroW)/(double) w.length + ", " + sumW + ", " + maxNonZero);
	}


	protected void updatedPosteriors( int currIdx, int label, double inc) {
	
		
		this.learningRate = this.gamma / (1 + this.gamma * this.lambda * this.Tarray[label]);
		this.Tarray[label]++;
		this.scalararray[label] *= (1 + this.learningRate * this.lambda);
		
		//System.out.println(this.learningRate + "\t" + this.scalar[label]);
		
		int n = traindata.x[currIdx].length;
		
		for(int i = 0; i < n; i++) {

			int index = fh.getIndex(label, traindata.x[currIdx][i].index);
			int sign = fh.getSign(label, traindata.x[currIdx][i].index);
			//System.out.println(sign);
			//double gradient = inc * traindata.x[currIdx][i].value; 
			//double update = this.learningRate * gradient;
			//this.w[index] -= update; 
			
			double gradient = this.scalararray[label] * inc * (traindata.x[currIdx][i].value * sign);
			double update = (this.learningRate * gradient);// / this.scalar;		
			this.w[index] -= update; 
			//System.out.println("w -> gradient, scalar, update: " + gradient + ", " + scalar +", " + update);
			
		}
		
		
		// Include bias term in weight vector:
		//int biasIndex = fh.getIndex(label, -1);
		//double gradient = inc;
		//double update = this.learningRate * gradient;	
		//this.w[biasIndex] -= update;
		//System.out.println("bias -> gradient, scalar, update: " + gradient + ", " + scalar +", " + update);

		
		double gradient = this.scalararray[label] * inc;
		double update = (this.learningRate * gradient);//  / this.scalar;		
		this.bias[label] -= update;
		//System.out.println("bias -> gradient, scalar, update: " + gradient + ", " + scalar +", " + update);
	}


	Sigmoid s = new Sigmoid();
	@Override
	public double getPartialPosteriors(AVPair[] x, int label) {
		double posterior = 0.0;
		
		
		for (int i = 0; i < x.length; i++) {
			
			int hi = fh.getIndex(label,  x[i].index); 
			int sign = fh.getSign(label, x[i].index);
			posterior += (x[i].value *sign) * (1/this.scalararray[label]) * this.w[hi];
		}
		
		posterior += (1/this.scalararray[label]) * this.bias[label]; 
		posterior = s.value(posterior);		
		
		return posterior;
	}
	
}