package onlineLaborQlearn;

import java.util.ArrayList;

public class NetworkLayer implements Cloneable{
	@Override
	public NetworkLayer clone()  {
		try {

			NetworkLayer cloned = (NetworkLayer) super.clone();

			cloned.nodeList = new ArrayList<NetworkNode>();

			for(int i=0; i < this.nodeList.size();i++ ) {

				cloned.nodeList.add( this.nodeList.get(i).clone());

			}

			return cloned;
		} catch (CloneNotSupportedException e) {

			e.printStackTrace();
			return null;
		}


	}

	int layerIndex;
	int numNodes;
	double[] actions; // Holds all possible actions
	String activationFunction; // Specifies the activation function used in this layer

	ArrayList<NetworkNode> nodeList; // Holds all nodes of this layer

	//Constructor
	NetworkLayer(int in, int nNo, String activFun, double[] act, boolean isOutputLayer, double learningsRate){

		layerIndex= in;
		numNodes = nNo;
		activationFunction = activFun;
		actions = act;
		nodeList = new ArrayList<NetworkNode>();

		// Setup nodes, distinguishing whether or not this is an output layer
		if(!isOutputLayer) {
			for(int i =0; i < numNodes; i++) {

				nodeList.add(new NetworkNode(i,layerIndex,activationFunction, 0.0,learningsRate));
			}
		}else {

			for(int i =0; i < actions.length; i++) {

				nodeList.add(new NetworkNode(i,layerIndex,activationFunction, actions[i],learningsRate));
			}
		}


	}

	NetworkLayer(){


	}



}
