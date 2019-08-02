package com.huaxindata.bluetoothtest.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private DbdataService  helper;
    private SQLiteDatabase db;
    private static int count = -1;

    public DBManager(Context context) {
        helper = DbdataService.getService(context);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
        beforeAdd(0);
    }

    public void beforeAdd(int add) {
        Cursor cursor = null;
        if (count == -1) {
            cursor = db.query("testinfo", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {//判断游标是否为空
                count = cursor.getCount();
            } else {
                count = 0;
            }
            Log.d("hebing", "beforeAdd get." + count);
        }
        if (cursor != null) {
            cursor.close();
        }
        count += add;
        //如果超过了300条数据，每次自动删除前一百条数据
        if (count > 300) {
            db.beginTransaction(); // 开始事务
            try {
                db.execSQL("DELETE FROM testinfo WHERE _id IN(SELECT _id FROM testinfo order by _id LIMIT 100)");
                db.setTransactionSuccessful(); // 设置事务成功完成
                count -= 100;
            } catch (Exception e) {
                count = 0;
                deleteALL();
            } finally {
                db.endTransaction(); // 结束事务
            }
            try {
                //                Util.execShellCmd("pm clear com.android.bluetooth");
                //Util.execShellCmd("rm /data/misc/bluedroid/*.*");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("hebing", "===================>call beforeAdd clear data:" + count);
        }
    }

    /**
     * add persons
     *
     * @param persons
     */
    public void add(List<TestInfoDTO> persons) {
        beforeAdd(1);
        db.beginTransaction(); // 开始事务
        try {
            for (TestInfoDTO person : persons) {
                db.execSQL("INSERT INTO testinfo VALUES(null, ?, ?, ?,?,?,?)",
                        new Object[]{person.vin, person.time,
                                person.statespeack, person.statelisten,
                                person.upload, person.show});
            }
            db.setTransactionSuccessful(); // 设置事务成功完成
        } finally {

            db.endTransaction(); // 结束事务
        }
    }

    public void updateSpeack(TestInfoDTO person) {
        ContentValues cv = new ContentValues();
        cv.put("statespeack", person.statespeack);
        db.update("person", cv, "vin = ?", new String[]{person.vin});
    }

    public void updateListen(TestInfoDTO person) {
        ContentValues cv = new ContentValues();
        cv.put("statelisten", person.statelisten);
        db.update("person", cv, "vin = ?", new String[]{person.vin});
    }

    public void updateUpload(TestInfoDTO person) {
        ContentValues cv = new ContentValues();
        cv.put("upload", person.upload);
        db.update("person", cv, "vin = ?", new String[]{person.vin});
    }

    // public void deleteOldPerson(TestInfoDTO person) {
    // db.delete("person", "age >= ?",
    // new String[] { String.valueOf(person.age) });
    // }

    /**
     * query all persons, return list
     *
     * @return List<Person>
     */
    public List<TestInfoDTO> query() {
        ArrayList<TestInfoDTO> persons = new ArrayList<TestInfoDTO>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            if (c.getInt(c.getColumnIndex("show")) == 0) {
                TestInfoDTO person = new TestInfoDTO();
                // person._id = c.getInt(c.getColumnIndex("_id"));
                person.vin = c.getString(c.getColumnIndex("vin"));
                person.time = c.getLong(c.getColumnIndex("time"));
                person.statelisten = c.getInt(c.getColumnIndex("statelisten"));
                person.statespeack = c.getInt(c.getColumnIndex("statespeack"));
                person.upload = c.getInt(c.getColumnIndex("upload"));
                person.show = c.getInt(c.getColumnIndex("show"));
                persons.add(person);
            }
        }
        c.close();
        return persons;
    }

    public List<TestInfoDTO> queryForVin(String vin) {
        ArrayList<TestInfoDTO> persons = new ArrayList<TestInfoDTO>();
        Cursor c = db.rawQuery(
                "SELECT * FROM testinfo WHERE show=0 and vin LIKE '%" + vin
                        + "%'", null);
        while (c.moveToNext()) {
            if (c.getInt(c.getColumnIndex("show")) == 0) {
                TestInfoDTO person = new TestInfoDTO();
                // person._id = c.getInt(c.getColumnIndex("_id"));
                person.vin = c.getString(c.getColumnIndex("vin"));
                person.time = c.getLong(c.getColumnIndex("time"));
                person.statelisten = c.getInt(c.getColumnIndex("statelisten"));
                person.statespeack = c.getInt(c.getColumnIndex("statespeack"));
                person.upload = c.getInt(c.getColumnIndex("upload"));
                person.show = c.getInt(c.getColumnIndex("show"));
                persons.add(person);
            }
        }
        c.close();
        return persons;
    }
    public void deleteForVin(String vin) {
        db.beginTransaction(); // 开始事务
        try {
            //DELETE FROM CUSTOMERS
            //db.execSQL("Update  testinfo set show=1 WHERE show=0");
            db.execSQL(
                    "DELETE * FROM testinfo WHERE show=0 and vin LIKE '%" + vin
                            + "%'", null);
            db.setTransactionSuccessful(); // 设置事务成功完成
        } finally {
            db.endTransaction(); // 结束事务
        }
    }
    public List<TestInfoDTO> queryForTime(long times, long timee) {
        ArrayList<TestInfoDTO> persons = new ArrayList<TestInfoDTO>();
        Cursor c = db.rawQuery("SELECT * FROM testinfo WHERE show=0 and time<"
                + timee + " and time>" + times, null);
        while (c.moveToNext()) {
            if (c.getInt(c.getColumnIndex("show")) == 0) {
                TestInfoDTO person = new TestInfoDTO();
                // person._id = c.getInt(c.getColumnIndex("_id"));
                person.vin = c.getString(c.getColumnIndex("vin"));
                person.time = c.getLong(c.getColumnIndex("time"));
                person.statelisten = c.getInt(c.getColumnIndex("statelisten"));
                person.statespeack = c.getInt(c.getColumnIndex("statespeack"));
                person.upload = c.getInt(c.getColumnIndex("upload"));
                person.show = c.getInt(c.getColumnIndex("show"));
                persons.add(person);
            }
        }
        c.close();
        return persons;
    }

    public List<TestInfoDTO> queryForState(boolean b) {
        ArrayList<TestInfoDTO> persons = new ArrayList<TestInfoDTO>();
        Cursor c = db.rawQuery("SELECT * FROM testinfo WHERE show=0 and "
                + (b ? " statelisten=1 and statespeack=1 "
                : " statelisten=0 or statespeack=0"), null);
        while (c.moveToNext()) {
            if (c.getInt(c.getColumnIndex("show")) == 0) {
                TestInfoDTO person = new TestInfoDTO();
                // person._id = c.getInt(c.getColumnIndex("_id"));
                person.vin = c.getString(c.getColumnIndex("vin"));
                person.time = c.getLong(c.getColumnIndex("time"));
                person.statelisten = c.getInt(c.getColumnIndex("statelisten"));
                person.statespeack = c.getInt(c.getColumnIndex("statespeack"));
                person.upload = c.getInt(c.getColumnIndex("upload"));
                person.show = c.getInt(c.getColumnIndex("show"));
                persons.add(person);
            }
        }
        c.close();
        return persons;
    }

    public List<TestInfoDTO> queryForUpload(boolean b) {
        ArrayList<TestInfoDTO> persons = new ArrayList<TestInfoDTO>();
        Cursor c = db.rawQuery("SELECT * FROM testinfo WHERE show=0 and "
                + (b ? " upload=1 " : " upload<>1 "), null);
        while (c.moveToNext()) {
            if (c.getInt(c.getColumnIndex("show")) == 0) {
                TestInfoDTO person = new TestInfoDTO();
                // person._id = c.getInt(c.getColumnIndex("_id"));
                person.vin = c.getString(c.getColumnIndex("vin"));
                person.time = c.getLong(c.getColumnIndex("time"));
                person.statelisten = c.getInt(c.getColumnIndex("statelisten"));
                person.statespeack = c.getInt(c.getColumnIndex("statespeack"));
                person.upload = c.getInt(c.getColumnIndex("upload"));
                person.show = c.getInt(c.getColumnIndex("show"));
                persons.add(person);
            }
        }
        c.close();
        return persons;
    }

    public void deleteALL() {
        // ArrayList<TestInfoDTO> persons = new ArrayList<TestInfoDTO>();
        // Cursor c = queryTheCursor();
        // while (c.moveToNext()) {
        // ContentValues cv = new ContentValues();
        // cv.put("show", 1);
        // db.update("person", cv, "vin = ?", new String[] { person.vin });
        // }
        db.beginTransaction(); // 开始事务
        try {
            //DELETE FROM CUSTOMERS
            //db.execSQL("Update  testinfo set show=1 WHERE show=0");
            db.execSQL("DELETE FROM testinfo");
            db.setTransactionSuccessful(); // 设置事务成功完成
        } finally {
            db.endTransaction(); // 结束事务
        }
    }

    /**
     * query all persons, return cursor
     *
     * @return Cursor
     */
    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM testinfo WHERE show=0 ", null);
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }
}
