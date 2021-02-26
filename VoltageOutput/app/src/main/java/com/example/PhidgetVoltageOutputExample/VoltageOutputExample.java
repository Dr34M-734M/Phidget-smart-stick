package com.example.PhidgetVoltageOutputExample;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.phidget22.*;

import java.util.ArrayList;
import java.util.List;

public class VoltageOutputExample extends Activity {

	VoltageOutput ch;
	SeekBar voltageBar;
    CheckBox enabledBox;
    Spinner voltageRangeSpinner;

	Toast errToast;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Hide device information and settings until one is attached
		LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);
		settingsAndData.setVisibility(LinearLayout.GONE);
		((LinearLayout)findViewById(R.id.voltageSection)).setVisibility(LinearLayout.GONE);

		//set data interval seek bar functionality
		voltageBar = (SeekBar) findViewById(R.id.voltageBar);
		voltageBar.setOnSeekBarChangeListener(new voltageChangeListener());

        //set up enabled functionality
        enabledBox = (CheckBox) findViewById(R.id.enabledBox);
        enabledBox.setOnCheckedChangeListener(new enabledChangeListener());
        LinearLayout enabledSection = (LinearLayout) findViewById(R.id.enabledSection);
        enabledSection.setVisibility(LinearLayout.GONE);

		//set voltage range spinner functionality
		voltageRangeSpinner = (Spinner) findViewById(R.id.voltageRangeSpinner);
		voltageRangeSpinner.setOnItemSelectedListener(new voltageRangeChangeListener());
		LinearLayout voltageRangeSection = (LinearLayout) findViewById(R.id.voltageRangeSection);
        voltageRangeSection.setVisibility(LinearLayout.GONE);


		try
        {
        	ch = new VoltageOutput();

        	//Allow direct USB connection of Phidgets
			if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST))
                com.phidget22.usb.Manager.Initialize(this);

			//Enable server discovery to list remote Phidgets
			this.getSystemService(Context.NSD_SERVICE);
			Net.enableServerDiscovery(ServerType.DEVICE_REMOTE);

			ch.addAttachListener(new AttachListener() {
				public void onAttach(final AttachEvent attachEvent) {
				    AttachEventHandler handler = new AttachEventHandler(ch);
                    synchronized(handler)
					{
						runOnUiThread(handler);
						try {
							handler.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});

			ch.addDetachListener(new DetachListener() {
				public void onDetach(final DetachEvent detachEvent) {
                    DetachEventHandler handler = new DetachEventHandler(ch);
                    synchronized(handler)
					{
						runOnUiThread(handler);
						try {
							handler.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});

			ch.addErrorListener(new ErrorListener() {
				public void onError(final ErrorEvent errorEvent) {
					ErrorEventHandler handler = new ErrorEventHandler(ch, errorEvent);
					runOnUiThread(handler);
				}
			});

			ch.open();
        } catch (PhidgetException pe) {
	        pe.printStackTrace();
		}

    }

	private class voltageChangeListener implements SeekBar.OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			try {
				TextView voltageTxt = (TextView) findViewById(R.id.voltageTxt);
                double voltage = ((double)progress/seekBar.getMax()) *
                        (ch.getMaxVoltage() - ch.getMinVoltage()) + ch.getMinVoltage();

                //Limit to 3 decimal places
                voltage = Math.round(voltage * 1000) / 1000.0;

                voltageTxt.setText(String.valueOf(voltage));
				ch.setVoltage(voltage);

			} catch (PhidgetException e) {
				e.printStackTrace();
			}
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {}
	}

    private class enabledChangeListener implements CheckBox.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            try {
                ch.setEnabled(isChecked);
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
        }
    }

	private class voltageRangeChangeListener implements Spinner.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			try {
				ch.setVoltageOutputRange(VoltageOutputRange.valueOf(parentView.getItemAtPosition(position).toString()));
			} catch (PhidgetException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
			
		}

	}

    class AttachEventHandler implements Runnable { 
    	Phidget ch;

		public AttachEventHandler(Phidget ch) {
			this.ch = ch;
		}

		public void run() {
			LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);
			settingsAndData.setVisibility(LinearLayout.VISIBLE);

			TextView attachedTxt = (TextView) findViewById(R.id.attachedTxt);

			attachedTxt.setText("Attached");
			try {
				TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
				TextView serialTxt = (TextView) findViewById(R.id.serialTxt);
				TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
				TextView channelTxt = (TextView) findViewById(R.id.channelTxt);
				TextView hubPortTxt = (TextView) findViewById(R.id.hubPortTxt);
				TextView labelTxt = (TextView) findViewById(R.id.labelTxt);

				nameTxt.setText(ch.getDeviceName());
				serialTxt.setText(Integer.toString(ch.getDeviceSerialNumber()));
				versionTxt.setText(Integer.toString(ch.getDeviceVersion()));
				channelTxt.setText(Integer.toString(ch.getChannel()));
				hubPortTxt.setText(Integer.toString(ch.getHubPort()));
				labelTxt.setText(ch.getDeviceLabel());

				TextView voltageTxt = (TextView) findViewById(R.id.voltageTxt);
				voltageTxt.setText(String.valueOf(((VoltageOutput)ch).getVoltage()));

                double maxVoltage = ((VoltageOutput)ch).getMaxVoltage();
                double minVoltage = ((VoltageOutput)ch).getMinVoltage();
                double voltage = ((VoltageOutput)ch).getVoltage();

				SeekBar voltageBar = (SeekBar) findViewById(R.id.voltageBar);
                voltageBar.setProgress((int)((voltage - minVoltage)/(maxVoltage - minVoltage) * voltageBar.getMax()));

                ((LinearLayout)findViewById(R.id.voltageSection)).setVisibility(LinearLayout.VISIBLE);

				List<VoltageOutputRange> supportedVoltages;
				switch (ch.getDeviceID()) { //initialize form elements based on detected device
					case PN_OUT1001:
					case PN_OUT1002:
						supportedVoltages = new ArrayList<VoltageOutputRange>();
						supportedVoltages.add(VoltageOutputRange.VOLTS_10);
                        supportedVoltages.add(VoltageOutputRange.VOLTS_5);

						voltageRangeSpinner.setAdapter(new ArrayAdapter<VoltageOutputRange>(getApplicationContext(),
								android.R.layout.simple_spinner_item, supportedVoltages));

						//10V mode by default
						voltageRangeSpinner.setSelection(0);

                        ((LinearLayout)findViewById(R.id.voltageRangeSection)).setVisibility(LinearLayout.VISIBLE);
						break;
                    case PN_1002:
                    case PN_OUT1000:
                        enabledBox.setChecked(((VoltageOutput)ch).getEnabled());
                        ((LinearLayout)findViewById(R.id.enabledSection)).setVisibility(LinearLayout.VISIBLE);
                        break;
				}
			} catch (PhidgetException e) {
				e.printStackTrace();
			}

			//notify that we're done
			synchronized(this)
			{
				this.notify();
			}
		}
    }
    
    class DetachEventHandler implements Runnable {
    	Phidget ch;
    	
    	public DetachEventHandler(Phidget ch) {
    		this.ch = ch;
    	}
    	
		public void run() {
			LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);

			settingsAndData.setVisibility(LinearLayout.GONE);

			TextView attachedTxt = (TextView) findViewById(R.id.attachedTxt);
			attachedTxt.setText("Detached");

			TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
			TextView serialTxt = (TextView) findViewById(R.id.serialTxt);
			TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
			TextView channelTxt = (TextView) findViewById(R.id.channelTxt);
			TextView hubPortTxt = (TextView) findViewById(R.id.hubPortTxt);
			TextView labelTxt = (TextView) findViewById(R.id.labelTxt);

			nameTxt.setText(R.string.unknown_val);
			serialTxt.setText(R.string.unknown_val);
			versionTxt.setText(R.string.unknown_val);
			channelTxt.setText(R.string.unknown_val);
			hubPortTxt.setText(R.string.unknown_val);
			labelTxt.setText(R.string.unknown_val);

			((LinearLayout)findViewById(R.id.voltageSection)).setVisibility(LinearLayout.GONE);
			((LinearLayout)findViewById(R.id.voltageRangeSection)).setVisibility(LinearLayout.GONE);
            ((LinearLayout)findViewById(R.id.enabledSection)).setVisibility(LinearLayout.GONE);
			
			//notify that we're done
			synchronized(this)
			{
				this.notify();
			}
		}
    }

	class ErrorEventHandler implements Runnable {
		Phidget ch;
		ErrorEvent errorEvent;

		public ErrorEventHandler(Phidget ch, ErrorEvent errorEvent) {
			this.ch = ch;
			this.errorEvent = errorEvent;
		}

		public void run() {
			 if (errToast == null)
				 errToast = Toast.makeText(getApplicationContext(), errorEvent.getDescription(), Toast.LENGTH_SHORT);

			 //replace the previous toast message if a new error occurs
			 errToast.setText(errorEvent.getDescription());
			 errToast.show();
        }
	}

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	try {
			ch.close();

		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		//Disable USB connection to Phidgets
    	if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST))
            com.phidget22.usb.Manager.Uninitialize();
    }

}

