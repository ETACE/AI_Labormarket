package onlineLaborQlearn;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.math3.util.Precision;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.ContextUtils;


public class onlineLaborBuilder implements ContextBuilder<Object> {


	//Needed to set parameters in the Repast display

	int numFirms;  
	int numWorkers ;
	double firmProductivity; //production function A
	double alpha ; //production function	exponent
	double epsilonInit ; //greedy learning
	double beta; //greedy learning
	double lowerLimitStrat; //lower limit of strategy space
	double grainStrat ; //how fine grained strategy space is;
	double deltaProductivity ; //change of productivity compared to baseline;
	int numStrat ; //num of steps for grainStrat starting from lowerLimitStrat to reach upper bound
	double fee ; //fee to platform, between 0 and 1;
	double effort ; // effort -> accounting for distance of worker to firm
	double shareFee ; //share fee to platform paid by firm, between 0 and 1;
	int modelType ; // is zero for take it of leave it, and is one for bidding model
	int numIterationsBatchRuns;// defines the number of iterations in a batch run experiment
	int randomProductivity;// If set to 1: firm productivity is random draw
	int frequencyWriteDatabase; // Frequency of writing data to data base
	double[] wageList; // List of wages firms can choose from
	int learningStart; // Initial phase in which firms do only random wage setting and no learning takes place
	int randomSeed;
	int switchDeepQLearning; // Switch: 1 -> deep q learning with default parameters, otherwise adjusted parameters to run ordinary q learning
	int asymetricProductivities; // firms have different productivities
	int databaseMode;  // Different saving options
	String optimizer;
	String activationFunction;

	int inputNormalization; 
	int rewardNormalization; // Training frequency

