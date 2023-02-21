 package onlineLaborQlearn;


import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;


public class Stat {
	
	ContinuousSpace<Object> space;
	Grid<Object> grid;
	double numEmployed;//number employed in economy
	double aggProfits; //calculates the aggregate profits of firms
	int numAgents; //number of agents on the grid
	int numApplications; //number of agents who applied to any firm
	double profitsFirm0; //profits firm 0
	double profitsFirm1;
	double profitsFirm2; 
	double profitsFirm3; 
	double wageFirm0; // wage firm 0
	double wageFirm1;
	double wageFirm2;
	double wageFirm3;
	double greedyWageFirm0;
	double greedyWageFirm1;
	double greedyWageFirm2;
	double greedyWageFirm3;
	double meanSquaredErrorFirm0;
	double meanSquaredErrorFirm1;
	double meanSquaredErrorFirm2;
	double meanSquaredErrorFirm3;
	double empFirm0; //employment at firm 0
	double empFirm1; //employment at firm 1
	double empFirm2; //employment at firm 0
	double empFirm3; //employment at firm 1
	double empID; //needed for GUI on timeseries
	double greedyWageID = 0; //needed for GUI on timeseries
	double wageID = 0; //needed for GUI on timeseries
	double profitID = 0;  //needed for GUI on timeseries
	double meanSquaredErrorID;
	int numIterationsBatchRuns; // Num of iterations in batch odus to doscinnect the data base
	int databaseMode;
	
