package onlineLaborQlearn;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Iterator;
import repast.simphony.query.space.grid.MooreQuery;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.math3.util.Precision;



//This defines the firm as an agent

public class Firms {
	
	//defines the space in which firms are placed; "space" is continuous; 
	//"grid" defines a grid
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	int sumAgents; 
	double countAgents;
	

	//ArrayList to put the workers in who work for this firm
	List<Workers> employmentList = new ArrayList<Workers>();
	
	//ArrayList to put the workers in who applied at this firm
	List<Workers> applicationList = new ArrayList<Workers>();
	
	NeuralNetwork mainNetwork;   // Main network used to determine optimal choices
	NeuralNetwork targetNetwork; // target network used to train the parameters of the main network
	
	ArrayList<Experience> memory; // Experience memory used for experience replay
	ArrayList<Experience> miniBatch; // Holds a random sample of past experiences for experience replay
	
	double[] currentState; // holds current state -> wages of firms at the beginning of the iteration 
	double[] nextState;  // holds next state ->  wages of firms after adjusting wages
	double[] wageList; // holds all possible wages firms can choose from
		
	int firmID; //holds firm ID
	int filledJobs; //number of filled jobs
	int position; // fixed position of firm on the circle
	double wageOffer; // firm's wage offer
	double greedyWage; //wage associated with higher Q value of current state
	double firmProductivity; // firm productivity
	double firmBaseProductivity; // firm base productivity
	double alpha; //firm production function parameter
	double profits; //a firm's profits
	double epsilon; //parameter greedy
	double beta; //parameter greedy
	int tick; //counts the ticks
	double fee; //platform fee
	double shareFee; // share of fee that firm has to pay
	double discontedReward;
	double sumReward;
	int numFirms; //number of firms in the market
	int modelType; //zero for take it or leave it, and one for bidding model
	int randomProductivity; //If set to 1: firm productivity is random draw
	int numIterationsBatchRuns;
	//Parameters Deep Q learning
	int freqTrainingNetwork; // Training frequency
	int miniBatchSize; //Size of mini batch drawn from experience memory
	int memorySize; // size of experience memory
	int freqUpdateTargetnet; // frequency of cloning the main network as target network
	double delta; // Discont factor
	double learningRate;  // learning rate to adjust weights in gradient descent method
	int learningStart; // Initial phase in which firms do only random wage setting and no learning takes place
	double weightRMSprop; // weight for smoothing the squared gradient
	double meanSquaredError = 0;  // Av Mean squared error of current mini batch
	double sumRMSE;
	double deltaProductivity;
	String optimizer;
	String activationFunction;
	int inputNormalization,rewardNormalization;
	
	
	int databaseMode;
	double[] factorNumNodesHiddenLayers;
	int numHiddenLayers;
	
	//constructor for a firm
	//defines the spaces / grids for the firms, and the parameters 
	//characterizing the firm (firmID ...)
	// We use different constructors depending on the type of q matrix
	
	
	
	public Firms(ContinuousSpace<Object> space, Grid<Object> grid, int firmID, int filledJobs, 
			int position, double wageOffer, double laggedOffer, double greedyWage, 
			double firmProductivity, double alpha, double profits, 
			double epsilon, double beta, 
			int freqTrainingNetwork,int miniBatchSize,int memorySize,int freqUpdateTargetnet,
			double delta,double learningRate,
			int tick, double fee, double shareFee, int numFirms, int modelType, 
			double[] wageList, int randomProductivity, int numIterationsBatchRuns, int learningStart, double weightRMSprop, int databaseMode, double[] factorNumNodesHiddenLayers,int numHiddenLayers,String optimizer, String activationFunction, int inputNormalization,int rewardNormalization, double deltaProductivity) {
		this.space = space;
		this.grid = grid;
		this.firmID = firmID; 
		this.filledJobs = filledJobs; 
		this.position = position; 
		this.wageOffer = wageOffer;
		this.greedyWage = greedyWage;
		this.firmProductivity = firmProductivity;
		this.firmBaseProductivity = firmProductivity;
		this.alpha = alpha;
		this.profits = profits;
		this.epsilon = epsilon;
		this.beta = beta;
		this.tick = tick;
		this.fee = fee;
		this.shareFee = shareFee;
		this.numFirms = numFirms;
		this.modelType = modelType;
		this.randomProductivity=randomProductivity;
		this.numIterationsBatchRuns =numIterationsBatchRuns;
		this.freqTrainingNetwork=freqTrainingNetwork;
		this.miniBatchSize=miniBatchSize;
		this.memorySize=memorySize;
		this.freqUpdateTargetnet=freqUpdateTargetnet;
		this.delta=delta;
		this.learningRate=learningRate;
		this.learningStart = learningStart;
		this.wageList = wageList;
		this.weightRMSprop=weightRMSprop;
		this.databaseMode=databaseMode;
		this.factorNumNodesHiddenLayers = factorNumNodesHiddenLayers;
		this.numHiddenLayers =  numHiddenLayers;
		this.discontedReward = 0;
		this.sumReward = 0;
		this.sumRMSE=0;
		this.optimizer = optimizer;
		this.activationFunction = activationFunction;
		this.rewardNormalization=rewardNormalization;
		this.deltaProductivity = deltaProductivity;
		
		
		this.inputNormalization=inputNormalization;
		memory = new ArrayList<Experience>();
		miniBatch = new ArrayList<Experience>();
		 
		// Distinguish two model types: mode 0 -> take it or leave it model; firms use wages of all firms to determine the best choice
		//								mode 1 -> bidding model; firms only consider own wage in wage updating 
		 
		 if(modelType==0) {
		
			 // Take it or leave it
			 
			 mainNetwork = new NeuralNetwork(0, wageList, numFirms, wageList.length,numHiddenLayers, learningRate,weightRMSprop, factorNumNodesHiddenLayers,miniBatchSize, optimizer, activationFunction, inputNormalization);
			 mainNetwork.setupNetwork();
			
			 // Create a deep copy of main network
			 targetNetwork = mainNetwork.clone();
			 targetNetwork.networkID=1;
			
			 // Determine the initial current state
			 currentState = new double[numFirms] ;
			 nextState = new double[numFirms] ;
			
		
			
			for(int i=0; i < numFirms; i++) {
					
				currentState[i] = 0.0;
				nextState[i] = 0.0;
			}
		
		}else {
			
			// Bidding
			
			mainNetwork = new NeuralNetwork(0, wageList, 1, wageList.length,numHiddenLayers,learningRate,weightRMSprop, factorNumNodesHiddenLayers,miniBatchSize, optimizer, activationFunction,inputNormalization);
			mainNetwork.setupNetwork();
			
			targetNetwork = mainNetwork.clone();
			targetNetwork.networkID=1;
		
			currentState = new double[1] ;
			nextState = new double[1] ;
			
			currentState[0] = 0.0;
			nextState[0] = 0.0;
			
		}
	}
	

