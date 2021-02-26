package com.example.VisualStick;

import android.app.Activity;
import com.phidget22.*;
import java.io.IOException;

public class VisualStick extends Activity {

	public static VoltageRatioInputSensorChangeListener onVoltageRatioInput_SensorChange =
			new VoltageRatioInputSensorChangeListener() {
				@Override
				public void onSensorChange(VoltageRatioInputSensorChangeEvent e) {
					System.out.println("SensorValue: " + e.getSensorValue());
					System.out.println("SensorUnit: " + e.getSensorUnit().symbol);
					System.out.println("----------");
				}
			};

	public static AttachListener onVoltageRatioInput_Attach =
			new AttachListener() {
				@Override
				public void onAttach(AttachEvent e) {
					System.out.println("Attach!");
				}
			};

	public static DetachListener onVoltageRatioInput_Detach =
			new DetachListener() {
				@Override
				public void onDetach(DetachEvent e) {
					System.out.println("Detach!");
				}
			};

	public static ErrorListener onVoltageRatioInput_Error =
			new ErrorListener() {
				@Override
				public void onError(ErrorEvent e) {
					System.out.println("Code: " + e.getCode().name());
					System.out.println("Description: " + e.getDescription());
					System.out.println("----------");
				}
			};

	public static void main(String[] args) throws InterruptedException,IOException {
		try {
			Log.enable(LogLevel.INFO, "phidgetlog.log");
			//Create your Phidget channels
			VoltageRatioInput voltageRatioInput0 = new VoltageRatioInput();
			VoltageRatioInput voltageRatioInput1 = new VoltageRatioInput();

			//Set addressing parameters to specify which channel to open (if any)
			//Assign any event handlers you need before calling open so that no events are missed.
			voltageRatioInput0.addSensorChangeListener(onVoltageRatioInput_SensorChange);
			voltageRatioInput0.addAttachListener(onVoltageRatioInput_Attach);
			voltageRatioInput0.addDetachListener(onVoltageRatioInput_Detach);
			voltageRatioInput0.addErrorListener(onVoltageRatioInput_Error);

			//Open your Phidgets and wait for attachment
			voltageRatioInput0.open(5000);

			//Do stuff with your Phidgets here or in your event handlers.
			if (voltageRatioInput0.getSensorValue() > 4){

				voltageRatioInput1.setChannel(1);
				voltageRatioInput1.addSensorChangeListener(onVoltageRatioInput_SensorChange);
				voltageRatioInput1.addAttachListener(onVoltageRatioInput_Attach);
				voltageRatioInput1.addDetachListener(onVoltageRatioInput_Detach);
				voltageRatioInput1.addErrorListener(onVoltageRatioInput_Error);
				voltageRatioInput1.open(5000);
				voltageRatioInput1.setSensorType(VoltageRatioSensorType.PN_1106);
			}
			//Set the sensor type to match the analog sensor you are using after opening the Phidget
			voltageRatioInput0.setSensorType(VoltageRatioSensorType.PN_1128);

			//Wait until Enter has been pressed before exiting
			System.in.read();
			Thread.sleep(5000);

			//Close your Phidgets once the program is done.
			voltageRatioInput0.close();
			voltageRatioInput1.close();

		} catch (PhidgetException ex) {
			//We will catch Phidget Exceptions here, and print the error informaiton.§
			ex.printStackTrace();
			System.out.println("");
			System.out.println("PhidgetException " + ex.getErrorCode() + " (" + ex.getDescription() + "): " + ex.getDetail());
		}
	}

}

