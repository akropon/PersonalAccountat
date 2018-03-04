package com.tgf.user.personalaccountant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Akropon on 30.01.2017.
 */

public class CalculationsListAdapter extends BaseAdapter{

    // TODO все переделать!!! создать новую разметку для листвьюва (03.07.2017: че тут не так то?)

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Calculation> calculationList;

    CalculationsListAdapter(Context _context, ArrayList<Calculation> _calculationList) {
        ctx = _context;
        calculationList = _calculationList;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return calculationList.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return calculationList.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.calc_item_for_listview, parent, false);
        }

        Calculation calculation = getCalculation(position);

        // заполняем View в пункте списка данными устройства
        ((TextView) view.findViewById(R.id.calc_name)).setText("Имя расчета: "
                + calculation.name);
        ((TextView) view.findViewById(R.id.calc_num_of_devices)).setText("Приборов: "
                + calculation.devicesList.size());
        ((TextView) view.findViewById(R.id.calc_period)).setText("Период: "
                + calculation.period_num + " " + ValueTypes.get_name(calculation.period_vlt));
        ((TextView) view.findViewById(R.id.calc_tarif)).setText("Тариф: "
                + calculation.tariff);
        ((TextView) view.findViewById(R.id.calc_total_cost)).setText("Общая стоимость: "
                + "≈ " + UsefulFunctions.getNonExponentialForm(
                    calculation.get_cost(), MemoryModule.settings_accuracy)
                +' '+MemoryModule.settings_valuta);

        return view;
    }

    // товар по позиции
    Calculation getCalculation(int position) {
        return ((Calculation) getItem(position));
    }
}
