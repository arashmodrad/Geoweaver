package gw.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.log4j.Logger;

import gw.utils.BaseTool;
import gw.utils.SysDir;

/**
 * Class DataBaseOperation contains a seires of functions which operate on a mysql database.
 * @author Ziheng Sun
 * @date 2014.12.25
 */
public class DataBaseOperation {

	private static String driver, database_url, user, password;
	
	private final static Logger logger = Logger.getLogger(DataBaseOperation.class);
    
	private static Connection conn;
	
//	private static List<Connection> conn_pool = new ArrayList(); //maximum 5 connections
//	
//	static int maximum_connection = 10;
//	
//	static void addNewConnection(Connection newcon) {
//		
//		if(conn_pool.size()>=maximum_connection) {
//			
//			//close the oldest connections
//			
//			try {
//				
//				conn_pool.get(0).close();
//				
//				conn_pool.remove(0);
//				
//			} catch (SQLException e) {
//				
//				e.printStackTrace();
//			
//			} 
//			
//		}
//			
//		conn_pool.add(newcon);
//		
//	}
//	
//	static synchronized Connection getLiveConnection() {
//		
//		Connection con = null;
//		
//		try {
//			
//			for(int i=0;i<conn_pool.size();i++) {
//				
//				if(!conn_pool.get(i).isValid(1)) {
//					
//					try {
//					
//						conn_pool.get(i).close();
//						
//						conn_pool.remove(i);
//						
//						i=0;
//						
//					}catch(Exception e) {
//						
//						e.printStackTrace();
//						
//					}
//					
//					
//				}
//				
//			}
//			
//			for(int i=0;i<conn_pool.size(); i++) {
//
//				if(!conn_pool.get(i).isClosed() && !conn_pool.get(i).isReadOnly() ) {
//					
//					con = conn_pool.get(i);
//					
//					break;
//					
//				}
//				
//			}
//
//			if(con == null) {
//				
//				Class.forName(driver);
//				
//				con = DriverManager.getConnection(database_url, user, password);
//				
//				addNewConnection(con);
//				
//			}
//			
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//			
//		}
//		
//		return con;
//	}
	
	static Connection reconnect() {
		
		
			try {
				
				if(BaseTool.isNull(conn) || conn.isClosed()) {
				
					Class.forName(driver);
					
					conn = DriverManager.getConnection(database_url, user, password);
					
	//				Connection conn = DriverManager.getConnection(database_url, user, password);
	//				
	//				if(conn.isClosed()) {
	//					
	////					database_url = SysDir.database_docker_url;
	//					
	//					logger.error("the normal database is closed. Switch to docker version.");
	//					
	//				}else {
	//					
	//					addNewConnection(conn);
	//					
	//				}

				}
			} catch(Exception e) {   
				
				e.printStackTrace();
				
//				database_url = SysDir.database_docker_url;
				
				logger.warn("the normal database has error. Switch to docker version.");
				
			} 
			
			return conn;
			
		
	}
	
