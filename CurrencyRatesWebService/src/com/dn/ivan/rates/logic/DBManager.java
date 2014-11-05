package com.dn.ivan.rates.logic;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBManager {

	public static Connection createConnection() throws IOException, ClassNotFoundException, SQLException {

		Connection connection = null;;
		
		try {
			
			Properties prop = new Properties();
			prop.load(new FileInputStream(System.getProperty("user.home") + "/mydb.cfg"));

			String host = prop.getProperty("host").toString();
			String username = prop.getProperty("username").toString();
			String password = prop.getProperty("password").toString();
			String driver = prop.getProperty("driver").toString();

			Class.forName(driver);
			
			connection = DriverManager.getConnection(host, username, password);
		}
		catch (Exception e) {
			e.printStackTrace();			
		}	

		return connection;
	}
}
