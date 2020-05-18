import java.io.RandomAccessFile;
import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Safal Tyagi
 * @version 1.0
 * @apiNote This class is main entry point for the DavisBase
 */
public class DavisBase {

	static Scanner scanner = new Scanner(System.in).useDelimiter(";");

	public static void main(String[] args) {
		DavisBasePrompt.init();
		/* Display the welcome screen */
		DavisBasePrompt.splashScreen();

		/* Variable to collect user input from the prompt */
		String query = "";

		while (!query.equals("exit")) {
			DavisBasePrompt.printPrompt();
			/* toLowerCase() renders command case insensitive */
			query = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
			DavisBasePrompt.parseQuery(query);
		}
		System.out.println("Exiting...");
	}// end main method
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * @author Safal Tyagi
 * @version 1.0
 * @apiNote This class manages the user prompt related functionalities like show
 *          help, process query strings and call respective methods from
 *          DavisBaseEngine class
 */
class DavisBasePrompt {

	/* This can be changed to whatever you like */
	static String prompt = "davisql> ";
	static String version = "v1.0a";
	static String copyright = "©2020 Safal Tyagi";
	static boolean isExit = false;

	public static void printPrompt() {
		System.out.print(prompt);
	}

	static Scanner scanner = new Scanner(System.in).useDelimiter(";");

	/**
	 * Display the splash screen
	 */
	public static void splashScreen() {
		System.out.println(line("-", 80));
		System.out.println("Welcome to DavisBase"); // Display the string.
		System.out.println("DavisBase Version " + getVersion());
		System.out.println(getCopyright());
		System.out.println("Type \"help;\" to display supported commands.");
		System.out.println(line("-", 80));
	}

	/**
	 * @param s   The String to be repeated
	 * @param num The number of time to repeat String s.
	 * @return String A String object, which is the String s appended to itself num
	 *         times.
	 */
	public static String line(String s, int num) {
		String a = "";
		for (int i = 0; i < num; i++) {
			a += s;
		}
		return a;
	}

	/**
	 * Help: Display supported commands
	 */
	private static void help() {
		System.out.println(line("*", 80));
		System.out.println("• SHOW tables – Displays a list of all tables in DavisBase.");
		System.out.println("	usage and example: SHOW tables;");

		System.out.println("• CREATE TABLE – Creates a new table schema, i.e. a new empty table.");
		System.out.println(
				" 	usage: CREATE TABLE table_name ( column_name1 INT PRIMARY KEY, column_name2 data_type2 [NOT NULL], column_name3 data_type3 [NOT NULL],...); ");
		System.out.println(
				" 		example: CREATE TABLE contacts ( ssn INT PRIMARY KEY, full_name TEXT [NOT NULL], phone_number TEXT); ");

		System.out.println("• DROP TABLE – Remove a table schema, and all of its contained data.");
		System.out.println("	usage: DROP TABLE table_name");
		System.out.println("		example: DROP TABLE contacts");

		System.out.println("• INSERT INTO TABLE – Inserts a single record into a table.");
		System.out.println("	usage: INSERT INTO table_name VALUES (value1,value2,value3,…);");
		System.out.println("		example: INSERT INTO contacts VALUES (1231231234, Safal Tyagi, 469-925-5256);");

		System.out.println("• SELECT ... FROM ... WHERE ...");
		System.out.println("	usage: SELECT * FROM table_name WHERE column_name operator value;");
		System.out.println("		example: SELECT * FROM contacts;");
		System.out.println("		example: SELECT * FROM contacts WHERE ssn=1231231234;");
		System.out.println("		example: SELECT * FROM davisbase_tables;");
		System.out.println("		example: SELECT * FROM davisbase_columns;");

		System.out.println("• EXIT – Cleanly exits the program and saves all table information in non-volatile files");
		System.out.println("	usage and example: EXIT;");
		System.out.println(line("*", 80));
	}

	/**
	 * Get Version
	 */
	public static String getVersion() {
		return version;
	}

	/**
	 * Get Copyright
	 */
	public static String getCopyright() {
		return copyright;
	}

	/**
	 * Get version and copyright info
	 */
	public static void displayVersion() {
		System.out.println("DavisBase Version " + getVersion());
		System.out.println(getCopyright());
	}

	/**
	 * Check if the teble exists
	 * 
	 * @param table table name to check
	 * @return true if table exists
	 */
	public static boolean tableExist(String table) {
		boolean exists = false;
		table = table + ".tbl";
		try {
			File dataDir = new File("data");
			String[] oldTableFiles;
			oldTableFiles = dataDir.list();
			for (int i = 0; i < oldTableFiles.length; i++) {
				if (oldTableFiles[i].equals(table))
					return true;
			}
		} catch (SecurityException se) {
			System.out.println("Unable to create data container directory");
			System.out.println(se);
		}

		return exists;
	}

	/**
	 * initializes the initial database file system in data directory with tables
	 * and column meta-data
	 * 
	 * @param
	 * @return
	 */
	public static void init() {
		try {
			File dataDir = new File("data");
			if (dataDir.mkdir()) {
				DavisBaseEngine.initSystemSchemaTables();
			} else {
				String meta1 = "davisbase_columns.tbl";
				String meta2 = "davisbase_tables.tbl";
				String[] oldTableFiles = dataDir.list();
				boolean check = false;
				for (int i = 0; i < oldTableFiles.length; i++) {
					if (oldTableFiles[i].equals(meta1))
						check = true;
				}
				if (!check) {
					DavisBaseEngine.initSystemSchemaTables();
				}
				check = false;
				for (int i = 0; i < oldTableFiles.length; i++) {
					if (oldTableFiles[i].equals(meta2))
						check = true;
				}
				if (!check) {
					DavisBaseEngine.initSystemSchemaTables();
				}
			}
		} catch (SecurityException e) {
			System.out.println(e);
		}

	}

	/**
	 * parses the equation part of query
	 * 
	 * @param equation full eqation like xyz >= 10
	 * @return array of strings with 3 components
	 */
	public static String[] parserEquation(String equation) {
		String parsed[] = new String[3];
		if (equation.contains("="))
			parsed = splitEquation(equation, "=");

		if (equation.contains(">"))
			parsed = splitEquation(equation, ">");

		if (equation.contains("<"))
			parsed = splitEquation(equation, "<");

		if (equation.contains(">="))
			parsed = splitEquation(equation, ">=");

		if (equation.contains("<="))
			parsed = splitEquation(equation, "<=");

		if (equation.contains("<>"))
			parsed = splitEquation(equation, "<>");

		return parsed;
	}

	/**
	 * parses the equation part of query
	 * 
	 * @param equation full eqation like xyz >= 10
	 * @param symbol   symbol along which to split eg >=
	 * @return array of strings with 3 components
	 */
	public static String[] splitEquation(String equation, String symbol) {
		String parts[] = new String[3];
		String temp[] = new String[2];

		temp = equation.split(symbol);
		parts[0] = temp[0].trim();
		parts[1] = symbol;
		parts[2] = temp[1].trim();

		return parts;
	}

