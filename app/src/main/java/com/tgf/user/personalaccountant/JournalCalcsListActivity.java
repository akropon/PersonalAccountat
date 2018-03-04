package com.tgf.user.personalaccountant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class JournalCalcsListActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, Button.OnClickListener {

    static final int RC_CHANGE_CALCULATION = 20;  // идентификатор активити изменения расчета (request code)

    ListView listViewJournal;
    Button btn_back;
    Button btn_more;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_calcs_list);

        btn_back = (Button)findViewById(R.id.journal_btn_back);
        btn_back.setOnClickListener(this);

        btn_more = (Button)findViewById(R.id.journal_btn_more);
        btn_more.setOnClickListener(this);

        listViewJournal = (ListView) findViewById(R.id.listView3);
        listViewJournal.setOnItemClickListener(this);
        CalculationsListAdapter calculationsListAdapter = new CalculationsListAdapter(this, MemoryModule.calculationsList);
        listViewJournal.setAdapter(calculationsListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        // Запоминаем выбранный элемент
        MemoryModule.selectedCalculation = (Calculation)listViewJournal.getAdapter().getItem(position);

        // Производим действо.
        // настроиваем менюшечку
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_calculation_option);

        popupMenu.setOnMenuItemClickListener(new MenuClickListener(this) {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mco_cancel:
                        // do nothing, it will close itself
                        return true;
                    case R.id.mco_copy:
                        MemoryModule.copiedCalculation = MemoryModule.selectedCalculation.get_clone();
                        Toast.makeText(this.parentActivity, "Скопировано", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.mco_paste_before:
                        ((JournalCalcsListActivity)this.parentActivity).pasteCalculation(position, false);
                        showData();
                        return true;
                    case R.id.mco_paste_after:
                        ((JournalCalcsListActivity)this.parentActivity).pasteCalculation(position, true);
                        showData();
                        return true;
                    case R.id.mco_watch:
                        ((JournalCalcsListActivity)this.parentActivity).watchSelectedCalculation();
                        return true;
                    case R.id.mco_change:
                        ((JournalCalcsListActivity)this.parentActivity).changeSelectedDevice();
                        return true;
                    case R.id.mco_delete:
                        ((JournalCalcsListActivity)this.parentActivity).deleteSelectedCalculation();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }



    // Удаление выбранного расчета с входящими в него устройствами
    private void deleteSelectedCalculation() {
        final Context context = this;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Внимание!");
        alertDialog.setMessage("Вы действительно хотите УДАЛИТЬ ВСЕ устройства?");
        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                /*--- Удаление из БД ---*/
                Log.d("Akropon", "[MemoryModule->ReadMemory()] Удаление Расчета с id="
                        + MemoryModule.selectedCalculation.database_row_id+" и соответствующих Устройств");
                DBHelper dbHelper = new DBHelper(context);
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                database.delete("CALCULATIONS", "id=?", new String[]{
                        String.valueOf(MemoryModule.selectedCalculation.database_row_id) });
                database.delete("DEVICES", "id_calculation=?", new String[]{
                        String.valueOf(MemoryModule.selectedCalculation.database_row_id) });
                dbHelper.close();

                /*--- Удаление из МП ---*/
                MemoryModule.calculationsList.remove(MemoryModule.selectedCalculation);

                showData();
            }
        });
        alertDialog.setNegativeButton("Нет", null);
        alertDialog.setCancelable(true);
        alertDialog.show();


    }

    // Отображение новой информации при любых изменениях в Списке Расчетов в Модуле Памяти
    private void showData() {
        CalculationsListAdapter calculationsListAdapter = new CalculationsListAdapter(this, MemoryModule.calculationsList);
        listViewJournal.setAdapter(calculationsListAdapter);
    }

    // Просмотр выбранного Расчета без возможности редактирования
    public void watchSelectedCalculation() {
        MemoryModule.currentCalculation = MemoryModule.selectedCalculation.get_clone();
        MemoryModule.isNewCalculation = false;
        MemoryModule.isEditableCalculation = false;
        Intent intent=new Intent(this,CalculationActivity.class);
        startActivityForResult(intent,1);
        int a = 0;
    }

    // Просмотр Расчета с возможностью внесения изменений и сохранения этих изменений
    private void changeSelectedDevice() {
        MemoryModule.currentCalculation = MemoryModule.selectedCalculation.get_clone();
        MemoryModule.isNewCalculation = false;
        MemoryModule.isEditableCalculation = true;
        Intent intent=new Intent(this,CalculationActivity.class);
        startActivityForResult(intent, RC_CHANGE_CALCULATION);
    }


    /**Вставка скопированно Расчета в указанную позицию (аргумент) в списке
     *
     * @param list_click_position_index - указанная позиция
     * @param next_position - true - делать +1 к указанной позиции, false - делать +0.
     */
    private void pasteCalculation(int list_click_position_index, boolean next_position) {
        /*--- Вставка в МП ---*/
        if (MemoryModule.copiedCalculation != null) {
            if (next_position) list_click_position_index++;

            /*--- Подбор нового имени ---*/
            String new_name_basis = MemoryModule.copiedCalculation.name+"_copy";
            String new_name_ready = new_name_basis;
            int i=1;
            while (MemoryModule.containsCalcWithName(new_name_ready))
                new_name_ready = new_name_basis + '(' + (i++) + ')';

            Calculation new_calculation = MemoryModule.copiedCalculation.get_clone();
            new_calculation.name = new_name_ready;
            MemoryModule.calculationsList.add(list_click_position_index, new_calculation);

            /*--- Копирование из МП в БД с обратной синхронизацией по row_id ---*/
            MemoryModule.copyMMtoDB(this);
            Toast.makeText(this, "Вставлено", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Ошибка. Ничто не скопировано.", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_CHANGE_CALCULATION:
                if (resultCode == RESULT_OK)
                    // обновляем информацию при изменении расчета
                    showData();
                break;
            default:
                break;
        }
    }


    /** Обработчик нажатий кнопок */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.journal_btn_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.journal_btn_more:
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.inflate(R.menu.menu_journal_more_actions);

                popupMenu.setOnMenuItemClickListener(new MenuClickListener(this) {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mjma_paste_to_begin:
                                ((JournalCalcsListActivity)this.parentActivity).pasteCalculation(0, false);
                                ((JournalCalcsListActivity)this.parentActivity).showData();
                                return true;
                            case R.id.mjma_delete_all:
                                deleteAllCalculations();
                                return true;
                            case R.id.mjma_help:
                                AlertDialog.Builder builder = new AlertDialog.Builder(this.parentActivity);
                                builder.setMessage(R.string.journal_str_help_message);
                                AlertDialog alert = builder.create();
                                alert.show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();

                break;
            default:
                break;
        }
    }

    private void deleteAllCalculations() {
        final Context context = this;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Внимание!");
        alertDialog.setMessage("Вы действительно хотите удалить ВСЕ данные?");
        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                MemoryModule.clearTablesInDB(context);
                MemoryModule.clearCalculationsList();
                showData();
            }
        });
        alertDialog.setNegativeButton("Нет", null);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
}
