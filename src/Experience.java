package onlineLaborQlearn;

public class Experience {
	
	double[] currentState;
	double profit;
	double action;
	double[] nextState;
	
	Experience(double[] curSta, double pr, double ac, double[] neSta){
		
		currentState = curSta;
		profit = pr;
		action = ac;
		nextState = neSta;
		
	}

}