	/**
	 * ` parses the full query
	 * 
	 * @param query full query like select * from table_name where xyz>=10
	 * @return
	 */
	public static void parseQuery(String query) {

		String[] queryTokens = query.split(" ");

		String table = "";
		String[] allValues, allColumns;
		String[] parsedConditions, parsedColumns;
		switch (queryTokens[0].toLowerCase()) {
			// this init may be used to reinitialize the meta-data
			case "init":
				DavisBaseEngine.initSystemSchemaTables();
				break;

			case "show":
				DavisBaseEngine.showTables();
				break;

			case "create":
				table = queryTokens[2];
				String[] queryAndColumns = query.split(table);
				String fullColumnsQuery = queryAndColumns[1].trim();
				allColumns = fullColumnsQuery.substring(1, fullColumnsQuery.length() - 1).split(",");
				for (int i = 0; i < allColumns.length; i++)
					allColumns[i] = allColumns[i].trim();

				if (tableExist(table)) {
					System.out.println("table " + table + " already exists.");
					System.out.println();
				} else {
					DavisBaseEngine.createTable(table, allColumns);
				}
				break;

			case "drop":
				table = queryTokens[2];
				if (!tableExist(table)) {
					System.out.println("table " + table + " does not exist.");
					System.out.println();
				} else {
					DavisBaseEngine.dropTable(table);
				}
				break;

			case "insert":
				table = queryTokens[2];
				String fullValuesQuery = query.split("values")[1].trim();
				fullValuesQuery = fullValuesQuery.substring(1, fullValuesQuery.length() - 1);
				allValues = fullValuesQuery.split(",");
				for (int i = 0; i < allValues.length; i++)
					allValues[i] = allValues[i].trim();
				if (!tableExist(table)) {
					System.out.println("table " + table + " does not exist.");
					System.out.println();
				} else {
					DavisBaseEngine.insertIntoTable(table, allValues);
				}
				break;

			case "select":
				String[] columnsTableAndConditions = query.split("where");
				// check if we have WHERE in query and process the condition part
				if (columnsTableAndConditions.length > 1) {
					String allConditionsString = columnsTableAndConditions[1].trim();
					parsedConditions = parserEquation(allConditionsString);
				} else {
					parsedConditions = new String[0];
				}
				// split table name and column names
				String[] columnsAndTable = columnsTableAndConditions[0].split("from");
				table = columnsAndTable[1].trim();

				// handle column names
				String allColumnsString = columnsAndTable[0].replace("select", "").trim(); // remove "select" word
				if (allColumnsString.contains("*")) {
					parsedColumns = new String[1];
					parsedColumns[0] = "*";
				} else {
					parsedColumns = allColumnsString.split(",");
					for (int i = 0; i < parsedColumns.length; i++)
						parsedColumns[i] = parsedColumns[i].trim();
				}
				if (!tableExist(table)) {
					System.out.println("Table " + table + " does not exist.");
					System.out.println();
				} else {
					DavisBaseEngine.selectFromTable(table, parsedColumns, parsedConditions);
				}
				break;

			case "help":
				help();
				break;

			case "version":
				displayVersion();
				break;

			case "exit":
				break;

			default:
				System.out.println("Unsupported Query Format: \"" + query + "\"");
				System.out.println();
				help();
				break;
		}
	} // end parseQuery

}

/////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * `
 * @author Safal Tyagi
 * @version 1.0
 * @apiNote this class provides the core database engine functions to process
 *          queries this uses BPlusTree to manage file indexing and
 *          DavisBaseDataBuffer classes to manage data streaming from or to
 *          database
 */
class DavisBaseEngine {

	private static RandomAccessFile davisbasetable;
	private static RandomAccessFile davisbasecolumn;

	/**
	 * ` shows the tables in database
	 * 
	 * @param
	 * @return
	 */
	public static void showTables() {

		String[] compare = new String[0];
		String[] columns = { "table_name" };
		String tablename = "davisbase_tables";
		selectFromTable(tablename, columns, compare);
	}

	/**
	 * ` creates a new table with columns
	 * 
	 * @param table   name of the table
	 * @param columns parsed columns
	 * @return
	 */
	public static void createTable(String table, String[] columns) {
		try {
			// create new table.tbl file and initialize paging
			RandomAccessFile file = new RandomAccessFile("data/" + table + ".tbl", "rw");
			file.setLength(512);
			file.seek(0);
			file.writeByte(0x0D);
			file.close();

			// add table to master davisbase_tables table
			file = new RandomAccessFile("data/davisbase_tables.tbl", "rw");
			int numPages = getPagesCount(file);
			int pageNo = 1;
			for (int page = 1; page <= numPages; page++) {
				int rm = DavisBaseFileIndexer.getRightPointer(file, page);
				if (rm == 0)
					pageNo = page;
			}

			int[] keyArray = DavisBaseFileIndexer.getKeys(file, pageNo);
			int key = keyArray[0];
			for (int i = 0; i < keyArray.length; i++)
				if (key < keyArray[i])
					key = keyArray[i];
			file.close();

			String[] tableValues = { Integer.toString(key + 1), table };

			// insert table attributes to davisbase_tables
			insertIntoTable("davisbase_tables", tableValues);

			// add columns to master column table davisbase_columns
			file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			String[] conditions = {};
			String[] columnNames = { "rowid", "table_name", "column_name", "data_type", "ordinal_position",
					"is_nullable" };
			DavisBaseDataBuffer buffer = new DavisBaseDataBuffer();

			// filter columns based on conditions
			filter(file, conditions, columnNames, buffer);

			key = buffer.content.size();

			for (int i = 0; i < columns.length; i++) {
				key = key + 1;
				String[] token = columns[i].split(" ");
				String isNullable = "YES";
				if (token.length > 2)
					isNullable = "NO";
				String columnName = token[0];
				String dataType = token[1].toUpperCase();
				String ordPos = Integer.toString(i + 1);
				String[] colValues = { Integer.toString(key), table, columnName, dataType, ordPos, isNullable };

				// insert table attributes to davisbase_columns
				insertIntoTable("davisbase_columns", colValues);
			}

			file.close();

		} catch (Exception e) {
			System.out.println("Table Creation Failed");
			e.printStackTrace();
		}
	}

	/**
	 * ` drops table table from database
	 * 
	 * @param table name of the table
	 * @return
	 */
	public static void dropTable(String table) {
		try {
			// remove entry from master table davisbase_tables
			RandomAccessFile file = new RandomAccessFile("data/davisbase_tables.tbl", "rw");
			int numPages = getPagesCount(file);
			for (int page = 1; page <= numPages; page++) {
				file.seek((page - 1) * 512);
				if (file.readByte() == 0x05)
					continue;
				else {
					short[] cells = DavisBaseFileIndexer.getCells(file, page);
					int id = 0;
					for (int j = 0; j < cells.length; j++) {
						long loc = DavisBaseFileIndexer.getCellOffsetLocation(file, page, j);
						String[] payloads = retrievePayload(file, loc);
						String tblName = payloads[1];
						if (!tblName.equals(table)) {
							DavisBaseFileIndexer.setCellOffset(file, page, id, cells[j]);
							id++;
						}
					}
					DavisBaseFileIndexer.setTotalCells(file, page, (byte) id);
				}
			}

			// remove entry from master columns davisbase_columns
			file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			numPages = getPagesCount(file);
			for (int page = 1; page <= numPages; page++) {
				file.seek((page - 1) * 512);
				if (file.readByte() == 0x05)
					continue;
				else {
					short[] cells = DavisBaseFileIndexer.getCells(file, page);
					int i = 0;
					for (int j = 0; j < cells.length; j++) {
						long loc = DavisBaseFileIndexer.getCellOffsetLocation(file, page, j);
						String[] payloads = retrievePayload(file, loc);
						String tblName = payloads[1];
						if (!tblName.equals(table)) {
							DavisBaseFileIndexer.setCellOffset(file, page, i, cells[j]);
							i++;
						}
					}
					DavisBaseFileIndexer.setTotalCells(file, page, (byte) i);
				}
			}

			// simply delete target table file
			File tableFile = new File("data", table + ".tbl");
			tableFile.delete();

		} catch (Exception e) {
			System.out.println("Drop Table Failed");
			System.out.println(e);
		}

	}

