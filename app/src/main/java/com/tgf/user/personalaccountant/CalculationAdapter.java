package com.tgf.user.personalaccountant;

/**
 * Created by Akropon on 28.01.2017.
 */

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CalculationAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    Calculation calculation;

    CalculationAdapter(Context _context, Calculation _calculation) {
        ctx = _context;
        calculation = _calculation;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return calculation.devicesList.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return calculation.devicesList.get(position);
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
            view = lInflater.inflate(R.layout.device_item_for_listview, parent, false);
        }

        Device device = getDevice(position);

        // заполняем View в пункте списка данными устройства
        ((TextView) view.findViewById(R.id.device_name)).setText("Прибор: " + device.name);
        ((TextView) view.findViewById(R.id.device_amount)).setText("Кол-во: " + device.amount);
        ((TextView) view.findViewById(R.id.device_power)).setText("Мощность: "
                + device.power_num + " " +ValueTypes.get_name(device.power_vlt));
        ((TextView) view.findViewById(R.id.device_usage_freq)).setText("Частота использования:\n      "
                + device.usage_freq_num + " " +ValueTypes.get_name(device.usage_freq_vlt_nmr)
                + "/" + ValueTypes.get_name(device.usage_freq_vlt_dnmr));
        ((TextView) view.findViewById(R.id.device_cost)).setText(
                        "≈ " + UsefulFunctions.getNonExponentialForm(
                                device.get_cost(calculation.tariff, calculation.period_num, calculation.period_vlt)
                                ,  MemoryModule.settings_accuracy));
//        ((TextView) view.findViewById(R.id.device_cost)).setText(
//                "≈ " + UsefulFunctions.getNonExponentialForm(
//                        device.get_cost(calculation.tariff, calculation.period_num, calculation.period_vlt)
//                        ,  MemoryModule.settings_accuracy)+' '+MemoryModule.settings_valuta);
        return view;
    }

    Device getDevice(int position) {
        return ((Device) getItem(position));
    }

    /*// содержимое корзины
    ArrayList<Product> getBox() {
        ArrayList<Product> box = new ArrayList<Product>();
        for (Product p : objects) {
            // если в корзине
            if (p.box)
                box.add(p);
        }
        return box;
    }*/
}