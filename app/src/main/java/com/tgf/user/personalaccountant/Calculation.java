package com.tgf.user.personalaccountant;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Akropon on 28.01.2017.
 */

public class Calculation implements Serializable{

    public String name;
    public ArrayList<Device> devicesList;
    public double tariff;     // стоимость в Денежных Единицах одного кВ*часа
    public double period_num; // временной период (количественная характеристика)
    public int period_vlt;    // временной период (величина измерения)
    public int database_row_id; // id соответствующей записи в таблице CALCULATIONS в базе данных

    public Calculation() {
        name = "calc";
        devicesList = new ArrayList<Device>();
        tariff = 5;
        period_num = 1;
        period_vlt = ValueTypes.MONTH;
        database_row_id = -1;
    }



    /** Стоимость (в Денежных Единицах) электроэнергии, потраченной всеми приборами в расчете при указанном Тарифе.
     *
     *
     * Тариф - стоимость (в Денежных Единицах) одного кВт-час
     * Выбор Денежной Единицы не влияет на результат выполнения функции.
     * Возвращаемая стоимость электроэнергии будет выражаться в тех же Денежных Единицах,
     * в которых указан Тариф.
     *
     * @return (double) - стоимость (в Денежных Единицах)
     */
    public double get_cost() {
        if (devicesList.size() == 0 || tariff == 0 || period_num == 0)
            return 0;
        double cost = 0;
        for (Device device : devicesList) {  // конструкция типа "foreach"
            cost += device.get_cost(tariff, period_num, period_vlt);
        }
        return cost;
    }

    /**
     * Создает клона.
     *
     * Все подобъекты рекурсивно клонируются.
     *
     * @return
     */
    public Calculation get_clone() {
        Calculation clone = new Calculation();
        clone.name = name;
        clone.period_num = period_num;
        clone.period_vlt = period_vlt;
        clone.tariff = tariff;
        clone.devicesList = new ArrayList<>();
        for ( Device device : devicesList ) {
            clone.devicesList.add(device.get_clone());
        }
        clone.database_row_id = database_row_id;
        return clone;
    }


    /**
     * Создает клона для указанного списка
     *
     * Все подобъекты рекурсивно клонируются.
     *
     * @return
     */
    public static ArrayList<Calculation> get_calculations_list_clone(
            ArrayList<Calculation> from) {
        ArrayList<Calculation> cloneList = new ArrayList<>();
        for ( Calculation calc : from) {
            cloneList.add(calc.get_clone());
        }
        return cloneList;
    }
}