	/**
	 * ` insert into table, warpper method to support recursive call
	 * 
	 * @param table  name of the table
	 * @param values column names with values
	 * @return
	 */
	public static void insertIntoTable(String table, String[] values) {
		try {
			RandomAccessFile file = new RandomAccessFile("data/" + table + ".tbl", "rw");
			insertIntoTable(file, table, values);
			file.close();

		} catch (Exception e) {
			System.out.println("Insert into Table Failed");
			e.printStackTrace();
		}
	}

	/**
	 * ` insert into file by recursively looking for leaf node and split if needed
	 * 
	 * @param file   table file to insert into
	 * @param table  name of the table
	 * @param values column names with values
	 * @return
	 */
	public static void insertIntoTable(RandomAccessFile file, String table, String[] values) {
		String[] dataTpye = getDataType(table);
		String[] nullable = isNullable(table);

		for (int i = 0; i < nullable.length; i++)
			if (values[i].equals("null") && nullable[i].equals("NO")) {
				System.out.println("NULL value constraint violation");
				System.out.println();
				return;
			}

		int key = Integer.valueOf(values[0]);
		int page = searchKey(file, key);
		if (page == 0)
			page = 1;
		else if (DavisBaseFileIndexer.hasKey(file, page, key)) {
			System.out.println("Uniqueness constraint violation");
			System.out.println();
			return;
		}

		byte[] stc = new byte[dataTpye.length - 1];
		short payloadSize = (short) calculatePayloadSize(table, values, stc);
		int cellSize = payloadSize + 6;
		int offset = DavisBaseFileIndexer.checkLeafSpace(file, page, cellSize);

		// add to leaf else recurse after split
		if (offset != -1) {
			DavisBaseFileIndexer.addLeafNode(file, page, offset, payloadSize, key, stc, values);
		} else {
			// split node
			DavisBaseFileIndexer.splitLeafNode(file, page);
			// recurse after split
			insertIntoTable(file, table, values);
		}
	}

	/**
	 * ` insert into file by recursively looking for leaf node and split if needed
	 * 
	 * @param table      name of the table
	 * @param columns    list of column names with values
	 * @param conditions list of conditions with values from where clause
	 * @return
	 */
	public static void selectFromTable(String table, String[] columns, String[] conditions) {
		try {
			String[] columnNames = getColName(table);
			String[] dataTypes = getDataType(table);

			DavisBaseDataBuffer buffer = new DavisBaseDataBuffer();
			RandomAccessFile file = new RandomAccessFile("data/" + table + ".tbl", "rw");
			filter(file, conditions, columnNames, dataTypes, buffer);
			buffer.display(columns);
			file.close();
		} catch (Exception e) {
			System.out.println("Select From Table Failed");
			System.out.println(e);
		}
	}

	/**
	 * ` insert into file by recursively looking for leaf node and split if needed
	 * 
	 * @param table      name of the table
	 * @param columns    list of column names with values
	 * @param conditions list of conditions with values from where clause
	 * @param types      list of column datatypes
	 * @return
	 */
	public static void filter(RandomAccessFile file, String[] conditions, String[] columns, String[] types,
			DavisBaseDataBuffer buffer) {
		try {
			int numPages = getPagesCount(file);
			// get column_name
			for (int page = 1; page <= numPages; page++) {
				file.seek((page - 1) * 512);
				byte pageType = file.readByte();
				if (pageType == 0x05)
					continue;
				else {
					byte numCells = DavisBaseFileIndexer.getTotalCells(file, page);

					for (int i = 0; i < numCells; i++) {
						// System.out.println("check point");
						long loc = DavisBaseFileIndexer.getCellOffsetLocation(file, page, i);
						file.seek(loc + 2); // seek to rowid
						int rowid = file.readInt(); // read rowid

						String[] payload = retrievePayload(file, loc);

						for (int j = 0; j < types.length; j++)
							if (types[j].equals("DATE") || types[j].equals("DATETIME"))
								payload[j] = "'" + payload[j] + "'";
						// check
						boolean check = checkComparisonValidity(payload, rowid, conditions, columns);

						// convert back date type
						for (int j = 0; j < types.length; j++)
							if (types[j].equals("DATE") || types[j].equals("DATETIME"))
								payload[j] = payload[j].substring(1, payload[j].length() - 1);

						if (check)
							buffer.add(rowid, payload);
					}
				}
			}

			buffer.columns = columns;
			buffer.format = new int[columns.length];

		} catch (Exception e) {
			System.out.println("Error at filter");
			e.printStackTrace();
		}

	}

	/**
	 * Filter out the rows bases on conditions
	 * 
	 * @param table      table name to check
	 * @param file       file from which the data is read
	 * @param conditions conditions to check, like where xyz >= 10
	 * @param columns    list of columns to filter
	 * @return
	 */
	public static void filter(RandomAccessFile file, String[] conditions, String[] columns,
			DavisBaseDataBuffer buffer) {
		try {
			int numPages = getPagesCount(file);
			// get column_name
			for (int page = 1; page <= numPages; page++) {
				file.seek((page - 1) * 512);
				byte pageType = file.readByte();
				if (pageType == 0x05)
					continue;
				else {
					byte numCells = DavisBaseFileIndexer.getTotalCells(file, page);

					for (int i = 0; i < numCells; i++) {
						long loc = DavisBaseFileIndexer.getCellOffsetLocation(file, page, i);
						file.seek(loc + 2); // seek to rowid
						int rowid = file.readInt(); // read rowid
						String[] payload = retrievePayload(file, loc);

						boolean check = checkComparisonValidity(payload, rowid, conditions, columns);
						if (check)
							buffer.add(rowid, payload);
					}
				}
			}

			buffer.columns = columns;
			buffer.format = new int[columns.length];

		} catch (Exception e) {
			System.out.println("Error at filter");
			e.printStackTrace();
		}

	}

	/**
	 * get payload from file as list of strings
	 * 
	 * @param file file from which the data is read
	 * @param loc  from where to read in file
	 * @return payload as list of strings
	 */
	public static String[] retrievePayload(RandomAccessFile file, long loc) {
		String[] payload = new String[0];
		final String dp = "yyyy-MM-dd_HH:mm:ss";
		try {
			Long tmp;
			SimpleDateFormat formater = new SimpleDateFormat(dp);

			// get stc
			file.seek(loc);
			file.readShort();
			int key = file.readInt();
			int num_cols = file.readByte();
			byte[] stc = new byte[num_cols];
			file.read(stc);
			payload = new String[num_cols + 1];
			payload[0] = Integer.toString(key);
			// get payLoad
			for (int i = 1; i <= num_cols; i++) {
				switch (stc[i - 1]) {
					case 0x00:
						payload[i] = Integer.toString(file.readByte());
						payload[i] = "null";
						break;

					case 0x01:
						payload[i] = Integer.toString(file.readShort());
						payload[i] = "null";
						break;

					case 0x02:
						payload[i] = Integer.toString(file.readInt());
						payload[i] = "null";
						break;

					case 0x03:
						payload[i] = Long.toString(file.readLong());
						payload[i] = "null";
						break;

					case 0x04:
						payload[i] = Integer.toString(file.readByte());
						break;

					case 0x05:
						payload[i] = Integer.toString(file.readShort());
						break;

					case 0x06:
						payload[i] = Integer.toString(file.readInt());
						break;

					case 0x07:
						payload[i] = Long.toString(file.readLong());
						break;

					case 0x08:
						payload[i] = String.valueOf(file.readFloat());
						break;

					case 0x09:
						payload[i] = String.valueOf(file.readDouble());
						break;

					case 0x0A:
						tmp = file.readLong();
						Date dateTime = new Date(tmp);
						payload[i] = formater.format(dateTime);
						break;

					case 0x0B:
						tmp = file.readLong();
						Date date = new Date(tmp);
						payload[i] = formater.format(date).substring(0, 10);
						break;

					default:
						int len = Integer.valueOf(stc[i - 1] - 0x0C);
						byte[] bytes = new byte[len];
						for (int j = 0; j < len; j++)
							bytes[j] = file.readByte();
						payload[i] = new String(bytes);
						break;
				}
			}

		} catch (Exception e) {
			System.out.println("Error at retrievePayload");
		}

		return payload;
	}

