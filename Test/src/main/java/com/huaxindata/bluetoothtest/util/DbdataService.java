package com.huaxindata.bluetoothtest.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbdataService extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "buletoothtest.db";
	private static final int DATABASE_VERSION = 1;

	private static DbdataService mService;

	private DbdataService(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public synchronized static DbdataService getService(Context context) {
		if (mService == null) {
			mService = new DbdataService(context);
		}
		return mService;
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL("CREATE TABLE IF NOT EXISTS testinfo"
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, vin VARCHAR,time LONG, statespeack INTEGER, statelisten INTEGER, upload TEXT,show INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
