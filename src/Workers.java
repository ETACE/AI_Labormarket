package onlineLaborQlearn;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;




public class Workers {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	int workerID; //This gives the worker an ID
	int employmentStatus; // if the worker is employed then 1 otherwise 0
	double payOff; // utility of worker
	int whereWork; // firmID of firm at which worker is employed; -1 if not employed;
	int position; //fixed position of worker on the circle
	int numPositions; //number of positions on Salop circle
	double fee; //platform fee
	double shareFee; //share of fee that firm has to pay
	double effort;  // effort -> accounting for distance of worker to firm
	int numFirms; //number of firms in market (either two or four)
	int modelType; //zero for take it or leave it, and one for bidding model

	
	//list of firms with vacancies
	List<Firms> vacancyList = new ArrayList<Firms>();
	
	//list of firm with best offer
	List<Firms> bestOfferList = new ArrayList<Firms>();
	
			
		
	//constructor for workers
	public Workers(ContinuousSpace<Object> space, Grid<Object> grid, int workerID, int employmentStatus, double payOff,
			int whereWork, int position, int numPositions, double fee, double shareFee, double effort,  int numFirms, int modelType) {
		this.space = space;
		this.grid = grid;
		this.workerID = workerID;
		this.employmentStatus = employmentStatus;
		this.payOff = payOff;
		this.whereWork = whereWork;
		this.position = position; 
		this.numPositions = numPositions;
		this.fee = fee;
		this.shareFee = shareFee;
		this.effort = effort;
		this.numFirms = numFirms;
		this.modelType = modelType;
	}
	
	
	//workers apply, only in bidding model
	//workers apply for task at both firms
	@ScheduledMethod (start = 1, interval = 1, priority = 100)
	public void applyingBid() {
		if(this.modelType == 1) {
			Context  context = ContextUtils.getContext(this);		
			Iterable<Firms> allFirms = context.getObjects(Firms.class);
			for(Firms obj : allFirms) {
				Firms aFirm = obj;
				aFirm.applicationList.add(this);				
			}
		}
	}
	
	
	
	//workers decide whether to work and where
	//code for take it or leave it only
	@ScheduledMethod (start = 1, interval = 1, priority = 70)
	public void applying() {
		
		if(this.modelType == 0) {
		
			//System.out.println(this.workerID + " workers apply");
			
			//puts firms with vacancies in vacancyList
			Context  context = ContextUtils.getContext(this);		
			Iterable<Firms> allFirms = context.getObjects(Firms.class);
			for(Firms obj : allFirms) {
				Firms aFirm = obj;
				this.vacancyList.add(aFirm);				
			}
			
			
			Collections.shuffle(this.vacancyList);
			
			//this worker goes through the list of vacancies and picks the best offer
			//double tempPayoffCurrent = -100;  // if all workers
			//are supposed to work this initialization has to be smaller than
			// (-1) * numPositions / 4
			double tempPayoffCurrent = 0; // workers only work if payoff is higher than zero
					//i.e. workers have reservation payoff of zero
			double tempPayoffNew = 0; 
			
			
			for(int i = 0; i < this.vacancyList.size(); i++) {
				Firms aFirm = this.vacancyList.get(i);
				
				int distance = Math.min(Math.abs(this.position - aFirm.position),this.position + numPositions - aFirm.position);
				distance = Math.min(distance, aFirm.position + numPositions -this.position);
				
				tempPayoffNew = (aFirm.wageOffer - aFirm.wageOffer * this.fee * this.shareFee) 
						- distance *this.effort;
				
		
				
				//new best offer	
				//if(tempPayoffNew > tempPayoffCurrent) {
				if(tempPayoffNew >= tempPayoffCurrent) {
					if(!this.bestOfferList.isEmpty()) {
						this.bestOfferList.clear(); //drops the current favorite firm
					}
					this.bestOfferList.add(aFirm);	//adds the new favorite firm			
					tempPayoffCurrent = tempPayoffNew;
					this.payOff = tempPayoffCurrent;
				}
			
				
			}
			
			
			
			
			
			
			//this worker applies at the best firm
			if(!this.bestOfferList.isEmpty()) {
				Firms aFirm = this.bestOfferList.get(0);
				aFirm.applicationList.add(this);			
			}		
			this.bestOfferList.clear(); //clears the best offer list
			this.vacancyList.clear(); //clears the vacancy list 
	
										 
		}
	}
	