	/**
	 * compute payload size
	 * 
	 * @param table which table
	 * @param stc   serial type code
	 */
	public static int calculatePayloadSize(String table, String[] vals, byte[] stc) {
		String[] dataType = getDataType(table);
		int size = 1;
		size = size + dataType.length - 1;
		for (int i = 1; i < dataType.length; i++) {
			byte tmp = stcCode(vals[i], dataType[i]);
			stc[i - 1] = tmp;
			size = size + feildLength(tmp);
		}
		return size;
	}

	/**
	 * calculate stc length
	 * 
	 * @param stc serial type code
	 */
	public static short feildLength(byte stc) {
		switch (stc) {
			case 0x00:
				return 1;
			case 0x01:
				return 2;
			case 0x02:
				return 4;
			case 0x03:
				return 8;
			case 0x04:
				return 1;
			case 0x05:
				return 2;
			case 0x06:
				return 4;
			case 0x07:
				return 8;
			case 0x08:
				return 4;
			case 0x09:
				return 8;
			case 0x0A:
				return 8;
			case 0x0B:
				return 8;
			default:
				return (short) (stc - 0x0C);
		}
	}

	/**
	 * serial type code from value and dataType
	 * 
	 * @param dataTyoe data type
	 */
	public static byte stcCode(String val, String dataType) {
		if (val.equals("null")) {
			switch (dataType) {
				case "TINYINT":
					return 0x00;
				case "SMALLINT":
					return 0x01;
				case "INT":
					return 0x02;
				case "BIGINT":
					return 0x03;
				case "REAL":
					return 0x02;
				case "DOUBLE":
					return 0x03;
				case "DATETIME":
					return 0x03;
				case "DATE":
					return 0x03;
				case "TEXT":
					return 0x03;
				default:
					return 0x00;
			}
		} else {
			switch (dataType) {
				case "TINYINT":
					return 0x04;
				case "SMALLINT":
					return 0x05;
				case "INT":
					return 0x06;
				case "BIGINT":
					return 0x07;
				case "REAL":
					return 0x08;
				case "DOUBLE":
					return 0x09;
				case "DATETIME":
					return 0x0A;
				case "DATE":
					return 0x0B;
				case "TEXT":
					return (byte) (val.length() + 0x0C);
				default:
					return 0x00;
			}
		}
	}

