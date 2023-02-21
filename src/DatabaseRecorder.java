package onlineLaborQlearn;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import repast.simphony.context.Context;
import org.apache.commons.math3.util.Precision;

public class DatabaseRecorder {
	
		String pathToDB;
		int writeQMatrixToDatabaseMode;
		int modelMode;
		 Connection c;
		 Statement stmt = null;
		
		 
		 public DatabaseRecorder(String pathToDB, String suffix)
		  {
			  
			  this.pathToDB = pathToDB;
			  this.modelMode = modelMode;
		      c = null;
		    
		    try {
		      Class.forName("org.sqlite.JDBC");
		    
		    	  c = DriverManager.getConnection("jdbc:sqlite:"+pathToDB+"/QMatrix"+suffix+".db");
		    
		     
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		    }
		   //System.out.println("Opened database successfully");
		    
		    //if data base is overwritten, we first drop the tables from the old data base
		    
		    try {
		    
		    	 stmt = null;
		    	 try {
					Class.forName("org.sqlite.JDBC");
				} catch (ClassNotFoundException e) {
					
					e.printStackTrace();
				}
		       
		  			stmt = c.createStatement();
		  			String sql = "DROP TABLE IF EXISTS NeuralNetwork"; 
		  			stmt.executeUpdate(sql);
		  			
		  			
		  			stmt = c.createStatement();
		  			sql = "DROP TABLE IF EXISTS Parameters"; 
		  			stmt.executeUpdate(sql);
		  			
		  			stmt = c.createStatement();
		  			sql = "DROP TABLE IF EXISTS Firms"; 
		  			stmt.executeUpdate(sql);
		  			
		  			stmt = c.createStatement();
		  			sql = "DROP TABLE IF EXISTS Stat"; 
		  			stmt.executeUpdate(sql);
		  	    	  	     
		  	  
			  	} catch (SQLException e1) {
			  		
			  		e1.printStackTrace();
			  	}
		    
		   
		
		  }
		 
		  
		 
		 
		 
		 
		 
		 
		 public void createNeuralNetworkTable()
		  {

		    try {
		    //  Class.forName("org.sqlite.JDBC");
		    //  c = DriverManager.getConnection("jdbc:sqlite:"+pathToDB+"/QMatrix.db");
		     //System.out.println("Opened database successfully");
		      
		      
		     
		      

		      stmt = c.createStatement();
		      String sql = "CREATE TABLE NeuralNetwork " +
		              "(iteration	INT,     firmID   INT,	layerID		INT, 	nodeID 		INT,  activationFunction 	varchar32  ";
		      
		    sql = sql +", qValue REAL"; 
		    
		    sql = sql +", action REAL"; 
		    
		    sql = sql +", bias REAL";
			
			sql = sql +", inputNodes varchar200";
			
			sql = sql +", weights LONGVARCHAR";
			
			
			
		      sql = sql+")";
		      stmt.executeUpdate(sql);
		     
		   
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );

		    }
		   //System.out.println("Table Firm created successfully");
		  }
		
		 
		 
		 public void createFirmsTable()
		  {

		    try {
		    //  Class.forName("org.sqlite.JDBC");
		    //  c = DriverManager.getConnection("jdbc:sqlite:"+pathToDB+"/QMatrix.db");
		     //System.out.println("Opened database successfully");
		      
		      
		     
		      

		      stmt = c.createStatement();
		      String sql = "CREATE TABLE Firms " +
		              "(iteration	INT ";
		      
		      
		  	Field[] fields = Firms.class.getDeclaredFields();
			
			for(int i=0; i < fields.length; i++){

				if(fields[i].getType().getName().equals("int") ){
					sql = sql +","+fields[i].getName()+"	INT";
				}
				else if( fields[i].getType().getName().equals("double")){
					sql = sql +","+fields[i].getName()+"	REAL";
				}else if(fields[i].getType().getName().equals("boolean")){
					
					sql = sql +","+fields[i].getName()+"	INT"; 
					
				}
				

			}
		      sql = sql+")";
		      stmt.executeUpdate(sql);
		     
		   
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );

		    }
		   //System.out.println("Table Firm created successfully");
		  }
		 

		 
		 public void createStatTable()
		  {

		    try {
		    //  Class.forName("org.sqlite.JDBC");
		    //  c = DriverManager.getConnection("jdbc:sqlite:"+pathToDB+"/QMatrix.db");
		     //System.out.println("Opened database successfully");
		      
		      
		     
		      

		      stmt = c.createStatement();
		      String sql = "CREATE TABLE Stat " +
		              "(iteration	INT ";
		      
		      
		  	Field[] fields = Stat.class.getDeclaredFields();
			
			for(int i=0; i < fields.length; i++){

				if(fields[i].getType().getName().equals("int") ){
					sql = sql +","+fields[i].getName()+"	INT";
				}
				else if( fields[i].getType().getName().equals("double")){
					sql = sql +","+fields[i].getName()+"	REAL";
				}else if(fields[i].getType().getName().equals("boolean")){
					
					sql = sql +","+fields[i].getName()+"	INT"; 
					
				}
				

			}
		      sql = sql+")";
		      stmt.executeUpdate(sql);
		     
		   
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );

		    }
		   //System.out.println("Table Firm created successfully");
		  }
		 
		 
		 
		 
		 public void createParameterTable()
		  {

		    try {
		    	 // //      Class.forName("org.sqlite.JDBC");
		    	 //v     c = DriverManager.getConnection("jdbc:sqlite:"+pathToDB+"/QMatrix.db");
		     //System.out.println("Opened database successfully");
		      
		      
		     
		      

		      stmt = c.createStatement();
		      String sql = "CREATE TABLE Parameters ( ";
		      
		      
		  	Field[] fields = onlineLaborBuilder.class.getDeclaredFields();
		  	
		  	
		  	
		  
			
			for(int i=0; i < fields.length; i++){
				
				if(i==0) {
					
					if(fields[i].getType().getName().equals("int") ){
						sql = sql +fields[i].getName()+"	INT";
					}
					else if( fields[i].getType().getName().equals("double")){
						sql = sql +fields[i].getName()+"	REAL";
					}else if(fields[i].getType().getName().equals("boolean")){
						
						sql = sql +fields[i].getName()+"	INT"; 
						
					}
					
					
				}else {
					
					if(fields[i].getType().getName().equals("int") ){
						sql = sql +","+fields[i].getName()+"	INT";
					}
					else if( fields[i].getType().getName().equals("double")){
						sql = sql +","+fields[i].getName()+"	REAL";
					}else if(fields[i].getType().getName().equals("boolean")){
						
						sql = sql +","+fields[i].getName()+"	INT"; 
						
					}
					
					
				}

		
				

			}
		      sql = sql+")";
		      stmt.executeUpdate(sql);
		     
		   
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );

		    }
		   //System.out.println("Table Firm created successfully");
		  }
		 
		 
		 
		 public void insertNetworke(int iteration, int firmID, NeuralNetwork aNetwork)
		  {
			 
			 for(int i=0; i < aNetwork.inputLayer.nodeList.size();i++) {
				 
				// insertNode(iteration, firmID, aNetwork.inputLayer.nodeList.get(i));
				 
			 }
			 
			 
			 for(int i=0; i < aNetwork.hiddenLayers.size();i++) {
				 for(int j=0; j < aNetwork.hiddenLayers.get(i).nodeList.size();j++) {
				 
				 insertNode(iteration, firmID, aNetwork.hiddenLayers.get(i).nodeList.get(j));
				 }
			 }
			 
			 for(int i=0; i < aNetwork.outputLayer.nodeList.size();i++) {
				 
				 insertNode(iteration, firmID, aNetwork.outputLayer.nodeList.get(i));
				 
			 }
			 
			 
			 
		  }
		 
		
		 
		 

		  public void insertNode(int iteration, int firmID, NetworkNode aNode)
		  {
		    
	
		  
		    try {
		   
		      c.setAutoCommit(false);
		     //System.out.println("Opened database successfully");

		      stmt = c.createStatement();
		      
		      String sql = "INSERT INTO NeuralNetwork (iteration, firmID, layerID, nodeID, activationFunction, action, qValue,  bias, inputNodes, weights ";
		    
		      
		    String inputNodes = "'{";
		    String weights = "'{";
		     
		     for(int i=0; i < aNode.inputNodes.size(); i++) {
		    	 
		    	 inputNodes= inputNodes+aNode.inputNodes.get(i).id;
		    	 weights = weights + Precision.round(aNode.inputNodes.get(i).weight, 6);
		    	 
		    	 if(i!= aNode.inputNodes.size()-1) {
		    		 
		    		 inputNodes=inputNodes+",";
		    		 weights= weights+",";
		    	 }
		     }
		      
		     inputNodes=inputNodes+"}'";
    		 weights= weights+"}'";
   		 	
   		
		    
		  	
		  	sql = sql +") VALUES ("+iteration+","+firmID+","+aNode.layerIndex+","+aNode.nodeIndex+",'"+aNode.activationFunction+"',"+aNode.action+","+aNode.outputValue+","+aNode.bias+","+inputNodes+","+weights+")" ;
		  	
		  	
		  	
		     
		      stmt.executeUpdate(sql);

		      

		     
		      //c.commit();
		     
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		    }
		   //System.out.println("Records created successfully");
		  }
		  
		  
		  
		  

		  public void insertFirm(int iteration,   Firms aFirm  )
		  {
		    
		  
		    try {
		   
		      c.setAutoCommit(false);
		     //System.out.println("Opened database successfully");

		      stmt = c.createStatement();
		      
		      String sql = "INSERT INTO Firms (iteration ";
		      
		      Field[] fields = Firms.class.getDeclaredFields();
		  	
		  	for(int i=0; i < fields.length; i++){

		  		if(fields[i].getType().getName().equals("int") || fields[i].getType().getName().equals("double") || fields[i].getType().getName().equals("boolean")){
		  			sql = sql +","+fields[i].getName();
		      
		  		}
		  	}
		  	
		  	sql = sql +") VALUES ("+iteration;
		  	
		  	for(int i=0; i < fields.length; i++){

		  		if(fields[i].getType().getName().equals("int") || fields[i].getType().getName().equals("double")){
		  			sql = sql +","+fields[i].get(aFirm);
		      
		  		}else if(fields[i].getType().getName().equals("boolean")){
		  			
		  			//System.out.println(fields[i].get(aFirm));
		  			
		  			if(fields[i].get(aFirm).equals(true)){
		  				
		  				sql = sql +", 1";
		  				
		  			}else{
		  				
		  				sql = sql +", 0";
		  			}
		  			
		  			
		  		}
		  	}
		  	
			sql = sql +");";
		      
		      
		     
		      stmt.executeUpdate(sql);

		      

		     
		      //c.commit();
		     
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		    }
		   //System.out.println("Records created successfully");
		  }
		  
		  
		  

		  public void insertParameters(onlineLaborBuilder aBuilder )
		  {
		    
		  
		    try {
		   
		      c.setAutoCommit(false);
		     //System.out.println("Opened database successfully");

		      stmt = c.createStatement();
		      
		      String sql = "INSERT INTO Parameters (";
		      
		      Field[] fields = onlineLaborBuilder.class.getDeclaredFields();
		  	
		  	for(int i=0; i < fields.length; i++){

		  		if(fields[i].getType().getName().equals("int") || fields[i].getType().getName().equals("double") || fields[i].getType().getName().equals("boolean")){
		  			
		  			if(i==0)
		  				sql = sql +fields[i].getName();
		  			else
		  				sql = sql +","+fields[i].getName();
		      
		  		}
		  	}
		  	
		  	sql = sql +") VALUES (";
		  	
		  	for(int i=0; i < fields.length; i++){

		  		if(fields[i].getType().getName().equals("int") || fields[i].getType().getName().equals("double")){
		  			
		  			if(i==0)
		  				sql = sql +fields[i].get(aBuilder);
		  			else
		  				sql = sql +","+fields[i].get(aBuilder);
		      
		  		}else if(fields[i].getType().getName().equals("boolean")){
		  			
		  			//System.out.println(fields[i].get(aFirm));
		  			
		  			if(fields[i].get(aBuilder).equals(true)){
		  				
		  				if(i==0)
		  					sql = sql +" 1";
		  				else
		  					sql = sql +", 1";
		  			}else{
		  				
		  				if(i==0)
		  					sql = sql +" 0";
		  				else
		  					sql = sql +", 0";
		  			}
		  			
		  			
		  		}
		  	}
		  	
			sql = sql +");";
		      
		      
		     
		      stmt.executeUpdate(sql);

		      

		     
		      //c.commit();
		     
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		    }
		   //System.out.println("Records created successfully");
		  }
		  
		  
		  
		  
		  
		  
		  public void insertStat(int iteration,   Stat aStat  )
		  {
		    
		  
		    try {
		   
		      c.setAutoCommit(false);
		     //System.out.println("Opened database successfully");

		      stmt = c.createStatement();
		      
		      String sql = "INSERT INTO Stat (iteration ";
		      
		      Field[] fields = Stat.class.getDeclaredFields();
		  	
		  	for(int i=0; i < fields.length; i++){

		  		if(fields[i].getType().getName().equals("int") || fields[i].getType().getName().equals("double") || fields[i].getType().getName().equals("boolean")){
		  			sql = sql +","+fields[i].getName();
		      
		  		}
		  	}
		  	
		  	sql = sql +") VALUES ("+iteration;
		  	
		  	for(int i=0; i < fields.length; i++){

		  		if(fields[i].getType().getName().equals("int") || fields[i].getType().getName().equals("double")){
		  			sql = sql +","+fields[i].get(aStat);
		      
		  		}else if(fields[i].getType().getName().equals("boolean")){
		  			
		  			//System.out.println(fields[i].get(aFirm));
		  			
		  			if(fields[i].get(aStat).equals(true)){
		  				
		  				sql = sql +", 1";
		  				
		  			}else{
		  				
		  				sql = sql +", 0";
		  			}
		  			
		  			
		  		}
		  	}
		  	
			sql = sql +");";
		      
		      
		     
		      stmt.executeUpdate(sql);

		      

		     
		      //c.commit();
		     
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		    }
		   //System.out.println("Records created successfully");
		  }
		  
		  
		  
		  

		  void commit(){
			  
			  
		 try{
				c.commit();
				  
			  }catch ( Exception e ) {
			      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			      System.exit(0);
			    }
			  
			  
		  }
		  
		  
		  void atEnd(){
			  
			  
			  try{
				  
				  stmt.close();
				  c.close();
				  
			  }catch ( Exception e ) {
			      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			      System.exit(0);
			    }
			  
		  }
		  

	}

	
	