	//Parameters Deep Q learning
	int freqTrainingNetwork; // Training frequency
	int miniBatchSize; //Size of mini batch drawn from experience memory
	int memorySize; // size of experience memory
	int freqUpdateTargetnet; // frequency of cloning the main network as target network
	double delta;  // Discont factor
	double learningRate; // learning rate to adjust weights in gradient descent method
	double weightRMSprop; // weight for smoothing the squared gradient
	int simulationExperimentSetup; // This is used to define different experimental setups; if 0 -> no predefined setup is used. Setups are hardcoded and defined below
	double[] factorNumNodesHiddenLayers;
	int numHiddenLayers;
	@Override
	public Context build(Context<Object> context) {
		context.setId("onlineLaborQlearn");

		int numPositions  = 104; // number of positions on Salop circle  - set it equal to number of workers
		//further must define number of positions such that it gives an even number when divided by 2 and divided by 4, e.g. 104

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		//defines space for numPositionsX1 and torus (with WrapAroundBorders)
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), numPositions,
				1);

		//discretize the space
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, numPositions, 1));

		//Needed to set parameters in the Repast display
		Parameters params = RunEnvironment.getInstance().getParameters();
		numFirms = (Integer) params.getValue("numFirms"); //code is written for two firms only
		numWorkers = (Integer) params.getValue("numWorkers");
		firmProductivity = (Double) params.getValue("firmProductivity"); //production function A
		alpha = (Double) params.getValue("alpha"); //production function	exponent
		epsilonInit = (Double) params.getValue("epsilonInit"); //greedy learning
		beta = (Double) params.getValue("beta"); //greedy learning
		lowerLimitStrat = (Double)params.getValue("lowerLimitStrat"); //lower limit of strategy space
		grainStrat = (Double) params.getValue("grainStrat"); //how fine grained strategy space is;
		numStrat = (int) params.getValue("numStrat"); //num of steps for grainStrat starting from lowerLimitStrat to reach upper bound
		fee = (Double) params.getValue("fee"); //fee to platform, between 0 and 1;
		shareFee = (Double) params.getValue("shareFee"); //share fee to platform paid by firm, between 0 and 1;
		modelType = (int) params.getValue("modelType"); // is zero for take it of leave it, and is one for bidding model
		frequencyWriteDatabase= (int) params.getValue("frequencyWriteDatabase");// defines the iteration at which in case dataBaseMode==1 the q matrix is written to the data base
		numIterationsBatchRuns= (int) params.getValue("numIterationsBatchRuns");// defines the number of iterations in a batch run experiment
		effort = (Double) params.getValue("effort"); // effort -> accounting for distance of worker to firm
		randomProductivity= (int) params.getValue("randomProductivity");// // If set to 1: firm productivity is random draw
		freqTrainingNetwork = (int) params.getValue("freqTrainingNetwork"); // Training frequency
		miniBatchSize = (int) params.getValue("miniBatchSize");  //Size of mini batch drawn from experience memory
		memorySize = (int) params.getValue("memorySize");  // size of experience memory
		freqUpdateTargetnet = (int) params.getValue("freqUpdateTargetnet"); // frequency of cloning the main network as target network
		delta = (double) params.getValue("delta");  // Discont factor
		learningRate= (double) params.getValue("learningRate"); // learning rate to adjust weights in gradient descent method
		numHiddenLayers= (int) params.getValue("numHiddenLayers");
		inputNormalization = (int) params.getValue("inputNormalization");
		rewardNormalization = (int) params.getValue("rewardNormalization");
		randomSeed = (int) params.getValue("randomSeed");
		learningStart= (int) params.getValue("learningStart");  // Initial phase in which firms do only random wage setting and no learning takes place
		switchDeepQLearning = (int) params.getValue("switchDeepQLearning"); // Switch: 1 -> deep q learning with default parameters, otherwise adjusted parameters to run ordinary q learningf
		weightRMSprop = (double) params.getValue("weightRMSprop"); // weight for smoothing the squared gradient
		optimizer = (String) params.getValue("optimizer");
		activationFunction = (String) params.getValue("activationFunction");
		deltaProductivity = (double) params.getValue("deltaProductivity");
		asymetricProductivities = (int) params.getValue("asymetricProductivities");

		simulationExperimentSetup= (int) params.getValue("simulationExperimentSetup");  // This is used to define different experimental setups; if 0 -> no predefined setup is used. Setups are hardcoded and defined below

		databaseMode = 1;
		//beta = 4e-6;



		factorNumNodesHiddenLayers = new double[2];
		factorNumNodesHiddenLayers[0]= 2/3.0;
		factorNumNodesHiddenLayers[1] = 3/2.0;



		int asymetricFirms = 0;



		beta =  6e-5; // default value; if beta chould be varied, use the next line! Oay attention to the scaling (scaling factor of 1e4)
		//beta = beta / 10000;

		//Predefined experimetal setups
		if(simulationExperimentSetup==1) {

			// Deep Q Network with large memory buffer
			switchDeepQLearning = 1;
			asymetricFirms = 0;
			freqUpdateTargetnet = 10000;
			memorySize = 100000;

		}else if(simulationExperimentSetup==2) {

			// Deep Q Network with medium memory buffer
			switchDeepQLearning = 1;
			asymetricFirms=0;

			freqUpdateTargetnet = 100;
			memorySize = 5000;


		}else if(simulationExperimentSetup==3) {

			// Deep Q Network with no memory buffer
			switchDeepQLearning = 0;
			asymetricFirms=0;

		}		



		// Adjusting the grid in if number of firms is changed.
		if(numFirms==3) {

			lowerLimitStrat = 1.32; //lower limit of strategy space
			grainStrat = 1.09 ; //how fine grained strategy space is;	

		}else if(numFirms==4){

			lowerLimitStrat = 1.37; //lower limit of strategy space
			grainStrat = 1.23 ; //how fine grained strategy space is;

		}else if(numFirms==5) {

			lowerLimitStrat = 0.78; //lower limit of strategy space
			grainStrat = 1.32 ; //how fine grained strategy space is;

		}



		//If switchDeepQLearning!=1 set DQN parameters to run simulations without experience replay
		if(!(switchDeepQLearning==1)) {

			freqTrainingNetwork = 1;
			miniBatchSize = 1;
			memorySize = 1;
			freqUpdateTargetnet = 1;
		}





		String currentPathDB = "./output";

		//defines number for firms -- however done in parameter panel now
		//numFirms = 2;

		// Initialize db recorder


		String fileSuffix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		fileSuffix = "_"+fileSuffix;


		DatabaseRecorder databaseRecorder = new DatabaseRecorder(currentPathDB, fileSuffix);

		databaseRecorder.createNeuralNetworkTable();
		databaseRecorder.createParameterTable();
		context.add(databaseRecorder);

		databaseRecorder.insertParameters(this);

		databaseRecorder.createFirmsTable();
		databaseRecorder.createStatTable();
		databaseRecorder.commit();




		for(int i = 0; i < numStrat; i++) {

			if(Precision.round(lowerLimitStrat + i*grainStrat,2) > firmProductivity) {

				numStrat = i-1;
				break;
			}


		}



		// Set wage list with all possible wages
		wageList= new double[numStrat];  

		for(int i = 0; i < numStrat; i++) {

			wageList[i] = Precision.round(lowerLimitStrat + i*grainStrat,2);

		}



		//initializes firms

		int positionFirm = 0; 
		for (int i = 0; i < numFirms; i++) {
			int firmID = i;
			int numFilledJobs = 0;
			int rowNumber=0;

			double productivity = firmProductivity;

			int freqTrainingNetworkFirm = freqTrainingNetwork;
			int miniBatchSizeFirm = miniBatchSize;
			int memorySizeFirm = memorySize;
			int freqUpdateTargetnetFirm = freqUpdateTargetnet;

			if(asymetricFirms==1) {

				if(i%2 ==0) {

					freqTrainingNetworkFirm = 1;
					miniBatchSizeFirm = 1;
					memorySizeFirm = 1;
					freqUpdateTargetnetFirm = 1;

				}


			}



			if(asymetricProductivities==1) {

				if(i%2 ==0) {

					productivity= firmProductivity*(1-deltaProductivity);

				}else if(i%1 ==0) {

					productivity= firmProductivity*(1+deltaProductivity);

				}
			}

			context.add(new Firms(space, grid, firmID, numFilledJobs, positionFirm, lowerLimitStrat + (numStrat-1) * grainStrat, 
					lowerLimitStrat + (numStrat-1) * grainStrat, lowerLimitStrat + (numStrat-1) * grainStrat, 
					productivity, alpha, 0,	epsilonInit, beta, 
					freqTrainingNetworkFirm,miniBatchSizeFirm,memorySizeFirm, freqUpdateTargetnetFirm,
					delta,learningRate,
					1, fee, shareFee, numFirms, modelType, wageList, randomProductivity,numIterationsBatchRuns,learningStart,weightRMSprop,databaseMode,factorNumNodesHiddenLayers, numHiddenLayers, optimizer, activationFunction, inputNormalization,rewardNormalization,deltaProductivity));

			positionFirm += numPositions / numFirms ; //firms are placed on opposite sites of circle

		} 



		//initializes workers
		for (int i = 0; i < numWorkers; i++) {
			int workerID = i;
			int employmentStatus = 0;
			context.add(new Workers(space, grid, workerID, employmentStatus, 0, -1, i, numPositions, fee, 
					shareFee, effort, numFirms, modelType));
		}

		//adds workers and firms to grids (after having them added to space) by rounding space coordinates
		//code iterates through all the agents in the context
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}


		//moves firms and workers to their place on the Salop cicle
		for (Object obj : context) {
			if(obj instanceof Workers) {
				Workers aWorker = (Workers) obj;
				grid.moveTo(aWorker, aWorker.position, 0);
			}
			if(obj instanceof Firms) {
				Firms aFirm = (Firms) obj;
				grid.moveTo(aFirm, aFirm.position, 0);
			}
		}



		//adds statistical office, sets initial values to zero
		double numEmployed = 0;
		double aggProfits = 0;
		int numAgents = 0;
		int numApplications = 0;
		double profitsFirm0 = 0;
		double profitsFirm1 = 0;
		double profitsFirm2 = 0;
		double profitsFirm3 = 0;
		double wageFirm0 = 0;
		double wageFirm1 = 0;
		double wageFirm2 = 0;
		double wageFirm3 = 0;
		double greedyWageFirm0 = 0;
		double greedyWageFirm1 = 0;
		double greedyWageFirm2 = 0;
		double greedyWageFirm3 = 0;
		double meanSquaredErrorFirm0 = 0;
		double meanSquaredErrorFirm1 = 0;
		double meanSquaredErrorFirm2 = 0;
		double meanSquaredErrorFirm3 = 0;
		double empFirm0 = 0;
		double empFirm1 = 0;
		double empFirm2 = 0;
		double empFirm3 = 0;
		double empID = 0;
		double greedyWageID = 0; 
		double wageID = 0;
		double profitID = 0; 
		double meanSquaredErrorID = 0;
		context.add(new Stat(space, grid, numEmployed, aggProfits, numAgents, numApplications, 
				profitsFirm0, profitsFirm1, profitsFirm2, profitsFirm3, 
				wageFirm0, wageFirm1, wageFirm2, wageFirm3,
				greedyWageFirm0, greedyWageFirm1, greedyWageFirm2, greedyWageFirm3, 
				meanSquaredErrorFirm0,meanSquaredErrorFirm1,meanSquaredErrorFirm2,meanSquaredErrorFirm3,
				empFirm0, empFirm1, empFirm2, empFirm3, empID, greedyWageID, wageID, profitID,meanSquaredErrorID,  numIterationsBatchRuns,databaseMode));



		//System.out.println("Test");


		//necessary to end batch runs, does XXX iterations (or XXX ticks)
		//to generate multiple runs for a particular parameter constellation
		//define as many random sees as you want runs
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(numIterationsBatchRuns);

		}	

		return context;
	}
}



