package com.tgf.user.personalaccountant;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 *  Класс, помогающий создавать, апдейтить и открывать БД приложения
 *
 *  Created by Akropon on 02.07.2017.
 */
public class DBHelper extends SQLiteOpenHelper {

    static final String DB_NAME = "APP_DB";

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, DB_NAME, null, MemoryModule.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Akropon", "--- onCreate database with 2 tables ---");

        // создаем таблицу CALCULATIONS с полями
        db.execSQL("create table CALCULATIONS ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "tariff real,"
                + "period_num real,"
                + "period_vlt integer" + ");");

        // создаем таблицу DEVICES с полями
        db.execSQL("create table DEVICES ("
                + "id integer primary key autoincrement,"
                + "id_calculation integer,"
                + "name text,"
                + "power_num real,"
                + "power_vlt integer,"
                + "amount integer,"
                + "usage_freq_num real,"
                + "usage_freq_vlt_nmr integer,"
                + "usage_freq_vlt_dnmr integer" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
