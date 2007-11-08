/*******************************************************
 * 
 *  @author spowell
 *  DatabaseLogger.java
 *  Aug 23, 2006
 *  $Id: DatabaseLogger.java,v 1.7 2006/12/31 16:59:10 shaneapowell Exp $
 *
 *
Copyright (C) 2006  Shane Powell

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 ******************************************************/
package net.sourceforge.JDash.logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import net.sourceforge.JDash.Setup;
import net.sourceforge.JDash.ecu.param.ECUParameter;
import net.sourceforge.JDash.ecu.param.Parameter;
import net.sourceforge.JDash.ecu.param.ParameterRegistry;


/*******************************************************
 * This class is the DAO for all database logging activity
 * Whant to read and/or write parameter values to a database,
 * then use this DAO.  Note that we only work with HSQLDB.
 ******************************************************/
public class DatabaseLogger extends DataLogger
{

	/* By default allow ologging, But, it can be overridden */
	private boolean disableLoggingOverride_ = true;
	
	
	private static final String DB_NAME = "eculogdb";

	private static final String TABLE_LOG = "eculog";
	private static final String COL_LOG_ID = "log_id";
	private static final String COL_TIMESTAMP = "log_timestamp";
	private static final String COL_PARAM_NAME = "param_name";
	private static final String COL_PARAM_VALUE = "param_value";
	
	private List<Parameter> parameterList_ = null;
	
	private Connection conn_ = null;

	private boolean pause_ = false;
	
	private PreparedStatement insertStatement_ = null;
	
	private int sequencialLogErrors_ = 0;
	
	/* When playback in underway, this is where the results are stored */
	private ResultSet playbackResultSet_ = null;
	
	private String logIdSuffix_ = "";
	
	private String currentLogId_ = null;
	
