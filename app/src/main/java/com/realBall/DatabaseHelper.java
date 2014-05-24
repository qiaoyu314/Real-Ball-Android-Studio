package com.realBall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for managing the DB
 * @author Yu, Marty and Lingchen
 *
 */

public class DatabaseHelper {
	   private static final String DATABASE_NAME = "ball.db";
	   private static final int DATABASE_VERSION = 1;
	   private static final String TABLE_NAME = "Accounts";
	   private Context context;
	   private SQLiteDatabase db;
	   private RealBallOpenHelper openHelper;
	   private SQLiteStatement insertStmt;
	   //sql for insert a new row into DB
	   private static final String INSERT = "insert into " + TABLE_NAME + "(name, password, level) values (?, ?, ?)" ;
	   /**
	    * constructor
	    * @param context
	    */
	   public DatabaseHelper(Context context) {
	      this.context = context;
	      openHelper = new RealBallOpenHelper(this.context);
	      
	   }
	   /**
	    * open the writable database
	    */
	   public void open() {
		   this.db = openHelper.getWritableDatabase();
	   }
	   /**
	    * insert a new user row into db
	    * @param name
	    * @param password
	    * @return
	    */
	   public long insert(String name, String password) {
		  this.insertStmt = this.db.compileStatement(INSERT);
	      this.insertStmt.bindString(1, name);
	      this.insertStmt.bindString(2, password);
	      this.insertStmt.bindString(3, "1");
	      return this.insertStmt.executeInsert();
	   }
	   
	   /**
	    * update the level record of the user
	    */
	   public void updateLevel(String username) {
		   Cursor cursor = this.db.query(TABLE_NAME, new String[] { "name",
					"level" }, "name = '" + username + "'", null, null, null, null);
		   int index;
		   index = cursor.getColumnIndex("level");
		   if(index>=0){
			   cursor.moveToFirst();
			   String oldLevel = cursor.getString(index);
			   ContentValues values = new ContentValues();
			   //increase the level by 1
			   int level_p = Integer.parseInt(oldLevel) + 1;
			   String newLevel = Integer.toString(level_p);
			   values.put("level", newLevel);
			   db.update(TABLE_NAME, values, "name= '" + username + "'", null);
			   cursor.close();
		   }
	   }
	   /**
	    * close db
	    */
	   public void close(){
		   db.close();
	   }
	   /**
	    * delete db
	    */
	   public void deleteAll() {

	      this.db.delete(TABLE_NAME, null, null);
	   }
	   /**
	    * Check if there's a row containing the username and password, if there is, add them into the list.
	    * @param username
	    * @param password
	    * @return
	    */
	   public List<String> selectAll(String username, String password) {
	      List<String> list = new ArrayList<String>();
	      Cursor cursor = this.db.query(TABLE_NAME, new String[] { "name", "password", "level" }, "name = '"+ username +"' AND password= '"+ password+"'", null, null, null, "name desc");
	      if (cursor.moveToFirst()) {
	        do {
	        	 list.add(cursor.getString(0));
	        	 list.add(cursor.getString(1));
	        	 list.add(cursor.getString(2));
	         } while (cursor.moveToNext()); 
	      }
	      if (cursor != null && !cursor.isClosed()) {
	         cursor.close();
	      }
	      return list;
	   }
	   
	   /**
	    * Check if there's a row containing the username, if there is, add them into the list.
	    * @param username
	    * @return
	    */
	   public List<String> selectName(String username) {
		   List<String> list = new ArrayList<String>();
		   Cursor cursor = this.db.query(TABLE_NAME, new String[] { "name" }, "name = '" + username + "'", null, null, null, null);
		   if (cursor.moveToFirst()) {
			   do {
				   list.add(cursor.getString(0));
			   } while (cursor.moveToNext());
		   }
		   if (cursor != null && !cursor.isClosed()) {
			   cursor.close();
		   }
		   
		   return list;
	   }
	   
	   private static class RealBallOpenHelper extends SQLiteOpenHelper {
		   RealBallOpenHelper(Context context) {
	    	  super(context, DATABASE_NAME, null, DATABASE_VERSION);
	      }

		  /**
		   * create db
		   */
	      @Override
	      public void onCreate(SQLiteDatabase db) {
	         db.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY, name TEXT, password TEXT, level TEXT)");
	      }

	      /**
	       * upgrade db
	       */
	      @Override
	      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	         Log.w("Example", "Upgrading database; this will drop and recreate the tables.");
	         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	         onCreate(db);
	      }
	   }
	}