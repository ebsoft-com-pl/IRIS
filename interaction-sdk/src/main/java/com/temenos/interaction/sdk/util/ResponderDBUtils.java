package com.temenos.interaction.sdk.util;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to read SQL insert statement froma file and 
 * inject them into a database. 
 */
public class ResponderDBUtils {
	private final static Logger LOGGER = LoggerFactory.getLogger(ResponderDBUtils.class);

	public static String fillDatabase(DataSource dataSource) {
		Connection conn = null;
		String line = "";
		try {
			LOGGER.debug("Attempting to connect to database");
			conn = dataSource.getConnection();
			Statement statement = conn.createStatement();
			
			LOGGER.debug("Loading SQL INSERTs file");
			InputStream xml = ResponderDBUtils.class.getResourceAsStream("/META-INF/responder_insert.sql");
			if (xml == null){
				return "ERROR: DML file not found [/META-INF/responder_insert.sql].";
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(xml, "UTF-8"));

			LOGGER.debug("Reading SQL INSERTs file");
			statement.execute("SET REFERENTIAL_INTEGRITY FALSE;");		//The order of INSERTs may not respect foreign key constraints
			int count = 0;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					line = line.replace("`", "");
					line = line.replace(");", ")");
					line = line.replace("'0x", "'");

					if (line.length() > 5) {
						LOGGER.debug("Inserting record: " + line);
						statement.executeUpdate(line);
						count++;
					}
				}
			}
			statement.execute("SET REFERENTIAL_INTEGRITY TRUE;");
			
			br.close();
			statement.close();
			LOGGER.info(count + " rows have been inserted into the database.");

		} catch (Exception ex) {
			LOGGER.error("Failed to insert SQL statements.", ex);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					LOGGER.error("Failed to close connection", ex);
				}
			}
		}
		return "OK";
	}

	public static void writeStringToFile(String fileName, String contents) {
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(fileName),
					"utf-8");
			out.write(contents);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LOGGER.error("Failed to write file.", e);					
				}
			}
		}

	}

	public static String readFileToString(String fileName) {
		return readFileToString(fileName, Charset.defaultCharset().name());
	}

	public static String readFileToString(String fileName, String charsetName) {
		StringBuilder strBuilder = new StringBuilder();
		
		try {
			InputStream buf = ResponderDBUtils.class
					.getResourceAsStream(fileName);

			BufferedReader in = new BufferedReader(new InputStreamReader(buf,
					charsetName));

			String str;

			try {
				while ((str = in.readLine()) != null) {
					strBuilder.append(str);
				}
				in.close();

			} catch (IOException ex) {
				LOGGER.error("There was an error", ex);
			}

		} catch (Exception ex) {
			LOGGER.error("There was an error", ex);
		}

		return strBuilder.toString();
	}

}