	/**
	 * search key in file indexer
	 * 
	 * @param
	 */
	public static int searchKey(RandomAccessFile file, int key) {
		int val = 1;
		try {
			int numPages = getPagesCount(file);
			for (int page = 1; page <= numPages; page++) {
				file.seek((page - 1) * 512);
				byte pageType = file.readByte();
				if (pageType == 0x0D) {
					int[] keys = DavisBaseFileIndexer.getKeys(file, page);
					if (keys.length == 0)
						return 0;
					int rm = DavisBaseFileIndexer.getRightPointer(file, page);
					if (keys[0] <= key && key <= keys[keys.length - 1]) {
						return page;
					} else if (rm == 0 && keys[keys.length - 1] < key) {
						return page;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error at searchKey");
			System.out.println(e);
		}

		return val;
	}

	/**
	 * get data types in table
	 * 
	 * @param
	 */
	public static String[] getDataType(String table) {
		String[] dataType = new String[0];
		try {
			RandomAccessFile file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			DavisBaseDataBuffer buffer = new DavisBaseDataBuffer();
			String[] columnName = { "rowid", "table_name", "column_name", "data_type", "ordinal_position",
					"is_nullable" };
			String[] conditions = { "table_name", "=", table };
			filter(file, conditions, columnName, buffer);
			HashMap<Integer, String[]> content = buffer.content;
			ArrayList<String> array = new ArrayList<String>();
			for (String[] i : content.values()) {
				array.add(i[3]);
			}
			dataType = array.toArray(new String[array.size()]);
			file.close();
			return dataType;
		} catch (Exception e) {
			System.out.println("Error at getDataType");
			System.out.println(e);
		}
		return dataType;
	}

	/**
	 * get columns in table
	 * 
	 * @param
	 */
	public static String[] getColName(String table) {
		String[] c = new String[0];
		try {
			RandomAccessFile file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			DavisBaseDataBuffer buffer = new DavisBaseDataBuffer();
			String[] columnName = { "rowid", "table_name", "column_name", "data_type", "ordinal_position",
					"is_nullable" };
			String[] conditions = { "table_name", "=", table };
			filter(file, conditions, columnName, buffer);
			HashMap<Integer, String[]> content = buffer.content;
			ArrayList<String> array = new ArrayList<String>();
			for (String[] i : content.values()) {
				array.add(i[2]);
			}
			c = array.toArray(new String[array.size()]);
			file.close();
			return c;
		} catch (Exception e) {
			System.out.println("Error at getColName");
			System.out.println(e);
		}
		return c;
	}

	/**
	 * get nullable status value in table
	 * 
	 * @param
	 */
	public static String[] isNullable(String table) {
		String[] n = new String[0];
		try {
			RandomAccessFile file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			DavisBaseDataBuffer buffer = new DavisBaseDataBuffer();
			String[] columnName = { "rowid", "table_name", "column_name", "data_type", "ordinal_position",
					"is_nullable" };
			String[] conditions = { "table_name", "=", table };
			filter(file, conditions, columnName, buffer);
			HashMap<Integer, String[]> content = buffer.content;
			ArrayList<String> array = new ArrayList<String>();
			for (String[] i : content.values()) {
				array.add(i[5]);
			}
			n = array.toArray(new String[array.size()]);
			file.close();
			return n;
		} catch (Exception e) {
			System.out.println("Error at getNullable");
			System.out.println(e);
		}
		return n;
	}

	/**
	 * get page count in file page size 512
	 * 
	 * @param
	 */
	public static int getPagesCount(RandomAccessFile file) {
		int num_pages = 0;
		try {
			num_pages = (int) (file.length() / (Long.valueOf(512)));
		} catch (Exception e) {
			System.out.println("Error at makeInteriorPage");
		}

		return num_pages;
	}

	/**
	 * check the select condition as per the operator
	 * 
	 * @param
	 */
	public static boolean checkComparisonValidity(String[] payload, int rowid, String[] conditions,
			String[] columnName) {

		boolean check = false;
		if (conditions.length == 0) {
			check = true;
		} else {
			int colPos = 1;
			for (int i = 0; i < columnName.length; i++) {
				if (columnName[i].equals(conditions[0])) {
					colPos = i + 1;
					break;
				}
			}
			String opt = conditions[1];
			String val = conditions[2];
			if (colPos == 1) {
				switch (opt) {
					case "=":
						check = (rowid == Integer.parseInt(val));
						break;
					case ">":
						check = (rowid > Integer.parseInt(val));
						break;
					case "<":
						check = (rowid < Integer.parseInt(val));
						break;
					case ">=":
						check = (rowid >= Integer.parseInt(val));
						break;
					case "<=":
						check = (rowid <= Integer.parseInt(val));
						break;
					case "<>":
						check = (rowid != Integer.parseInt(val));
						break;
				}
			} else {
				if (val.equals(payload[colPos - 1]))
					check = true;
				else
					check = false;
			}
		}
		return check;
	}

	/**
	 * main method to prepare the database schema
	 * 
	 * @param
	 */
	public static void initSystemSchemaTables() {

		try {
			File dataDir = new File("data");
			dataDir.mkdir();
			String[] oldTableFiles;
			oldTableFiles = dataDir.list();
			for (int i = 0; i < oldTableFiles.length; i++) {
				File anOldFile = new File(dataDir, oldTableFiles[i]);
				anOldFile.delete();
			}
		} catch (SecurityException se) {
			System.out.println("Unable to create data container directory");
			System.out.println(se);
		}

		try {
			davisbasetable = new RandomAccessFile("data/davisbase_tables.tbl", "rw");
			davisbasetable.setLength(512);
			davisbasetable.seek(0);
			davisbasetable.write(0x0D);// page type
			davisbasetable.write(0x02);// num cell
			int[] offset = new int[2];
			int size1 = 24;// table size
			int size2 = 25;// column size
			offset[0] = 512 - size1;
			offset[1] = offset[0] - size2;
			davisbasetable.writeShort(offset[1]);// content offset
			davisbasetable.writeInt(0);// rightmost
			davisbasetable.writeInt(10);// parent
			davisbasetable.writeShort(offset[1]);// cell arrary 1
			davisbasetable.writeShort(offset[0]);// cell arrary 2

			// data
			writeTableSchema(davisbasetable, offset[0], 20, 1, 1, 28, "davisbase_tables");
			writeTableSchema(davisbasetable, offset[1], 21, 2, 1, 29, "davisbase_columns");

		} catch (Exception e) {
			System.out.println("Unable to create the database_tables file");
			System.out.println(e);
		}
		try {
			davisbasecolumn = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			davisbasecolumn.setLength(512);
			davisbasecolumn.seek(0);
			davisbasecolumn.writeByte(0x0D); // page type: leaf page
			davisbasecolumn.writeByte(0x08); // number of cells
			int[] offset = new int[10];
			offset[0] = 512 - 43;
			offset[1] = offset[0] - 47;
			offset[2] = offset[1] - 44;
			offset[3] = offset[2] - 48;
			offset[4] = offset[3] - 49;
			offset[5] = offset[4] - 47;
			offset[6] = offset[5] - 57;
			offset[7] = offset[6] - 49;
			offset[8] = offset[7] - 49;
			davisbasecolumn.writeShort(offset[8]); // content offset
			davisbasecolumn.writeInt(0); // rightmost
			davisbasecolumn.writeInt(0); // parent
			// cell array
			for (int i = 0; i < 9; i++)
				davisbasecolumn.writeShort(offset[i]);

			// data
			writeColumnSchema(davisbasecolumn, offset[0], 33, 1, 5, 28, 17, 15, 4, 14, "davisbase_tables", "rowid",
					"INT", 1, "NO");
			writeColumnSchema(davisbasecolumn, offset[1], 39, 2, 5, 28, 22, 16, 4, 14, "davisbase_tables", "table_name",
					"TEXT", 2, "NO");
			writeColumnSchema(davisbasecolumn, offset[2], 34, 3, 5, 29, 17, 15, 4, 14, "davisbase_columns", "rowid",
					"INT", 1, "NO");
			writeColumnSchema(davisbasecolumn, offset[3], 40, 4, 5, 29, 22, 16, 4, 14, "davisbase_columns",
					"table_name", "TEXT", 2, "NO");
			writeColumnSchema(davisbasecolumn, offset[4], 41, 5, 5, 29, 23, 16, 4, 14, "davisbase_columns",
					"column_name", "TEXT", 3, "NO");
			writeColumnSchema(davisbasecolumn, offset[5], 39, 6, 5, 29, 21, 16, 4, 14, "davisbase_columns", "data_type",
					"TEXT", 4, "NO");
			writeColumnSchema(davisbasecolumn, offset[6], 49, 7, 5, 29, 28, 19, 4, 14, "davisbase_columns",
					"ordinal_position", "TINYINT", 5, "NO");
			writeColumnSchema(davisbasecolumn, offset[7], 41, 8, 5, 29, 23, 16, 4, 14, "davisbase_columns",
					"is_nullable", "TEXT", 6, "NO");

		} catch (Exception e) {
			System.out.println("Unable to create the database_columns file");
			System.out.println(e);
		}
	}

	/**
	 * service method
	 * 
	 * @param
	 */
	public static void writeTableSchema(RandomAccessFile file, int offset, int short1, int int1, int byte1, int byte2,
			String table) {
		try {
			davisbasetable.seek(offset);
			davisbasetable.writeShort(short1);
			davisbasetable.writeInt(int1);
			davisbasetable.writeByte(byte1);
			davisbasetable.writeByte(byte2);
			davisbasetable.writeBytes(table);
		} catch (Exception e) {
			System.out.println("Unable to create the database_columns file");
			System.out.println(e);
		}

	}

	/**
	 * service method
	 * 
	 * @param
	 */
	public static void writeColumnSchema(RandomAccessFile file, int offset, int short1, int int1, int byte1, int byte2,
			int byte3, int byte4, int byte5, int byte6, String column, String isNullable, String dataType, int byte7,
			String isNull) {
		try {
			file.seek(offset);
			file.writeShort(short1);
			file.writeInt(int1);
			file.writeByte(byte1);
			file.writeByte(byte2);
			file.writeByte(byte3);
			file.writeByte(byte4);
			file.writeByte(byte5);
			file.writeByte(byte6);
			file.writeBytes(column);
			file.writeBytes(isNullable);
			file.writeBytes(dataType);
			file.writeByte(byte7);
			file.writeBytes(isNull);
		} catch (Exception e) {
			System.out.println("Unable to create the database_columns file");
			System.out.println(e);
		}
	}

}

/////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * `
 * @author Safal Tyagi
 * @version 1.0
 * @apiNote this class implements the B+ tree based file indexing system for DB
 */

class DavisBaseFileIndexer {

	public static final String dp = "yyyy-MM-dd_HH:mm:ss";

	/** insert a cell in to leaf page */
	public static void addLeafNode(RandomAccessFile tablefile, int page, int offset, short Sz, int key, byte[] stc,
			String[] vals) {
		try {
			String s;
			tablefile.seek((page - 1) * 512 + offset);
			tablefile.writeShort(Sz);
			tablefile.writeInt(key);
			int col = vals.length - 1;
			tablefile.writeByte(col);
			tablefile.write(stc);
			for (int i = 1; i < vals.length; i++) {
				switch (stc[i - 1]) {
					case 0x00:
						tablefile.writeByte(0);
						break;
					case 0x01:
						tablefile.writeShort(0);
						break;
					case 0x02:
						tablefile.writeInt(0);
						break;
					case 0x03:
						tablefile.writeLong(0);
						break;
					case 0x04:
						tablefile.writeByte(Byte.parseByte(vals[i]));
						break;
					case 0x05:
						tablefile.writeShort(Short.parseShort(vals[i]));
						break;
					case 0x06:
						tablefile.writeInt(Integer.parseInt(vals[i]));
						break;
					case 0x07:
						tablefile.writeLong(Long.parseLong(vals[i]));
						break;
					case 0x08:
						tablefile.writeFloat(Float.parseFloat(vals[i]));
						break;
					case 0x09:
						tablefile.writeDouble(Double.parseDouble(vals[i]));
						break;
					case 0x0A:
						s = vals[i];
						Date temp = new SimpleDateFormat(dp).parse(s.substring(1, s.length() - 1));
						long time = temp.getTime();
						tablefile.writeLong(time);
						break;
					case 0x0B:
						s = vals[i];
						s = s.substring(1, s.length() - 1);
						System.out.println(s);
						s = s + "_00:00:00";
						Date temp2 = new SimpleDateFormat(dp, Locale.US).parse(s);
						long time2 = temp2.getTime();
						tablefile.writeLong(time2);
						break;
					default:
						tablefile.writeBytes(vals[i]);
						break;
				}
			}
			int n = getTotalCells(tablefile, page);
			byte tmp = (byte) (n + 1);
			setTotalCells(tablefile, page, tmp);
			tablefile.seek((page - 1) * 512 + 12 + n * 2);
			tablefile.writeShort(offset);
			tablefile.seek((page - 1) * 512 + 2);
			int content = tablefile.readShort();
			if (content >= offset || content == 0) {
				tablefile.seek((page - 1) * 512 + 2);
				tablefile.writeShort(offset);
			}
		} catch (Exception e) {
			System.out.println("Error at insertLeafCell");
			e.printStackTrace();
		}
	}

	/** split leaf node for overflow */
	public static void splitLeafNode(RandomAccessFile tablefile, int pagenum) {
		int newPage = makeLeafPage(tablefile);
		int midindexKey = fetchIndexKeyMid(pagenum, tablefile);
		distributeLeafNodeData(tablefile, pagenum, newPage);
		int parent = getParent(tablefile, pagenum);
		if (parent == 0) {
			int root = convertInnerNode(tablefile);
			setParent(tablefile, pagenum, root);
			setParent(tablefile, newPage, root);
			setRightpointer(tablefile, root, newPage);
			addInnerNode(tablefile, root, pagenum, midindexKey);
		} else {
			long ploc = getPointerLocation(tablefile, pagenum, parent);
			setPointerLocation(tablefile, ploc, parent, newPage);
			addInnerNode(tablefile, parent, pagenum, midindexKey);
			sortDataCells(tablefile, parent);
			while (checkInteriorSpace(tablefile, parent)) {
				parent = splitInnerNode(tablefile, parent);
			}
		}
	}

	/** readjust content of parent node to children and pointers accordingly */
	public static void distributeLeafNodeData(RandomAccessFile tablefile, int origPagenum, int newPagenum) {
		try {
			// num cells in cur page
			int num_records = getTotalCells(tablefile, origPagenum);
			// id of mid cell
			int middlekey = (int) Math.ceil((double) num_records / 2);

			int leftoffset = middlekey - 1;
			int RightOffset = num_records - middlekey;
			int totalbytes = 512;

			for (int i = leftoffset; i < num_records; i++) {
				long location = getCellOffsetLocation(tablefile, origPagenum, i);
				// read cell size
				tablefile.seek(location);
				int data_cell = tablefile.readShort() + 6;
				totalbytes = totalbytes - data_cell;
				// reading the data from the parent node...
				tablefile.seek(location);
				byte[] tempb = new byte[data_cell];
				tablefile.readFully(tempb);
				// writing the data to left child
				tablefile.seek((newPagenum - 1) * 512 + totalbytes);
				tablefile.write(tempb);
				// fix cell arrary in the new page TODO
				setCellOffset(tablefile, newPagenum, i - leftoffset, totalbytes);
			}

			// write the offsetss to new page node...
			tablefile.seek((newPagenum - 1) * 512 + 2);
			tablefile.writeShort(totalbytes);

			// rewrite offset in the original node...
			short offset = getCellOffset(tablefile, origPagenum, leftoffset - 1);
			tablefile.seek((origPagenum - 1) * 512 + 2);
			tablefile.writeShort(offset);

			// re-point pointer for right most nodes..
			int rightpointer = getRightPointer(tablefile, origPagenum);
			setRightpointer(tablefile, newPagenum, rightpointer);
			setRightpointer(tablefile, origPagenum, newPagenum);

			// adjust the parent pointer
			int parent = getParent(tablefile, origPagenum);
			setParent(tablefile, newPagenum, parent);

			// rewrite starting offset values....
			byte temp = (byte) leftoffset;
			setTotalCells(tablefile, origPagenum, temp);
			temp = (byte) RightOffset;
			setTotalCells(tablefile, newPagenum, temp);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** update data in leaf nodes */
	public static void updateLeafNode(RandomAccessFile tablefile, int page, int offset, int plsize, int key, byte[] stc,
			String[] vals) {
		try {
			String s;
			tablefile.seek((page - 1) * 512 + offset);
			tablefile.writeShort(plsize);
			tablefile.writeInt(key);
			int col = vals.length - 1;
			tablefile.writeByte(col);
			tablefile.write(stc);
			for (int i = 1; i < vals.length; i++) {
				switch (stc[i - 1]) {
					case 0x00:
						tablefile.writeByte(0);
						break;
					case 0x01:
						tablefile.writeShort(0);
						break;
					case 0x02:
						tablefile.writeInt(0);
						break;
					case 0x03:
						tablefile.writeLong(0);
						break;
					case 0x04:
						tablefile.writeByte(Byte.parseByte(vals[i]));
						break;
					case 0x05:
						tablefile.writeShort(Short.parseShort(vals[i]));
						break;
					case 0x06:
						tablefile.writeInt(Integer.parseInt(vals[i]));
						break;
					case 0x07:
						tablefile.writeLong(Long.parseLong(vals[i]));
						break;
					case 0x08:
						tablefile.writeFloat(Float.parseFloat(vals[i]));
						break;
					case 0x09:
						tablefile.writeDouble(Double.parseDouble(vals[i]));
						break;
					case 0x0A:
						s = vals[i];
						Date temp = new SimpleDateFormat(dp).parse(s.substring(1, s.length() - 1));
						long time = temp.getTime();
						tablefile.writeLong(time);
						break;
					case 0x0B:
						s = vals[i];
						s = s.substring(1, s.length() - 1);
						s = s + "_00:00:00";
						Date temp2 = new SimpleDateFormat(dp).parse(s);
						long time2 = temp2.getTime();
						tablefile.writeLong(time2);
						break;
					default:
						tablefile.writeBytes(vals[i]);
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** create a new leaf node on overflow */
	public static int makeLeafPage(RandomAccessFile tablefile) {
		int num_recs = 0;
		try {
			num_recs = (int) (tablefile.length() / (Long.valueOf(512)));
			num_recs += 1;
			tablefile.setLength(512 * num_recs);
			tablefile.seek((num_recs - 1) * 512);
			tablefile.writeByte(0x0D); // This page is a table leaf page
		} catch (Exception e) {
			System.out.println("Error at makeLeafPage");
		}

		return num_recs;

	}

	/** check how much space is left in a leaf */
	public static int checkLeafSpace(RandomAccessFile file, int page, int size) {
		int val = -1;

		try {
			file.seek((page - 1) * 512 + 2);
			int content = file.readShort();
			if (content == 0)
				return 512 - size;
			int numCells = getTotalCells(file, page);
			int space = content - 20 - 2 * numCells;
			if (size < space)
				return content - size;

		} catch (Exception e) {
			System.out.println("Error at checkLeafSpace");
		}

		return val;
	}

	/** insert the key pairs to inner node */
	public static void addInnerNode(RandomAccessFile tablefile, int page, int child, int key) {
		try {
			// find location
			tablefile.seek((page - 1) * 512 + 2);
			short content = tablefile.readShort();
			if (content == 0)
				content = 512;
			content = (short) (content - 8);
			// write data
			tablefile.seek((page - 1) * 512 + content);
			tablefile.writeInt(child);
			tablefile.writeInt(key);
			// fix content
			tablefile.seek((page - 1) * 512 + 2);
			tablefile.writeShort(content);
			byte temp = getTotalCells(tablefile, page);
			setCellOffset(tablefile, page, temp, content);
			// fix number of cell
			temp = (byte) (temp + 1);
			setTotalCells(tablefile, page, temp);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** expand inner nodes */
	public static int splitInnerNode(RandomAccessFile tablefile, int page) {
		int newPage = convertInnerNode(tablefile);
		int midindexKey = fetchIndexKeyMid(page, tablefile);
		distributeInnerNodeData(tablefile, page, newPage);
		int parent = getParent(tablefile, page);
		if (parent == 0) {
			int rootPage = convertInnerNode(tablefile);
			setParent(tablefile, page, rootPage);
			setParent(tablefile, newPage, rootPage);
			setRightpointer(tablefile, rootPage, newPage);
			addInnerNode(tablefile, rootPage, page, midindexKey);
			return rootPage;
		} else {
			long location = getPointerLocation(tablefile, page, parent);
			setPointerLocation(tablefile, location, parent, newPage);
			addInnerNode(tablefile, parent, page, midindexKey);
			sortDataCells(tablefile, parent);
			return parent;
		}
	}

	/** readjust internal pointers to new inner node */
	public static void distributeInnerNodeData(RandomAccessFile tablefile, int origPagenum, int newPagenum) {
		try {

			int num_records = getTotalCells(tablefile, origPagenum);
			int middlekey = (int) Math.ceil((double) num_records / 2);

			int leftoffset = middlekey - 1;
			int rigthtoffset = num_records - leftoffset - 1;
			short totalbytes = 512;

			for (int i = leftoffset + 1; i < num_records; i++) {
				long location = getCellOffsetLocation(tablefile, origPagenum, i);
				// read cell size
				short data_len = 8;
				totalbytes = (short) (totalbytes - data_len);
				// read cell data
				tablefile.seek(location);
				byte[] cell = new byte[data_len];
				tablefile.read(cell);
				// write cell data
				tablefile.seek((newPagenum - 1) * 512 + totalbytes);
				tablefile.write(cell);
				// fix parent pointer in target page
				tablefile.seek(location);
				int pagnum = tablefile.readInt();
				setParent(tablefile, pagnum, newPagenum);
				// fix cell arrary in new page
				setCellOffset(tablefile, newPagenum, i - (leftoffset + 1), totalbytes);
			}

			// re-point pointer for right most nodes..
			int tmp = getRightPointer(tablefile, origPagenum);
			setRightpointer(tablefile, newPagenum, tmp);

			long midLocation = getCellOffsetLocation(tablefile, origPagenum, middlekey - 1);
			tablefile.seek(midLocation);
			tmp = tablefile.readInt();
			setRightpointer(tablefile, origPagenum, tmp);

			// write content offset to new page
			tablefile.seek((newPagenum - 1) * 512 + 2);
			tablefile.writeShort(totalbytes);

			// rewrite the contents of offsets...
			short offset = getCellOffset(tablefile, origPagenum, leftoffset - 1);
			tablefile.seek((origPagenum - 1) * 512 + 2);
			tablefile.writeShort(offset);

			// adjust parent pointer...
			int parent = getParent(tablefile, origPagenum);
			setParent(tablefile, newPagenum, parent);
			// fix cell number
			byte temp = (byte) leftoffset;
			setTotalCells(tablefile, origPagenum, temp);
			temp = (byte) rigthtoffset;
			setTotalCells(tablefile, newPagenum, temp);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Convert leaf node in to inner node and then return the page number */
	public static int convertInnerNode(RandomAccessFile tableFile) {
		int recordsCount = 0;
		try {
			recordsCount = (int) (tableFile.length() / (Long.valueOf(512)));
			recordsCount += 1;
			tableFile.setLength(512 * recordsCount);
			tableFile.seek((recordsCount - 1) * 512);
			tableFile.writeByte(0x05); // inner node
		} catch (Exception e) {
			e.printStackTrace();
		}

		return recordsCount;
	}

	/** assmue interior page has 10 more key implys full */
	public static boolean checkInteriorSpace(RandomAccessFile file, int page) {
		byte numCells = getTotalCells(file, page);
		if (numCells > 30)
			return true;
		else
			return false;
	}

	/** set the offset as child pointer */
	public static void setPointerLocation(RandomAccessFile tablefile, long location, int parent, int page) {
		try {
			if (location == 0) {
				tablefile.seek((parent - 1) * 512 + 4);
			} else {
				tablefile.seek(location);
			}
			tablefile.writeInt(page);
		} catch (Exception e) {
			System.out.println("Error at setPointerLoc");
		}
	}

	/** get child pointer file from parent page */
	public static long getPointerLocation(RandomAccessFile tablefile, int page, int parent) {
		long value = 0;
		try {
			int numCells = Integer.valueOf(getTotalCells(tablefile, parent));
			for (int i = 0; i < numCells; i++) {
				long loc = getCellOffsetLocation(tablefile, parent, i);
				tablefile.seek(loc);
				int childPage = tablefile.readInt();
				if (childPage == page) {
					value = loc;
				}
			}
		} catch (Exception e) {
			System.out.println("Error at getPointerLoc");
		}

		return value;
	}

	/** sort data inside table */
	public static void sortDataCells(RandomAccessFile tablefile, int page) {
		byte num = getTotalCells(tablefile, page);
		short[] cells = getCells(tablefile, page);
		int[] keys = getKeys(tablefile, page);

		int ltmp;
		short rtmp;

		for (int i = 1; i < num; i++) {
			for (int j = i; j > 0; j--) {
				if (keys[j] < keys[j - 1]) {
					// swap the keys....
					ltmp = keys[j];
					keys[j] = keys[j - 1];
					keys[j - 1] = ltmp;
					// swap the data cells as well..
					rtmp = cells[j];
					cells[j] = cells[j - 1];
					cells[j - 1] = rtmp;
				}
			}
		}

		try {
			tablefile.seek((page - 1) * 512 + 12);
			for (int i = 0; i < num; i++) {
				tablefile.writeShort(cells[i]);
			}
		} catch (Exception e) {
			System.out.println("Error at sortDataCells");
		}
	}

	/** ` get cells from page in file */
	public static short[] getCells(RandomAccessFile tablefile, int page) {
		int num = Integer.valueOf(getTotalCells(tablefile, page));
		short[] cells = new short[num];

		try {
			tablefile.seek((page - 1) * 512 + 12);
			for (int i = 0; i < num; i++) {
				cells[i] = tablefile.readShort();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cells;
	}

	/** get keys from page in file */
	public static int[] getKeys(RandomAccessFile tablefile, int page) {
		int num = Integer.valueOf(getTotalCells(tablefile, page));
		int[] keys = new int[num];

		try {
			tablefile.seek((page - 1) * 512);
			byte pageType = tablefile.readByte();
			byte offset = 0;
			switch (pageType) {
				case 0x0d: // leaf node
					offset = 2;
					break;
				case 0x05:// inner node..
					offset = 4;
					break;
				default:
					offset = 2;
					break;
			}

			for (int i = 0; i < num; i++) {
				long loc = getCellOffsetLocation(tablefile, page, i);
				tablefile.seek(loc + offset);
				keys[i] = tablefile.readInt();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return keys;
	}

	/** return the parent page number of page */
	public static int getParent(RandomAccessFile tablefile, int page) {
		int value = 0;

		try {
			tablefile.seek((page - 1) * 512 + 8);
			value = tablefile.readInt();
		} catch (Exception e) {
			System.out.println("Error at getParent");
		}

		return value;
	}

	/** set the parent page number of page */
	public static void setParent(RandomAccessFile tablefile, int page, int parent) {
		try {
			tablefile.seek((page - 1) * 512 + 8);
			tablefile.writeInt(parent);
		} catch (Exception e) {
			System.out.println("Error at setParent");
		}
	}

	/** get right pointer for page in file */
	public static int getRightPointer(RandomAccessFile tablefile, int page) {
		int value = 0;

		try {
			tablefile.seek((page - 1) * 512 + 4);
			value = tablefile.readInt();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	/** set right pointer for page in file */
	public static void setRightpointer(RandomAccessFile file, int page, int rightMost) {

		try {
			file.seek((page - 1) * 512 + 4);
			file.writeInt(rightMost);
		} catch (Exception e) {
			System.out.println("Error at setRightMost");
		}

	}

	/** get total number of records */
	public static byte getTotalCells(RandomAccessFile tablefile, int page) {
		byte totalcells = 0;

		try {
			tablefile.seek((page - 1) * 512 + 1);
			totalcells = tablefile.readByte();
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error at getTotalCells");
		}

		return totalcells;
	}

	/** get total number of records */
	public static void setTotalCells(RandomAccessFile file, int page, byte num) {
		try {
			file.seek((page - 1) * 512 + 1);
			file.writeByte(num);
		} catch (Exception e) {
			System.out.println("Error at setTotalCells");
		}
	}

	/** fetch middle index key */
	public static int fetchIndexKeyMid(int pagenum, RandomAccessFile Tablefile) {
		int val = 0;
		try {
			Tablefile.seek((pagenum - 1) * 512);
			byte pageType = Tablefile.readByte();
			// num cells in cur page
			int numCells = getTotalCells(Tablefile, pagenum);
			// id of mid cell
			int index = (int) Math.ceil((double) numCells / 2);
			long loc = getCellOffsetLocation(Tablefile, pagenum, index - 1);
			Tablefile.seek(loc);

			switch (pageType) {
				case 0x05:
					val = Tablefile.readInt();
					val = Tablefile.readInt();
					break;
				case 0x0D:
					val = Tablefile.readShort();
					val = Tablefile.readInt();
					break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return val;
	}

	/** search for a key */
	public static boolean hasKey(RandomAccessFile tablefile, int page, int key) {
		int[] array = getKeys(tablefile, page);
		for (int i : array)
			if (key == i)
				return true;
		return false;
	}

	/** read location of cell in the page, for inner nodes */
	public static long getCellOffsetLocation(RandomAccessFile tablefile, int page, int id) {
		long location = 0;
		try {
			tablefile.seek((page - 1) * 512 + 12 + id * 2);
			short offset = tablefile.readShort();
			long original = (page - 1) * 512;
			location = original + offset;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

	/** set offset for a page in table file */
	public static short getCellOffset(RandomAccessFile tablefile, int page, int id) {
		short offset = 0;
		try {
			tablefile.seek((page - 1) * 512 + 12 + id * 2);
			offset = tablefile.readShort();
		} catch (Exception e) {
			System.out.println("Error at getCellOffsetLocation ");
		}
		return offset;
	}

	/** set offset for a page in table file */
	public static void setCellOffset(RandomAccessFile tablefile, int page, int id, int offset) {
		try {
			tablefile.seek((page - 1) * 512 + 12 + id * 2);
			tablefile.writeShort(offset);
		} catch (Exception e) {
			System.out.println("Error at setCellOffset");
		}
	}

}


/////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * `
 * @author Safal Tyagi
 * @version 1.0
 * @apiNote this class is responsible for formatting the data in pretty printed tables
 */

class DavisBaseDataBuffer {
	// hash map to store data, represent all data of each row. Integer represent
	// rowid(key), String[]
	public HashMap<Integer, String[]> content; // rowid : list of row content
	// table pretty printing, stores -- for each column
	public int[] format;
	// column name of all columns
	public String[] columns;
	// number of rows
	public int rowsCount;

	/**
	 * ` construct DavisBaseDataBuffer
	 */
	public DavisBaseDataBuffer() {
		rowsCount = 0;
		content = new HashMap<Integer, String[]>();
	}

	/**
	 * ` add data into content container row by row
	 * 
	 * @param rowid      row id
	 * @param rowContent content of each row, all columns
	 */
	public void add(int rowid, String[] rowContent) {
		content.put(rowid, rowContent);
		rowsCount = rowsCount + 1;
	}

	/**
	 * ` prepare the right sized table structure
	 */
	public void format() {
		for (int i = 0; i < format.length; i++)
			format[i] = columns[i].length();
		for (String[] i : content.values()) {
			for (int j = 0; j < i.length; j++)
				if (format[j] < i[j].length())
					format[j] = i[j].length();
		}
	}

	/**
	 * ` displays the formatted table
	 * 
	 * @param cols columns data
	 */
	public void display(String[] cols) {
		// Prepare the format
		format();

		// display columns
		if (cols[0].equals("*")) { // all columns
			// print line
			for (int l : format)
				System.out.print(line("-", l + 3));
			System.out.println();

			// print column name
			for (int j = 0; j < columns.length; j++)
				System.out.print(fix(columns[j], format[j]) + "|");
			System.out.println();

			// print line
			for (int l : format)
				System.out.print(line("-", l + 3));
			System.out.println();

			// print data
			for (String[] i : content.values()) {
				for (int j = 0; j < i.length; j++)
					System.out.print(fix(i[j], format[j]) + "|");
				System.out.println();
			}

			System.out.println();

		} else { // selected column
			int[] control = new int[cols.length];
			for (int j = 0; j < cols.length; j++)
				for (int i = 0; i < columns.length; i++)
					if (cols[j].equals(columns[i]))
						control[j] = i;

			// print line
			for (int j = 0; j < control.length; j++)
				System.out.print(line("-", format[control[j]] + 3));
			System.out.println();

			// print column name
			for (int j = 0; j < control.length; j++)
				System.out.print(fix(columns[control[j]], format[control[j]]) + "|");
			System.out.println();

			// print line
			for (int j = 0; j < control.length; j++)
				System.out.print(line("-", format[control[j]] + 3));
			System.out.println();

			// print data
			for (String[] i : content.values()) {
				for (int j = 0; j < control.length; j++)
					System.out.print(fix(i[control[j]], format[control[j]]) + "|");
				System.out.println();
			}

			System.out.println();
		}

	}

	/**
	 * ` Fix lenght string filled with space
	 * 
	 * @param str which string
	 * @param len what length
	 */
	public String fix(String str, int len) {
		return String.format("%-" + (len + 3) + "s", str);
	}

	/**
	 * ` print a string str to len number of times
	 * 
	 * @param str which string
	 * @param len what length
	 */
	public String line(String str, int len) {
		String full = "";
		for (int i = 0; i < len; i++) {
			full += str;
		}
		return full;
	}

}
