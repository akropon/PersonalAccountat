package com.tgf.user.personalaccountant;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Settings extends AppCompatActivity
        implements Button.OnClickListener, SeekBar.OnSeekBarChangeListener{

    TextView txt_valuta;
    SeekBar skbr_accuracy;
    TextView txt_accuracy_status;
    Button btn_save;
    Button btn_cancel;
    Button btn_default;
    Button btn_help;

    static int ACCURACY_MIN = -3;
    static int ACCURACY_MAX = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        txt_valuta = (TextView)findViewById(R.id.settings_valuta);
        skbr_accuracy = (SeekBar) findViewById(R.id.settings_accuracy);
        txt_accuracy_status = (TextView)findViewById(R.id.settings_accuracy_status);
        btn_save = (Button)findViewById(R.id.settings_btn_save);
        btn_cancel = (Button)findViewById(R.id.settings_btn_cancel);
        btn_default = (Button)findViewById(R.id.settings_btn_default);
        btn_help = (Button)findViewById(R.id.settings_btn_help);


        skbr_accuracy.setMax(ACCURACY_MAX-ACCURACY_MIN);
        skbr_accuracy.setProgress(MemoryModule.settings_accuracy-ACCURACY_MIN);
        txt_accuracy_status.setText(String.valueOf(skbr_accuracy.getProgress()+ACCURACY_MIN));
        txt_valuta.setText(MemoryModule.settings_valuta);

        btn_save.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_default.setOnClickListener(this);
        btn_help.setOnClickListener(this);
        skbr_accuracy.setOnSeekBarChangeListener(this);


        // TODO RELEASE удалить к релизу
        /*((Button)findViewById(R.id.settings_btn_delete_db)).setOnClickListener(this);
        ((Button)findViewById(R.id.settings_btn_create_memory)).setOnClickListener(this);
        ((Button)findViewById(R.id.settings_btn_add_calc_pack)).setOnClickListener(this);*/


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_btn_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.settings_btn_save:
                this.save();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.settings_btn_default:
                this.setToDefault();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.settings_btn_help:
                this.showHelpMessage();
                break;
            /*// TODO RELEASE удалить к релизу
            case R.id.settings_btn_delete_db:
                this.deleteDB();
                break;
            // TODO RELEASE удалить к релизу
            case R.id.settings_btn_create_memory:
                this.createMemory();
                break;
            // TODO RELEASE удалить к релизу
            case R.id.settings_btn_add_calc_pack:
                int N = 50;
                for (int i=0; i<N; i++) {
                    MemoryModule.addRandomCalculation(this);
                }
                Toast.makeText(this, "Создано "+N+" записей", Toast.LENGTH_SHORT).show();
                break;*/
            default:
                Log.e("Akropon", "[Settings->OnClick] Нажата неизвестная кнопка");
        }
    }


    // TODO RELEASE удалить к релизу
    void deleteDB() {
        Log.d("Akropon", "Удаляем БД");
        if (SQLiteDatabase.deleteDatabase(this.getDatabasePath(DBHelper.DB_NAME))) {
            Log.d("Akropon", "БД успено стерта");
            Toast.makeText(this, "БД успено стерта", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("Akropon", "БД не была стерта");
            Toast.makeText(this, "БД не была стерта", Toast.LENGTH_SHORT).show();
        }
    }

    // TODO RELEASE удалить к релизу
    void createMemory() {
        /*Создаем память с нуля и в МП и в БД*/


        Log.d("Akropon", "Создаем память и пишем в МП");
        Calculation calculation = new Calculation();
        calculation.name = "calc_name1";
        calculation.period_num = 2;
        calculation.period_vlt = ValueTypes.HOUR;
        calculation.tariff = 5;

        calculation.devicesList.add(
                new Device(
                        "Лампа-хуямпа",
                        40, ValueTypes.W,
                        2,
                        5, ValueTypes.HOUR, ValueTypes.DAY));
        calculation.devicesList.add(
                new Device(
                        "Холодульник",
                        10, ValueTypes.W,
                        1,
                        24, ValueTypes.HOUR, ValueTypes.DAY));
        calculation.devicesList.add(
                new Device(
                        "Пылесос",
                        2, ValueTypes.kW,
                        1,
                        1, ValueTypes.HOUR, ValueTypes.WEEK));

        Calculation calculation1 = new Calculation();
        calculation1.name = "calc_name2";
        calculation1.period_num = 3;
        calculation1.period_vlt = ValueTypes.YEAR;
        calculation1.tariff = 10;

        calculation1.devicesList.add(
                new Device(
                        "Еще1",
                        20, ValueTypes.W,
                        2,
                        10, ValueTypes.HOUR, ValueTypes.DAY));
        calculation1.devicesList.add(
                new Device(
                        "Еще2",
                        20, ValueTypes.W,
                        1,
                        48, ValueTypes.HOUR, ValueTypes.DAY));

        MemoryModule.calculationsList = new ArrayList<>();
        MemoryModule.calculationsList.add(calculation);
        MemoryModule.calculationsList.add(calculation1);

        DBHelper dbHelper;
        Log.d("Akropon", "Удаляем БД");
//        dbHelper = new DBHelper(this);
//        String dbName = dbHelper.getDatabaseName();
        if (SQLiteDatabase.deleteDatabase(this.getDatabasePath(DBHelper.DB_NAME))) {
            Log.d("Akropon", "БД успешно стерта");
        } else {
            Log.d("Akropon", "БД не была стерта");
        }

        Log.d("Akropon", "Создаем БД снова и пишем в БД");
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv;
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
        dbHelper.close();

        Toast.makeText(this, "Память создана ( вроде =) )", Toast.LENGTH_SHORT).show();
    }

    /** Показывает всплывающий текст помощи пользователю
     */
    private void showHelpMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.settings_str_help_message);
        AlertDialog alert = builder.create();
        alert.show();
    }

    /** Сохраняет новые наcтройки
     *
     * 1. Парсит введенные в поля новые данные
     * 2. Обновляет текущие настройки
     * 3. Записывает новые настройки в память
     *
     * @return
     */
    private void save() {
        String new_valuta = txt_valuta.getText().toString();
        int new_accuracy = skbr_accuracy.getProgress()+ACCURACY_MIN;

        MemoryModule.settings_accuracy = new_accuracy;
        MemoryModule.settings_valuta = new_valuta;

        SharedPreferences sPref =
                getSharedPreferences(MemoryModule.SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString("valuta", new_valuta);
        editor.putInt("accuracy", new_accuracy);
        // Записываем версию настроек для поддержки ее в след версиях приложения
        editor.putString("preferences_version", MemoryModule.PREFERENCES_VERSION);
        editor.commit(); // команда для внесения изменений (как заливон в гитхабе)

        Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
    }


    /** Восстанавливает стандартные настройки
     *
     * 1. Текущие настройки устанавливает в стандарные
     * 2. Стирает пользовательские настройки из памяти
     *
     * @return
     */
    private void setToDefault() {
        MemoryModule.settings_accuracy = MemoryModule.settings_accuracy_default;
        MemoryModule.settings_valuta = MemoryModule.settings_valuta_default;

        SharedPreferences sPref =
                getSharedPreferences(MemoryModule.SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.remove("valuta");
        editor.remove("accuracy");
        editor.remove("preferences_version");
        editor.commit(); // команда для внесения изменений (как заливон в гитхабе)

        Toast.makeText(this, "Сброшено", Toast.LENGTH_SHORT).show();
    }

    // Обработчик передвижения пользователем ползунка, задающего значение Точности
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        txt_accuracy_status.setText(String.valueOf(skbr_accuracy.getProgress()+ACCURACY_MIN));
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}
