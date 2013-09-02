package com.alvarosantisteban.berlincurator.utils;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alvarosantisteban.berlincurator.Event;
import com.alvarosantisteban.berlincurator.R;
import com.alvarosantisteban.berlincurator.R.raw;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "berlincurator.db";
	// any time you make changes to your database objects, you may have to increase the database version
	//private static final int DATABASE_VERSION = 1;
	private static final int DATABASE_VERSION = 3;

	// the DAO object we use to access the Events table
	private Dao<Event, Integer> eventDao = null;
	private RuntimeExceptionDao<Event, Integer> eventRuntimeDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
		//super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Event.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}

		// here we try inserting data in the on-create as a test
		RuntimeExceptionDao<Event, Integer> dao = getEventDataDao();
		long millis = System.currentTimeMillis();
		// create some entries in the onCreate
		Event simple = new Event();
		dao.create(simple);
		simple = new Event();
		dao.create(simple);
		Log.i(DatabaseHelper.class.getName(), "created new entries in onCreate: " + millis);
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "/////////////////////////////////////onUpgrade");
			TableUtils.dropTable(connectionSource, Event.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
		/*
		 * Un comment this but then don't drop and create the table, it will update
		if (oldVersion == 1) {
			System.out.println("Y YO ESTOY AQUI ESPERANDOTEEEEEEE");
			RuntimeExceptionDao<Event, Integer> dao = getEventDataDao();
			// we added the themaTag and typeTag column in version 2
			dao.executeRaw("ALTER TABLE `events` ADD COLUMN themaTag VARCHAR;");
			dao.executeRaw("ALTER TABLE `events` ADD COLUMN typeTag VARCHAR;");
		}*/
		/*
		if (oldVersion == 2) {
			System.out.println("Y YO ESTOY AQUI ESPERANDOTEEEEEEE");
			RuntimeExceptionDao<Event, Integer> dao = getEventDataDao();
			// we added the origin's website column in version 2
			dao.executeRaw("ALTER TABLE `events` ADD COLUMN originsWebsite VARCHAR;");
		}*/
	}

	/**
	 * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Event, Integer> getEventDao() throws SQLException {
		if (eventDao == null) {
			eventDao = getDao(Event.class);
		}
		return eventDao;
	}

	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our SimpleData class. It will
	 * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<Event, Integer> getEventDataDao() {
		if (eventRuntimeDao == null) {
			eventRuntimeDao = getRuntimeExceptionDao(Event.class);
		}
		return eventRuntimeDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		eventRuntimeDao = null;
	}
}

