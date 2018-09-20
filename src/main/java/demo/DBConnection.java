package demo;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection; 
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException; 
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DBConnection {
	private static final Logger LOGGER = LoggerFactory.getLogger(DBConnection.class);

	   // JDBC driver name and database URL 
	   static final String JDBC_DRIVER = "org.h2.Driver";   
	   static final String DB_URL = "jdbc:h2:~/test";  
	   
	   //  Database credentials 
	   static final String USER = "sa"; 
	   static final String PASS = ""; 
	  
	   public static void main(String[] args) { 
	      Connection conn = null; 
	      Statement stmt = null; 
	      try { 
	         // STEP 1: Register JDBC driver 
	         Class.forName(JDBC_DRIVER); 
	             
	         Properties prop = new Properties();
	         try {
	             //load a properties file from class path, inside static method
	             prop.load(DBConnection.class.getClassLoader().getResourceAsStream("application.properties"));

	             //get the property value and print it out
	             System.out.println(prop.getProperty("spring.h2.console.enabled"));
	             System.out.println(prop.getProperty("spring.datasource.url"));
//	             System.out.println(prop.getProperty("dbpassword"));

	         } 
	         catch (IOException ex) {
	             ex.printStackTrace();
	         }
	         //STEP 2: Open a connection 
	         System.out.println("Connecting to database..."); 
	         conn = DriverManager.getConnection(DB_URL,USER,PASS);  
	         
	         //STEP 3: Execute a query 
//	         System.out.println("Creating table in given database..."); 
//	         stmt = conn.createStatement(); 
//	         String sql =  "CREATE TABLE   REGISTRATION " + 
//	            "(id INTEGER not NULL, " + 
//	            " first VARCHAR(255), " +  
//	            " last VARCHAR(255), " +  
//	            " age INTEGER, " +  
//	            " PRIMARY KEY ( id ))";  
//	         stmt.executeUpdate(sql);
//	         System.out.println("Created table in given database..."); 
	         
	         
	      // STEP 3: Execute a query 
	         stmt = conn.createStatement();  
//	         String sql = "INSERT INTO Registration " + "VALUES (100, 'Zara', 'Ali', 18)"; 
//	         
//	         stmt.executeUpdate(sql); 
//	         sql = "INSERT INTO Registration " + "VALUES (101, 'Mahnaz', 'Fatma', 25)";  
//	         
//	         stmt.executeUpdate(sql); 
//	         sql = "INSERT INTO Registration " + "VALUES (102, 'Zaid', 'Khan', 30)"; 
//	         
//	         stmt.executeUpdate(sql); 
//	         sql = "INSERT INTO Registration " + "VALUES(103, 'Sumit', 'Mittal', 28)"; 
//	         
//	         stmt.executeUpdate(sql); 
//	         System.out.println("Inserted records into the table..."); 
	         
	         String sql = "SELECT id, first, last, age FROM Registration"; 
	         ResultSet rs = stmt.executeQuery(sql); 
	         LOGGER.info("Start waiting...");
	         Thread.sleep(1000 * 60 * 2);
	         // STEP 4: Extract data from result set 
	         while(rs.next()) { 
	            // Retrieve by column name 
	            int id  = rs.getInt("id"); 
	            int age = rs.getInt("age"); 
	            String first = rs.getString("first"); 
	            String last = rs.getString("last");  
	            
	            // Display values 
	            System.out.print("ID: " + id); 
	            System.out.print(", Age: " + age); 
	            System.out.print(", First: " + first); 
	            System.out.println(", Last: " + last); 
	         } 
	         // STEP 4: Clean-up environment 
	         stmt.close(); 
	         conn.close(); 
	      } catch(SQLException se) { 
	         //Handle errors for JDBC 
	         se.printStackTrace(); 
	      } catch(Exception e) { 
	         //Handle errors for Class.forName 
	         e.printStackTrace(); 
	      } finally { 
	         //finally block used to close resources 
	         try{ 
	            if(stmt!=null) stmt.close(); 
	         } catch(SQLException se2) { 
	         } // nothing we can do 
	         try { 
	            if(conn!=null) conn.close(); 
	         } catch(SQLException se){ 
	            se.printStackTrace(); 
	         } //end finally try 
	      } //end try 
	      System.out.println("Goodbye!");
	   } 
}
