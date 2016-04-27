package com.tokool.bra.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
	
	private final static int VERSION =1;

	public DatabaseHelper(Context context, String name) {
		super(context, name, null, VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql="CREATE TABLE steps(_id INTEGER PRIMARY KEY AUTOINCREMENT, totalSteps INT, targetSteps INT,"
				+ "calorie FLOAT, distance FLOAT, stepString VARCHAR, date VARCHAR)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
}
