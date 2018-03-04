package com.tgf.user.personalaccountant;

import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;

/** ПРИБОР
 *
 *  Класс, описывающий характеристики прибора.
 *
 * Created by Akropon on 20.01.2017.
 */

public class Device implements Serializable{

    public String name;             // имя прибора
    public double power_num;        // потребляемая мощность прибора (количественная характеристика) (num - NUMber)
    public int    power_vlt;        // потребляемая модность прибора (величина измерения)            (vlt - VaLue Type)
    public int    amount;           // кол-во приборов (количественная характеристика)
    public double usage_freq_num;       // частота использования (количественная характеристика)
    public int    usage_freq_vlt_nmr;   // частота использования (величина измерения в числетеле)    (nmr  - NuMeRator)
    public int    usage_freq_vlt_dnmr;  // частота использования (величина измерения в знаменателе)  (dnmr - DeNuMeRator)


    // Конструктор
    public Device(String name,
                  double power_num,
                  int    power_vlt,
                  int    amount,
                  double usage_freq_num,
                  int    usage_freq_vlt_nmr,
                  int    usage_freq_vlt_dnmr ) {
        this.name      = name;
        this.power_num = power_num;
        this.amount    = amount;
        this.power_vlt = power_vlt;
        this.usage_freq_num      = usage_freq_num;
        this.usage_freq_vlt_nmr  = usage_freq_vlt_nmr;
        this.usage_freq_vlt_dnmr = usage_freq_vlt_dnmr;
    }


    /** Работа(потребленная энергия) электроприбора в кВт-часах
     *  за указанный период времени.
     *
     * @param period_num - временной период (количественная характеристика)
     * @param period_vlt - временной период (величина измерения)
     * @return - (int) кол-во наработанных кВт-часов
     */
    public double get_work_in_kwhr(double period_num, int period_vlt) {

        // временной период в часах
        double period_num_hr = ValueTypes.convert_time_to_hr(period_num, period_vlt);

        // частота использования безразмерная
        double usage_freg_none_vlt =
                usage_freq_num * ValueTypes.convert_time_to_hr(1, usage_freq_vlt_nmr)
                / ValueTypes.convert_time_to_hr(1, usage_freq_vlt_dnmr);

        // общее время работы (в часах) прибора за расчитываемый временной период
        double total_working_time_hr = period_num_hr * usage_freg_none_vlt;

        // Мощность прибора в кВт
        double power_num_kW = ValueTypes.convert_power_to_kW(power_num, power_vlt);

        // Общая работа прибора (в киловатт-часах):
        return power_num_kW*total_working_time_hr;
    }


    /** Стоимость (в Денежных Единицах) электроэнергии, потраченной прибором при указанном Тарифе.
     *
     *
     * Тариф - стоимость (в Денежных Единицах) одного кВт-час
     * Выбор Денежной Единицы не влияет на результат выполнения функции.
     * Возвращаемая стоимость электроэнергии будет выражаться в тех же Денежных Единицах,
     * в которых указан Тариф.
     *
     *
     * @param tariff - Тариф
     * @param period_num - временной период (количественная характеристика)
     * @param period_vlt - временной период (величина измерения)
     * @return (double) - стоимость (в Денежных Единицах)
     */
    public double get_cost(double tariff, double period_num, int period_vlt) {
        return tariff*this.get_work_in_kwhr(period_num, period_vlt);
    }

    public Device get_clone() {
        return new Device(name, power_num, power_vlt, amount, usage_freq_num,
                usage_freq_vlt_nmr, usage_freq_vlt_dnmr);
    }
}
