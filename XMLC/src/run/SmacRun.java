package run;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import Data.AVTable;
import IO.DataReader;
import IO.Evaluator;
import Learner.AbstractLearner;
import util.MasterSeed;

public class SmacRun {

	/*
	 * ################### SMAC Params ###################
	 */
	// // should be empty. not used, but for parsing smac stuff
	@Parameter()
	private final List<String> mainParams = new ArrayList<>();

	// just for parsing the one silly smac parameter
	@Parameter(names = "-1", hidden = true)
	private Boolean bla;
	
	
	@Parameter(names="-gamma")
	double gamma;
	
	@Parameter(names="-lambda")
	double lambda;
	
	@Parameter(names="-k")
	int k;
	
	String trainFile;
	
	String testFile;
	
	Properties properties = new Properties();

	private AVTable traindata;

	private AVTable testdata;

	
	public static void main(String[] args) throws Exception {
		final SmacRun main = new SmacRun();
		final JCommander jc = new JCommander(main);
		jc.parse(args);
		MasterSeed.setSeed(Long.parseLong(main.mainParams.get(4)));
		main.properties = main.readProperty(main.mainParams.get(0));
		main.readTrainData();
		main.readTestData();
		main.run();
	}

	private void run() {
		properties.put("gamma", gamma);
		properties.put("lambda", lambda);
		properties.put("k", k);
		AbstractLearner learner = AbstractLearner.learnerFactory(properties);
		
		learner.allocateClassifiers(traindata);
		learner.train(traindata);
		
		
		Map<String,Double> perftestpreck = Evaluator.computePrecisionAtk(learner, testdata, 1);
		
		for ( String perfName : perftestpreck.keySet() ) {
			System.out.println("Result for SMAC: SUCCESS, 0, 0, " + (1 - perftestpreck.get(perfName)) + ", 0");
		}
	}
	
	public void readTrainData() throws Exception {
		// reading train data
		DataReader datareader = new DataReader(properties.getProperty("TrainFile"), false, Boolean.parseBoolean(properties.getProperty("IsHeader")));
		traindata = datareader.read();
	}

	public void readTestData() throws Exception {
		// test
		DataReader testdatareader = new DataReader(properties.getProperty("TestFile"),false, Boolean.parseBoolean(properties.getProperty("IsHeader")));
		testdata = testdatareader.read();
	}
	public Properties readProperty(String fname) {
		System.out.print("Reading property file...");
		Properties properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(fname);
			properties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		System.out.println("Done.");

		return properties;
	}
}
