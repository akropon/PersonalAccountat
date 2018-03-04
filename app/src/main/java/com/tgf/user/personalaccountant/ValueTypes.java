package com.tgf.user.personalaccountant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Map;

/**
 * Величины имзмерения: секунды, минуты, часы и т.д.
 *
 * Created by Akropon on 20.01.2017.
 */

public class ValueTypes {

    ///////////////////////////////
    // ВЕЛИЧИНЫ ИЗМЕРЕНИЯ //  |  //
    ////////////////////////  V  //

    public static final int NULL  =  0;    // не определена
    public static final int NONE  =  1;    // безразмерная

    public static final int SEC   =  101;  // секунда
    public static final int MIN   =  102;  // минута
    public static final int HOUR  =  103;  // час
    public static final int DAY   =  104;  // день
    public static final int WEEK  =  105;  // неделя
    public static final int MONTH =  106;  // месяц
    public static final int YEAR  =  107;  // год

    public static final int mW    =  201;  // милли-ватт (мВт)
    public static final int W     =  202;  // ватт (Вт)
    public static final int kW    =  203;  // кило-ватт (кВт)

    public static final int RUBLE =  301;  // рубль


    //////////////////////////////////////////////////////////////////
    // КОЛИЧЕСТВЕННЫЕ ОТНОШЕНИЯ ОДНОРОДНЫХ ВЕЛИЧИН ИЗМЕРЕНИЯ //  |  //
    ///////////////////////////////////////////////////////////  V  //

    public static final double SECS_IN_HOUR    = 3600;  // кол-во секуна в 1-м часе
    public static final double SECS_IN_MIN     = 60;
    public static final double MINS_IN_HOUR    = 60;
    public static final double HOURS_IN_DAY    = 24;
    public static final double DAYS_IN_WEEK    = 7;
    public static final double DAYS_IN_MONTH   = 30;
    public static final double MONTHS_IN_YEAR  = 12;
    public static final double DAYS_IN_YEAR    = 365;

    public static final double HOURS_IN_SEC = 1/SECS_IN_HOUR;
    public static final double HOURS_IN_MIN = 1/MINS_IN_HOUR;
    public static final double HOURS_IN_WEEK = HOURS_IN_DAY*DAYS_IN_WEEK;
    public static final double HOURS_IN_MONTH = HOURS_IN_DAY*DAYS_IN_MONTH;
    public static final double HOURS_IN_YEAR = HOURS_IN_DAY*DAYS_IN_YEAR;

    public static final double mW_in_W = 1000;        // кол-во мВт в 1-м Вт
    public static final double W_in_kW = 1000;        // кол-во Вт в 1-м кВт
    public static final double W_in_mW = 1/mW_in_W;   // кол-во Вт в 1-м мВт
    public static final double kW_in_mW = 1/W_in_kW/mW_in_W;    // кол-во кВт в 1-м мВт
    public static final double kW_in_W = 1/W_in_kW;             // кол-во кВт в 1-м Вт


    public static boolean isTime(int value_type) {
        if (value_type < SEC) return false;
        if (value_type > YEAR) return false;
        return true;
    }

    public static boolean isPower(int value_type) {
        if (value_type < mW) return false;
        if (value_type > kW) return false;
        return true;
    }

    public static String get_name(int value_type) {
        switch(value_type) {
            case NONE:  return "";
            case SEC:   return "сек";
            case MIN:   return "мин";
            case HOUR:  return "ч";
            case DAY:   return "дн";
            case WEEK:  return "нед";
            case MONTH: return "мес";
            case YEAR:  return "лет";
            case mW:    return "мВт";
            case W:     return "Вт";
            case kW:    return "кВт";
            case RUBLE: return "руб";
            default:
                throw new IllegalArgumentException(
                    "Argument <value_type> = "
                            +value_type
                            +" cannot be handled by this function");
        }
    }


