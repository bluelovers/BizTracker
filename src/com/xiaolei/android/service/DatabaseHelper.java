/**
 * 
 */
package com.xiaolei.android.service;

import java.util.ArrayList;

import com.xiaolei.android.entity.CurrencySchema;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author xiaolei
 * 
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String databaseName = "biz_tracker_data.db";
	private static final int databaseVersion = 7;

	public DatabaseHelper(Context context) {
		super(context, databaseName, null, databaseVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createTable_Stuff = "CREATE TABLE IF NOT EXISTS \"Stuff\" (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , \"Name\" TEXT NOT NULL , \"Tag\" TEXT, \"LastUpdateTime\" DATETIME DEFAULT CURRENT_TIMESTAMP, \"Picture\" TEXT, \"LastUsedTime\"  DATETIME DEFAULT CURRENT_TIMESTAMP, \"IsActive\" BOOL NOT NULL  DEFAULT true)";
		String createIndex_Stuff = "CREATE INDEX IF NOT EXISTS \"Idx_Stuff\" ON \"Stuff\" (\"Name\" ASC, \"LastUpdateTime\" ASC, LastUsedTime ASC)";
		String createTable_Parameter = "CREATE TABLE IF NOT EXISTS \"Parameter\" (\"Id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , \"Key\" TEXT NOT NULL  UNIQUE , \"Value\" TEXT, \"Tag\" TEXT, \"LastUpdateTime\" DATETIME DEFAULT CURRENT_TIMESTAMP)";
		String createIndex_Parameter = "CREATE INDEX IF NOT EXISTS \"Idx_Parameter\" ON \"Parameter\" (\"Id\" ASC, \"Key\" ASC, \"LastUpdateTime\" ASC)";
		String createTable_BizLog = "CREATE TABLE IF NOT EXISTS \"BizLog\" (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , \"StuffId\" INTEGER NOT NULL , \"Cost\" DOUBLE NOT NULL , \"LastUpdateTime\" DATETIME DEFAULT CURRENT_TIMESTAMP, \"Star\" BOOL DEFAULT false, \"CurrencyCode\" TEXT, \"Comment\" TEXT, \"Tag\" TEXT, \"LocationName\" TEXT, \"Location\" TEXT)";
		String createIndex_BizLog = "CREATE INDEX IF NOT EXISTS \"Idx_BizLog\" ON \"BizLog\" (\"Cost\" ASC, \"LastUpdateTime\" ASC)";
		String createTable_Project = "CREATE TABLE IF NOT EXISTS \"Project\" (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"Name\" TEXT NOT NULL , \"Description\" TEXT, \"CreatedTime\" DATETIME NOT NULL  DEFAULT CURRENT_TIMESTAMP, \"LastUpdateTime\" DATETIME NOT NULL  DEFAULT CURRENT_TIMESTAMP, \"IsActive\" BOOL NOT NULL  DEFAULT true, \"Tag\" TEXT)";
		String createIndex_Project = "CREATE INDEX IF NOT EXISTS \"Idx_Project\" ON \"Project\" (\"Name\" DESC, \"CreatedTime\" DESC, \"LastUpdateTime\" DESC, \"IsActive\" DESC)";

		db.execSQL(createTable_Stuff);
		db.execSQL(createIndex_Stuff);

		db.execSQL(createTable_Parameter);
		db.execSQL(createIndex_Parameter);

		db.execSQL(createTable_BizLog);
		db.execSQL(createIndex_BizLog);

		supportMultiCurrency(db);

		db.execSQL(createTable_Project);
		db.execSQL(createIndex_Project);

		updateDBToVersion5(db);
		updateDBToVersion6(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		int currentDBVersion = db.getVersion();
		if (currentDBVersion < 2) {
			if (!columnExists(db, "BizLog", "Star")) {
				String sql = "ALTER TABLE \"BizLog\" ADD COLUMN \"Star\" BOOL DEFAULT false";
				db.execSQL(sql);
			}
		}

		if (currentDBVersion <= 2) {
			updateDBToVersion3(db);
		}

		if (currentDBVersion <= 3) {
			updateDBToVersion4(db);
		}

		if (currentDBVersion <= 4) {
			updateDBToVersion5(db);
		}

		if (currentDBVersion <= 5) {
			updateDBToVersion6(db);
		}

		if (currentDBVersion <= 6) {
			updateDBToVersion7(db);
		}
	}

	private void updateDBToVersion7(SQLiteDatabase db) {
		Boolean rowExists = false;
		Cursor cursor = db.query(CurrencySchema.TableName,
				new String[] { CurrencySchema.Code }, " Code=? ",
				new String[] { "MOP" }, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			rowExists = true;
			cursor.close();
			cursor = null;
		}

		if (rowExists == false) {
			// Add "MOP" currency for Macau
			String sql_AddMOPCurrency = "INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Macau Pataca\",\"MOP\",null,\"7.989\",\"2012-02-01 19:11:00\",\"true\");";

			db.execSQL(sql_AddMOPCurrency);
		}
	}

	private void updateDBToVersion6(SQLiteDatabase db) {
		String createTable_TransactionProjectRelation = "CREATE TABLE IF NOT EXISTS \"TransactionProjectRelation\" (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"TransactionId\" INTEGER NOT NULL , \"ProjectId\" INTEGER NOT NULL , \"CreatedTime\" DATETIME DEFAULT CURRENT_TIMESTAMP)";
		String createIndex_TransProjectRelation = "CREATE INDEX IF NOT EXISTS \"Idx_TransProjectRelation\" ON \"TransactionProjectRelation\" (\"TransactionId\" ASC, \"ProjectId\" ASC, \"CreatedTime\" ASC)";

		db.execSQL(createTable_TransactionProjectRelation);
		db.execSQL(createIndex_TransProjectRelation);
	}

	private void updateDBToVersion5(SQLiteDatabase db) {
		// v1.2.5
		String createTable_Photo = "CREATE TABLE IF NOT EXISTS \"Photo\" (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"BizLogId\" INTEGER NOT NULL , \"FileName\" TEXT NOT NULL , \"Thumbnail\" BLOB, \"CreatedTime\" DATETIME DEFAULT CURRENT_TIMESTAMP, \"Comment\" TEXT, \"Name\" TEXT, \"Tag\" TEXT)";
		String createIndex_Photo = "CREATE INDEX IF NOT EXISTS \"Idx_Photo\" ON \"Photo\" (\"_id\" ASC, \"BizLogId\" ASC, \"CreatedTime\" ASC, \"Comment\" ASC, \"Name\" ASC)";

		db.execSQL(createTable_Photo);
		db.execSQL(createIndex_Photo);
	}

	private void updateDBToVersion4(SQLiteDatabase db) {
		String createTable_Project = "CREATE TABLE IF NOT EXISTS \"Project\" (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"Name\" TEXT NOT NULL , \"Description\" TEXT, \"CreatedTime\" DATETIME NOT NULL  DEFAULT CURRENT_TIMESTAMP, \"LastUpdateTime\" DATETIME NOT NULL  DEFAULT CURRENT_TIMESTAMP, \"IsActive\" BOOL NOT NULL  DEFAULT true, \"Tag\" TEXT)";
		String createIndex_Project = "CREATE INDEX IF NOT EXISTS \"Idx_Project\" ON \"Project\" (\"Name\" DESC, \"CreatedTime\" DESC, \"LastUpdateTime\" DESC, \"IsActive\" DESC)";

		db.execSQL(createTable_Project);
		db.execSQL(createIndex_Project);

		if (!columnExists(db, "BizLog", "Tag")) {
			String sql_AddTagColumn = "ALTER TABLE \"BizLog\" ADD COLUMN \"Tag\" TEXT";
			db.execSQL(sql_AddTagColumn);
		}

		if (!columnExists(db, "BizLog", "LocationName")) {
			String sql_AddLocationNameColumn = "ALTER TABLE \"BizLog\" ADD COLUMN \"LocationName\" TEXT";
			db.execSQL(sql_AddLocationNameColumn);
		}

		if (!columnExists(db, "BizLog", "Location")) {
			String sql_AddLocationColumn = "ALTER TABLE \"BizLog\" ADD COLUMN \"Location\" TEXT";
			db.execSQL(sql_AddLocationColumn);
		}
	}

	private void updateDBToVersion3(SQLiteDatabase db) {
		if (!columnExists(db, "BizLog", "Comment")) {
			String sql_AddCommentColumn = "ALTER TABLE \"BizLog\" ADD COLUMN \"Comment\" TEXT";
			db.execSQL(sql_AddCommentColumn);
		}

		if (!columnExists(db, "BizLog", "CurrencyCode")) {
			String sql_AddCurrencyCodeColumn = "ALTER TABLE \"BizLog\" ADD COLUMN \"CurrencyCode\" TEXT";
			db.execSQL(sql_AddCurrencyCodeColumn);
		}

		supportMultiCurrency(db);
	}

	private Boolean columnExists(SQLiteDatabase db, String tableName,
			String columnName) {
		String sql = String.format("select * from %S where 1 <> 1", tableName);
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor != null) {
			String[] colNames = cursor.getColumnNames();
			if (colNames != null && colNames.length > 0) {
				for (int i = 0; i < colNames.length; i++) {
					if (colNames[i].equalsIgnoreCase(columnName)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private void supportMultiCurrency(SQLiteDatabase db) {
		String createTable_Currency = "CREATE TABLE IF NOT EXISTS \"Currency\" (\"_id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"Name\" TEXT NOT NULL , \"Code\" TEXT NOT NULL , \"Symbol\" TEXT, \"USDExchangeRate\" DOUBLE NOT NULL , \"LastUpdateTime\" DATETIME DEFAULT CURRENT_TIMESTAMP, \"IsActive\" BOOL NOT NULL  DEFAULT true)";
		String createIndex_Currency = "CREATE INDEX IF NOT EXISTS \"Idx_Currency\" ON \"Currency\" (\"Code\" ASC)";

		db.execSQL(createTable_Currency);
		db.execSQL(createIndex_Currency);
		db.execSQL("Delete from Currency;");

		ArrayList<String> records_Currency = new ArrayList<String>();

		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"United Arab Emirates Dirham\",\"AED\",null,\"3.673\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Netherlands Antillean Guilder\",\"ANG\",null,\"1.75\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Argentine Peso\",\"ARS\",null,\"4.148\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Australian Dollar\",\"AUD\",null,\"0.9464\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Bangladeshi Taka\",\"BDT\",null,\"74.7\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Bulgarian Lev\",\"BGN\",null,\"1.3746\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Bahraini Dinar\",\"BHD\",null,\"0.377\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Brunei Dollar\",\"BND\",null,\"1.2149\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Bolivian Boliviano\",\"BOB\",null,\"7.01\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Brazilian Real\",\"BRL\",null,\"1.57\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Botswanan Pula\",\"BWP\",null,\"6.6094\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Canadian Dollar\",\"CAD\",null,\"0.9719\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Swiss Franc\",\"CHF\",null,\"0.7739\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Chilean Peso\",\"CLP\",null,\"458\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Chinese Yuan\",\"CNY\",null,\"6.4378\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Colombian Peso\",\"COP\",null,\"1771.2\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Costa Rican Col¨®n\",\"CRC\",null,\"502.7\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Czech Republic Koruna\",\"CZK\",null,\"17.145\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Danish Krone\",\"DKK\",null,\"5.235\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Dominican Peso\",\"DOP\",null,\"37.6\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Algerian Dinar\",\"DZD\",null,\"72.4386\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Estonian Kroon\",\"EEK\",null,\"11.7303\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Egyptian Pound\",\"EGP\",null,\"5.953\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Euro\",\"EUR\",null,\"0.7028\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Fijian Dollar\",\"FJD\",null,\"1.7301\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"British Pound Sterling\",\"GBP\",null,\"0.6124\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Hong Kong Dollar\",\"HKD\",null,\"7.7998\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Honduran Lempira\",\"HNL\",null,\"18.77\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Croatian Kuna\",\"HRK\",null,\"5.2349\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Hungarian Forint\",\"HUF\",null,\"191.21\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Indonesian Rupiah\",\"IDR\",null,\"8495\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Israeli New Sheqel\",\"ILS\",null,\"3.491\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Indian Rupee\",\"INR\",null,\"44.54\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Jamaican Dollar\",\"JMD\",null,\"85.05\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Jordanian Dinar\",\"JOD\",null,\"0.7077\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Japanese Yen\",\"JPY\",null,\"79.59\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Kenyan Shilling\",\"KES\",null,\"92.2\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"South Korean Won\",\"KRW\",null,\"1061.5\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Kuwaiti Dinar\",\"KWD\",null,\"0.274\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Cayman Islands Dollar\",\"KYD\",null,\"0.81\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Kazakhstani Tenge\",\"KZT\",null,\"146.45\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Lebanese Pound\",\"LBP\",null,\"1513\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Sri Lankan Rupee\",\"LKR\",null,\"109.66\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Lithuanian Litas\",\"LTL\",null,\"2.44\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Latvian Lats\",\"LVL\",null,\"0.5012\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Moroccan Dirham\",\"MAD\",null,\"7.9851\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Moldovan Leu\",\"MDL\",null,\"11.4575\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Macedonian Denar\",\"MKD\",null,\"43.195\",\"2011-08-06 05:33:04\",\"true\");");
		// Add "MOP" currency for Macau
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Macau Pataca\",\"MOP\",null,\"7.989\",\"2012-02-01 19:11:00\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Mauritian Rupee\",\"MUR\",null,\"28.1\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Maldivian Rufiyaa\",\"MVR\",null,\"15.38\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Mexican Peso\",\"MXN\",null,\"11.9365\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Malaysian Ringgit\",\"MYR\",null,\"2.981\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Namibian Dollar\",\"NAD\",null,\"6.868\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Nigerian Naira\",\"NGN\",null,\"152.55\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Nicaraguan C¨®rdoba\",\"NIO\",null,\"22.52\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Norwegian Krone\",\"NOK\",null,\"5.4761\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Nepalese Rupee\",\"NPR\",null,\"70.77\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"New Zealand Dollar\",\"NZD\",null,\"1.1738\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Omani Rial\",\"OMR\",null,\"0.385\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Peruvian Nuevo Sol\",\"PEN\",null,\"2.7435\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Papua New Guinean Kina\",\"PGK\",null,\"2.3063\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Philippine Peso\",\"PHP\",null,\"42.33\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Pakistani Rupee\",\"PKR\",null,\"86.3\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Polish Zloty\",\"PLN\",null,\"2.8462\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Paraguayan Guarani\",\"PYG\",null,\"3830\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Qatari Rial\",\"QAR\",null,\"3.6413\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Romanian Leu\",\"RON\",null,\"2.9909\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Serbian Dinar\",\"RSD\",null,\"72.4956\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Russian Ruble\",\"RUB\",null,\"28.0557\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Saudi Riyal\",\"SAR\",null,\"3.7502\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Seychellois Rupee\",\"SCR\",null,\"12.0497\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Swedish Krona\",\"SEK\",null,\"6.486\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Singapore Dollar\",\"SGD\",null,\"1.2139\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Slovak Koruna\",\"SKK\",null,\"21.289\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Sierra Leonean Leone\",\"SLL\",null,\"4354\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Salvadoran Col¨®n\",\"SVC\",null,\"8.75\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Thai Baht\",\"THB\",null,\"29.81\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Tunisian Dinar\",\"TND\",null,\"1.392\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Turkish Lira\",\"TRY\",null,\"1.7326\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Trinidad and Tobago Dollar\",\"TTD\",null,\"6.39\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"New Taiwan Dollar\",\"TWD\",null,\"28.938\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Tanzanian Shilling\",\"TZS\",null,\"1600\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Ukrainian Hryvnia\",\"UAH\",null,\"7.995\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Ugandan Shilling\",\"UGX\",null,\"2665\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"US Dollar\",\"USD\",null,\"1\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Uruguayan Peso\",\"UYU\",null,\"18.35\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Uzbekistan Som\",\"UZS\",null,\"1727\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Venezuelan Bol¨ªvar\",\"VEF\",null,\"4.2946\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Vietnamese Dong\",\"VND\",null,\"20590\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"CFA Franc BCEAO\",\"XOF\",null,\"465.64\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Yemeni Rial\",\"YER\",null,\"216.77\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"South African Rand\",\"ZAR\",null,\"6.9031\",\"2011-08-06 05:33:04\",\"true\");");
		records_Currency
				.add("INSERT INTO \"Currency\" (Name, Code, Symbol, USDExchangeRate, LastUpdateTime, IsActive)  VALUES (\"Zambian Kwacha\",\"ZMK\",null,\"4810\",\"2011-08-06 05:33:04\",\"true\");");

		for (String insertSql : records_Currency) {
			db.execSQL(insertSql);
		}
	}
}
