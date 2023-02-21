package onlineLaborQlearn;

public class Weight implements Cloneable{
	
	public Weight clone()  {
        try {
        	
        	
			return  (Weight) super.clone();
		} catch (CloneNotSupportedException e) {
			
			e.printStackTrace();
			return null;
		}
		
        
    }
	
	int nodeID; // ID of node the weight belongs to
	int inputID;  //ID of input node       
	
	double weight;
	
	Weight(int nID, int iID, double init){
		
		nodeID = nID;
		inputID = iID;
		weight = init;
		
	}
	
	


}
