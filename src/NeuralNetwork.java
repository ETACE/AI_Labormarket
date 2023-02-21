package onlineLaborQlearn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class NeuralNetwork implements Cloneable,Serializable  {


	// Used for deep copying
	@Override
	public NeuralNetwork clone()  {
        try {
			
        	NeuralNetwork cloned = (NeuralNetwork) super.clone();
        	
        	cloned.inputLayer = new NetworkLayer();		
        	cloned.inputLayer =  this.inputLayer.clone();
        	cloned.outputLayer =  new NetworkLayer();
        	cloned.outputLayer =  this.outputLayer.clone();
        	cloned.hiddenLayers = new ArrayList<NetworkLayer>();
        	
        	for(int i=0; i < this.hiddenLayers.size();i++ ) {
        		
        		cloned.hiddenLayers.add( this.hiddenLayers.get(i).clone());
        		
        	}
        	
        	return cloned;
		} catch (CloneNotSupportedException e) {
			
			e.printStackTrace();
			return null;
		}
		
        
    }
	
	/*Variables*/
	
	int networkID;
	
	//Number of layers and input and outputs of the network
	int numHiddenLayers;
	int numInputs, numOutputs;
	
	ActionQValue bestAction;
	
	double[] actions; //Array of possible actions
	double meanSquaredError; // Mean squared error
	double weightRMSprop; // weight for smoothing the squared gradient
	double w_min =0, w_max=0;
	int miniBatchSize;
	//Input, output and hidden layers
	NetworkLayer inputLayer;
	NetworkLayer outputLayer ;
	ArrayList<NetworkLayer> hiddenLayers ;
	Random r;
	String optimizer, activationFunction;
	double beta_1 = 0.9;
	double beta_2 = 0.999;
	double epsilon = 1e-8;
	
	int inputNormalization;
	
	double[] factorNumNodesHiddenLayers;
	
	//Constructor
	NeuralNetwork(int id, double[] act, int nIn, int nOut,int nHidLay, double learningRate, double weightRMSprop, double[] factorNumNodesHiddenLayers, int miniBatchSize,String optimizer, String activationFunction, int inputNormalization){
		
		networkID = id;
		numInputs = nIn;
		numOutputs = nOut;
		numHiddenLayers = nHidLay;
		actions = act;
		this.weightRMSprop = weightRMSprop;
		
		this.factorNumNodesHiddenLayers = factorNumNodesHiddenLayers;
		this.inputNormalization = inputNormalization;
		bestAction = new ActionQValue(); 
		meanSquaredError = 0.0;
		this.miniBatchSize = miniBatchSize;
		
		w_min = actions[0];
		w_max = actions[actions.length-1];
		// Setup the input layer
		
		
		this.optimizer = optimizer;
		this.activationFunction  = activationFunction;
		
		r = new Random();
		
		inputLayer = new NetworkLayer(0, numInputs, null, actions,false, learningRate);
		
		//Set up the hidden layers; we use a logistic function as activation function
		hiddenLayers = new ArrayList<NetworkLayer>();
		for(int i=1; i < nHidLay+1; i++) {
			
			hiddenLayers.add(new NetworkLayer(i,(int)((factorNumNodesHiddenLayers[i-1])*(numInputs+numOutputs)),activationFunction, actions, false,learningRate));
			
			//hiddenLayers.add(new NetworkLayer(i,(int)((factorNumNodesHiddenLayers[i-1])*(numInputs+numOutputs)),"ReLU", actions, false,learningRate));
		}
		//Set up the output layer, we use the identity y=x as activation function
		outputLayer = new NetworkLayer(nHidLay+1,numOutputs,"Identity", actions, true,learningRate);
		
		
		
	}
	
	NeuralNetwork(){
		
		
	}
	
	
	
	// Setup the network; we assume a fully connected network
	void setupNetwork() {
	
		// These loops go through each layer's nodes and connect these nodes with the corresponding input nodes 
		
		//Start with first hidden layer as it is connected with the input layer
		NetworkLayer ahiddenLayer = hiddenLayers.get(0);
		
		for(int i=0; i < ahiddenLayer.nodeList.size();i++ ) {
			
			for(int j=0; j < inputLayer.nodeList.size();j++) {
				
				NetworkNode aNode = inputLayer.nodeList.get(j);
				//Setup input connection with random weight, drawn from a U(0,1) distribution
				//ahiddenLayer.nodeList.get(i).inputNodes.add(new InputNode(aNode.nodeIndex,Math.random(),aNode.outputValue));
				
				//Setup input connection with random weight, drawn from a N(0,0.1) distribution
				
				double stdev = Math.sqrt(1/(1.0*numInputs));
				
				ahiddenLayer.nodeList.get(i).inputNodes.add(new InputNode(aNode.nodeIndex,r.nextGaussian()*stdev,aNode.outputValue));
				
				
				
			}
			
		}
		
		// Continue with all other hidden layers; if only one hidden layer is used, then this code is not executed
		for(int i=1; i < hiddenLayers.size(); i++) {
			
			ahiddenLayer = hiddenLayers.get(i);
			NetworkLayer bHiddenLayer = hiddenLayers.get(i-1);
			
			
			double stdev = Math.sqrt(1/(1.0*hiddenLayers.get(i-1).numNodes));
			
			for(int j=0; j < ahiddenLayer.nodeList.size();j++ ) {
		
				for(int k=0; k < bHiddenLayer.nodeList.size();k++) {
				
					 NetworkNode aNode = bHiddenLayer.nodeList.get(k);
					//Setup input connection with random weight, drawn from a U(0,1) distribution
					 //ahiddenLayer.nodeList.get(j).inputNodes.add(new InputNode(aNode.nodeIndex,Math.random(), aNode.outputValue));
			
					//Setup input connection with random weight, drawn from a N(0,0.1) distribution
					ahiddenLayer.nodeList.get(j).inputNodes.add(new InputNode(aNode.nodeIndex,r.nextGaussian()*stdev, aNode.outputValue));
				
					 
				}
				
			}
			
		}

		// Set the input connections for the output layer
		for(int j=0; j < outputLayer.nodeList.size();j++ ) {
			
			double stdev = Math.sqrt(1/(1.0* hiddenLayers.get(hiddenLayers.size()-1).numNodes));
			
			
			for(int k=0; k < hiddenLayers.get(hiddenLayers.size()-1).nodeList.size();k++) {
				
				 NetworkNode aNode =hiddenLayers.get(hiddenLayers.size()-1).nodeList.get(k);
				
				//Setup input connection with random weight, drawn from a U(0,1) distribution
				// outputLayer.nodeList.get(j).inputNodes.add(new InputNode(aNode.nodeIndex,Math.random(), aNode.outputValue));
				
				 outputLayer.nodeList.get(j).inputNodes.add(new InputNode(aNode.nodeIndex,r.nextGaussian()*stdev, aNode.outputValue));
			}
			
		}
		
		//Here we connect all nodes with their corresponding output nodes

		// We start with the input layer
		for(int i=0; i < inputLayer.nodeList.size();i++) {
			
			NetworkNode inNode = inputLayer.nodeList.get(i);
			
			for(int j=0; j < hiddenLayers.get(0).nodeList.size();j++) {
			
				NetworkNode outNode= hiddenLayers.get(0).nodeList.get(j);
				
				for(int k=0; k <outNode.inputNodes.size(); k++) {
					
					if(outNode.inputNodes.get(k).id == inNode.layerIndex) {
						
						double weight = outNode.inputNodes.get(k).weight;
						
						inNode.outputNodes.add(new OutputNode(outNode.nodeIndex,outNode.outputValue, weight));
						
						break;
					}	
				}
			}			
		}
		
		//We continue with the hidden layers up the the one before the last hidden layer; this code is not executed if only one hidden layer exists
		for(int i=0; i < hiddenLayers.size()-1;i++) {

			for(int j=0; j < hiddenLayers.get(i).nodeList.size();j++) {
				
				NetworkNode inNode = hiddenLayers.get(i).nodeList.get(j);

				for(int k=0; k < hiddenLayers.get(i+1).nodeList.size();k++) {
				
					NetworkNode outNode= hiddenLayers.get(i+1).nodeList.get(k);
					
					for(int l=0; l <outNode.inputNodes.size(); l++) {
						
						if(outNode.inputNodes.get(l).id == inNode.layerIndex) {
							
							double weight = outNode.inputNodes.get(l).weight;
							
							inNode.outputNodes.add(new OutputNode(outNode.nodeIndex,outNode.outputValue, weight));
							
							break;
						}	
					}
		
				}

			}

		}

		// Connecting the last hidden layer with the ouput layer
		for(int i=0; i < hiddenLayers.get(hiddenLayers.size()-1).nodeList.size();i++) {
			
			NetworkNode inNode = hiddenLayers.get(hiddenLayers.size()-1).nodeList.get(i);
			
			for(int j=0; j < outputLayer.nodeList.size();j++) {
			
				NetworkNode outNode= outputLayer.nodeList.get(j);
				
				for(int k=0; k <outNode.inputNodes.size(); k++) {
					
					if(outNode.inputNodes.get(k).id == inNode.layerIndex) {
						
						double weight = outNode.inputNodes.get(k).weight;
						
						inNode.outputNodes.add(new OutputNode(outNode.nodeIndex,outNode.outputValue, weight));
						
						break;
					}	
				}
			}			
		}
		

}
	
	
// this function updates the weights in the output connections of a node; to make the code more generic, this function calls a 
// a second function updateNetwork2 using the previous and the next layer as inputs
void updateNetwork( ) {
	

	for(int i=0; i < outputLayer.nodeList.size();i++ ) {
				
				NetworkNode aNode = outputLayer.nodeList.get(i);
				
				aNode.bias = aNode.bias - aNode.updateBias;
						
				for(int j=0; j < aNode.inputNodes.size();j++) {
					
					aNode.inputNodes.get(j).weight = aNode.inputNodes.get(j).weight - aNode.inputNodes.get(j).updateWeight;
		
				}
						
			}
			
			
	
			for(int i= hiddenLayers.size()-1; i>=0; i--) {
			
				for(int j=0; j < hiddenLayers.get(i).nodeList.size();j++ ) {
	
					NetworkNode aNode = hiddenLayers.get(i).nodeList.get(j);
					
					aNode.bias = aNode.bias - aNode.updateBias;
							
					for(int k=0; k < aNode.inputNodes.size();k++) {
						
						aNode.inputNodes.get(k).weight = aNode.inputNodes.get(k).weight - aNode.inputNodes.get(k).updateWeight;
					}
				}
			}
			
		
	
	
	updateNetwork2(inputLayer, hiddenLayers.get(0));
	
	for(int i=1; i < hiddenLayers.size();i++) {
		
		updateNetwork2(hiddenLayers.get(i-1), hiddenLayers.get(i));
		
	}
	
	updateNetwork2(hiddenLayers.get(hiddenLayers.size()-1), outputLayer);
	
	
}
	
	
void updateNetwork2(NetworkLayer previoudLayer,NetworkLayer nextLayer ) {

	for(int i=0; i < previoudLayer.nodeList.size();i++) {
			
			NetworkNode inNode = previoudLayer.nodeList.get(i);
	
			for(int j=0; j <nextLayer.nodeList.size();j++) {
			
				NetworkNode outNode= nextLayer.nodeList.get(j);
				
				for(int k=0; k <outNode.inputNodes.size(); k++) {
					
					if(outNode.inputNodes.get(k).id == inNode.layerIndex) {
						
						double weight = outNode.inputNodes.get(k).weight;
						
						for(int l=0; l < inNode.outputNodes.size();l++) {
							
							if(inNode.outputNodes.get(l).id==outNode.nodeIndex) {
								
								inNode.outputNodes.get(l).weight = weight;
								inNode.outputNodes.get(l).delta_k = 0.0;
								break;
							}
				
						break;
					}	
				}
			}			
		}
	}

}









// Feed forward procedure; determines the best action given the current state
ActionQValue feedforward(double[] currentState) {
		
		/*Setup the input nodes*/
		if(currentState.length == inputLayer.nodeList.size()) {
			
			for(int i=0; i<currentState.length;i++ ) {
				
				if(inputNormalization==1) {
					inputLayer.nodeList.get(i).outputValue= ( currentState[i] - w_min)/(w_max-w_min);
				}else {
					
					inputLayer.nodeList.get(i).outputValue= currentState[i];
					
				}
			}
			
		}else {
			System.out.println("Num current states != num input nodes");
			System.exit(0);
		}
		
		
		// Loop through the nodes of the hidden layers and evaluate each node given the input from the previous layer 
		for(int i=0; i < hiddenLayers.size(); i++) {
			
			NetworkLayer ahiddenLayer = hiddenLayers.get(i);
			
			for(int j=0; j < ahiddenLayer.nodeList.size();j++ ) {
				
				if(i==0)
					ahiddenLayer.nodeList.get(j).evaluate(inputLayer);
				else
					ahiddenLayer.nodeList.get(j).evaluate(hiddenLayers.get(i-1));
				
			}
			
		}

		// Loop through the output layer and evaluate each node given the input from the previous hidden layer 
		for(int j=0; j < outputLayer.nodeList.size();j++ ) {
	
			outputLayer.nodeList.get(j).evaluate(hiddenLayers.get(hiddenLayers.size()-1));
			
		}
		
		// Determine the action with the highest qvalue in the output layer
		double max = outputLayer.nodeList.get(0).outputValue;
		bestAction.action = outputLayer.nodeList.get(0).action;
		bestAction.qValue = outputLayer.nodeList.get(0).outputValue;
		
		for(int i=1; i < outputLayer.nodeList.size();i++ ) {
			
			if(outputLayer.nodeList.get(i).outputValue > max) {
				
				max = outputLayer.nodeList.get(i).outputValue;
				bestAction.action = outputLayer.nodeList.get(i).action;
				bestAction.qValue = outputLayer.nodeList.get(i).outputValue;
				
			}
			
			
		}	
		
		
	return 	bestAction;
		
		
	}



	void setupBackPropagate() {
		
		
		meanSquaredError = 0;
		
		
		for(int i=0; i < outputLayer.nodeList.size();i++ ) {
			
			NetworkNode aNode = outputLayer.nodeList.get(i);
			
			aNode.updateBias = 0;
			aNode.m=0;
			aNode.v=0;
					
			for(int j=0; j < aNode.inputNodes.size();j++) {
				
				aNode.inputNodes.get(j).updateWeight = 0;
				aNode.inputNodes.get(j).m=0;
				aNode.inputNodes.get(j).v=0;
	
			}
					
		}
		
		

		for(int i= hiddenLayers.size()-1; i>=0; i--) {
		
			for(int j=0; j < hiddenLayers.get(i).nodeList.size();j++ ) {

				NetworkNode aNode = hiddenLayers.get(i).nodeList.get(j);
				
				aNode.updateBias = 0;
				aNode.m=0;
				aNode.v=0;
						
				for(int k=0; k < aNode.inputNodes.size();k++) {
					
					aNode.inputNodes.get(k).updateWeight = 0;
					aNode.inputNodes.get(k).m=0;
					aNode.inputNodes.get(k).v=0;
				}
			}
		}
	}


// back propagation to adjust the parameters of the network using the gradient descent method

	// Check https://dustinstansbury.github.io/theclevermachine/derivation-backpropagation for derivation of this method
	// Adam implemented acc to https://towardsdatascience.com/how-to-implement-an-adam-optimizer-from-scratch-76e7b217f1cc
	
	void backPropagate(ActionQValue target, int minibatchNo) {
	
		for(int i=0; i < outputLayer.nodeList.size();i++ ) {
			
			outputLayer.nodeList.get(i).delta_k=0.0;
					
		}

		// Loop through the output layer and select the node corresponding to the action under consideration
		for(int i=0; i < outputLayer.nodeList.size();i++ ) {
	
				if(outputLayer.nodeList.get(i).action==target.action) {
					
					NetworkNode aNode = outputLayer.nodeList.get(i);
					
					aNode.targetValue = target.qValue;
					
					// determine mean squared error; only used for performance checks
					meanSquaredError += 0.5*Math.pow(aNode.outputValue - aNode.targetValue,2);
					
					// determine the network output error delta_k
					aNode.delta_k =  (aNode.outputValue - aNode.targetValue)*aNode.derivativeActivationFunction(aNode.weightedSum);
					
					boolean update_bias =false;
					if(update_bias) {
						if(this.optimizer.equals("Adam") ||this.optimizer.equals("ADAM")) {
							aNode.m = this.beta_1*aNode.m + (1-this.beta_1)*aNode.delta_k;
							aNode.v = this.beta_2*aNode.v + (1-this.beta_2)*Math.pow(aNode.delta_k,2);
							
							double m_hat = aNode.m / (1 - Math.pow(this.beta_1,minibatchNo +1));
							double v_hat = aNode.v / (1 - Math.pow(this.beta_2,minibatchNo +1));
							
							aNode.updateBias += aNode.learningRate*m_hat/(Math.sqrt(v_hat) + this.epsilon);
						
						}else {
							aNode.updateBias += aNode.learningRate*aNode.delta_k/(1.0*miniBatchSize);
						}
					}
				
					
					//Update the weights	
   					for(int j=0; j < aNode.inputNodes.size();j++) {
						
						
						double gradient = aNode.delta_k*aNode.inputNodes.get(j).outputValue;
						
						if(this.optimizer.equals("Adam") ||this.optimizer.equals("ADAM")) {
							aNode.m = this.beta_1*aNode.m + (1-this.beta_1)*gradient;
							aNode.v = this.beta_2*aNode.v + (1-this.beta_2)*Math.pow(gradient,2);
							
							double m_hat = aNode.m / (1 - Math.pow(this.beta_1,minibatchNo+1 ));
							double v_hat = aNode.v / (1 - Math.pow(this.beta_2,minibatchNo+1 ));
							
							aNode.inputNodes.get(j).updateWeight  += aNode.learningRate*m_hat/(Math.sqrt(v_hat) + this.epsilon);
						
						}else {
						
						aNode.inputNodes.get(j).updateWeight +=  aNode.learningRate*gradient/(1.0*miniBatchSize);
						
						}
						
					}
	
				break;
				
			}
		
		}
		
		//Loop through the hidden layers and adjust bias and weights in these nodes
		
		for(int i= hiddenLayers.size()-1; i>=0; i--) {
		
			// Update the delta_k variable from the next layers in the memory of the node 
			if(i==hiddenLayers.size()-1) {
				updateDeltaK(hiddenLayers.get(i), outputLayer);
				
			}else {	
				updateDeltaK(hiddenLayers.get(i), hiddenLayers.get(i+1));
				
			}
		

			for(int j=0; j < hiddenLayers.get(i).nodeList.size();j++ ) {
				
				NetworkNode aNode = hiddenLayers.get(i).nodeList.get(j);
		
				//Update aNode.delta_k in current node
				aNode.delta_k = 0; 
				for(int k=0; k < aNode.outputNodes.size();k++) {
					aNode.delta_k += aNode.outputNodes.get(k).weight* aNode.outputNodes.get(k).delta_k;
				}

				aNode.delta_k = aNode.delta_k*aNode.derivativeActivationFunction(aNode.weightedSum);
				boolean update_bias =false;
				if(update_bias) {
					if(this.optimizer.equals("Adam") ||this.optimizer.equals("ADAM")) {
						aNode.m = this.beta_1*aNode.m + (1-this.beta_1)*aNode.delta_k;
						aNode.v = this.beta_2*aNode.v + (1-this.beta_2)*Math.pow(aNode.delta_k,2);
						
						double m_hat = aNode.m / (1 - Math.pow(this.beta_1,minibatchNo +1));
						double v_hat = aNode.v / (1 - Math.pow(this.beta_2,minibatchNo+1 ));
						
						aNode.updateBias += aNode.learningRate*m_hat/(Math.sqrt(v_hat) + this.epsilon);
					
					}else {
						aNode.updateBias += aNode.learningRate*aNode.delta_k/(1.0*miniBatchSize);
					}
				}

				//Update weights
				for(int k=0; k < aNode.inputNodes.size();k++) {

					double gradient = aNode.delta_k*aNode.inputNodes.get(k).outputValue;
					if(this.optimizer.equals("Adam") ||this.optimizer.equals("ADAM")) {
						aNode.inputNodes.get(k).m = this.beta_1*aNode.inputNodes.get(k).m + (1-this.beta_1)*gradient;
						aNode.inputNodes.get(k).v = this.beta_2*aNode.inputNodes.get(k).v + (1-this.beta_2)*Math.pow(gradient,2);
						
						double m_hat = aNode.m / (1 - Math.pow(this.beta_1,minibatchNo +1));
						double v_hat = aNode.v / (1 - Math.pow(this.beta_2,minibatchNo +1));
						
						aNode.inputNodes.get(k).updateWeight  += aNode.learningRate*m_hat/(Math.sqrt(v_hat) + this.epsilon);
					
					}else {
					
						aNode.inputNodes.get(k).updateWeight += aNode.learningRate*gradient/(1.0*miniBatchSize);
					}
					
				}
	
			}
		
		
		}
		
	
	}
	
	
	
	
	
	// This function is used to update the delta_k of the next layer's nodes in the memroy of the current layer's nodes 
	void updateDeltaK(NetworkLayer previous, NetworkLayer next) {

		for(int i=0; i< previous.nodeList.size();i++) {
			
			for(int j=0; j < previous.nodeList.get(i).outputNodes.size();j++) {
				
				for(int k=0; k < next.nodeList.size();k++) {
					
					if(next.nodeList.get(k).nodeIndex   ==previous.nodeList.get(i).outputNodes.get(j).id) {
						
						previous.nodeList.get(i).outputNodes.get(j).delta_k = next.nodeList.get(k).delta_k;
						break;
					}
				}
			}
	
		}

	}
	
	
	
	
}


