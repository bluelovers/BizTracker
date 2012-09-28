/**
 * 
 */
package com.xiaolei.android.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.text.TextUtils;

import com.xiaolei.android.common.Utility;
import com.xiaolei.android.entity.BizLog;
import com.xiaolei.android.entity.BizLogSchema;
import com.xiaolei.android.entity.CurrencySchema;
import com.xiaolei.android.entity.LocationCacheSchema;
import com.xiaolei.android.entity.Parameter;
import com.xiaolei.android.entity.ParameterSchema;
import com.xiaolei.android.entity.PhotoSchema;
import com.xiaolei.android.entity.Project;
import com.xiaolei.android.entity.ProjectSchema;
import com.xiaolei.android.entity.Stuff;
import com.xiaolei.android.entity.StuffSchema;
import com.xiaolei.android.entity.TransactionPhoto;
import com.xiaolei.android.entity.TransactionProjectRelationSchema;
import com.xiaolei.android.entity.VoiceNote;
import com.xiaolei.android.entity.VoiceNoteSchema;
import com.xiaolei.android.preference.PreferenceHelper;
import com.xiaolei.android.preference.PreferenceKeys;

/**
 * @author xiaolei
 * 
 */
public class DataService {

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private static DataService instance;
	private static Boolean isOpened = false;
	private Context context;

	private DataService(Context context) {
		this.context = context;

		String activeUserId = PreferenceHelper.getActiveUserId(context);
		String activeUserDbFileName = PreferenceHelper.getUserDatabaseFileName(
				context, activeUserId);

		dbHelper = new DatabaseHelper(context, activeUserDbFileName);
	}

	public static DataService GetInstance(Context context) {
		if (instance == null) {
			instance = new DataService(context);
			instance.open();
		}

		return instance;
	}

	public void open() {
		if (!isOpened) {
			db = dbHelper.getWritableDatabase();
			isOpened = true;
		}
	}

	public void close() {
		db.close();
	}

	public long createStuff(Stuff stuff) {
		long result = 0;
		if (!stuffExists(stuff.getName())) {
			ContentValues values = new ContentValues();
			values.put(StuffSchema.Name, stuff.getName());
			values.put(StuffSchema.Tag, stuff.getTag());
			values.put(StuffSchema.Picture, stuff.getPicture());
			values.put(StuffSchema.LastUpdateTime,
					Utility.getCurrentDateTimeString());
			values.put(StuffSchema.LastUsedTime,
					Utility.getCurrentDateTimeString());

			result = db.insert(StuffSchema.TableName, null, values);
		} else {
			this.updateLastUsedTime(stuff.getName());
		}
		return result;
	}

	public Cursor getAllStuffs() {
		String sql = "select * from Stuff where IsActive = 'true' order by LastUsedTime desc";
		Cursor result = db.rawQuery(sql, null);

		return result;
	}

	public Cursor getAllStuffs(int count) {
		if (count <= 0) {
			return null;
		}
		String sql = "select * from Stuff where IsActive = 'true' order by LastUsedTime desc limit ?";
		Cursor result = db.rawQuery(sql,
				new String[] { Integer.toString(count) });

		return result;
	}

	public Cursor getAllStuffs(int limitCount, int offsetCount) {
		if (limitCount <= 0 || offsetCount < 0) {
			return null;
		}

		String sql = "select * from Stuff where IsActive = 'true' order by LastUsedTime desc limit ? offset ?";
		Cursor result = db.rawQuery(
				sql,
				new String[] { Integer.toString(limitCount),
						Integer.toString(offsetCount) });
		result.moveToFirst();

		return result;
	}

	/**
	 * Calculate the stuff page count by the specified page size.
	 * 
	 * @param pageSize
	 * @return
	 */
	public int calcStuffsPageCount(int pageSize) {
		if (pageSize <= 0) {
			return 0;
		}

		int pageCount = 0;
		String sql = "select count(_id) from Stuff where IsActive = 'true'";
		Cursor cursor = db.rawQuery(sql, null);
		try {
			if (cursor.moveToFirst()) {
				int totalCount = cursor.getInt(0);
				if (totalCount > 0) {
					pageCount = (int) (android.util.FloatMath
							.floor((totalCount * 1.0f) / (pageSize * 1.0f)) + ((totalCount >= pageSize)
							&& totalCount % pageSize == 0 ? 0 : 1));
				}
			}
		} finally {
			cursor.close();
		}

		return pageCount;
	}