	static{
		
		driver = SysDir.database_driver;
		database_url = SysDir.database_url;
		user = SysDir.database_user;
		password = SysDir.database_password;
		
		//check if docker 
		reconnect();
		
//		try {
//			Properties p = new Properties();			
//			FileInputStream ferr = new FileInputStream(BaseTool.getClassPath() + File.separator +"database.properties");
//			p.load(ferr);
//			ferr.close();
//			driver = p.getProperty("driver");
//			database_url = p.getProperty("database_url");
//			user = p.getProperty("user");
//			password = p.getProperty("password");
//		}catch(IOException e){
//			e.printStackTrace();
//		}
	}
	/**
	 * Execute SQL in the current database.
	 * @param sql
	 * SQL sentence.
	 * eg.String sql = "select * from users";
	 * @return
	 * The query results.
	 */
	public synchronized static ResultSet query(String sql){		
                                            Statement statement = null;
		try {
			
			Connection q_conn = reconnect();
			
//			q_conn = DriverManager.getConnection(database_url, user, password);	
//			if(!conn.isClosed())	
//				logger.info("Succeeded connecting to the Database!");
			statement = q_conn.createStatement();
			ResultSet rs = null;
			rs = statement.executeQuery(sql);
			return rs;		
                        
		} catch(SQLException e) {   
			logger.error("The SQL query causes exception."+e.getLocalizedMessage());
            throw new RuntimeException("The SQL query causes exception."+e.getClass().getName()+":"+e.getLocalizedMessage());
			//e.printStackTrace();   
		} catch(Exception e) {   
			logger.error("Exception happens." + e.getLocalizedMessage());
			throw new RuntimeException("Exception happens." +e.getClass().getName()+":"+ e.getLocalizedMessage());
			//e.printStackTrace();   
		}  
	}
    /**
     * Executes the given SQL statement, which may return multiple results.
     * In some (uncommon) situations, a single SQL statement may return
     * multiple result sets and/or update counts.  Normally you can ignore
     * this unless you are (1) executing a stored procedure that you know may
     * return multiple results or (2) you are dynamically executing an
     * unknown SQL string.
     * <P>
     * The <code>execute</code> method executes an SQL statement and indicates the
     * form of the first result.  You must then use the methods
     * <code>getResultSet</code> or <code>getUpdateCount</code>
     * to retrieve the result, and <code>getMoreResults</code> to
     * move to any subsequent result(s).
     * <p>
     *<strong>Note:</strong>This method cannot be called on a
     * <code>PreparedStatement</code> or <code>CallableStatement</code>.
     * @param sql any SQL statement
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *         object; <code>false</code> if it is an update count or there are
     *         no results
     * @exception SQLException if a database access error occurs,
     * this method is called on a closed <code>Statement</code>,
     * the method is called on a
     * <code>PreparedStatement</code> or <code>CallableStatement</code>
     * @throws SQLTimeoutException when the driver has determined that the
     * timeout value that was specified by the {@code setQueryTimeout}
     * method has been exceeded and has at least attempted to cancel
     * the currently running {@code Statement}
     * @see #getResultSet
     * @see #getUpdateCount
     * @see #getMoreResults
     * 
     */
	 public synchronized static boolean execute(String sql){
		 boolean issuccess = false;
		 try {
			Connection e_conn = reconnect();
//			Class.forName(driver);		
//			e_conn = DriverManager.getConnection(database_url, user, password);		
//			if(!conn.isClosed())		
//			logger.info("Succeeded connecting to the Database!");	
			Statement statement = e_conn.createStatement();
			issuccess = statement.execute(sql);
			e_conn.close();
		} catch(SQLException e) {   
			logger.error("The SQL query causes exception."+e.getLocalizedMessage());
            throw new RuntimeException("The SQL query causes exception."+e.getLocalizedMessage());
			//e.printStackTrace();   
		} catch(Exception e) {   
			logger.error("Exception happens." + e.getLocalizedMessage());
            throw new RuntimeException("Exception happens." + e.getLocalizedMessage());
			//e.printStackTrace();   
		}  finally{
//			try {
//				if(!e_conn.isClosed()){
//					e_conn.close();
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//				throw new RuntimeException("Exception happens." + e.getLocalizedMessage());
//			}
		
		}
		
		 return issuccess;
		 
	 }
	 
