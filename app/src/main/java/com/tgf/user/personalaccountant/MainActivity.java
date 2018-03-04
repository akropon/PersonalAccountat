package com.tgf.user.personalaccountant;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    //Button butGo;
    //Button butSetting;
    //Button butQuit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.calculator);

        //butGo = (Button) findViewById(R.id.button);
        //butSetting = (Button) findViewById(R.id.button2);
        //butQuit = (Button) findViewById(R.id.button3);

        ReadSettings();
        MemoryModule.ReadMemory(this);
    }


    // Чтение настроек из памяти
    private void ReadSettings() {
        SharedPreferences sPref =
                getSharedPreferences(MemoryModule.SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        MemoryModule.settings_valuta =
                sPref.getString("valuta", MemoryModule.settings_valuta_default);
        MemoryModule.settings_accuracy =
                sPref.getInt("accuracy", MemoryModule.settings_accuracy_default);

    }

    public void onClick(View v) {// Функция обработчик нажатия трех кнопок


        switch(v.getId()) {
            case R.id.button: //"расчет"
                MemoryModule.currentCalculation = null; // "сброс" перед созданием нового расчета
                MemoryModule.isNewCalculation = true; // задаем режим создания нового расчета
                MemoryModule.isEditableCalculation = true; // разрешение на редактирование расчета
                Intent intent = new Intent(this,CalculationActivity.class);// Переходим к расчету
                startActivityForResult(intent,1);
                break;
            case R.id.button2: //"настройки"
                Intent intent1=new Intent(this,Settings.class);// Переходим на настройки
                startActivityForResult(intent1,1);
                break;
            case R.id.button11: //"журнал"
                Intent intent3=new Intent(this,JournalCalcsListActivity.class);
                startActivityForResult(intent3,1);
                break;
            case R.id.button3: //"выход"
                finish();
                break;


        }


    }

}