	/*******************************************************
	 * Create a new instance of a database logger DAO. This
	 * constructor will open the existing database, or create
	 * a new one if it doesn't yet exist.
	 *****************************************************/
	public DatabaseLogger(String logIdSuffix) throws Exception
	{
		this.logIdSuffix_ = logIdSuffix;
		this.parameterList_ = new ArrayList<Parameter>();
	}
	
	
	/*******************************************************
	 * Override
	 * @see java.lang.Object#finalize()
	 *******************************************************/
	@Override
	protected void finalize() throws Throwable
	{
		if (this.insertStatement_ != null)
		{
			try { this.insertStatement_.close(); } catch (Exception e) {}
		}
		
		
		if (this.playbackResultSet_ != null)
		{
			try { this.playbackResultSet_.close(); } catch (Exception e) {}
		}
		
		if (this.conn_ != null)
		{
			/* close the connection */
			this.conn_.commit();
			this.conn_.close();
		}
		
		this.insertStatement_ = null;
		this.sequencialLogErrors_ = 0;
		this.conn_ = null;
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#enableOverride(boolean)
	 *******************************************************/
	@Override
	public void disableOverride(boolean disableLoggingOverride) throws Exception
	{
		this.disableLoggingOverride_ = disableLoggingOverride;
		
	}
	
	/*******************************************************
	 * Initialize the data table schema. If the tables don't
	 * yet exist, then create them now.
	 *******************************************************/
	private void initSchema() throws Exception
	{
		
		Statement stmt = null;
		ResultSet rs = null;
		
		/* Return if the connection is already made */
		if ((conn_ != null) && (conn_.isClosed() == false))
		{
			return;
		}
		
		try
		{
			
			File dbDir = new File(Setup.SETUP_CONFIG_LOGDB_DIR);
			
			if (dbDir.isDirectory() == false)
			{
				throw new Exception("Cannot initialize database logger, the base directory [" + dbDir.getAbsolutePath() + "] is not a valid directory");
			}
			
			/* Connect to the database */
			Properties conProps = new Properties();
			conProps.put("user", "sa");
			conProps.put("password", "");
			
			Class.forName("org.h2.Driver").newInstance();
			conProps.put("storage", "binary");
			conProps.put("ifexists", "false");
			conn_ = DriverManager.getConnection("jdbc:h2:file:" + dbDir.getAbsolutePath() + "/" + DB_NAME, conProps);
			
			conn_.setAutoCommit(true);
				
			
			/* Look for our tables */
			//String tableSelect = "select table_name from information_schema.system_tables where upper(table_schem) = upper('PUBLIC')";
			String tableSelect = "select table_name from information_schema.tables where upper(table_schema) = upper('PUBLIC')";
			stmt = this.conn_.createStatement();
			rs = stmt.executeQuery(tableSelect);
			
			boolean logTableFound = false;
			
			while(rs.next())
			{
				if (TABLE_LOG.equalsIgnoreCase(rs.getString(1)) == true)
				{
					logTableFound = true;
				}
			}
	
			
			/* create any tables that are missing */
			if (logTableFound == false)
			{
				String createTable = "create table " + TABLE_LOG + "(" + 
										COL_LOG_ID + " varchar(128) not null,\n" +
										COL_TIMESTAMP + " bigint not null,\n" +
										COL_PARAM_NAME + " varchar(128) not null,\n" +
										COL_PARAM_VALUE + " double not null );" +
										"create index " + COL_LOG_ID + "_ndx on " + TABLE_LOG + " ( " + COL_LOG_ID + " ); \n";
				
				stmt.execute(createTable);
				this.conn_.commit();
			}
		
		}
		finally
		{
			if (rs != null)
			{
				try { rs.close(); } catch (Exception e) {}
			}
			
			if (stmt != null)
			{
				try { stmt.close(); } catch (Exception e) {}
			}
			
		}
		
	}

	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#setParameters(java.util.List)
	 *******************************************************/
	@Override
	public void setParameters(List<Parameter> parameters) throws Exception
	{
		if (this.parameterList_ != null)
		{
			enable(false);
		}
		
		/* Copy the ECU Parameters, into our internal array list of parameters */
		for (Parameter p : parameters)
		{
			addParameter(p);
		}

	}
	
	
	/*******************************************************
	 * Add a parameter to the log.  We only track ECUParameters and the TIME parameter, any
	 * attempt to add a parameter that isn't supported will simply be ignored.
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#addParameter(net.sourceforge.JDash.ecu.param.Parameter)
	 *******************************************************/
	@Override
	public void addParameter(Parameter param) throws Exception
	{
		/* Force the adding of the TIME parameter */
		if (param.getName().equals(ParameterRegistry.TIME_PARAM) == true)
		{
			this.parameterList_.add(param);
			return;
		}
		
		
		/* we only track ECU parameters, and the time parameter */
		if (param instanceof ECUParameter == false)
		{
			return;
		}
		else
		{		
			this.parameterList_.add(param);
		}
		
	}
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#isEnabled()
	 *******************************************************/
	@Override
	public boolean isEnabled() throws Exception
	{
		
		if (this.currentLogId_ == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#enable(boolean)
	 *******************************************************/
	public void enable(boolean enable) throws Exception
	{
		
		/* The override is disabled */
		if (this.disableLoggingOverride_ == true)
		{
			return;
		}
		
		/* do NOT enable the logger if playback is active. Why? Uh.. why would 
		 * you log what you are already playing back! */
		if (this.playbackResultSet_ != null)
		{
			return; 
		}

		
		try
		{
			
			
			/* Enable or disable */
			if (enable == true)
			{
				/* Open the database */
				Calendar now = GregorianCalendar.getInstance();
				this.currentLogId_ = String.format("%tF %tI:%tM:%tS %tp %tZ - %s", now, now, now, now, now, now, this.logIdSuffix_);
				initSchema();
				
				/* Observe the parameters */
				for (Parameter p : this.parameterList_)
				{
					p.addObserver(this);
				}
			}
			else
			{
				/* Stop observing the parameters */
				for (Parameter p : this.parameterList_)
				{
					p.deleteObserver(this);
				}
				
				/* Close the database */
				this.currentLogId_ = null;
				finalize(); /* This will close the database */
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			throw new Exception("There was an unexpected error connecting to logger database.");
		}

	}
	
	
	/*******************************************************
	 * We've received an update even from atleast one 
	 * of the monitors.
	 * Override
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 *******************************************************/
	public void update(Observable observable, Object obj)
	{
		try
		{
			if (this.pause_ == true)
			{
				return;
			}
			
			/* We only care about ECU Parameters */
			Parameter param = (Parameter)observable;
			if (param instanceof ECUParameter)
			{
				addParamToDatabase(param);
			}
			
			
			/* We also care about the special Time parameter */
			if (param.getName().equals(ParameterRegistry.TIME_PARAM) == true)
			{
				addParamToDatabase(param);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
			
	}
	
	
	/*******************************************************
	 * @param param
	 ******************************************************/
	private void addParamToDatabase(Parameter param) throws RuntimeException
	{
		try
		{
			
			/* Initialize the insert statement */
			if (this.insertStatement_ == null)
			{
				String stmt = "insert into " + TABLE_LOG + " (" + COL_LOG_ID + "," + COL_TIMESTAMP + "," + COL_PARAM_NAME + "," + COL_PARAM_VALUE + ") values (?,?,?,?)";
				this.insertStatement_ = this.conn_.prepareStatement(stmt);
			}

			/* Insert the param vales */
			this.insertStatement_.setString(1, this.currentLogId_);
			this.insertStatement_.setLong(2, System.currentTimeMillis());
			this.insertStatement_.setString(3, param.getName());
			this.insertStatement_.setDouble(4, param.getResult());
			insertStatement_.execute();
			this.conn_.commit();
			
			/* If we get here, then reset the sequencial error count */
			this.sequencialLogErrors_ = 0;
			
		}
		catch(Exception e)
		{
			/* Increment the error counter */
			this.sequencialLogErrors_++;
			
			e.printStackTrace();
			System.err.println("Error inserting value into database.  Skipping. But if this keeps up, we'll have to quit");
			
			/* Too many log errors in a row? */
			if (this.sequencialLogErrors_ >= 5)
			{
				throw new RuntimeException("Too many database logging errors in a row, something serious is wrong");
			}
		}
	}
	
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#getLogCount()
	 *******************************************************/
	@Override
	public int getLogCount() throws Exception
	{
		initSchema();

		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			/* Load the list of parameters */
			String query = "select count(distinct " + COL_LOG_ID + ") " + " from " + TABLE_LOG;
			stmt = this.conn_.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next() == false)
			{
				return 0;
			}
			
			return rs.getInt(1);
		}
		finally
		{
			try {stmt.close();} catch(Exception e) {}
			try {rs.close();} catch(Exception e) {}
		}

	}
	
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#getLogName(int)
	 *******************************************************/
	@Override
	public String getLogName(int logIndex) throws Exception
	{
		
		initSchema();

		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			/* Load the list of parameters */
			String query = "select " + COL_LOG_ID + " from (select distinct " + COL_LOG_ID + " from " + TABLE_LOG + ") order by upper(" + COL_LOG_ID + ") desc ";
			stmt = this.conn_.createStatement();
			rs = stmt.executeQuery(query);
			
			/* Go to the requested index */
			while ((logIndex-- >= 0) && (rs.next()));
			
			/* Return the log at the given index */
			return rs.getString(1);
		}
		finally
		{
			try {stmt.close();} catch(Exception e) {}
			try {rs.close();} catch(Exception e) {}
		}
	}
	
	
	
	
	/*****************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#setLogName(int, java.lang.String)
	 ******************************************************/
	public void setLogName(int logIndex, String newName) throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		initSchema();
		
		/* Check the index reference */
		int logCount = getLogCount();
		if ((logIndex < 0) || (logIndex >= logCount))
		{
			throw new Exception("Unabele to rename log, the index [" + logIndex + "] is out of range [0-" + logCount + "]"); 
		}
		
		/* Get the current name */
		String logName = getLogName(logIndex);
		
		try
		{
			
			String existsStatement = "select count(*) from " + TABLE_LOG + " where " + COL_LOG_ID + " = '" + newName + "'";
			String updateStatement = "update " + TABLE_LOG + " set " + COL_LOG_ID + " = '" + newName + "' where " + COL_LOG_ID + " = '" + logName + "'";
			
			/* Check if it exists already */
			stmt = this.conn_.createStatement();
			rs = stmt.executeQuery(existsStatement);
			rs.next();
			if (rs.getInt(1) != 0)
			{
				throw new  Exception("A log by that name already exists");
			}

			
			/* run the update */
			stmt.executeUpdate(updateStatement);
			
			
		}
		finally
		{
			try {stmt.close();} catch(Exception e) {}
			try {rs.close();} catch(Exception e) {}
		}
		
	}
	
	
	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#prepareForPlayback()
	 *******************************************************/
	public void prepareForPlayback(int logIndex) throws Exception
	{
		initSchema();
		
			
			/* If the result set exists, then close it and start a new one */
			if (this.playbackResultSet_ != null)
			{
				synchronized(this.playbackResultSet_)
				{
					try { this.playbackResultSet_.close(); } catch (Exception e) {}
					this.playbackResultSet_ = null;
				}
			}
			
			/* Get the desired log name */
			String logId = getLogName(logIndex);
			
			/* Load the list of parameters */
			String query = "select " + COL_TIMESTAMP + "," + 
									COL_PARAM_NAME + "," +
									COL_PARAM_VALUE + " " +
									"from " + TABLE_LOG + 
									" where " + COL_LOG_ID + " = '" + logId + 
									"' order by " + COL_TIMESTAMP + " asc ";
			Statement stmt = this.conn_.createStatement();
			this.playbackResultSet_ = stmt.executeQuery(query);
			
		
		
	}
	
	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#getNextPlayback()
	 *******************************************************/
	public LogParameter getNext() throws Exception
	{
		
		if (this.playbackResultSet_ == null)
		{
			return null;
		}
		
		if (this.playbackResultSet_.isAfterLast())
		{
			return null;
		}
		
		if (this.playbackResultSet_.next() == false)
		{
			return null;
		}

		LogParameter param = null;
		synchronized (this.playbackResultSet_)
		{
			param = new LogParameter(this.playbackResultSet_.getString(COL_PARAM_NAME), 
													this.playbackResultSet_.getDouble(COL_PARAM_VALUE), 
													this.playbackResultSet_.getLong(COL_TIMESTAMP));
		}
		
		return param;
		
	}
	

	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#getPrevious()
	 *******************************************************/
	@Override
	public LogParameter getPrevious() throws Exception
	{
		if (this.playbackResultSet_.isBeforeFirst())
		{
			return null;
		}
		
		if (this.playbackResultSet_.previous() == false)
		{
			return null;
		}

		LogParameter param = null;
		
		synchronized(this.playbackResultSet_)
		{
			param = new LogParameter(this.playbackResultSet_.getString(COL_PARAM_NAME), 
									this.playbackResultSet_.getDouble(COL_PARAM_VALUE), 
									this.playbackResultSet_.getLong(COL_TIMESTAMP));
		}
		
		return param;
	}
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#deleteLog(int)
	 *******************************************************/
	@Override
	public void deleteLog(int logIndex) throws Exception
	{
		initSchema();
		enable(false);
		
		/* Get the desired log name */
		String logId = getLogName(logIndex);
		
		/* Load the list of parameters */
		String deleteStatement = "delete from " + TABLE_LOG + " where " + COL_LOG_ID + " = '" + logId + "'";
		Statement stmt = this.conn_.createStatement();
		stmt.executeUpdate(deleteStatement);
		this.conn_.commit();
		
		
		stmt.close();
			
	}

	
	/******************************************************
	 * Override
	 * @see net.sourceforge.JDash.logger.DataLogger#purge()
	 *******************************************************/
	public void deleteAll() throws Exception
	{
		
		/* Look for our tables */
		String purgeStmt = "delete from " + TABLE_LOG;
		Statement stmt = this.conn_.createStatement();
		stmt.execute(purgeStmt);
		this.conn_.commit();
		stmt.close();
		
	}

	
}
