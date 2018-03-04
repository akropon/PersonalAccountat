package com.tgf.user.personalaccountant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Akropon on 04.02.2017.
 */

public class MemoryModule {
    public static boolean isNewDevice = true;
    public static boolean isEditableDevice = true;
    public static boolean isNewCalculation = true;
    public static boolean isEditableCalculation = true;
    public static Device currentDevice = null;
    public static Calculation currentCalculation = null;
    public static Device selectedDevice = null;
    public static Calculation selectedCalculation = null;
    public static ArrayList<Calculation> calculationsList = null;
    public static Calculation copiedCalculation = null;
    public static Device copiedDevice = null;


    public static String SHARED_PREFERENCES_FILE_NAME = "application_preferences";
    public static String PREFERENCES_VERSION = "1.0.0";
    public static String settings_valuta = null;
    public static String settings_valuta_default = "руб";
    public static int    settings_accuracy = 0;
    public static int    settings_accuracy_default = 1;

    public static int    DB_VERSION = 1;




    // Восстановление сохраненных данных из жесткой памяти
    public static void ReadMemory(Context context) {
        // Создаем класс списка Расчетов
        MemoryModule.calculationsList = new ArrayList<>();

        /*Пытаемся считать все данные из памяти упаковывая их в МП*/
        // подготовка
        Log.d("Akropon", "[MemoryModule->ReadMemory()] Читаем таблицы на старте");
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor_calculation;
        Cursor cursor_device;
        int id_calculation;

        //Берем таблицу CALCULATIONS
        Log.d("Akropon", "[MemoryModule->ReadMemory()] читаем CALCULATIONS");
        cursor_calculation = database.query("CALCULATIONS", null, null, null, null, null, null);
        if (cursor_calculation.moveToFirst()) { // Если в таблице CALCULATIONS есть записи:
            int i_id = cursor_calculation.getColumnIndex("id");
            int i_name = cursor_calculation.getColumnIndex("name");
            int i_period_num = cursor_calculation.getColumnIndex("period_num");
            int i_period_vlt = cursor_calculation.getColumnIndex("period_vlt");
            int i_tariff = cursor_calculation.getColumnIndex("tariff");
            do { // пробегаемся по каждой записи таблицы CALCULATIONS
                // Создаем Расчет, читаем из записи и заполняем данные Расчета
                Calculation calculation = new Calculation();
                calculation.name = cursor_calculation.getString(i_name);
                calculation.period_num = cursor_calculation.getDouble(i_period_num);
                calculation.period_vlt = cursor_calculation.getInt(i_period_vlt);
                calculation.tariff = cursor_calculation.getDouble(i_tariff);
                calculation.database_row_id = cursor_calculation.getInt(i_id);

                // Заполняем Список Устройст текущего Расчета по данным из БД
                TakeAllDevicesForCalculationFromDB(calculation, calculation.database_row_id, database);

                // Присоединяем Расчет к Списку Расчетов
                MemoryModule.calculationsList.add(calculation);
            } while (cursor_calculation.moveToNext()); // Пытаемся переместить курсор на след. запись

        } else // Таблица CALCULATIONS пуста
            Log.d("Akropon", "BD id empty");

        dbHelper.close();
    }


    // Удаляет все записи из всех таблиц в БД
    public static void clearTablesInDB(Context context) {
        /*--- Открываем БД ---*/
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        /*--- Стираем все записи в таблицах ---*/
        db.delete("CALCULATIONS", null, null);
        db.delete("DEVICES", null, null);
        /*--- Закрываем БД ---*/
        dbHelper.close();
    }

    // Удаляет все Расчеты с соответствующими Устройствами из Списка Расчетов
    public static void clearCalculationsList() {
        for (Calculation calc : MemoryModule.calculationsList)
            calc.devicesList.clear();
        MemoryModule.calculationsList.clear();
    }