	//scheduler start at tick=1, with interval = 1 the step() method is called for at ticks 1, 2, ...
	//priority determines when within a tick this method is called (higher numbers go first)
	//when the method is called it is executed on all firms that populate the model
	
	
//firms fire workers 
@ScheduledMethod (start = 1, interval = 1, priority = 110) 
	 public void firing(){
	
	 	 //System.out.println(this.firmID + " now firing");
	 	 
	 	 for(int i = 0; i < this.employmentList.size(); i++) {
	 		Workers aWorker = this.employmentList.get(i);
			aWorker.employmentStatus = 0;
			aWorker.whereWork = -1;
			aWorker.payOff = 0;
	 	 }
	 
		 
		//clears application  and employment list from previous round
		 this.applicationList.clear(); 
		 this.employmentList.clear();
		 this.filledJobs = 0;
		
	  }


//firms fire workers 
@ScheduledMethod (start = 1, interval = 1, priority = 105) 
	 public void setProductivity(){
	
	 	 if(this.randomProductivity==1) {
	 		 
	 		double randomDouble = RandomHelper.nextDoubleFromTo((-1)*deltaProductivity,deltaProductivity);
	 		 
	 		this.firmProductivity = firmBaseProductivity*(1 + randomDouble);
	 		 
	 	 }
		
	  }




//determines wage offer using greedy algorithm
@ScheduledMethod (start = 1, interval = 1, priority = 95)
	public void setCurrentState() {
	
	
		Context  context = ContextUtils.getContext(this);
	
		// Set current state depending on the model type
		if(modelType==0) {
			Iterable<Firms> allFirms = context.getObjects(Firms.class);
			for(Firms obj : allFirms) {
				Firms aFirm = obj;
					currentState[aFirm.firmID] = aFirm.wageOffer;
				
			}
		}else {
			
			currentState[0] = this.wageOffer;
			
		}
		
		
	}			








//determines wage offer using greedy algorithm
@ScheduledMethod (start = 1, interval = 1, priority = 90)
	public void wageOffer() {
	
		
		// Exploration vs exploitation
		
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		//System.out.println("random and epsilon: " + " " + random + " " + this.epsilon) ;
		if(random < 1 - this.epsilon) {
		
			// Exploitation: determine greedy wage using feedforwarding of the main network
			this.wageOffer = mainNetwork.feedforward(currentState).action; //assigns action to wage offer of firm	
			this.greedyWage = this.wageOffer;
		
		}	
		else {
			// Exploration: determione wages on a random base
			int randomInt = RandomHelper.nextIntFromTo(0,  wageList.length - 1);
			this.wageOffer = wageList[randomInt]; 
	
		}
		
	}			


//firms hiring workers	 
@ScheduledMethod (start = 1, interval = 1, priority = 60) 
	  public void hiring(){ 
		if(modelType == 0) {
			//System.out.println(this.firmID + " hiring workers");			
			for(int i = 0; i < this.applicationList.size(); i++) {
				Workers aWorker = this.applicationList.get(i);
				this.employmentList.add(aWorker);
				aWorker.whereWork = this.firmID;
				aWorker.employmentStatus = 1;			
			}
		}
		
		
		this.filledJobs = this.employmentList.size();
		
	  }



//firm calculates profits
@ScheduledMethod (start = 1, interval = 1, priority = 50)	  
	public void profits() {

		//System.out.println(this.firmID + " calculates profits");	

		this.profits = this.firmProductivity * Math.pow(this.employmentList.size(), this.alpha) 
		- (this.wageOffer + this.wageOffer * fee * shareFee)* this.employmentList.size();
		
		
		if(rewardNormalization==1) {
			this.profits = this.profits /1000.0;
		}
		
		this.discontedReward += Math.pow(this.delta,this.tick - this.learningStart)*this.profits;
		this.sumReward += this.profits;

	}




//firm updates its Q matrix
@ScheduledMethod (start = 1, interval = 1, priority = 40)	  
	public void updateQ() {
	
	
	tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	
	//Update epsilon
	
	this.epsilon = Math.exp( (-1) * this.beta * Math.max(0.0, this.tick-learningStart));
	
	//System.out.println(epsilon);
	
	//Update next state, depeding on the model type
	Context  context = ContextUtils.getContext(this);
	if(this.modelType==0) {
		Iterable<Firms> allFirms = context.getObjects(Firms.class);
		for(Firms obj : allFirms) {
			Firms aFirm = obj;
			
			nextState[aFirm.firmID] = aFirm.wageOffer;
		
			
		}
	}else {
		
		nextState[0] = this.wageOffer;
		
	}
	
	// Add current experience to the experience memory. Experience is a vector consisting of current state, profit, action and next state
		
	if(memory.size()>=memorySize)
		memory.remove(0);
	memory.add(new Experience(currentState, this.profits,this.wageOffer,nextState));
	
	// Online training
	if(tick > learningStart && tick % freqTrainingNetwork==0) {

		// Draw mini batch
		miniBatch.clear();
		for(int i=0; i < this.miniBatchSize;i++ ) {
			miniBatch.add(memory.get(RandomHelper.nextIntFromTo(0, memory.size()-1)));
		}
		
		meanSquaredError = 0.0;
		
		
		mainNetwork.setupBackPropagate();
		
		// Loop through the mini  batches and update the parameters of main using mean squared errors and gradient descent
		for(int i=0; i < miniBatch.size();i++) {
			
			// Uodate q values of main network based on drawn experience
			mainNetwork.feedforward(miniBatch.get(i).currentState);
			
			// Determine the optimal action in the next state, based on the target network
			ActionQValue nextAction =  targetNetwork.feedforward(miniBatch.get(i).nextState);
			
			// Determine the temporal difference y_j = pi + max_a Q^(s_t+1,a)
			ActionQValue target = new ActionQValue();
			target.action= miniBatch.get(i).action;
			target.qValue= miniBatch.get(i).profit + delta*nextAction.qValue;
			
			// Run back propagation based on the temporal difference error y_j -Q(s_t,a)
			mainNetwork.backPropagate(target, i);
			//Update the network with adjusted weights
			
			
			
		}
		
		
		mainNetwork.updateNetwork();
		
		
		// Compute average MSE of the current mini batch
		meanSquaredError =  mainNetwork.meanSquaredError /((double)miniBatch.size());
		
		
		
		sumRMSE += Math.sqrt(meanSquaredError);
		
	
		
		
		
	}
		
	//Clone the main network Q and set the target network equal the cloned main network
	if(tick %  this.freqUpdateTargetnet==0) {
		
		//Deep copy of mainNetwork
		targetNetwork = mainNetwork.clone();
		
		targetNetwork.networkID=1;
	
		
	}
	
	
	
		
	}


	
	
	
@ScheduledMethod (start = 1, interval = 1, priority = 10)	  
public void writeFirmMemoryToDatabase() {
	
	if(databaseMode==0 || databaseMode==1 || databaseMode==2) {
		if(this.tick>numIterationsBatchRuns-1000) {
		
				
				Context  context = ContextUtils.getContext(this);
				Iterable<DatabaseRecorder> allDatabaseRecorder = context.getObjects(DatabaseRecorder.class);
		
				for(DatabaseRecorder obj : allDatabaseRecorder) {
					
					DatabaseRecorder aDatabaseRecorder = obj;
					
				
					aDatabaseRecorder.insertFirm(this.tick, this);		
					
					aDatabaseRecorder.commit();
				}
		
		}
		
	}
	
	if(databaseMode==2) {
		if(this.tick>this.learningStart) {
		if((this.tick % 100000 == 0) ) {
			//if((this.tick >=50000 && this.tick <= 60000) ) {
			Context  context = ContextUtils.getContext(this);
			Iterable<DatabaseRecorder> allDatabaseRecorder = context.getObjects(DatabaseRecorder.class);
	
			for(DatabaseRecorder obj : allDatabaseRecorder) {
				
				DatabaseRecorder aDatabaseRecorder = obj;
				
			
				aDatabaseRecorder.insertNetworke(this.tick, this.firmID, this.mainNetwork);		
				
				
				aDatabaseRecorder.insertFirm(this.tick, this);	
				
				aDatabaseRecorder.commit();
			}
			
		}

	}
			
}
}		
				
}			


	  
