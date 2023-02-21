package onlineLaborQlearn;

public class InputNode implements Cloneable{
	@Override
	public InputNode clone()  {
        try {
			return  (InputNode) super.clone();
		} catch (CloneNotSupportedException e) {
			
			e.printStackTrace();
			return null;
		}
		
        
    }
	

	int id;
	double weight;
	double outputValue;
	double avSqrGradient;
	double updateWeight;
	double m, m_hat, v, v_hat;
	
	
	InputNode(int i, double w, double o){
		
		id=i;
		weight = w; 
		outputValue = o;
		avSqrGradient = 0.0;
		updateWeight = 0;
		
	}
	

}