	//constructor for statistical agent
	public Stat(ContinuousSpace<Object> space, Grid<Object> grid, double numEmployed, double aggProfits, int numAgents, 
			int numApplications, double profitsFirm0, double profitsFirm1, double profitsFirm2, double profitsFirm3,
			double wageFirm0, double wageFirm1, double wageFirm2, double wageFirm3,
			double greedyWageFirm0, double greedyWageFirm1, double greedyWageFirm2, double greedyWageFirm3, double meanSquaredErrorFirm0, 
			double meanSquaredErrorFirm1,double meanSquaredErrorFirm2,double meanSquaredErrorFirm3,
			double empFirm0, double empFirm1, double empFirm2, double empFirm3, double empID, 
			double greedyWageID, double wageID, double profitID, double meanSquaredErrorID, int numIterationsBatchRuns, int databaseMode) {
		this.numEmployed = numEmployed;
		this.aggProfits = aggProfits;
		this.numAgents = numAgents;
		this.numApplications = numApplications;
		this.profitsFirm0 = profitsFirm0;
		this.profitsFirm1 = profitsFirm1;
		this.profitsFirm2 = profitsFirm2;
		this.profitsFirm3 = profitsFirm3;
		this.wageFirm0 = wageFirm0;
		this.wageFirm1 = wageFirm1;
		this.wageFirm2 = wageFirm2;
		this.wageFirm3 = wageFirm3;
		this.greedyWageFirm0 = greedyWageFirm0;
		this.greedyWageFirm1 = greedyWageFirm1;
		this.greedyWageFirm2 = greedyWageFirm2;
		this.greedyWageFirm3 = greedyWageFirm3;
		this.meanSquaredErrorFirm0 = meanSquaredErrorFirm0;
		this.meanSquaredErrorFirm1 = meanSquaredErrorFirm1;
		this.meanSquaredErrorFirm2 = meanSquaredErrorFirm2;
		this.meanSquaredErrorFirm3 = meanSquaredErrorFirm3;
		this.empFirm0 = empFirm0;
		this.empFirm1 = empFirm1;
		this.empFirm2 = empFirm2;
		this.empFirm3 = empFirm3;
		this.empID = empID;
		this.greedyWageID = greedyWageID;
		this.wageID = wageID;
		this.profitID = profitID;
		this.meanSquaredErrorID = meanSquaredErrorID;
		this.numIterationsBatchRuns = numIterationsBatchRuns;
		this.databaseMode = databaseMode;
	}
	
	
@ScheduledMethod (start = 1, interval = 1, priority = 10)
	public void stats() {
	
		//System.out.println("stat office calculates market outcomes at end of tick");
		
		//resetting stats
		this.numEmployed = 0;
		this.aggProfits = 0;
		this.numAgents = 0;
		this.numApplications = 0;
		this.profitsFirm0 = 0;
		this.profitsFirm1 = 0;
		this.profitsFirm2 = 0;
		this.profitsFirm3 = 0;
		this.wageFirm0 = 0;
		this.wageFirm1 = 0;
		this.wageFirm2 = 0;
		this.wageFirm3 = 0;
		this.greedyWageFirm0 = 0;
		this.greedyWageFirm1 = 0;
		this.greedyWageFirm2 = 0;
		this.greedyWageFirm3 = 0;
		this.meanSquaredErrorFirm0 = 0;
		this.meanSquaredErrorFirm1 = 0;
		this.meanSquaredErrorFirm2 = 0;
		this.meanSquaredErrorFirm3 = 0;
		this.meanSquaredErrorID=0.0;
		this.empFirm0 = 0;
		this.empFirm1 = 0;
		this.empFirm2 = 0;
		this.empFirm3 = 0;
		this.empID = 0;
		this.greedyWageID = 0;
		this.wageID = 0;
		
		
		
		//calls the context for the statistical office
		Context  context = ContextUtils.getContext(this);
		
				
		
		//goes through the iterable for workers and calculates employment levels
		Iterable<Workers> allWorkers = context.getObjects(Workers.class);
		for(Workers obj : allWorkers) {
			Workers aWorker = obj;
			this.numEmployed = this.numEmployed + aWorker.employmentStatus;
			if(aWorker.whereWork == 0  && aWorker.employmentStatus == 1) {
				this.empFirm0 = this.empFirm0 + aWorker.employmentStatus;
			}
			if(aWorker.whereWork == 1  && aWorker.employmentStatus == 1) {
				this.empFirm1 = this.empFirm1 + aWorker.employmentStatus;
			}
			if(aWorker.whereWork == 2  && aWorker.employmentStatus == 1) {
				this.empFirm2 = this.empFirm2 + aWorker.employmentStatus;
			}
			if(aWorker.whereWork == 3  && aWorker.employmentStatus == 1) {
				this.empFirm3 = this.empFirm3 + aWorker.employmentStatus;
			}
			
			
		}
		
		//goes through the iterable for firms
		Iterable<Firms> allFirms = context.getObjects(Firms.class);
		for(Firms obj : allFirms) {
			Firms aFirm = obj;
			this.aggProfits = this.aggProfits + aFirm.profits;
			this.numApplications = this.numApplications + aFirm.applicationList.size();
			if(aFirm.firmID == 0) {
				this.profitsFirm0 = aFirm.profits;
				this.wageFirm0 = aFirm.wageOffer;
				this.greedyWageFirm0 = aFirm.greedyWage;
				this.meanSquaredErrorFirm0 = aFirm.meanSquaredError;
			}
			if(aFirm.firmID == 1) {
				this.profitsFirm1 = aFirm.profits;
				this.wageFirm1 = aFirm.wageOffer;
				this.greedyWageFirm1 = aFirm.greedyWage;
				this.meanSquaredErrorFirm1 = aFirm.meanSquaredError;
			}
			if(aFirm.firmID == 2) {
				this.profitsFirm2 = aFirm.profits;
				this.wageFirm2 = aFirm.wageOffer;
				this.greedyWageFirm2 = aFirm.greedyWage;
				this.meanSquaredErrorFirm2 = aFirm.meanSquaredError;
			}
			if(aFirm.firmID == 3) {
				this.profitsFirm3 = aFirm.profits;
				this.wageFirm3 = aFirm.wageOffer;
				this.greedyWageFirm3 = aFirm.greedyWage;
				this.meanSquaredErrorFirm3 = aFirm.meanSquaredError;
			}
			
		}
		
		for (Object obj : context) {
			this.numAgents = this.numAgents + 1;
		}
		
				
		//System.out.println("hello i am the statistical office");
		//System.out.println(this.numEmployed);
		
		
		
		if(databaseMode==1 || databaseMode==2) {
			

			if((int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount() % 1000 ==0 ||  RunEnvironment.getInstance().getCurrentSchedule().getTickCount()>this.numIterationsBatchRuns-1000) {
			
			
		
			Iterable<DatabaseRecorder> databaseRecorder = context.getObjects(DatabaseRecorder.class);
	
			for(DatabaseRecorder obj : databaseRecorder) {
				
				DatabaseRecorder aDatabaseRecorder = obj;
				aDatabaseRecorder.insertStat( (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount(), this);		
				aDatabaseRecorder.commit();
				}
	
			}
		
	
		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()==this.numIterationsBatchRuns) {
			
		
				context = ContextUtils.getContext(this);
				
				
				Iterable<DatabaseRecorder> databaseRecorder = context.getObjects(DatabaseRecorder.class);
				

					for(DatabaseRecorder obj : databaseRecorder) {
						
						DatabaseRecorder aDatabaseRecorder = obj;
						
						aDatabaseRecorder.atEnd();
						
					}

				}
			
			
		}
	
		
		
		}

//needs to be specified so that data sets can be constructed		
	public double getNumEmployed() {
	    return this.numEmployed;
	}
	
	public double getAggProfits() {
	    return this.aggProfits;
	}

	public int getNumAgents() {
	    return this.numAgents;
	}
	
	public int getNumApplications() {
	    return this.numApplications;
	}
	
	public double getProfitsFirm0() {
		return this.profitsFirm0;
	}
	
	public double getProfitsFirm1() {
		return this.profitsFirm1;
	}
	
	public double getProfitsFirm2() {
		return this.profitsFirm2;
	}
	
	public double getProfitsFirm3() {
		return this.profitsFirm3;
	}
	
	public double getProfitID() {
		return this.profitID;
	}

	public double getWageFirm0() {
		return this.wageFirm0;
	}
	
	public double getWageFirm1() {
		return this.wageFirm1;
	}	
	
	public double getWageFirm2() {
		return this.wageFirm2;
	}
	
	public double getWageFirm3() {
		return this.wageFirm3;
	}	
	
	public double getWageID() {
		return this.wageID;
	}
	
	public double getGreedyWageFirm0() {
		return this.greedyWageFirm0;
	}		
	
	public double getGreedyWageFirm1() {
		return this.greedyWageFirm1;
	}		

	public double getGreedyWageFirm2() {
		return this.greedyWageFirm2;
	}		
	
	public double getGreedyWageFirm3() {
		return this.greedyWageFirm3;
	}
	
	public double getGreedyWageID() {
		return this.greedyWageID;
	}
	
	public double getMeanSquaredErrorFirm0() {
		return this.meanSquaredErrorFirm0;
	}
	
	public double getMeanSquaredErrorFirm1() {
		return this.meanSquaredErrorFirm1;
	}
	
	public double getMeanSquaredErrorFirm2() {
		return this.meanSquaredErrorFirm2;
	}
	
	public double getMeanSquaredErrorFirm3() {
		return this.meanSquaredErrorFirm3;
	}
	
	public double getMeanSquaredErrorID() {
		return this.meanSquaredErrorID;
	}
	
	
	
	public double getEmpFirm0() {
		return this.empFirm0;
	}	
	
	public double getEmpFirm1() {
		return this.empFirm1;
	}	
	
	public double getEmpFirm2() {
		return this.empFirm2;
	}	
	
	public double getEmpFirm3() {
		return this.empFirm3;
	}	
	
	public double getEmpID() {
		return this.empID;
	}	

}