	@ScheduledMethod (start = 1, interval = 1, priority = 65)
	public void workersAccepts() {
		if(this.modelType == 1) {	
				
			//puts firms with vacancies and wage offers larger than
			//this worker's reservation wage in vacancyList
			Context  context = ContextUtils.getContext(this);		
			Iterable<Firms> allFirms = context.getObjects(Firms.class);
			for(Firms obj : allFirms) {
				Firms aFirm = obj;
				//put only those firms on a worker's vacancy list that offer wage which is 
				//above the worker's reservation payoff
				
				int distance = Math.min(Math.abs(this.position - aFirm.position),this.position + numPositions - aFirm.position);
				distance = Math.min(distance, aFirm.position + numPositions -this.position);
				
				//calculating net wage; case distinction for where firm and worker sit 
				double tempPayoff = 0; 
				
				tempPayoff = (aFirm.wageOffer - aFirm.wageOffer * this.fee * this.shareFee) 
						- distance*this.effort;
				
				
								
				if(aFirm.wageOffer >= tempPayoff) {
					this.vacancyList.add(aFirm);
				}
				
			}
			
			Collections.shuffle(this.vacancyList);
			
			//this worker goes through the list of vacancies and picks the best offer
			//double tempPayoffCurrent = -100;  // if all workers
			//are supposed to work this initialization has to be smaller than
			// (-1) * numPositions / 4 
			double tempPayoffCurrent = 0; //workers have reservation payoff of zero
			double tempPayoffNew = 0; 
			
	
			for(int i = 0; i < this.vacancyList.size(); i++) {
				Firms aFirm = this.vacancyList.get(i);
				
				int distance = Math.min(Math.abs(this.position - aFirm.position),this.position + numPositions - aFirm.position);
				distance = Math.min(distance, aFirm.position + numPositions -this.position);
				
				tempPayoffNew = (aFirm.wageOffer - aFirm.wageOffer * this.fee * this.shareFee) - distance*this.effort;
						
				
				//new best offer		
				//if(tempPayoffNew > tempPayoffCurrent) {
				if(tempPayoffNew >= tempPayoffCurrent) {
					if(!this.bestOfferList.isEmpty()) {
						this.bestOfferList.clear(); //drops the current favorite firm
					}
					this.bestOfferList.add(aFirm);	//adds the new favorite firm			
					tempPayoffCurrent = tempPayoffNew;
					this.payOff = tempPayoffCurrent;
				}
				
				/*if(tempPayoffNew == tempPayoffCurrent) { 
					//draws random number to resolve indifference
					double random = RandomHelper.nextDoubleFromTo(0, 1);
					if(random <= 0.5) {
						if(!this.bestOfferList.isEmpty()) {
							this.bestOfferList.clear(); //drops the current favorite firm
						}
						this.bestOfferList.add(aFirm);	//adds the new favorite firm			
						tempPayoffCurrent = tempPayoffNew;
						this.payOff = tempPayoffCurrent;
					}
				}*/
				
			}
			
			
			//this worker accepts offer at best firm and is employed there
			if(!this.bestOfferList.isEmpty()) {
				Firms aFirm = this.bestOfferList.get(0);
				aFirm.employmentList.add(this);	
				this.whereWork = aFirm.firmID;
				this.employmentStatus = 1;
			}		
			this.bestOfferList.clear(); //clears the best offer list
			this.vacancyList.clear(); //clears the vacancy list 	
			
			//System.out.println(this.workerID + " " + this.payOff + " " + this.employmentStatus + " " + this.whereWork);
		}	
			
			
			

	}
	


	//example code for including a watch class sequencing
	//executes run() if @Watch is true; i.e. if in watheeClass the watcheeField is true;
	//@Watch(watcheeClassName = "jzombies.Zombie", watcheeFieldNames = "moved", 
	//		query = "within_vn 1", whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	


}

