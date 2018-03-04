package com.tgf.user.personalaccountant;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by akropon on 21.07.2017.
 */

public class AlertDialogYesNo {
    public static boolean show(Context context, String title, String message, String btn_yes_text, String btn_no_text) {
        final boolean[] result = new boolean[1];
        final boolean[] ready  = new boolean[1];
        result[0] = false;
        ready[0]  = false;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        if (title != null) alertDialog.setTitle(title);
        if (message != null) alertDialog.setMessage(message);
        alertDialog.setPositiveButton(btn_yes_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                result[0] = true;
                ready[0]  = true;
            }
        });
        alertDialog.setNegativeButton(btn_no_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                result[0] = false;
                ready[0]  = true;
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();

        while(ready[0] == false)
            try {
                Thread.sleep(1000);
                Log.d("Akropon", "[AlertDialogYesNo] жду");
            } catch (InterruptedException e) {
                Log.d("Akropon", "[AlertDialogYesNo] ошибка ожидания!");
                e.printStackTrace();
            }
        Log.d("Akropon", "[AlertDialogYesNo] готово. Ответ:"+result[0]);
        return result[0];
    }
}