    /** Перевести значение времени из любой доступной единицы времени в часы.
     *
     * Примеры:
     *      2  (дня)   = convert_time_to_hr(2,  ValueType.DAY)  = 48 (часов)
     *      30 (минут) = convert_time_to_hr(30, ValueType.MIN) = 0.5 (часов)
     *
     *
     * @param value_num - значение времени (количественная характеристика)
     * @param value_type - значение времени (величина измерения)
     * @return (int) - значение времени (количественная характеристика) в часах.
     * @throws IllegalArgumentException
     */
    public static double convert_time_to_hr(double value_num, int value_type)
            throws IllegalArgumentException {

        double multiplier = 0;
        switch (value_type) {
            case SEC:   multiplier = HOURS_IN_SEC;   break;
            case MIN:   multiplier = HOURS_IN_MIN;   break;
            case HOUR:  multiplier = 1;              break;
            case DAY:   multiplier = HOURS_IN_DAY;   break;
            case WEEK:  multiplier = HOURS_IN_WEEK;  break;
            case MONTH: multiplier = HOURS_IN_MONTH; break;
            case YEAR:  multiplier = HOURS_IN_YEAR;  break;
            default:
                throw new IllegalArgumentException(
                        "Argument <value_type> = "
                                +value_type
                                +" cannot be handled by this function");
        }
        return value_num*multiplier;
    }



    /** Перевести значение мощности из любой доступной единицы мощности в кВт.
     *
     * Примеры:
     *      2 000 000 (мВт) = convert_power_to_W(2000000, ValueType.mW) = 2 (кВт)
     *      3 000    (Вт)   = convert_power_to_W(3000,    ValueType.kW) = 3 (кВт)
     *
     *
     * @param value_num  - значение мощности (количественная характеристика)
     * @param value_type - значение мощности (величина измерения)
     * @return (int) - значение мощности (количественная характеристика) в кВт.
     * @throws IllegalArgumentException
     */
    public static double convert_power_to_kW(double value_num, int value_type)
            throws IllegalArgumentException {

        double multiplier = 0;
        switch (value_type) {
            case mW:  multiplier = kW_in_mW;   break;
            case W:   multiplier = kW_in_W;    break;
            case kW:  multiplier = 1;          break;
            default:
                throw new IllegalArgumentException(
                        "Argument <value_type> = "
                                +value_type
                                +" cannot be handled by this function");
        }
        return value_num*multiplier;
    }


    public static SpinnerAdapter get_spinner_adapter_for_time(Context _context) {
        int[] arr = {SEC, MIN, HOUR, DAY, WEEK, MONTH, YEAR};
        return new SpinnerAdapter(_context, arr);
    }


    public static SpinnerAdapter get_spinner_adapter_for_power(Context _context) {
        int[] arr = {mW, W, kW};
        return new SpinnerAdapter(_context, arr);
    }



    public static class SpinnerAdapter extends BaseAdapter{
        Context ctx;
        LayoutInflater lInflater;
        int[] vltArr;  // массив идентификаторов используемых величин измерения

        SpinnerAdapter(Context _context, int[] _vltArr) {
            ctx = _context;
            vltArr = _vltArr;
            lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        // кол-во элементов
        @Override
        public int getCount() {
            return vltArr.length;
        }

        // элемент по позиции
        @Override
        public Object getItem(int position) {
            return vltArr[position];
        }

        // id по позиции
        @Override
        public long getItemId(int position) {
            return vltArr[position];
        }

        // пункт списка
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // используем созданные, но не используемые view
            View view = convertView;
            if (view == null) {
                //view = lInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                view = lInflater.inflate(R.layout.spinner_style, parent, false);
            }

            int vlt = (int)getItem(position);

            // заполняем View в пункте списка
            ((TextView) view.findViewById(android.R.id.text1)).setText(ValueTypes.get_name(vlt));

            return view;
        }

        public int getPosition(int value) {
            for (int i=0; i<vltArr.length; i++) {
                if (vltArr[i] == value) return i;
            }
            throw new IllegalArgumentException("[value] = "+value+" is not exits in current adapter");
        }
    }
}
