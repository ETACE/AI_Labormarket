package onlineLaborQlearn;

import java.util.ArrayList;

public class NetworkNode implements Cloneable{
	
	@Override
	public NetworkNode clone()  {
        try {
        	
        	
        	NetworkNode cloned = (NetworkNode) super.clone();
        	
        	cloned.inputNodes = new ArrayList<InputNode>();
        	
        	
        	for(int i=0; i < this.inputNodes.size();i++) {
        		
        		cloned.inputNodes.add(this.inputNodes.get(i).clone());
     
        	}
        	
        	cloned.outputNodes = new ArrayList<OutputNode>();
        	
        	for(int i=0; i < this.outputNodes.size();i++) {
        		
        		cloned.outputNodes.add( this.outputNodes.get(i).clone());
     
        	}
        	
        	
			return cloned;
		} catch (CloneNotSupportedException e) {
			
			e.printStackTrace();
			return null;
		}
		
        
    }
	
	
	int nodeIndex;
	int layerIndex;
	double bias; // bias in the weighted sum
	double outputValue; // Output value used as input for the next layer or as qvalue in the output layer
	double weightedSum; // weighted sum of inputs
	double action;  //Holds the action associated with this node
	double targetValue; // Holds the target 
	double delta_k; //  network output error
	double avSquaredGradientDelta_k; 
	double updateBias;
	double m, m_hat, v, v_hat;
	
	double learningRate; // Leanring rate
	
	
	String activationFunction; // String to identify the used activation function
	
	ArrayList<InputNode> inputNodes;  // Input nodes the node is connected to
	ArrayList<OutputNode> outputNodes; // Output nodes the node is connected to
	
	//Constructor
	NetworkNode(int nodeIndex, int layerIndex, String activationFunction, double action, double learningRate){
		
		this.nodeIndex = nodeIndex;
		this.layerIndex = layerIndex;
		bias = 0;
		updateBias = 0;
		outputValue = 0;
		weightedSum = 0;
		this.activationFunction = activationFunction;
		this.action = action;
		this.learningRate = learningRate;
		inputNodes = new ArrayList<InputNode>();
		outputNodes = new ArrayList<OutputNode>();
		
	}
	
	NetworkNode(){
		
		
	}
	
	// Evaluates the inputs and determines the output value of the node
	void evaluate(NetworkLayer previousLayer) {
		
		// Update the output value of the input nodes
		for(int i=0; i < previousLayer.nodeList.size();i++) {
			
			for(int j=0; j < inputNodes.size();j++) {
				
				if( previousLayer.nodeList.get(i).nodeIndex==inputNodes.get(j).id) {
					
					inputNodes.get(j).outputValue = previousLayer.nodeList.get(i).outputValue;
					break;
				}
								
			}

		}
		
		
		// Determine the weightes sum of inputs
		weightedSum = bias;
				
		for(int i=0; i < inputNodes.size();i++) {
						
				weightedSum += inputNodes.get(i).outputValue*inputNodes.get(i).weight;
								
		}

		// Determine the output value as the output of the activation function
		outputValue = activationFunction(weightedSum);
	
		
	}
	
	
	// Activation function, identified by activationFunction String
	double activationFunction(double w) {
		
		
		if(activationFunction.equals("ReLU")||activationFunction.equals("ReLu")|| activationFunction.equals("Relu")||activationFunction.equals("relU")) {
			
		   return Math.max(0.0, w);
			
		}else if(activationFunction.equals("Identity")) {
			
			return  w;
			
			
		}else if(activationFunction.equals("Logistic")|| activationFunction.equals("logistic")) {
			
			return 1/(1 + Math.exp((-1)*w));
			
			
		}else {
			
			System.out.println("No proper activation function provided");
			System.exit(0);
			return 0;
			
			
		}
		
		
		
	}
	
	// Derivative of Activation function, identified by activationFunction String
double derivativeActivationFunction(double w) {
		
		
	if(activationFunction.equals("ReLU")||activationFunction.equals("ReLu")|| activationFunction.equals("Relu")||activationFunction.equals("relU")) {
			if(w>0)
				return 1;
			else
				return 0;
			
		}else if(activationFunction.equals("Identity")) {
			
			return  1;
			
			
		}else if(activationFunction.equals("Logistic") || activationFunction.equals("logistic")) {
			
			return activationFunction(w)*(1-activationFunction(w));
			
			
		}else {
			
			System.out.println("No proper activation function spezified");
			System.exit(0);
			return 0;
			
			
		}
		
		
		
	}



	
}
