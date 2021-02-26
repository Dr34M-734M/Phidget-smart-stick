package com.example.PhidgetVoltageInputExample;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class VoltageInputExample extends Activity {

	VoltageInput ch;
	SeekBar dataIntervalBar;
	Spinner voltageRangeSpinner;
	Spinner sensorTypeSpinner;
	Spinner powerSupplySpinner;
	CheckBox isHubPortBox;

	boolean isHubPort;

	Toast errToast;

	int minDataInterval;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Hide device information and settings until one is attached
		LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);
		settingsAndData.setVisibility(LinearLayout.GONE);

		//set data interval seek bar functionality
		dataIntervalBar = (SeekBar) findViewById(R.id.dataIntervalBar);
		dataIntervalBar.setOnSeekBarChangeListener(new dataIntervalChangeListener());

		//set voltage range spinner functionality
		voltageRangeSpinner = (Spinner) findViewById(R.id.voltageRangeSpinner);
		voltageRangeSpinner.setOnItemSelectedListener(new voltageRangeChangeListener());
		LinearLayout voltageRangeSection = (LinearLayout) findViewById(R.id.voltageRangeSection);
		voltageRangeSection.setVisibility(LinearLayout.GONE);

		//set sensor type spinner functionality
		sensorTypeSpinner = (Spinner) findViewById(R.id.sensorTypeSpinner);
		sensorTypeSpinner.setOnItemSelectedListener(new sensorTypeChangeListener());
		LinearLayout sensorTypeSection = (LinearLayout) findViewById(R.id.sensorTypeSection);
		sensorTypeSection.setVisibility(LinearLayout.GONE);

		//set power supply spinner functionality
		powerSupplySpinner = (Spinner) findViewById(R.id.powerSupplySpinner);
		powerSupplySpinner.setOnItemSelectedListener(new powerSupplyChangeListener());
		LinearLayout powerSupplySection = (LinearLayout) findViewById(R.id.powerSupplySection);
		powerSupplySection.setVisibility(LinearLayout.GONE);

		//Voltage visible and sensor value not by default
		((LinearLayout) findViewById(R.id.voltageInfo)).setVisibility(LinearLayout.VISIBLE);
		((LinearLayout) findViewById(R.id.sensorInfo)).setVisibility(LinearLayout.GONE);

        //set up "is hub port" functionality
        isHubPortBox = (CheckBox) findViewById(R.id.isHubPortBox);
        isHubPortBox.setOnCheckedChangeListener(new isHubPortChangeListener());

        try
        {
        	ch = new VoltageInput();

        	//Allow direct USB connection of Phidgets
			if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST))
                com.phidget22.usb.Manager.Initialize(this);

			//Enable server discovery to list remote Phidgets
			this.getSystemService(Context.NSD_SERVICE);
			Net.enableServerDiscovery(ServerType.DEVICE_REMOTE);

            //Remember isHubPort setting
            if (savedInstanceState != null) {
                isHubPort = savedInstanceState.getBoolean("isHubPort");
            } else {
                isHubPort = false;
            }

            ch.setIsHubPortDevice(isHubPort);

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

			ch.addVoltageChangeListener(new VoltageInputVoltageChangeListener() {
				public void onVoltageChange(VoltageInputVoltageChangeEvent voltageChangeEvent) {
                    VoltageInputVoltageChangeEventHandler handler = new VoltageInputVoltageChangeEventHandler(ch, voltageChangeEvent);
                    runOnUiThread(handler);
   		        }
			});

			ch.addSensorChangeListener(new VoltageInputSensorChangeListener() {
				public void onSensorChange(VoltageInputSensorChangeEvent sensorChangeEvent) {
					VoltageInputSensorChangeEventHandler handler = new VoltageInputSensorChangeEventHandler(ch, sensorChangeEvent);
					runOnUiThread(handler);
				}
			});

			ch.open();
        } catch (PhidgetException pe) {
	        pe.printStackTrace();
		}

    }

	private class dataIntervalChangeListener implements SeekBar.OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {
			try {
				TextView dataIntervalTxt = (TextView) findViewById(R.id.dataIntervalTxt);
				int dataInterval = progress + minDataInterval;
				dataIntervalTxt.setText(String.valueOf(dataInterval));
				ch.setDataInterval(dataInterval);
			} catch (PhidgetException e) {
				e.printStackTrace();
			}
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {}
	}

	private class voltageRangeChangeListener implements Spinner.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			try {
				ch.setVoltageRange(VoltageRange.valueOf(parentView.getItemAtPosition(position).toString()));
			} catch (PhidgetException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
			
		}

	}

	private class sensorTypeChangeListener implements Spinner.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			try {
				ch.setSensorType(VoltageSensorType.valueOf(parentView.getItemAtPosition(position).toString()));
				if(VoltageSensorType.valueOf(parentView.getItemAtPosition(position).toString()) == VoltageSensorType.VOLTAGE) {
					((LinearLayout) findViewById(R.id.voltageInfo)).setVisibility(LinearLayout.VISIBLE);
					((LinearLayout) findViewById(R.id.sensorInfo)).setVisibility(LinearLayout.GONE);
				} else {
					((LinearLayout) findViewById(R.id.voltageInfo)).setVisibility(LinearLayout.GONE);
					((LinearLayout) findViewById(R.id.sensorInfo)).setVisibility(LinearLayout.VISIBLE);
				}
			} catch (PhidgetException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
			
		}

	}

	private class powerSupplyChangeListener implements Spinner.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			try {
				ch.setPowerSupply(PowerSupply.valueOf(parentView.getItemAtPosition(position).toString()));
			} catch (PhidgetException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
			
		}
	}

    private class isHubPortChangeListener implements CheckBox.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            try {
                runOnUiThread(new DetachEventHandler(ch));
                ch.close();
                ch.setIsHubPortDevice(isChecked);
                ch.open();
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
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

				TextView dataIntervalTxt = (TextView) findViewById(R.id.dataIntervalTxt);
				dataIntervalTxt.setText(String.valueOf(((VoltageInput)ch).getDataInterval()));

				minDataInterval = ((VoltageInput)ch).getMinDataInterval();

				SeekBar dataIntervalBar = (SeekBar) findViewById(R.id.dataIntervalBar);
				dataIntervalBar.setProgress(((VoltageInput)ch).getDataInterval() - minDataInterval);

				//Limit the maximum dataInterval on the SeekBar to 5000 so it remains usable
				if(((VoltageInput)ch).getMaxDataInterval() >= 5000)
                    dataIntervalBar.setMax(5000 - minDataInterval);
				else
                    dataIntervalBar.setMax(((VoltageInput)ch).getMaxDataInterval() - minDataInterval);

				List<VoltageRange> supportedVoltageRanges;
				List<PowerSupply> supportedPowerSupplies;
				switch (ch.getDeviceID()) { //initialize form elements based on detected device
				case PN_ADP1000:
					supportedVoltageRanges = new ArrayList<VoltageRange>();
					supportedVoltageRanges.add(VoltageRange.MILLIVOLTS_400);
					supportedVoltageRanges.add(VoltageRange.VOLTS_2);

					voltageRangeSpinner.setAdapter(new ArrayAdapter<VoltageRange>(getApplicationContext(),
							android.R.layout.simple_spinner_item, supportedVoltageRanges));

					//400mV by default
					voltageRangeSpinner.setSelection(0);

					((LinearLayout)findViewById(R.id.voltageRangeSection)).setVisibility(LinearLayout.VISIBLE);
					break;
				case PN_VCP1000:
					supportedVoltageRanges = new ArrayList<VoltageRange>();
					supportedVoltageRanges.add(VoltageRange.MILLIVOLTS_312_5);
					supportedVoltageRanges.add(VoltageRange.VOLTS_40);

					voltageRangeSpinner.setAdapter(new ArrayAdapter<VoltageRange>(getApplicationContext(),
							android.R.layout.simple_spinner_item, supportedVoltageRanges));

					//40V by default
					voltageRangeSpinner.setSelection(1);

					((LinearLayout)findViewById(R.id.voltageRangeSection)).setVisibility(LinearLayout.VISIBLE);
					break;
				case PN_VCP1001:
					supportedVoltageRanges = new ArrayList<VoltageRange>();
					supportedVoltageRanges.add(VoltageRange.VOLTS_5);
					supportedVoltageRanges.add(VoltageRange.VOLTS_15);
					supportedVoltageRanges.add(VoltageRange.VOLTS_40);
					supportedVoltageRanges.add(VoltageRange.AUTO);

					voltageRangeSpinner.setAdapter(new ArrayAdapter<VoltageRange>(getApplicationContext(),
							android.R.layout.simple_spinner_item, supportedVoltageRanges));

					//AUTO by default
					voltageRangeSpinner.setSelection(3);

					((LinearLayout)findViewById(R.id.voltageRangeSection)).setVisibility(LinearLayout.VISIBLE);
					break;
				case PN_VCP1002:
					supportedVoltageRanges = new ArrayList<VoltageRange>();
					supportedVoltageRanges.add(VoltageRange.MILLIVOLTS_10);
					supportedVoltageRanges.add(VoltageRange.MILLIVOLTS_40);
					supportedVoltageRanges.add(VoltageRange.MILLIVOLTS_200);
					supportedVoltageRanges.add(VoltageRange.MILLIVOLTS_1000);
					supportedVoltageRanges.add(VoltageRange.AUTO);

					voltageRangeSpinner.setAdapter(new ArrayAdapter<VoltageRange>(getApplicationContext(),
							android.R.layout.simple_spinner_item, supportedVoltageRanges));

					//AUTO by default
					voltageRangeSpinner.setSelection(4);

					((LinearLayout)findViewById(R.id.voltageRangeSection)).setVisibility(LinearLayout.VISIBLE);
					break;
				case PN_DAQ1400:
					supportedPowerSupplies = new ArrayList<PowerSupply>();
					supportedPowerSupplies.add(PowerSupply.OFF);
					supportedPowerSupplies.add(PowerSupply.VOLTS_12);
					supportedPowerSupplies.add(PowerSupply.VOLTS_24);

					powerSupplySpinner.setAdapter(new ArrayAdapter<PowerSupply>(getApplicationContext(),
							android.R.layout.simple_spinner_item, supportedPowerSupplies));

					//Off by default
					powerSupplySpinner.setSelection(0);

					((LinearLayout)findViewById(R.id.powerSupplySection)).setVisibility(LinearLayout.VISIBLE);
					break;
				default: //standard 5V sensor port
					if(ch.getChannelSubclass() == ChannelSubclass.VOLTAGE_INPUT_SENSOR_PORT) {
						sensorTypeSpinner.setAdapter(new ArrayAdapter<VoltageSensorType>(getApplicationContext(),
								android.R.layout.simple_spinner_item, VoltageSensorType.values()));

						//Voltage Sensor by default
						sensorTypeSpinner.setSelection(0);

						((LinearLayout)findViewById(R.id.sensorTypeSection)).setVisibility(LinearLayout.VISIBLE);
					}
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
			
			((LinearLayout)findViewById(R.id.voltageRangeSection)).setVisibility(LinearLayout.GONE);
			((LinearLayout)findViewById(R.id.sensorTypeSection)).setVisibility(LinearLayout.GONE);
			((LinearLayout)findViewById(R.id.powerSupplySection)).setVisibility(LinearLayout.GONE);

			//reset voltage visibility
			((LinearLayout) findViewById(R.id.voltageInfo)).setVisibility(LinearLayout.VISIBLE);
			((LinearLayout) findViewById(R.id.sensorInfo)).setVisibility(LinearLayout.GONE);

			//clear voltage information
			((TextView)findViewById(R.id.voltageTxt)).setText("");
			((TextView)findViewById(R.id.sensorValueTxt)).setText("");
			((TextView)findViewById(R.id.sensorUnits)).setText("");

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

	class VoltageInputVoltageChangeEventHandler implements Runnable {
		Phidget ch;
		VoltageInputVoltageChangeEvent voltageChangeEvent;

		public VoltageInputVoltageChangeEventHandler(Phidget ch, VoltageInputVoltageChangeEvent voltageChangeEvent) {
			this.ch = ch;
			this.voltageChangeEvent = voltageChangeEvent;
		}

		public void run() {
		    TextView voltageTxt = (TextView)findViewById(R.id.voltageTxt);

			voltageTxt.setText(String.valueOf(voltageChangeEvent.getVoltage()));
		}
	}

	class VoltageInputSensorChangeEventHandler implements Runnable {
		Phidget ch;
		VoltageInputSensorChangeEvent sensorChangeEvent;

		public VoltageInputSensorChangeEventHandler(Phidget ch, VoltageInputSensorChangeEvent sensorChangeEvent) {
			this.ch = ch;
			this.sensorChangeEvent = sensorChangeEvent;
		}

		public void run() {
			TextView sensorValueTxt = (TextView)findViewById(R.id.sensorValueTxt);
			TextView sensorUnits = (TextView)findViewById(R.id.sensorUnits);

			sensorValueTxt.setText(String.valueOf(sensorChangeEvent.getSensorValue()));
			sensorUnits.setText(sensorChangeEvent.getSensorUnit().symbol);
		}
	}

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putBoolean("isHubPort", isHubPort);
        super.onSaveInstanceState(savedInstanceState);
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

