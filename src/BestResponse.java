package onlineLaborQlearn;

public class BestResponse {
	
	int iteration;
	
	double[] currentState;
	double bestResponse;
	
	
	BestResponse(double[] currentState){
		
		this.currentState =  currentState;
		this.bestResponse = 0;
	}

}