	public int updateLastUsedTime(int stuffId) {
		ContentValues values = new ContentValues();
		values.put(StuffSchema.LastUsedTime, Utility.getCurrentDateTimeString());

		int result = db.update(StuffSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(stuffId) });
		return result;
	}

	public int updateLastUsedTime(String name) {
		ContentValues values = new ContentValues();
		values.put(StuffSchema.LastUsedTime, Utility.getCurrentDateTimeString());
		values.put(StuffSchema.IsActive, "true");

		int result = db.update(StuffSchema.TableName, values, "Name=?",
				new String[] { name });
		return result;
	}

	public int updateBizLogLocation(int bizLogId, String locationName,
			String locationXYZ) {
		ContentValues values = new ContentValues();
		values.put(BizLogSchema.LocationName, locationName);
		values.put(BizLogSchema.Location, locationXYZ);

		int result = db.update(BizLogSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(bizLogId) });
		return result;
	}

	/**
	 * Remove the location information of the specified transaction by
	 * transaction id.
	 * 
	 * @param transactionId
	 * @return
	 */
	public int removeTransactionLocation(long transactionId) {
		ContentValues values = new ContentValues();
		values.putNull(BizLogSchema.LocationName);
		values.putNull(BizLogSchema.Location);

		int result = db.update(BizLogSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(transactionId) });
		return result;
	}

	public int getTotalStuffCount() {
		int result = 0;
		String sql = "SELECT count(_id) from " + StuffSchema.TableName;
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor != null && cursor.moveToFirst()) {
			result = cursor.getInt(0);
		}

		return result;
	}

	/**
	 * Get total count of all existing transaction list.
	 * 
	 * @return
	 */
	public int getTransactionsTotalCount() {
		int result = 0;
		String sql = "Select count(_id) from BizLog";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor != null && cursor.getCount() > 0) {
			try {
				if (cursor.moveToFirst()) {
					result = cursor.getInt(0);
				}
			} finally {
				cursor.close();
				cursor = null;
			}
		}

		return result;
	}

	/**
	 * Get the date range of all the transactions.
	 * 
	 * @return Date[0] is the minimum date, Date[1] is the maximum date
	 */
	public Date[] getTransactionsDateRange() {
		Date[] result = new Date[2];
		String sql = "SELECT Min(LastUpdateTime) as MinTime, Max(LastUpdateTime) as MaxTime FROM bizlog";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor != null && cursor.getCount() > 0) {
			try {
				if (cursor.moveToFirst()) {
					result[0] = Utility.convertToDate(cursor.getString(0));
					result[1] = Utility.convertToDate(cursor.getString(1));
				}
			} finally {
				cursor.close();
				cursor = null;
			}
		} else {
			result = null;
		}

		return result;
	}

	/**
	 * Get transaction total income/expense/balance money
	 * 
	 * @return result[0] is total income, result[1] is total expense, result[2]
	 *         is total balance.
	 */
	public double[] getTransactionsTotalCost() {
		double[] result = new double[3];
		String sql = "SELECT ifnull((select sum(cost) FROM bizlog where cost > 0), 0) as TotalIncome, ifnull((SELECT sum(cost) FROM bizlog where cost <= 0), 0) as TotalExpense, ifnull((select sum(cost) from bizLog), 0) as Balance";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor != null && cursor.getCount() > 0) {
			try {
				if (cursor.moveToFirst()) {
					result[0] = cursor.getDouble(0);
					result[1] = cursor.getDouble(1);
					result[2] = cursor.getDouble(2);
				}
			} finally {
				cursor.close();
				cursor = null;
			}
		}

		return result;
	}

	public int getTransactionCountByStuffId(int stuffId) {
		int result = 0;
		String sql = "SELECT count(_id) from " + BizLogSchema.TableName
				+ " where StuffId=?";
		Cursor cursor = db.rawQuery(sql,
				new String[] { String.valueOf(stuffId) });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getInt(0);
			}

			cursor.close();
			cursor = null;
		}

		return result;
	}

	public double getAverageCostByStuffId(int stuffId) {
		double result = 0;
		String sql = "SELECT avg(Cost) from " + BizLogSchema.TableName
				+ " where StuffId=?";
		Cursor cursor = db.rawQuery(sql,
				new String[] { String.valueOf(stuffId) });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}

			cursor.close();
			cursor = null;
		}

		return result;
	}

	public Stuff getStuffById(int id) {
		Stuff result = null;
		Cursor cursor = db.query(true, StuffSchema.TableName, null, "_id=?",
				new String[] { String.valueOf(id) }, null, null, null, "1");
		if (cursor.moveToFirst()) {
			result = new Stuff();
			result.setId(id);

			int colIndex_Name = cursor.getColumnIndex(StuffSchema.Name);
			int colIndex_Picture = cursor.getColumnIndex(StuffSchema.Picture);
			int colIndex_LastUpdateTime = cursor
					.getColumnIndex(StuffSchema.LastUpdateTime);

			String name = cursor.getString(colIndex_Name);
			String picture = cursor.getString(colIndex_Picture);
			String lastUpdateTime = cursor.getString(colIndex_LastUpdateTime);

			result.setName(name);
			result.setPicture(picture);
			result.setId(id);
			result.setLastUpdateTime(Utility.parseDate(lastUpdateTime,
					new Date()));
		}

		return result;
	}

	public Stuff getStuffByName(String name) {
		if (TextUtils.isEmpty(name)) {
			return null;
		}

		Stuff result = null;
		Cursor cursor = db.query(true, StuffSchema.TableName, null, "Name=?",
				new String[] { name }, null, null, null, "1");
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = new Stuff();

				result.setName(name);
				result.setId(cursor.getInt(cursor
						.getColumnIndex(StuffSchema.Id)));
			}
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public Boolean stuffExists(int id) {
		Boolean result = false;
		String sql = "select count(_Id) from Stuff where _id=?";
		String[] selectionArgs = new String[] { String.valueOf(id) };
		Cursor cursor = db.rawQuery(sql, selectionArgs);

		if (cursor != null && cursor.moveToFirst()) {
			result = cursor.getInt(0) > 0;
		}

		return result;
	}

	public Boolean stuffExists(String name) {
		Boolean result = false;
		String sql = "select count(_Id) from Stuff where Name=?";
		String[] selectionArgs = new String[] { name };
		Cursor cursor = db.rawQuery(sql, selectionArgs);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getInt(0) > 0;
			}
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public int updateStuff(Stuff stuff) {
		ContentValues values = new ContentValues();
		values.put(StuffSchema.Name, stuff.getName());
		values.put(StuffSchema.Picture, stuff.getPicture());
		values.put(StuffSchema.LastUpdateTime,
				Utility.getCurrentDateTimeString());
		values.put(StuffSchema.Id, stuff.getId());

		int result = db.update(StuffSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(stuff.getId()) });
		return result;
	}

	public void saveStuff(Stuff stuff) {
		Boolean exists = this.stuffExists(stuff.getId());
		if (exists) {
			this.updateStuff(stuff);
		} else {
			this.createStuff(stuff);
		}
	}

	public void deactiveStuffById(int id) {
		String sql = "Update Stuff set IsActive = 'false' where _id = ?";
		db.execSQL(sql, new String[] { String.valueOf(id) });
	}

	public void deleteStuffById(int id) {
		String sql = "select count(_id) from BizLog where StuffId = ?";
		Cursor cursor = db.rawQuery(sql, new String[] { Integer.toString(id) });
		cursor.moveToFirst();
		int count = cursor.getInt(0);

		if (count == 0) {
			db.delete(StuffSchema.TableName, "_id=?",
					new String[] { String.valueOf(id) });
		} else {
			this.deactiveStuffById(id);
		}
	}

	public int deleteAllStuffs() {
		return db.delete(StuffSchema.TableName, null, null);
	}

	public Cursor searchStuff(String keyword, int limit) {
		if (TextUtils.isEmpty(keyword) || limit <= 0) {
			return null;
		}

		String sql = "select * from Stuff where Name like ? order by LastUpdateTime desc limit ?";
		Cursor result = db.rawQuery(sql, new String[] { "%" + keyword + "%",
				Integer.toString(limit) });

		return result;
	}

	public Cursor searchStuff(String keyword, int limit, int offset) {
		if (keyword == null || keyword.length() == 0 || limit <= 0) {
			return this.getAllStuffs(limit, offset);
		}

		String sql = "select * from Stuff where Name like ? order by LastUpdateTime desc limit ? offset ?";
		Cursor result = db.rawQuery(sql, new String[] { "%" + keyword + "%",
				Integer.toString(limit), Integer.toString(offset) });

		return result;
	}

	public Parameter getParameterByKey(String key) {
		Parameter result = new Parameter();
		result.setKey(key);
		result.setValue("");
		if (!TextUtils.isEmpty(key)) {
			String sql = String.format("select * from Parameter where %s = ?",
					ParameterSchema.Key);
			Cursor cursor = db.rawQuery(sql, new String[] { key });
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					result.setKey(key);
					result.setId(cursor.getInt(cursor
							.getColumnIndex(ParameterSchema.Id)));
					result.setValue(cursor.getString(cursor
							.getColumnIndex(ParameterSchema.Value)));
					result.setTag(cursor.getString(cursor
							.getColumnIndex(ParameterSchema.Tag)));
					String lastUpdateTime = cursor.getString(cursor
							.getColumnIndex(ParameterSchema.LastUpdateTime));
					result.setLastUpdateTime(Utility.parseDate(lastUpdateTime,
							new Date()));
				}
				cursor.close();
				cursor = null;
			}

		}

		return result;
	}

	public Parameter getParameterById(int id) {
		Parameter result = new Parameter();
		result.setId(id);
		result.setValue("");

		if (id > 0) {
			String sql = String.format("select * from Parameter where %s = ?",
					ParameterSchema.Id);
			Cursor cursor = db.rawQuery(sql,
					new String[] { String.valueOf(id) });
			if (cursor != null && cursor.moveToFirst()) {
				result.setKey(cursor.getString(cursor
						.getColumnIndex(ParameterSchema.Key)));
				result.setId(id);
				result.setValue(cursor.getString(cursor
						.getColumnIndex(ParameterSchema.Value)));
				result.setTag(cursor.getString(cursor
						.getColumnIndex(ParameterSchema.Tag)));
				String lastUpdateTime = cursor.getString(cursor
						.getColumnIndex(ParameterSchema.LastUpdateTime));
				result.setLastUpdateTime(Utility.parseDate(lastUpdateTime,
						new Date()));

				return result;
			}
		}
		return result;
	}

	public long addParameter(Parameter param) {
		if (param == null || param.isEmpty()) {
			return -1;
		}

		ContentValues values = new ContentValues();
		values.put(ParameterSchema.Key, param.getKey());
		values.put(ParameterSchema.Value, param.getValue());
		values.put(ParameterSchema.Tag, param.getTag());

		long result = db.insert(ParameterSchema.TableName, null, values);
		return result;
	}

	public void saveParameter(Parameter param) {
		if (param == null || param.isEmpty()) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put(ParameterSchema.Value, param.getValue());
		values.put(ParameterSchema.Tag, param.getTag());

		int result = db.update(ParameterSchema.TableName, values, "Key=?",
				new String[] { param.getKey() });
		if (result == 0) {
			addParameter(param);
		}
	}

	public Boolean parameterExists(String key) {
		Parameter param = getParameterByKey(key);
		return param != null && !param.isEmpty();
	}

	public long addTransaction(BizLog log) {
		ContentValues values = new ContentValues();
		values.put(BizLogSchema.StuffId, log.getStuffId());
		values.put(BizLogSchema.Cost, log.getCost());
		values.put(BizLogSchema.CurrencyCode, log.getCurrencyCode());
		int count = log.getStuffCount();
		if (count < 1) {
			count = 1;
		}
		values.put(BizLogSchema.StuffCount, count);
		if (log.getPrimaryPhotoId() > 0) {
			values.put(BizLogSchema.PrimaryPhotoId, log.getPrimaryPhotoId());
		}
		if (log.getPrimaryVoiceNoteId() > 0) {
			values.put(BizLogSchema.PrimaryVoiceNoteId,
					log.getPrimaryVoiceNoteId());
		}
		if (log.getLastUpdateTime() != null) {
			values.put(BizLogSchema.LastUpdateTime,
					Utility.getSqliteDateTimeString(log.getLastUpdateTime()));
		} else {
			values.put(BizLogSchema.LastUpdateTime,
					Utility.getSqliteDateTimeString(new Date()));
		}

		long result = db.insert(BizLogSchema.TableName, null, values);

		return result;
	}

	public int deleteBizLogById(long id) {
		return db.delete(BizLogSchema.TableName, "_id=?",
				new String[] { String.valueOf(id) });
	}

	public double getTodaySumPay() {
		double result = 0;
		String sql = "SELECT sum(cost) from BizLog where cost < 0 and LastUpdateTime between ? and ?";
		String defaultCurrencyCode = this.getDefaultCurrencyCode();
		double defaultCurrencyUSDExchangeRate = this
				.getUSDExchangeRate(defaultCurrencyCode);

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			sql = String
					.format("SELECT sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost * bl.StuffCount) / c.USDExchangeRate) * %s end) as TotalValue from BizLog bl left join Currency c on bl.CurrencyCode = c.Code where bl.cost < 0 and bl.LastUpdateTime between ? and ?",
							defaultCurrencyCode,
							String.valueOf(defaultCurrencyUSDExchangeRate));
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date today = cal.getTime();

		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		Date tomorrow = cal.getTime();

		Cursor cursor = db.rawQuery(
				sql,
				new String[] { Utility.getSqliteDateTimeString(today),
						Utility.getSqliteDateTimeString(tomorrow) });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public double getTodaySumEarn() {
		double result = 0;
		String sql = "SELECT sum(cost) from BizLog where cost > 0 and LastUpdateTime between ? and ?";
		String defaultCurrencyCode = this.getDefaultCurrencyCode();
		double defaultCurrencyUSDExchangeRate = this
				.getUSDExchangeRate(defaultCurrencyCode);

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			sql = String
					.format("SELECT sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost * bl.StuffCount) / c.USDExchangeRate) * %s end) as TotalValue from BizLog bl left join Currency c on bl.CurrencyCode = c.Code where bl.cost > 0 and bl.LastUpdateTime between ? and ?",
							defaultCurrencyCode,
							String.valueOf(defaultCurrencyUSDExchangeRate));
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date today = cal.getTime();

		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		Date tomorrow = cal.getTime();

		Cursor cursor = db.rawQuery(
				sql,
				new String[] { Utility.getSqliteDateTimeString(today),
						Utility.getSqliteDateTimeString(tomorrow) });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public void resetHistoryByDate(Date date) {
		String sql = "Delete from BizLog where LastUpdateTime between ? and ?";

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date today = cal.getTime();

		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		Date tomorrow = cal.getTime();

		db.execSQL(sql, new String[] { Utility.getSqliteDateTimeString(today),
				Utility.getSqliteDateTimeString(tomorrow) });
	}

	public Cursor getGroupedTransactionListByDay(Date date,
			String defaultCurrencyCode, double defaultCurrencyUSDExchangeRate) {
		String sql = "SELECT 1 as StuffCounts, bl.LastUpdateTime as FromTime, bl.LastUpdateTime as ToTime, bl.Cost as SumCost,  s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id where bl.LastUpdateTime between ? and ? "
				+ " and (select count(_id) from BizLog where StuffId = bl.StuffId) = 1 "

				+ " union "

				+ " select * from (SELECT count(bl.StuffId) as StuffCounts, min(bl.LastUpdateTime) as FromTime, max(bl.LastUpdateTime) as ToTime "
				+ String.format(
						" , sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost*bl.StuffCount) / c.USDExchangeRate) * %s end) as SumCost ",
						defaultCurrencyCode,
						String.valueOf(defaultCurrencyUSDExchangeRate))
				+ " , s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id left join Currency c on bl.CurrencyCode = c.Code where bl.LastUpdateTime between ? and ? group by bl.StuffId) as t where StuffCounts > 1"

				+ " order by ToTime desc ";

		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);

		Date nextDay = Utility.getEndTimeOfDate(date);

		Cursor cursor = db.rawQuery(
				sql,
				new String[] { Utility.getSqliteDateTimeString(date),
						Utility.getSqliteDateTimeString(nextDay),
						Utility.getSqliteDateTimeString(date),
						Utility.getSqliteDateTimeString(nextDay) });
		return cursor;
	}

	public Cursor getTransactionListByStuffId(long stuffId, Date date) {
		String sql = "SELECT s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id where bl.LastUpdateTime between ? and ? and bl.StuffId = ? order by bl.LastUpdateTime desc";

		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);

		Date nextDay = Utility.getEndTimeOfDate(date);

		Cursor cursor = db.rawQuery(
				sql,
				new String[] { Utility.getSqliteDateTimeString(date),
						Utility.getSqliteDateTimeString(nextDay),
						String.valueOf(stuffId) });
		return cursor;
	}

	public Cursor getBizLogByDay(Date date) {
		String sql = "SELECT s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id where bl.LastUpdateTime between ? and ? order by bl.LastUpdateTime desc";
		// String sql =
		// "SELECT s.Name as StuffName, bl.*, p.FileName FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id left join Photo p on p.BizLogId = bl._id where bl.LastUpdateTime between ? and ? order by bl.LastUpdateTime desc";

		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);

		Date nextDay = Utility.getEndTimeOfDate(date);

		Cursor cursor = db.rawQuery(
				sql,
				new String[] { Utility.getSqliteDateTimeString(date),
						Utility.getSqliteDateTimeString(nextDay) });
		return cursor;
	}

	public Cursor getTransactionListByDateRange(Date startDate, Date endDate) {
		Cursor result = null;
		if (startDate == null || endDate == null) {
			return result;
		}

		String sql = "SELECT s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id where bl.LastUpdateTime between ? and ? order by bl.LastUpdateTime desc";

		startDate.setHours(0);
		startDate.setMinutes(0);
		startDate.setSeconds(0);

		endDate.setHours(23);
		endDate.setMinutes(59);
		endDate.setSeconds(59);

		result = db.rawQuery(sql,
				new String[] { Utility.getSqliteDateTimeString(startDate),
						Utility.getSqliteDateTimeString(endDate) });

		return result;
	}

	public Cursor getBizLogByProjectId(long projectId) {
		String sql = "SELECT s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id"
				+ " where bl._id in (Select TransactionId from TransactionProjectRelation where ProjectId = ?) order by bl.LastUpdateTime desc";

		Cursor cursor = db.rawQuery(sql,
				new String[] { String.valueOf(projectId) });
		return cursor;
	}

	public double getTotalIncomeMoneyByProjectId(long projectId,
			String defaultCurrencyCode, double defaultCurrencyUSDExchangeRate) {
		double result = 0;

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			String sql = String
					.format("SELECT sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost * bl.StuffCount) / c.USDExchangeRate) * %s end) as TotalValue from BizLog bl left join Currency c on bl.CurrencyCode = c.Code where bl.cost > 0 and bl._id in (Select TransactionId from TransactionProjectRelation where ProjectId = ?)",
							defaultCurrencyCode,
							String.valueOf(defaultCurrencyUSDExchangeRate));

			Cursor cursor = db.rawQuery(sql,
					new String[] { String.valueOf(projectId) });
			if (cursor != null && cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}
		}

		return result;
	}

	public double getTotalCostMoneyByProjectId(long projectId,
			String defaultCurrencyCode, double defaultCurrencyUSDExchangeRate) {
		double result = 0;

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			String sql = String
					.format("SELECT sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost*bl.StuffCount) / c.USDExchangeRate) * %s end) as TotalValue from BizLog bl left join Currency c on bl.CurrencyCode = c.Code where bl.cost < 0 and bl._id in (Select TransactionId from TransactionProjectRelation where ProjectId = ?)",
							defaultCurrencyCode,
							String.valueOf(defaultCurrencyUSDExchangeRate));

			Cursor cursor = db.rawQuery(sql,
					new String[] { String.valueOf(projectId) });
			if (cursor != null && cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}
		}

		return result;
	}

	public int getTotalTransactionCountByProjectId(long projectId) {
		int result = 0;

		String sql = "SELECT count(bl._id) FROM BizLog as bl where bl._id in (Select TransactionId from TransactionProjectRelation where ProjectId = 1)";
		Cursor cursor = db.rawQuery(sql,
				new String[] { String.valueOf(projectId) });
		if (cursor != null && cursor.moveToFirst()) {
			result = cursor.getInt(0);
		}

		return result;
	}

	public Cursor getAllTransactions() {
		String sql = "SELECT s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id order by bl.LastUpdateTime desc";

		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}

	public Cursor getTransactionsForExport() {
		String sql = "SELECT s.Name as StuffName, bl.Cost, bl.StuffCount, bl.CurrencyCode, bl.LastUpdateTime, bl.Star FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id order by bl.LastUpdateTime desc";

		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}

	public Cursor searchBizLog(String keyword) {
		String sql = "SELECT s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id where s.Name like ? or bl.Comment like ? order by bl.LastUpdateTime desc";

		String arg = "%" + keyword + "%";
		Cursor cursor = db.rawQuery(sql, new String[] { arg, arg });
		return cursor;
	}

	public enum TransactionType {
		Unknown, Income, Expense
	}

	public enum ComparationType {
		Unknown, Minimum, Maximum
	}

	public Cursor getTransaction(Date startDate, Date endDate,
			TransactionType transactionType, ComparationType comparationType) {
		Cursor result = null;
		String sql = "";

		String defaultCurrencyCode = this.getDefaultCurrencyCode();
		double defaultCurrencyUSDExchangeRate = this
				.getUSDExchangeRate(defaultCurrencyCode);

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			sql = "SELECT * from ("
					+ String.format(
							"SELECT bl.*, case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost else ((bl.Cost * bl.StuffCount) / c.USDExchangeRate) * %s end as CostOfDefaultCurrency from BizLog bl left join Currency c on bl.CurrencyCode = c.Code where bl.cost "
									+ (transactionType == TransactionType.Income ? ">"
											: "<")
									+ " 0 and bl.LastUpdateTime between ? and ?",
							defaultCurrencyCode, String
									.valueOf(defaultCurrencyUSDExchangeRate))
					+ ") order by CostOfDefaultCurrency "
					+ (comparationType == ComparationType.Minimum ? "asc"
							: "desc") + " limit 1";
		}

		String sd = Utility.getSqliteDateTimeString(startDate);
		String ed = Utility.getSqliteDateTimeString(endDate);
		result = db.rawQuery(sql, new String[] { sd, ed });

		return result;
	}

	/**
	 * Gets the total money of the matched transaction list with the given
	 * keyword.
	 * 
	 * @param keyword
	 *            Search keyword
	 * @param transactionType
	 * @return
	 */
	public double getTotalMoneyOfSearchedTransactions(String keyword,
			TransactionType transactionType) {
		double result = 0;
		if (TextUtils.isEmpty(keyword)
				|| transactionType == TransactionType.Unknown) {
			return result;
		}

		String sql = "";
		String defaultCurrencyCode = this.getDefaultCurrencyCode();
		double defaultCurrencyUSDExchangeRate = this
				.getUSDExchangeRate(defaultCurrencyCode);

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			sql = String
					.format("SELECT sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost * bl.StuffCount) / c.USDExchangeRate) * %s end) as TotalValue from BizLog bl left join Currency c on bl.CurrencyCode = c.Code left join Stuff s on bl.StuffId = s._Id where bl.cost "
							+ (transactionType == TransactionType.Income ? ">"
									: "<")
							+ " 0 and (s.Name like ? or bl.Comment like ?) ",
							defaultCurrencyCode, String
									.valueOf(defaultCurrencyUSDExchangeRate));
		}

		String searchKeyword = "%" + keyword + "%";
		Cursor cursor = db.rawQuery(sql, new String[] { searchKeyword,
				searchKeyword });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public double getTotalMoneyOfTransactionList(
			ArrayList<Long> transactionIds, TransactionType transactionType) {
		double result = 0d;
		if (transactionIds == null || transactionIds.size() == 0) {
			return result;
		}

		StringBuilder text = new StringBuilder();
		for (Long id : transactionIds) {
			if (text.length() > 0) {
				text.append("," + Long.toString(id));
			} else {
				text.append(Long.toString(id));
			}
		}

		String sql = "";
		String defaultCurrencyCode = this.getDefaultCurrencyCode();
		double defaultCurrencyUSDExchangeRate = this
				.getUSDExchangeRate(defaultCurrencyCode);

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			sql = String
					.format("SELECT sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost * bl.StuffCount) / c.USDExchangeRate) * %s end) as TotalValue from BizLog bl left join Currency c on bl.CurrencyCode = c.Code left join Stuff s on bl.StuffId = s._Id where bl.cost "
							+ (transactionType == TransactionType.Income ? ">"
									: "<")
							+ " 0 and bl._id in ("
							+ text.toString() + ")", defaultCurrencyCode,
							String.valueOf(defaultCurrencyUSDExchangeRate));
		}

		Cursor cursor = db.rawQuery(sql, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public double getTotalMoneyOfFavouriteTransactions(
			TransactionType transactionType) {
		double result = 0;
		if (transactionType == TransactionType.Unknown) {
			return result;
		}

		String sql = "";
		String defaultCurrencyCode = this.getDefaultCurrencyCode();
		double defaultCurrencyUSDExchangeRate = this
				.getUSDExchangeRate(defaultCurrencyCode);

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			sql = String
					.format("SELECT sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost * bl.StuffCount) / c.USDExchangeRate) * %s end) as TotalValue from BizLog bl left join Currency c on bl.CurrencyCode = c.Code where bl.cost "
							+ (transactionType == TransactionType.Income ? ">"
									: "<") + " 0 and Star = 'true'",
							defaultCurrencyCode,
							String.valueOf(defaultCurrencyUSDExchangeRate));
		}

		Cursor cursor = db.rawQuery(sql, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public double getTotalPay(Date startDate, Date endDate) {
		double result = 0;

		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		startDate = cal.getTime();

		cal.setTime(endDate);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);

		endDate = cal.getTime();

		String sql = "SELECT sum(cost) FROM BizLog where Cost < 0 and LastUpdateTime between ? and ?";
		String defaultCurrencyCode = this.getDefaultCurrencyCode();
		double defaultCurrencyUSDExchangeRate = this
				.getUSDExchangeRate(defaultCurrencyCode);

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			sql = String
					.format("SELECT sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost * bl.StuffCount) / c.USDExchangeRate) * %s end) as TotalValue from BizLog bl left join Currency c on bl.CurrencyCode = c.Code where bl.cost < 0 and bl.LastUpdateTime between ? and ?",
							defaultCurrencyCode,
							String.valueOf(defaultCurrencyUSDExchangeRate));
		}

		String sd = Utility.getSqliteDateTimeString(startDate);
		String ed = Utility.getSqliteDateTimeString(endDate);
		Cursor cursor = db.rawQuery(sql, new String[] { sd, ed });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public double getTotalEarn(Date startDate, Date endDate) {
		double result = 0;
		if (startDate == null || endDate == null) {
			return result;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		startDate = cal.getTime();

		cal.setTime(endDate);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);

		endDate = cal.getTime();

		String sql = "SELECT sum(cost) FROM BizLog where Cost > 0 and LastUpdateTime between ? and ?";
		String defaultCurrencyCode = this.getDefaultCurrencyCode();
		double defaultCurrencyUSDExchangeRate = this
				.getUSDExchangeRate(defaultCurrencyCode);

		if (!TextUtils.isEmpty(defaultCurrencyCode)
				&& defaultCurrencyUSDExchangeRate > 0) {
			sql = String
					.format("SELECT sum(case when bl.CurrencyCode = \"%s\" or bl.CurrencyCode is null then bl.Cost*bl.StuffCount else ((bl.Cost * bl.StuffCount) / c.USDExchangeRate) * %s end) as TotalValue from BizLog bl left join Currency c on bl.CurrencyCode = c.Code where bl.cost > 0 and bl.LastUpdateTime between ? and ?",
							defaultCurrencyCode,
							String.valueOf(defaultCurrencyUSDExchangeRate));
		}

		Cursor cursor = db.rawQuery(sql,
				new String[] { Utility.getSqliteDateTimeString(startDate),
						Utility.getSqliteDateTimeString(endDate) });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}
			cursor.close();
			cursor = null;
		}
		return result;
	}

	public void addStar(long bizLogId) {
		String sql = "Update BizLog set Star = 'true' where _id = ?";
		db.execSQL(sql, new String[] { String.valueOf(bizLogId) });
	}

	public void removeStar(long bizLogId) {
		String sql = "Update BizLog set Star = 'false' where _id = ?";
		db.execSQL(sql, new String[] { String.valueOf(bizLogId) });
	}

	public void reverseStar(long transactionId) {
		Cursor cursor = db.rawQuery("select star from BizLog where _id=?",
				new String[] { String.valueOf(transactionId) });
		if (cursor.moveToFirst()) {
			String star = cursor.getString(cursor.getColumnIndex("Star"));
			cursor.close();
			cursor = null;

			if ("true".equals(star)) {
				star = "false";
			} else {
				star = "true";
			}
			String sql = "Update BizLog set Star = ? where _id = ?";
			db.execSQL(sql,
					new String[] { star, String.valueOf(transactionId) });
		}
	}

	public Cursor getStarredBizLog() {
		String sql = "SELECT s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id where Star = 'true' order by bl.LastUpdateTime desc";

		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}

	public Cursor getAllExchangeRate() {
		String sql = "SELECT * FROM Currency where IsActive = 'true' order by Code asc";

		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}

	public double getUSDExchangeRate(String currencyCode) {
		if (TextUtils.isEmpty(currencyCode)) {
			return 0;
		}
		double result = 0;

		String sql = "Select USDExchangeRate from Currency where Code = ?";
		Cursor cursor = db.rawQuery(sql, new String[] { currencyCode });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getDouble(0);
			}

			cursor.close();
			cursor = null;
		}

		return result;
	}

	public void updateEmptyCurrencyCodeToDefaultCurrencyCode() {
		String defaultCurrencyCode = "";
		SharedPreferences prefs = PreferenceHelper
				.getActiveUserSharedPreferences(context);
		if (prefs != null) {
			defaultCurrencyCode = prefs.getString(
					PreferenceKeys.DefaultCurrencyCode, "");
		}

		if (!TextUtils.isEmpty(defaultCurrencyCode)) {
			String sql = String
					.format("Update BizLog set CurrencyCode = \"%s\" where CurrencyCode is null",
							defaultCurrencyCode);
			db.execSQL(sql);
		}
	}

	public String getDefaultCurrencyCode() {
		String defaultCurrencyCode = "";
		SharedPreferences prefs = PreferenceHelper
				.getActiveUserSharedPreferences(context);
		if (prefs != null) {
			defaultCurrencyCode = prefs.getString(
					PreferenceKeys.DefaultCurrencyCode, "");
		}

		return defaultCurrencyCode != null ? defaultCurrencyCode : "";
	}

	public void changeBizLogCurrencyCode(String originalCode, String newCode) {
		if (TextUtils.isEmpty(originalCode)) {
			return;
		}

		String sql = "Update BizLog set CurrencyCode = ? where CurrencyCode = ?";
		db.execSQL(sql, new String[] { newCode, originalCode });
	}

	public void dropTable(String tableName) {
		String sql = String.format("drop table %s", tableName);
		db.execSQL(sql);
	}

	public void updateExchangeRateById(long id, double newValue) {
		if (newValue > 0) {
			String sql = "Update Currency set USDExchangeRate = ?, LastUpdateTime = ? where _id = ?";
			db.execSQL(
					sql,
					new String[] { String.valueOf(newValue),
							Utility.getSqliteDateTimeString(new Date()),
							String.valueOf(id) });
		}
	}

	/**
	 * Update exchange rate by currency code.
	 * 
	 * @param currencyCode
	 * @param exchangeRate
	 * @return
	 */
	public int updateExchangeRateByCurrencyCode(String currencyCode,
			double exchangeRate, Date updateTime) {
		int result = -1;
		if (!TextUtils.isEmpty(currencyCode)) {
			ContentValues values = new ContentValues();
			values.put(CurrencySchema.USDExchangeRate, exchangeRate);
			values.put(CurrencySchema.LastUpdateTime,
					Utility.getSqliteDateTimeString(updateTime));

			result = db.update(CurrencySchema.TableName, values, "Code=?",
					new String[] { currencyCode });
		}
		return result;
	}

	public int updateBizLog(BizLog log) {
		if (log == null) {
			return -1;
		}

		ContentValues values = new ContentValues();
		values.put(BizLogSchema.StuffId, log.getStuffId());
		values.put(BizLogSchema.Star, log.getStar());
		values.put(BizLogSchema.CurrencyCode, log.getCurrencyCode());
		values.put(BizLogSchema.Cost, log.getCost());
		values.put(BizLogSchema.Comment, log.getComment());
		values.put(BizLogSchema.LastUpdateTime,
				Utility.getCurrentDateTimeString());

		int result = db.update(BizLogSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(log.getId()) });
		return result;
	}

	/**
	 * Update the location of the specified transaction.
	 * 
	 * @param transactionId
	 * @param location
	 * @param address
	 * @return
	 */
	public int updateTransactionLocation(long transactionId, Location location,
			String address) {
		if (transactionId <= 0) {
			return -1;
		}

		ContentValues values = new ContentValues();
		values.put(BizLogSchema.LocationName, address);
		if (location != null) {
			values.put(
					BizLogSchema.Location,
					String.format("%f,%f", location.getLatitude(),
							location.getLongitude()));
		}

		int result = db.update(BizLogSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(transactionId) });
		return result;
	}

	public int updatePrimaryPhoto(long transactionId, long photoId) {
		ContentValues values = new ContentValues();
		values.put(BizLogSchema.PrimaryPhotoId, photoId);

		int result = db.update(BizLogSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(transactionId) });
		return result;
	}

	public int getPrimaryPhotoPosition(long transactionId) {
		int result = 0;

		int primaryPhotoId = getPrimaryPhotoId(transactionId);
		if (primaryPhotoId > 0) {
			Cursor cursor = getTransactionPhotos(transactionId);
			try {
				if (cursor.moveToFirst()) {
					int index = 0;
					do {
						int photoId = cursor.getInt(cursor
								.getColumnIndex(PhotoSchema.Id));
						if (photoId == primaryPhotoId) {
							result = index;
							break;
						}
						index++;
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
				cursor = null;
			}
		}

		return result;
	}

	public int getPrimaryPhotoId(long transactionId) {
		int result = -1;
		String sql = "Select PrimaryPhotoId from BizLog where _id=?";
		Cursor cursor = db.rawQuery(sql,
				new String[] { String.valueOf(transactionId) });
		try {
			if (cursor.getCount() > 0) {
				if (cursor.moveToFirst()) {
					result = cursor.getInt(0);
				}
			}
		} finally {
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public int updatePrimaryVoiceNote(long transactionId, long voiceNoteId) {
		ContentValues values = new ContentValues();
		values.put(BizLogSchema.PrimaryVoiceNoteId, voiceNoteId);

		int result = db.update(BizLogSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(transactionId) });
		return result;
	}

	public int updateBizLog(long bizLogId, String stuffName, double cost,
			String currencyCode, Date updateTime) {
		if (bizLogId <= 0 || TextUtils.isEmpty(stuffName)
				|| TextUtils.isEmpty(currencyCode)) {
			return -1;
		}

		Stuff stuff = this.getStuffByName(stuffName);
		if (stuff == null) {
			stuff = new Stuff();
			stuff.setName(stuffName);
			stuff.setLastUsedTime(new Date());
			stuff.setLastUpdateTime(new Date());

			long newId = this.createStuff(stuff);
			stuff.setId((int) newId);
		}

		ContentValues values = new ContentValues();
		values.put(BizLogSchema.StuffId, stuff.getId());
		values.put(BizLogSchema.Cost, cost);
		values.put(BizLogSchema.CurrencyCode, currencyCode);
		values.put(BizLogSchema.LastUpdateTime,
				Utility.getSqliteDateTimeString(updateTime));

		int result = db.update(BizLogSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(bizLogId) });
		return result;
	}

	public int updateBizLogComment(long bizLogId, String comment,
			Boolean needUpdateTime) {
		ContentValues values = new ContentValues();
		values.put(BizLogSchema.Comment, comment);
		if (needUpdateTime) {
			values.put(BizLogSchema.LastUpdateTime,
					Utility.getSqliteDateTimeString(new Date()));
		}

		int result = db.update(BizLogSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(bizLogId) });
		return result;
	}

	public Cursor getAllProjects() {
		String sql = "Select * from Project where IsActive=? order by LastUpdateTime desc";
		return db.rawQuery(sql, new String[] { "true" });
	}

	public Cursor getProjects(long transactionId) {
		if (transactionId <= 0) {
			String sql = "Select _id, -1 as RelationId, 0 as IsChecked, Name from Project where IsActive=? order by LastUpdateTime desc";
			return db.rawQuery(sql, new String[] { "true" });
		} else {
			String sql = "select p._id, tpr._id as RelationId, case when tpr.TransactionId is null then 0 else 1 end as IsChecked, p.Name from Project as p left join TransactionProjectRelation as tpr on p._id = tpr.ProjectId where p.IsActive = ? and tpr.TransactionId = ? or tpr.TransactionId is null order by p.LastUpdateTime desc";
			return db.rawQuery(sql,
					new String[] { "true", String.valueOf(transactionId) });
		}
	}

	public long addProject(Project project) {
		if (project == null) {
			return -1;
		}

		long result = -1;
		if (TextUtils.isEmpty(project.getName())) {
			return result;
		}

		if (!projectExists(project.getName())) {
			ContentValues values = new ContentValues();
			values.put(ProjectSchema.Name, project.getName());
			values.put(ProjectSchema.Description, project.getDescription());
			values.put(ProjectSchema.Tag, project.getTag());
			values.put(ProjectSchema.CreatedTime,
					Utility.getSqliteDateTimeString(new Date()));

			result = db.insert(ProjectSchema.TableName, null, values);
			project.setId(result);
		}

		return result;
	}

	public int updateProject(Project project) {
		if (project == null) {
			return -1;
		}

		ContentValues values = new ContentValues();
		values.put(ProjectSchema.Name, project.getName());
		values.put(ProjectSchema.Description, project.getDescription());
		values.put(ProjectSchema.Tag, project.getTag());
		values.put(ProjectSchema.LastUpdateTime,
				Utility.getCurrentDateTimeString());

		int result = db.update(ProjectSchema.TableName, values, "_id=?",
				new String[] { String.valueOf(project.getId()) });
		return result;
	}

	public boolean projectExists(String projectName) {
		String sql = "Select _id from Project where Name=?";
		Cursor cursor = db.rawQuery(sql, new String[] { projectName });
		boolean result = false;
		try {
			if (cursor != null) {
				result = cursor.getCount() > 0;
			}
		} finally {
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public Boolean isProjectInUsed(long projectId) {
		String sql = "Select _id from BizLog where Tag like ?";
		Cursor cursor = db
				.rawQuery(sql, new String[] { "%" + projectId + "%" });
		if (cursor != null) {
			int count = cursor.getCount();
			return count > 0;
		} else {
			return false;
		}
	}

	public void deleteProject(long projectId) {
		if (!isProjectInUsed(projectId)) {
			String sql = "delete from Project where _id=?";
			db.execSQL(sql, new String[] { String.valueOf(projectId) });
		} else {
			String sql = "update Project set IsActive=\"false\" where _id=?";
			db.execSQL(sql, new String[] { String.valueOf(projectId) });
		}
	}

	public BizLog getTransactionDetailsById(long transactionId) {
		BizLog result = null;
		String sql = "SELECT s.Name as StuffName, bl.* FROM BizLog as bl left join Stuff s on bl.StuffId = s._Id where bl._id=?";

		Cursor cursor = db.rawQuery(sql,
				new String[] { String.valueOf(transactionId) });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = new BizLog();
				result.setId(cursor.getLong(cursor
						.getColumnIndex(BizLogSchema.Id)));
				result.setStuffName(cursor.getString(cursor
						.getColumnIndex("StuffName")));
				result.setComment(cursor.getString(cursor
						.getColumnIndex(BizLogSchema.Comment)));
				result.setCost(cursor.getDouble(cursor
						.getColumnIndex(BizLogSchema.Cost)));
				result.setLastUpdateTime(Utility.parseDate(cursor
						.getString(cursor
								.getColumnIndex(BizLogSchema.LastUpdateTime)),
						new Date()));
				String star = cursor.getString(cursor
						.getColumnIndex(BizLogSchema.Star));
				result.setStar("true".equals(star));
				result.setLocation(cursor.getString(cursor
						.getColumnIndex(BizLogSchema.Location)));
				result.setLocationName(cursor.getString(cursor
						.getColumnIndex(BizLogSchema.LocationName)));
				result.setStuffCount(cursor.getInt(cursor
						.getColumnIndex(BizLogSchema.StuffCount)));
				result.setPrimaryPhotoId(cursor.getInt(cursor
						.getColumnIndex(BizLogSchema.PrimaryPhotoId)));
				result.setPrimaryVoiceNoteId(cursor.getInt(cursor
						.getColumnIndex(BizLogSchema.PrimaryVoiceNoteId)));
			}
			cursor.close();
			cursor = null;
		}

		return result;
	}

	public Cursor getTransactionPhotos(long transactionId) {
		String sql = "select * from Photo where BizLogId=? order by CreatedTime desc";
		Cursor result = db.rawQuery(sql,
				new String[] { String.valueOf(transactionId) });

		return result;
	}

	public int removeTransactionPhotoRelation(long id) {
		return db.delete(PhotoSchema.TableName, "_id=?",
				new String[] { String.valueOf(id) });
	}

	public int getTransactionPhotoCount(long transactionId) {
		int result = 0;
		String sql = "select count(_id) as PhotoCount from Photo where BizLogId=? order by CreatedTime desc";
		Cursor cursor = db.rawQuery(sql,
				new String[] { String.valueOf(transactionId) });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				result = cursor.getInt(0);
			}

			cursor.close();
			cursor = null;
		}

		return result;
	}

	public long addTransactionPhoto(TransactionPhoto photo) {
		if (photo == null) {
			return -1;
		}

		ContentValues values = new ContentValues();
		values.put(PhotoSchema.BizLogId, photo.getBizLogId());
		values.put(PhotoSchema.FileName, photo.getFileName());
		values.put(PhotoSchema.Name, photo.getName());
		values.put(PhotoSchema.Comment, photo.getComment());
		values.put(PhotoSchema.Tag, photo.getTag());
		values.put(PhotoSchema.Thumbnail, photo.getThumbnail());
		values.put(PhotoSchema.CreatedTime,
				Utility.getSqliteDateTimeString(photo.getCreatedTime()));

		long result = db.insert(PhotoSchema.TableName, null, values);
		photo.setId(result);

		return result;
	}

	public int deleteTransactionProjectRelation(long relationId) {
		return db.delete(TransactionProjectRelationSchema.TableName, "_id=?",
				new String[] { String.valueOf(relationId) });
	}

	public long addTransactionProjectRelation(long transactionId, long projectId) {
		long result = -1;
		if (transactionId <= 0 || projectId <= 0) {
			return result;
		}

		String sqlCheckIfExists = "Select count(_id) from TransactionProjectRelation where TransactionId = ? and ProjectId = ?";
		Cursor cursor = db.rawQuery(
				sqlCheckIfExists,
				new String[] { String.valueOf(transactionId),
						String.valueOf(projectId) });
		Boolean exists = false;
		if (cursor != null && cursor.moveToFirst()) {
			exists = cursor.getInt(0) > 0;
		}

		if (exists == false) {
			ContentValues values = new ContentValues();
			values.put(TransactionProjectRelationSchema.TransactionId,
					transactionId);
			values.put(TransactionProjectRelationSchema.ProjectId, projectId);

			result = db.insert(TransactionProjectRelationSchema.TableName,
					null, values);
		}
		return result;
	}

	public Cursor getAllVoiceNotes(long transactionId) {
		String sql = "select * from VoiceNote where TransactionId=? order by LastUpdatedTime desc";
		return db.rawQuery(sql, new String[] { String.valueOf(transactionId) });
	}

	public long addVoiceNote(VoiceNote voiceNote) {
		long newId = 0;
		if (voiceNote == null) {
			return newId;
		}

		ContentValues values = new ContentValues();
		values.put(VoiceNoteSchema.FileName, voiceNote.getFileName());
		values.put(VoiceNoteSchema.Duration, voiceNote.getDuration());
		values.put(VoiceNoteSchema.Summary, voiceNote.getSummary());
		values.put(VoiceNoteSchema.Title, voiceNote.getTitle());
		if (voiceNote.getTransactionId() > 0) {
			values.put(VoiceNoteSchema.TransactionId,
					voiceNote.getTransactionId());
		}
		values.put(VoiceNoteSchema.Tag, voiceNote.getTag());
		values.put(VoiceNoteSchema.LastUpdatedTime,
				Utility.getSqliteDateTimeString(new Date()));

		newId = db.insert(VoiceNoteSchema.TableName, null, values);
		if (newId > 0) {
			setPrimaryVoiceNote(voiceNote.getTransactionId(), newId);
		}

		return newId;
	}

	public void deleteVoiceNote(long voiceNoteId) {
		if (voiceNoteId <= 0) {
			return;
		}

		ContentValues values = new ContentValues();
		values.putNull(BizLogSchema.PrimaryVoiceNoteId);
		db.update(BizLogSchema.TableName, values, "PrimaryVoiceNoteId=?",
				new String[] { String.valueOf(voiceNoteId) });

		String fullFileName = "";
		Cursor cursor = db.query(VoiceNoteSchema.TableName,
				new String[] { VoiceNoteSchema.FileName }, "_id=?",
				new String[] { String.valueOf(voiceNoteId) }, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				String fileName = cursor.getString(cursor
						.getColumnIndex(VoiceNoteSchema.FileName));
				cursor.close();
				cursor = null;

				if (!TextUtils.isEmpty(fileName)) {
					fullFileName = Utility.getAudioFullFileName(context,
							fileName);
					File file = new File(fullFileName);
					if (file.exists()) {
						file.delete();
					}
				}
			}
		}

		db.delete(VoiceNoteSchema.TableName, "_id=?",
				new String[] { String.valueOf(voiceNoteId) });
	}

	public int setPrimaryVoiceNote(long transactionId, long voiceNoteId) {
		ContentValues values = new ContentValues();
		values.put(BizLogSchema.PrimaryVoiceNoteId, voiceNoteId);

		int result = db.update(BizLogSchema.TableName, values, "_Id=?",
				new String[] { String.valueOf(transactionId) });
		return result;
	}

	public String getAddressFormLocationCache(double latitude, double longitude) {
		String result = "";

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, 30);
		Date minDate = cal.getTime();

		String sql = "SELECT Address from LocationCache where Latitude=? and Longitude=? and UpdatedTime >=?";
		Cursor cursor = db.rawQuery(
				sql,
				new String[] { String.valueOf(latitude),
						String.valueOf(longitude),
						Utility.getSqliteDateTimeString(minDate) });
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			result = cursor.getString(0);
		}

		cursor.close();
		cursor = null;

		return result;
	}

	public long updateAddressToLocationCache(double latitude, double longitude,
			String address) {
		db.delete(
				LocationCacheSchema.TableName,
				"Latitude=? and Longitude=?",
				new String[] { String.valueOf(latitude),
						String.valueOf(longitude) });

		ContentValues values = new ContentValues();
		values.put(LocationCacheSchema.Latitude, latitude);
		values.put(LocationCacheSchema.Longitude, longitude);
		values.put(LocationCacheSchema.Address, address);
		values.put(LocationCacheSchema.UpdatedTime,
				Utility.getSqliteDateTimeString(new Date()));

		return db.insert(LocationCacheSchema.TableName, null, values);
	}
}
