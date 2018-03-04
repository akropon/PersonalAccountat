package com.tgf.user.personalaccountant;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class CalculationActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, TextView.OnKeyListener,
        Spinner.OnItemSelectedListener{

    static final int RC_ADD_DEVICE= 10;  // идентификатор активити создания нового девайса (request code)
    static final int RC_CHANGE_DEVICE = 11;  // идентификатор активити изменения девайса

    TextView name;
    TextView tariff;
    TextView period_num;
    Spinner period_vlt_spinner;
    ListView listView;
    Button btn_bck;
    Button btn_add;
    Button btn_save;
    Button btn_more;
    TextView sum_cost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculation);

        // Скрывает назойливо выскакивающую саму по себе клавиатуру.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        name = (TextView)findViewById(R.id.editcalc_name);
        tariff = (TextView)findViewById(R.id.editcalc_tariff);
        period_num = (TextView)findViewById(R.id.editcalc_period_num);
        sum_cost = (TextView)findViewById(R.id.editcalc_sum_cost);
        period_vlt_spinner = (Spinner)findViewById(R.id.editcalc_period_vlt_spinner);
        listView = (ListView)findViewById(R.id.editcalc_listview);

        btn_bck = (Button)findViewById(R.id.editcalc_btn_bck);
        btn_add = (Button)findViewById(R.id.editcalc_btn_add);
        btn_save = (Button)findViewById(R.id.editcalc_btn_save);
        btn_more = (Button)findViewById(R.id.editcalc_btn_more);

        // Создаем обработчик нажатия кнопок, используя метод onBtnClickHandler(..) этого класса
        View.OnClickListener btnClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnClickHandler(v);
            }
        };
        // Подключаем обработчик к кнопкам на активити
        btn_bck.setOnClickListener(btnClickListner);
        btn_add.setOnClickListener(btnClickListner);
        btn_save.setOnClickListener(btnClickListner);
        btn_more.setOnClickListener(btnClickListner);
        // Подключаем обработчик к полям ввода текста (только тех, которые используются в пересчете)
        tariff.setOnKeyListener(this);
        period_num.setOnKeyListener(this);

        // TODO RELEASE удалить к релизу
        //((Button)findViewById(R.id.editcalc_btn_random)).setOnClickListener(btnClickListner);

        period_vlt_spinner.setAdapter(ValueTypes.get_spinner_adapter_for_time(this));
        period_vlt_spinner.setOnItemSelectedListener(this);

        listView.setOnItemClickListener(this);

        ((TextView)findViewById(R.id.editcalc_textview3)).setText(
                "  "+MemoryModule.settings_valuta+" за кВт*час");


        if ( MemoryModule.isNewCalculation )
            MemoryModule.currentCalculation = new Calculation();

        setAttributesAccordingToMode();

        showData();
    }

    /** Устанавливает видимость и активность элементов управления в соответствии с
     *      режимом запуска текущей активити.
     *
     *      (режим запуска задается в родительской активити)
     *      (данные по режиму запуска хранятся в MemoryModule)
     *
     */
    protected void setAttributesAccordingToMode() {

        if ( MemoryModule.isEditableCalculation ) {
            // РЕЖИМ РЕДАКТИРОВАНИЯ
            name.setEnabled(true);
            tariff.setEnabled(true);
            period_num.setEnabled(true);
            period_vlt_spinner.setEnabled(true);
            btn_bck.setEnabled(true);
            btn_add.setEnabled(true);
            btn_save.setEnabled(true);
            btn_more.setEnabled(true);

        } else {
            // РЕЖИМ ПРОСМОТРА
            name.setEnabled(false);
            tariff.setEnabled(false);
            period_num.setEnabled(false);
            period_vlt_spinner.setEnabled(false);
            btn_bck.setEnabled(true);
            btn_add.setEnabled(false);
            btn_save.setEnabled(false);
            btn_more.setEnabled(false);
        }
    }


    public void showData() {
        name.setText(MemoryModule.currentCalculation.name);
        tariff.setText(String.valueOf(MemoryModule.currentCalculation.tariff));
        period_num.setText(String.valueOf(MemoryModule.currentCalculation.period_num));
        period_vlt_spinner.setSelection(
                ( (ValueTypes.SpinnerAdapter)period_vlt_spinner.getAdapter() )
                        .getPosition(MemoryModule.currentCalculation.period_vlt));
        listView.setAdapter(new CalculationAdapter(this, MemoryModule.currentCalculation));
        sum_cost.setText( "≈ " + UsefulFunctions.getNonExponentialForm(
                MemoryModule.currentCalculation.get_cost(), MemoryModule.settings_accuracy)
                +' '+MemoryModule.settings_valuta);
    }

    public void showData_OnlySumCost() {
        sum_cost.setText( "≈ " + UsefulFunctions.getNonExponentialForm(
                MemoryModule.currentCalculation.get_cost(), MemoryModule.settings_accuracy)
                +' '+MemoryModule.settings_valuta);
    }

    public void showData_OnlyDeviceListAndSumCost() {
        listView.setAdapter(new CalculationAdapter(this, MemoryModule.currentCalculation));
        sum_cost.setText( "≈ " + UsefulFunctions.getNonExponentialForm(
                MemoryModule.currentCalculation.get_cost(), MemoryModule.settings_accuracy)
                +' '+MemoryModule.settings_valuta);
    }


    /** Обработчик нажатия на элемент списка
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        // Сразу же запоминаем, какой элемент выбрали.
        MemoryModule.selectedDevice = (Device)listView.getAdapter().getItem(position);

        // Производим действо.
        if (MemoryModule.isEditableCalculation) {
            // настроиваем менюшечку
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.inflate(R.menu.menu_device_options); // Для Android 4.0

            popupMenu.setOnMenuItemClickListener(new MenuClickListener(this) {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // Toast.makeText(PopupMenuDemoActivity.this,
                    // item.toString(), Toast.LENGTH_LONG).show();
                    // return true;
                    switch (item.getItemId()) {

                        case R.id.mdo_change:
                            ((CalculationActivity)this.parentActivity).changeSelectedDevice();
                            /* Следующая строчка не нужна, т.к. при финишировании активити
                            * вызывается что-то типо перерисовки предыдущей активити,
                            * где данные сами собой обновятся*/
                            //((CalculationActivity)this.parentActivity).showData();
                            return true;
                        case R.id.mdo_copy:
                            MemoryModule.copiedDevice = MemoryModule.selectedDevice.get_clone();
                            Toast.makeText(this.parentActivity, "Скопировано", Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.mdo_paste_before:
                            pasteCopiedDevice(position, false);
                            showData_OnlyDeviceListAndSumCost();
                            return true;
                        case R.id.mdo_paste_after:
                            pasteCopiedDevice(position, true);
                            showData_OnlyDeviceListAndSumCost();
                            return true;
                        case R.id.mdo_delete:
                            ((CalculationActivity)this.parentActivity).deleteSelectedDevice();
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        } else { // MemoryModule.isEditableCalculation == false
            // настроиваем менюшечку
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.inflate(R.menu.menu_device_options_watchmode); // Для Android 4.0
            popupMenu.setOnMenuItemClickListener(new MenuClickListener(this) {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.mdowm_watch:
                            ((CalculationActivity)this.parentActivity).watchSelectedDevice();
                            return true;
                        case R.id.mdowm_copy:
                            MemoryModule.copiedDevice = MemoryModule.selectedDevice.get_clone();
                            Toast.makeText(this.parentActivity, "Скопировано", Toast.LENGTH_SHORT).show();
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }
    }

    /**
     *   Запускает редактор девайса.
     *
     *   ВНИМАНИЕ: При использовании целевой девайс
     *   должен быть указан в MemoryModule.selectedDevice
     *
     */
    public void changeSelectedDevice() {
        MemoryModule.currentDevice = MemoryModule.selectedDevice;
        MemoryModule.isNewDevice = false; // режим "открытия" существующего устройства
        MemoryModule.isEditableDevice = true;
        Intent intent=new Intent(this,DeviceActivity.class);
        startActivityForResult(intent, RC_CHANGE_DEVICE);
    }

    /**
     *   Запускает просмотрщик девайса.
     *
     *   ВНИМАНИЕ: При использовании целевой девайс
     *   должен быть указан в MemoryModule.selectedDevice
     *
     */
    public void watchSelectedDevice() {
        MemoryModule.currentDevice = MemoryModule.selectedDevice;
        MemoryModule.isNewDevice = false; // режим "открытия" существующего устройства
        MemoryModule.isEditableDevice = false;
        Intent intent=new Intent(this,DeviceActivity.class);
        startActivityForResult(intent,1);
    }

    /**  Удаляет выбранный девайс.
     *
     *   ВНИМАНИЕ: При использовании целевой девайс
     *   должен быть указан в MemoryModule.selectedDevice
     *
     */
    public void deleteSelectedDevice() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Внимание!");
        alertDialog.setMessage("Вы действительно хотите удалить это устройство?");
        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (MemoryModule.currentCalculation.devicesList.remove(MemoryModule.selectedDevice))
                    Toast.makeText(getApplicationContext(),"Удалено",Toast.LENGTH_SHORT)
                            .show();
                else
                    Toast.makeText(getApplicationContext(),"Упс. Ошибка удаления.",Toast.LENGTH_LONG)
                            .show();
                showData();
            }
        });
        alertDialog.setNegativeButton("Нет", null);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    public void pasteCopiedDevice(int position, boolean next_position) {
        /*--- Вставка в МП ---*/
        if (MemoryModule.copiedDevice != null) {
            if ( next_position ) position++;

            /*--- Подбор нового имени ---*/
            String new_name_basis = MemoryModule.copiedDevice.name+"_copy";
            String new_name_ready = new_name_basis;
            int i=1;
            while (MemoryModule.containsDeviceWithName(
                    MemoryModule.currentCalculation, new_name_ready))
                new_name_ready = new_name_basis + '(' + (i++) + ')';

            Device new_device = MemoryModule.copiedDevice.get_clone();
            new_device.name = new_name_ready;
            MemoryModule.currentCalculation.devicesList.add(position, new_device);

            Toast.makeText(this, "Вставлено", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Ошибка. Ничто не скопировано.", Toast.LENGTH_SHORT).show();
    }


    /** Парсит данные из полей CalculationActivity и записывает их в Расчет.
     *
     *  Содержит защиту от "кривых"(тех, которые не парсятся) данных.
     *
     * @param calculation - Расчет
     * @return true - если парсинг успешен.
     */
    protected boolean parseDataFromActivityToCalculation(Calculation calculation) {
        // подготовка буфферных переменных
        String new_name;
        Double new_tariff;
        Double new_period_num;
        int new_period_vlt;
        // пытаемся пропарсить данные из полей
        try {
            new_name = name.getText().toString();
            new_tariff = Double.parseDouble(tariff.getText().toString());
            new_period_num = Double.parseDouble(period_num.getText().toString());
            new_period_vlt = (int) period_vlt_spinner.getSelectedItem();
        } catch (Exception exc) {
            return false; // неудача
        }
        // парсинг успешен. Записываем данные в Расчет.
        calculation.name = new_name;
        calculation.tariff = new_tariff;
        calculation.period_num = new_period_num;
        calculation.period_vlt = new_period_vlt;
        return true;
    }

    public void onBtnClickHandler(View v) {// Обработчик нажатия кнопок

        switch(v.getId()) {
            case R.id.editcalc_btn_add:
                MemoryModule.currentDevice = null; // "сброс" перед созданием нового устройтва
                MemoryModule.isNewDevice = true; // задаем ржим создания нвоого устройства
                MemoryModule.isEditableDevice = true; // разрешение на редактирование устройства
                Intent intent = new Intent(this,DeviceActivity.class);// Переходим к редактору устр-в
                startActivityForResult(intent, RC_ADD_DEVICE);
                break;
            case R.id.editcalc_btn_save:
                saveCalculation();
                break;
            case R.id.editcalc_btn_bck:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.editcalc_btn_more:
                onClickButtonMore(v); //передаем обработку другой функции
                break;
            // TODO RELEASE удалить к релизу
            /*case R.id.editcalc_btn_random:
                fillRandomCalculation();
                break;*/
            default:
                break;

        }
    }

    // Выделенная фукнция-обработчик нажатия кнопки "Еще"
    protected void onClickButtonMore(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_deviceslist_more_actions);

        popupMenu.setOnMenuItemClickListener(new MenuClickListener(this) {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mdma_paste_to_begin:
                        ((CalculationActivity)this.parentActivity).pasteCopiedDevice(0, false);
                        showData_OnlyDeviceListAndSumCost();
                        return true;
                    case R.id.mdma_delete_all:
                        deleteAllDevices();
                        return true;
                    case R.id.mdma_help:
                        AlertDialog.Builder builder = new AlertDialog.Builder(this.parentActivity);
                        builder.setMessage(R.string.editcalc_str_help_message);
                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    protected void deleteAllDevices() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Внимание!");
        alertDialog.setMessage("Вы действительно хотите УДАЛИТЬ ВСЕ устройства?");
        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                MemoryModule.currentCalculation.devicesList.clear();
                showData_OnlyDeviceListAndSumCost();
            }
        });
        alertDialog.setNegativeButton("Нет", null);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    // TODO RELEASE удалить к релизу
    protected void fillRandomCalculation() {
        Random random = new Random(System.nanoTime());

        MemoryModule.currentCalculation = new Calculation();
        MemoryModule.currentCalculation.name = "calc"+Math.abs(random.nextInt())%1000;
        MemoryModule.currentCalculation.tariff = random.nextDouble()*4+3;
        MemoryModule.currentCalculation.period_num = Math.abs(random.nextInt())%30+1;
        MemoryModule.currentCalculation.period_vlt =
                Math.abs(random.nextInt())%(ValueTypes.YEAR+1-ValueTypes.SEC) + ValueTypes.SEC;

        int num_of_devices = Math.abs(random.nextInt()%5);
        if (num_of_devices==0) num_of_devices = Math.abs(random.nextInt()%5); // уменьшаем шансы нуля
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

        showData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_ADD_DEVICE: // Обработка результата добавления нового устрройства
                if ( resultCode == RESULT_OK ) {  // Устройство успешно создано
                    // Устройство создано и лежит в MemoryModule.currentDevice.
                    // Копируем его в текущий список устройств MemoryModule.currentCalculation
                    // и обновляем информацию на экране
                    MemoryModule.currentCalculation.devicesList.add(
                            MemoryModule.currentDevice.get_clone());
                    this.showData_OnlyDeviceListAndSumCost();
                }
                break;
            case RC_CHANGE_DEVICE: // Обработка результата добавления редактирования устройства
                if ( resultCode == RESULT_OK )
                    showData_OnlyDeviceListAndSumCost();
                break;
            default:
                break;
        }
    }


    /**
     * Обработчик события любого ввода в текстовые поля.
     *
     * Вызывает попытку пересчета результирующей стоимости при вводе нового символа
     */
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_UP
                && (keyCode >= KeyEvent.KEYCODE_0
                    && keyCode <= KeyEvent.KEYCODE_9
                    || keyCode == KeyEvent.KEYCODE_DEL)  ) {
            //Log.d("Akropon", "CalcAct->onEditorAction: enteredKeyCode = "+event.getKeyCode());
            try {
                parseDataFromActivityToCalculation(MemoryModule.currentCalculation);
                showData_OnlyDeviceListAndSumCost();
                Log.d("Akropon", "CalcAct->onEditorAction: successful recounting");
            } catch (Exception exc){
                Log.d("Akropon", "CalcAct->onEditorAction: recouning failed");
            };
            return true;
        }
        return false;
    }

    private boolean saveCalculation() {
        // Пробуем пропарсить данные их полей
        if (parseDataFromActivityToCalculation(MemoryModule.currentCalculation)) {
            if (MemoryModule.isNewCalculation) { /*--- Если расчет новый ---*/
                Log.d("Akropon", "[CalculationActivity->saveCalculation] Добавляю новый Расчет");

                /*--- Добавляем в БД (+запоминаем row_id) ---*/
                // Добавляем в БД новые записи
                DBHelper dbHelper = new DBHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
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
                // закрываем работу с БД
                dbHelper.close();


                /*--- Добавляем в МП (+запоминаем его как MemoryModule.selectedCalculation) ---*/
                MemoryModule.selectedCalculation = MemoryModule.currentCalculation.get_clone();
                MemoryModule.calculationsList.add(MemoryModule.selectedCalculation);


                /*--- Отмечаем, что расчет уже не новый, а существует в памяти ---*/
                MemoryModule.isNewCalculation = false;

            } else { /*--- Если расчет уже существует ---*/
                Log.d("Akropon", "[CalculationActivity->saveCalculation] Перезаписываю старый Расчет");

                /*--- Обновляем соответствующие записи в БД ---*/
                // подготовка БД
                DBHelper dbHelper = new DBHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                // готовим запись
                ContentValues cv = new ContentValues();
                cv.put("name", MemoryModule.currentCalculation.name);
                cv.put("tariff", MemoryModule.currentCalculation.tariff);
                cv.put("period_num", MemoryModule.currentCalculation.period_num);
                cv.put("period_vlt", MemoryModule.currentCalculation.period_vlt);
                // обновляем запись Расчета
                db.update("CALCULATIONS", cv, "id="+MemoryModule.currentCalculation.database_row_id, null);
                // очищаем список "устаревших" Устройств
                db.delete("DEVICES", "id_calculation=?", new String[]{
                        String.valueOf(MemoryModule.currentCalculation.database_row_id) });
                // создаем новые записи "новых" Устройств
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
                // закрываем работу с БД
                dbHelper.close();


                /*--- Обновляем содержимое МП ---*/
                // (реализация методом "замещения старого объекта новым объектом в списке ")
                int index = MemoryModule.calculationsList.indexOf(MemoryModule.selectedCalculation);
                MemoryModule.selectedCalculation = MemoryModule.currentCalculation.get_clone();
                MemoryModule.calculationsList.remove(index);
                MemoryModule.calculationsList.add(index, MemoryModule.selectedCalculation);
            }

            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
            this.setResult(RESULT_OK);
            this.finish();
            return true;
        } else {
            // TODO IMPROVE - заменить на вспылавающее сообщение о возможных причинах неудачи.
            Toast.makeText(this, "Упс. Ошибка сохранения.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }



    // Spinner change handler BEGIN
    // При любых изменениях производить пересчет
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        parseDataFromActivityToCalculation(MemoryModule.currentCalculation);
        showData_OnlyDeviceListAndSumCost();
        Log.d("Akropon", "CalcAct->onEditorAction: successful recounting");
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        /*do nothing*/
    }
    // Spinner handler END
}