    // Копирование всей МП в БД с предварительным затиранием памяти БД
    //    с обратной синхронизацией по row_id расчетов
    public static void copyMMtoDB(Context context) {
        long start_time = System.nanoTime();

        /*--- Открываем БД ---*/
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv;
        /*--- Стираем все записи в таблицах ---*/
        db.delete("CALCULATIONS", null, null);
        db.delete("DEVICES", null, null);
        /*--- Копируем память из МП в БД ---*/
        db.beginTransaction();
        for (Calculation calc : MemoryModule.calculationsList ) {
            cv = new ContentValues();
            cv.put("name", calc.name);
            cv.put("tariff", calc.tariff);
            cv.put("period_num", calc.period_num);
            cv.put("period_vlt", calc.period_vlt);
            calc.database_row_id = (int)db.insert("CALCULATIONS", null, cv);


            for (Device dev : calc.devicesList) {
                cv = new ContentValues();
                cv.put("id_calculation", calc.database_row_id);
                cv.put("name", dev.name);
                cv.put("power_num", dev.power_num);
                cv.put("power_vlt", dev.power_vlt);
                cv.put("amount", dev.amount);
                cv.put("usage_freq_num", dev.usage_freq_num);
                cv.put("usage_freq_vlt_nmr", dev.usage_freq_vlt_nmr);
                cv.put("usage_freq_vlt_dnmr", dev.usage_freq_vlt_dnmr);
                db.insert("DEVICES", null, cv);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        /*--- Закрываем БД ---*/
        dbHelper.close();

        long finish_time = System.nanoTime();
        long wasted_time = finish_time - start_time;
        //Toast.makeText(context, "Потрачено "+wasted_time+" нс", Toast.LENGTH_SHORT).show();
        Log.d("Akropon", "[MemoryModule->copyMMtoDB] Потрачено "+wasted_time+" нс");
    }


    /** Достает из БД все Устройства, принадлежащие Расчету и записывает их в Расчет
     *
     * @param calculation - Расчет
     * @param id_calculation - id Расчета
     * @param database - текущая открытая база данных
     */
    private static void TakeAllDevicesForCalculationFromDB(Calculation calculation,
                                                    int id_calculation,
                                                    SQLiteDatabase database) {
        /*Запрашиваем из таблицы DEVICES записи, где id_calculation совпадает
                        с id записи текущего calculation*/
        Cursor cursor_device = database.rawQuery(
                "SELECT * FROM DEVICES WHERE id_calculation = ?",
                new String[] {String.valueOf(id_calculation)}); // (такая форма использования...)

        if (cursor_device.moveToFirst()) { // Если в таблице DEVICES есть записи:
            int i_id = cursor_device.getColumnIndex("id");
            int i_name = cursor_device.getColumnIndex("name");
            int i_power_num = cursor_device.getColumnIndex("power_num");
            int i_power_vlt = cursor_device.getColumnIndex("power_vlt");
            int i_amount = cursor_device.getColumnIndex("amount");
            int i_usage_freq_num = cursor_device.getColumnIndex("usage_freq_num");
            int i_usage_freq_vlt_nmr = cursor_device.getColumnIndex("usage_freq_vlt_nmr");
            int i_usage_freq_vlt_dnmr = cursor_device.getColumnIndex("usage_freq_vlt_dnmr");
            do { // пробегаемся по каждой записи таблицы DEVICES
                // Создаем Устройство, читаем из записи и заполняем данные Устройства
                Device device = new Device(
                        cursor_device.getString(i_name),
                        cursor_device.getDouble(i_power_num),
                        cursor_device.getInt(i_power_vlt),
                        cursor_device.getInt(i_amount),
                        cursor_device.getDouble(i_usage_freq_num),
                        cursor_device.getInt(i_usage_freq_vlt_nmr),
                        cursor_device.getInt(i_usage_freq_vlt_dnmr) );
                // присоединяем Устройство к Расчету
                calculation.devicesList.add(device);
            } while (cursor_device.moveToNext()); // Пытаемся переместить курсор на след. запись
        }
    }

    // TODO RELEASE убрать в релизе
    public static void addRandomCalculation(Context context) {
        /*--- Генерим рандомный Расчет с рандомными девайсами) ---*/
        Random random = new Random(System.nanoTime());

        MemoryModule.currentCalculation = new Calculation();
        MemoryModule.currentCalculation.name = "calc"+Math.abs(random.nextInt())%1000;
        MemoryModule.currentCalculation.tariff = random.nextDouble()*4+3;
        MemoryModule.currentCalculation.period_num = Math.abs(random.nextInt())%30+1;
        MemoryModule.currentCalculation.period_vlt =
                Math.abs(random.nextInt())%(ValueTypes.YEAR+1-ValueTypes.SEC) + ValueTypes.SEC;

        int num_of_devices = Math.abs(random.nextInt()%5);
        if (num_of_devices==0) num_of_devices = Math.abs(random.nextInt()%5); // уменьшаем шансы нуля
        if (num_of_devices==0) num_of_devices = Math.abs(random.nextInt()%5); // уменьшаем шансы нуля дважды
        for (int i=0; i<num_of_devices; i++) {
            int var = Math.abs(random.nextInt())%(ValueTypes.MONTH+1-ValueTypes.SEC) + ValueTypes.SEC;
            Device device = new Device(
                    MemoryModule.currentCalculation.name + "_dev"+Math.abs(random.nextInt())%1000,
                    (Math.abs(random.nextInt())%1000)/100.0,
                    Math.abs(random.nextInt())%(ValueTypes.kW+1-ValueTypes.mW) + ValueTypes.mW,
                    (Math.abs(random.nextInt())%5+1>1 ? Math.abs(random.nextInt())%5+1 : 1),
                    (Math.abs(random.nextInt())%10+1),
                    var,
                    Math.abs(random.nextInt())%(ValueTypes.MONTH+1-var) + var );
            MemoryModule.currentCalculation.devicesList.add(device);
        }

        /*--- Добавляем в БД (+запоминаем row_id) ---*/
        // Добавляем в БД новые записи
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        db.beginTransaction();

        cv.put("name", MemoryModule.currentCalculation.name);
        cv.put("tariff", MemoryModule.currentCalculation.tariff);
        cv.put("period_num", MemoryModule.currentCalculation.period_num);
        cv.put("period_vlt", MemoryModule.currentCalculation.period_vlt);
        // добавляем запись и запоминаем row_id в соответствующее поле Расчета
        MemoryModule.currentCalculation.database_row_id = (int)db.insert("CALCULATIONS", null, cv);
        for (Device dev : MemoryModule.currentCalculation.devicesList) {
            cv = new ContentValues();
            cv.put("id_calculation", MemoryModule.currentCalculation.database_row_id);
            cv.put("name", dev.name);
            cv.put("power_num", dev.power_num);
            cv.put("power_vlt", dev.power_vlt);
            cv.put("amount", dev.amount);
            cv.put("usage_freq_num", dev.usage_freq_num);
            cv.put("usage_freq_vlt_nmr", dev.usage_freq_vlt_nmr);
            cv.put("usage_freq_vlt_dnmr", dev.usage_freq_vlt_dnmr);
            db.insert("DEVICES", null, cv);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        // закрываем работу с БД
        dbHelper.close();


        /*--- Добавляем в МП ---*/
        MemoryModule.calculationsList.add(MemoryModule.currentCalculation.get_clone());


    }


    /** Функция проверяет, содержит ли MemoryModule.calculationsList
     *    хоть один расчет с указанным имерем
     *
     * @param calculation_name - указанное имя
     * @return - true, если такой Расчет существует; false, иначе
     */
    public static boolean containsCalcWithName(String calculation_name) {
        for (Calculation calc : MemoryModule.calculationsList)
            if (calculation_name.compareTo(calc.name)==0) return true;
        return false;
    }

    /** Функция проверяет, содержит ли указанный Расчет
     *    хоть одно устройство с указанным именем
     *
     * @param calculation - указанный Расчет
     * @param device_name - указанное имя
     * @return - true, если содержит; false, иначе.
     */
    public static boolean containsDeviceWithName(Calculation calculation, String device_name) {
        for (Device dev : calculation.devicesList)
            if (dev.name.compareTo(device_name)==0) return true;
        return false;
    }
}