	 public synchronized static boolean preexecute(String sql, String... variables){
		 
		 boolean issuccess = false;
		 
		 try {
			
//			Class.forName(driver);		
//			
//			e_conn = DriverManager.getConnection(database_url, user, password);
			
			Connection e_conn = reconnect();
			
			PreparedStatement statement= e_conn.prepareStatement   (sql );
			
			for(int i=1;i<=variables.length;i++) {
				
				statement.setString(i,variables[i-1]);
				
			}
			
			statement.executeUpdate();
			
			issuccess = true;
			
//			e_conn.close();
			
		} catch(SQLException e) {   
			logger.error("The SQL query causes exception."+e.getLocalizedMessage());
            throw new RuntimeException("The SQL query causes exception."+e.getLocalizedMessage());
			//e.printStackTrace();   
		} catch(Exception e) {   
//			e.printStackTrace();
			logger.error("Exception happens." + e.getLocalizedMessage());
            throw new RuntimeException("Exception happens." + e.getLocalizedMessage());
			//e.printStackTrace();   
		}  finally{
//			try {
//				if(!e_conn.isClosed()){
//					e_conn.close();
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//				throw new RuntimeException("Exception happens." + e.getLocalizedMessage());
//			}
		}
		return issuccess;
	 }
	 /**
	  * Update a record
	  * @param sql
	  * @return
	  */
	 public synchronized static int update(String sql){
		int rt = -1;
		try {
			Connection u_conn = reconnect();
//			Class.forName(driver);	
//			u_conn = DriverManager.getConnection(database_url, user, password);	
//			if(!conn.isClosed())	
//			logger.info("Succeeded connecting to the Database!");	
			Statement statement = u_conn.createStatement();
			rt = statement.executeUpdate(sql);
//			u_conn.close();
		} catch(SQLException e) {   
			logger.error("The SQL query causes exception."+e.getLocalizedMessage());
            throw new RuntimeException("The SQL query causes exception."+e.getLocalizedMessage());
			//e.printStackTrace();   
		} catch(Exception e) {   
			logger.error("Exception happens." + e.getLocalizedMessage());
           throw new RuntimeException("Exception happens." + e.getLocalizedMessage());
			//e.printStackTrace();   
		}  
		return rt;
	}
	 /**
	  * Get column from database
	  * @param name
	  * @param featureid
	  * @param column
	  * @param storeimgpath
	  * @return
	  */
	 public synchronized static boolean GetColumnFromDatabase(String name, String featureid, String column, String storeimgpath) {
	    	boolean suc = false;
	    	try{
	    		Connection u_conn = reconnect();
//	    		Class.forName(driver);
//                u_conn = DriverManager.getConnection(database_url, user, password);
                String sql = "select "+ column +" from igfds.sample where feature_id = '"+featureid + "' and name = '"+name+"'";
                PreparedStatement stmt = u_conn.prepareStatement(sql);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    File image = new File(storeimgpath);
                    FileOutputStream fos = new FileOutputStream(image);
                    byte[] buffer = new byte[1];
                    InputStream is = resultSet.getBinaryStream(1);
                    while (is.read(buffer) > 0) {
                              fos.write(buffer);
                    }
                    fos.close();
                }
//                u_conn.close();
                suc = true;
	    	}catch(Exception e){
	    		e.printStackTrace();
//	    		throw new RuntimeException("Fail to read image from database and write into a new file.");
	    	}
	    	return suc;
	    	
	        
	    }
	 
	/**
	 * 
	 * @param name
	 * @param featureid
	 * @param storeimgpath
	 * @return
	 */
	public synchronized static boolean GetImageFromDatabase(String name, String featureid, String storeimgpath) {
		return GetColumnFromDatabase(name, featureid, "image_block", storeimgpath);
	}
	 
	 /**
	  * Inject image into a sample database
	  * @param name
	  * Sample name
	  * @param fid
	  * Feature Id
	  * @param imgpath
	  * Image file path
	  * @return
	  * 
	  */
	 public synchronized static boolean injectImage2Sample(String name, String fid, String imgpath){
	    boolean ret = false;
	    FileInputStream fis = null;
	    PreparedStatement ps = null;
	    try {
//	      Class.forName(driver);
//	      u_conn = DriverManager.getConnection(database_url, user, password);
	      Connection u_conn = reconnect();
		  String INSERT_PICTURE = "update igfds.sample set image_block = ? where name = ? and feature_id = ?";

		  u_conn.setAutoCommit(false);
		  File file = new File(imgpath);
		  fis = new FileInputStream(file);
		  ps = u_conn.prepareStatement(INSERT_PICTURE);
		  ps.setBinaryStream(1, fis, (int) file.length());
		  ps.setString(2, name);
		  ps.setString(3, fid);
		  ps.executeUpdate();
		  u_conn.commit();
		  ret = true;
	    }catch (Exception e){
	      ret = false;
	      e.printStackTrace();
	    } finally {
		    try {
				ps.close();
			    fis.close();
			} catch (SQLException e) {
				e.printStackTrace();
				ret = false;
			} catch (IOException e) {
				e.printStackTrace();
				ret = false;
			}
	    }
	    return ret;
	 } 
	 /**
	  * Inject zip file to database
	  * @param name
	  * @param fid
	  * @param zipfilepath
	  * @return
	  */
	 public synchronized static boolean injectZipFile2Database(String name, String fid, String zipfilepath){
		 	boolean ret = false;
		    FileInputStream fis = null;
		    PreparedStatement ps = null;
		    try {
		      Connection u_conn = reconnect();
//		      Class.forName(driver);
//		      u_conn = DriverManager.getConnection(database_url, user, password);	
			  String INSERT_PICTURE = "update igfds.sample set geometry = ? where name = ? and feature_id = ?";
		
			  u_conn.setAutoCommit(false);
			  File file = new File(zipfilepath);
			  fis = new FileInputStream(file);
			  ps = u_conn.prepareStatement(INSERT_PICTURE);
			  ps.setBinaryStream(1, fis, (int) file.length());
			  ps.setString(2, name);
			  ps.setString(3, fid);
			  ps.executeUpdate();
			  u_conn.commit();
			  ret = true;
		    }catch (Exception e){
		      ret = false;
		      e.printStackTrace();
		    } finally {
			    try {
					ps.close();
				    fis.close();
				} catch (SQLException e) {
					e.printStackTrace();
					ret = false;
				} catch (IOException e) {
					e.printStackTrace();
					ret = false;
				}
		    }
		    return ret;
		 
	 } 
	 /**
	  * Close connections
	  */
     public static void closeConnection(){
//                try {
//                            if(!q_conn.isClosed())
//                                q_conn.close();
//                } catch (SQLException ex) {
//                            java.util.logging.Logger.getLogger(DataBaseOperation.class.getName()).log(Level.SEVERE, null, ex);
//                            throw new RuntimeException("Exception happens. We are unable to close the mysql connections" +ex.getClass().getName()+ ex.getLocalizedMessage());
//                }
     }
     /**
      * Get the number of records in a table
      * @param tablename
      * @return record_number
      */
     public static int getRecordNumberOfTable(String tablename){
                int rn = -1;
                try {
                    String querysql = "select count(*) from "+tablename;
                    ResultSet rs = DataBaseOperation.query(querysql);
                    if(rs.next()){
                        rn = rs.getInt("count(*)");
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(DataBaseOperation.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("ERR: Fail to get the number of records. " +ex.getClass().getName()+ ex.getLocalizedMessage());
                }
                return rn;
     }
     /**
      * Get the number of previous records
      * @param tablename
      * @param startingrecordid
      * @return 
      */
     public static int getPreviousRecordNumber(String tablename, String startingrecordid){
                int rn = -1;
                try {
                    String querysql = "select count(*) from "+tablename + " where id < '"+ startingrecordid + "';";
                    ResultSet rs = DataBaseOperation.query(querysql);
                    if(rs.next()){
                        rn = rs.getInt("count(*)");
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(DataBaseOperation.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("ERR: Fail to get the number of previous records. " +ex.getClass().getName()+ ex.getLocalizedMessage());
                }
                return rn;
     }
     
     /**
      * Check order status
      * @param on
      * @return
      */
 	public static String checkOrderStatus(String on) {
 		StringBuffer sql = new StringBuffer();
 		sql.append("select * from cyberconnector.orders where orderid = '").append(on).append("';");
 		ResultSet rs = query(sql.toString());
 		String status = null;
 		String message = null;
 		String ordertime = null;
 		String updatetime = null;
 		String begintime = null;
 		String endtime = null;
 		try {
			if(rs.next()){
				status = rs.getString("status");
				message = rs.getString("message");
				ordertime = rs.getString("ordertime");
				updatetime = rs.getString("updatetime");
				begintime = rs.getString("begintime");
				endtime = rs.getString("endtime");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Fail to check order status."+on+" : " +e.getLocalizedMessage());
		}finally{
			DataBaseOperation.closeConnection();
		}
 		return "{\"status\": \""
 			+ status
 			+ "\", \"message\": \""
 			+ BaseTool.escape(message)
 			+ "\", \"ordertime\": \""
 			+ BaseTool.escape(ordertime)
 			+ "\", \"updatetime\": \""
 			+ BaseTool.escape(updatetime)
 			+ "\", \"begintime\": \""
 			+ BaseTool.escape(begintime)
 			+ "\", \"endtime\": \""
 			+ BaseTool.escape(endtime)
 			+ "\"}";
 	}
     /**
      * Main function
      * @param args
      */
     public static void main(String[] args){
    	 DataBaseOperation.GetColumnFromDatabase("3ab34f18.fusion.sv.esp.vector.zip", "548", "image_block", "/home/zsun/test_get_tif1.tif");
     }
}
