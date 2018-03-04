package com.tgf.user.personalaccountant;

/**
 * Created by Akropon on 01.07.2017.
 */

public class UsefulFunctions {
    /**
     * Получить строковое представление числа с плавающей запятой с заданной точностью
     * в обыном десятичном виде.
     *
     * Точность - кол-во знаков ПОСЛЕ запятой (если значение отрицательное, значит идет округление до десятков, сотен, тысяч...)
     *
     * #Пример:
     *  getNonExponentialForm(1.3234, 2) = "1.32"
     *  getNonExponentialForm(-0.000123, 1) = "-0.0"
     *  getNonExponentialForm(15.12, 5) = "15.12000"
     *  getNonExponentialForm(1е-2, 5) = "0.01000"
     *  getNonExponentialForm(163.456, -2) = "200"
     *
     * @param value - (double) исходное число с плавающей запятой
     * @param accuracy - (int, +) точноть
     * @return - (String)
     */
    public static String getNonExponentialForm(double value, int accuracy) {
        if (accuracy >= 0)
            return String.format( ("%."+String.valueOf(accuracy)+"f"), value);
        else {
            double multiplier = Math.pow(10, -accuracy);
            value /= multiplier;
            int int_value = (int)Math.round(value);
            int_value *= multiplier;
            return String.valueOf(int_value);
        }
    }



}
