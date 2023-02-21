package onlineLaborQlearn;

import java.util.ArrayList;

public class OutputNode implements Cloneable{
	@Override
	public OutputNode clone()  {
        try {
			return  (OutputNode) super.clone();
		} catch (CloneNotSupportedException e) {
			
			e.printStackTrace();
			return null;
		}
		
	} 
	
	
	int id;
	double outputValue;
	double weight;
	double delta_k;
	
	OutputNode(int i, double o, double w){
		
		id = i;
		outputValue = o;
		weight = w;
		
	}

}
