package com.tgf.user.personalaccountant;

import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class DeviceActivity extends AppCompatActivity implements View.OnClickListener{

    EditText name;
    EditText power_num;
    Spinner power_vlt;
    EditText amount;
    EditText usfreq_num;
    Spinner usfreq_vlt_nmr;
    Spinner usfreq_vlt_dnmr;
    Button btn_ok;
    Button btn_cancel;
    Button btn_help;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        name = (EditText) findViewById(R.id.editdev_name);
        power_num = (EditText)findViewById(R.id.editdev_power_num);
        power_vlt = (Spinner)findViewById(R.id.editdev_power_vlt_spn);
        amount = (EditText)findViewById(R.id.editdev_amount);
        usfreq_num = (EditText)findViewById(R.id.editdev_usfreq_num);
        usfreq_vlt_nmr = (Spinner)findViewById(R.id.editdev_usfreq_vlt_nmr_spn);
        usfreq_vlt_dnmr = (Spinner)findViewById(R.id.editdev_usfreq_vlt_dnmr_spn);

        power_vlt.setAdapter(ValueTypes.get_spinner_adapter_for_power(this));
        usfreq_vlt_nmr.setAdapter(ValueTypes.get_spinner_adapter_for_time(this));
        usfreq_vlt_dnmr.setAdapter(ValueTypes.get_spinner_adapter_for_time(this));

        btn_ok = (Button)findViewById(R.id.editdev_btn_ok);
        btn_cancel = (Button)findViewById(R.id.editdev_btn_cancel);
        btn_help = (Button)findViewById(R.id.editdev_btn_help);

        btn_ok.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_help.setOnClickListener(this);

        if ( MemoryModule.isNewDevice )
            MemoryModule.currentDevice = new Device("dev", 100, ValueTypes.W,
                    1, 24, ValueTypes.HOUR, ValueTypes.DAY);

        setAttributesAccordingToMode();

        showDeviceData();
    }

    /** Устанавливает видимость и активность элементов управления в соответствии с
     *      режимом запуска текущей активити.
     *
     *      (режим запуска задается в родительской активити)
     *      (данные по режиму запуска хранятся в MemoryModule)
     *
     */
    protected void setAttributesAccordingToMode() {

        if ( MemoryModule.isEditableDevice ) {
            // РЕЖИМ РЕДАКТИРОВАНИЯ
            btn_ok.setEnabled(true);
            name.setEnabled(true);
            power_num.setEnabled(true);
            power_vlt.setEnabled(true);
            amount.setEnabled(true);
            usfreq_num.setEnabled(true);
            usfreq_vlt_nmr.setEnabled(true);
            usfreq_vlt_dnmr.setEnabled(true);

        } else {
            // РЕЖИМ ПРОСМОТРА
            btn_ok.setEnabled(false);
            name.setEnabled(false);
            power_num.setEnabled(false);
            power_vlt.setEnabled(false);
            amount.setEnabled(false);
            usfreq_num.setEnabled(false);
            usfreq_vlt_nmr.setEnabled(false);
            usfreq_vlt_dnmr.setEnabled(false);
        }
    }

    protected void showDeviceData() {
            name.setText(MemoryModule.currentDevice.name);
            power_num.setText(String.valueOf(MemoryModule.currentDevice.power_num));
            power_vlt.setSelection(
                    ( (ValueTypes.SpinnerAdapter)power_vlt.getAdapter() )
                            .getPosition(MemoryModule.currentDevice.power_vlt));
            amount.setText(String.valueOf(MemoryModule.currentDevice.amount));
            usfreq_num.setText(String.valueOf(MemoryModule.currentDevice.usage_freq_num));
            usfreq_vlt_nmr.setSelection(
                    ( (ValueTypes.SpinnerAdapter)usfreq_vlt_nmr.getAdapter() )
                            .getPosition(MemoryModule.currentDevice.usage_freq_vlt_nmr));
            usfreq_vlt_dnmr.setSelection(
                    ( (ValueTypes.SpinnerAdapter)usfreq_vlt_dnmr.getAdapter() )
                            .getPosition(MemoryModule.currentDevice.usage_freq_vlt_dnmr));
    }

    protected void parseDataFromActivityToDevice(Device device) {
        device.name = name.getText().toString();
        device.power_num = Double.parseDouble(power_num.getText().toString());
        device.power_vlt = (int)power_vlt.getSelectedItem();
        device.amount = Integer.parseInt(amount.getText().toString());
        device.usage_freq_num = Double.parseDouble(usfreq_num.getText().toString());
        device.usage_freq_vlt_nmr = (int)usfreq_vlt_nmr.getSelectedItem();
        device.usage_freq_vlt_dnmr = (int)usfreq_vlt_dnmr.getSelectedItem();
    }

    @Override
    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.editdev_btn_ok:
                if ( MemoryModule.isEditableDevice ) {
                    parseDataFromActivityToDevice(MemoryModule.currentDevice);
                }
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.editdev_btn_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.editdev_btn_help:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.editdev_str_help_message);
                AlertDialog alert = builder.create();
                alert.show();
                break;
            default:
                break;
        }
    }
}
