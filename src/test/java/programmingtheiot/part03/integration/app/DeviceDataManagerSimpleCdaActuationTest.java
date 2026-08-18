/**
 * This class is part of the Programming the Internet of Things project.
 *
 * PIOT-GDA-10-003: verifies DeviceDataManager humidity analysis / actuation.
 */

package programmingtheiot.part03.integration.app;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.gda.app.DeviceDataManager;

/**
 * Simple integration test that feeds humidity SensorData into the
 * DeviceDataManager and checks the log output for actuation events.
 */
public class DeviceDataManagerSimpleCdaActuationTest
{
	private static final Logger _Logger =
		Logger.getLogger(DeviceDataManagerSimpleCdaActuationTest.class.getName());

	@BeforeClass
	public static void setUpBeforeClass() throws Exception { }

	@AfterClass
	public static void tearDownAfterClass() throws Exception { }

	@Before
	public void setUp() throws Exception { }

	@After
	public void tearDown() throws Exception { }

	@Test
	public void testSendActuationEventsToCda()
	{
		DeviceDataManager devDataMgr = new DeviceDataManager();
		devDataMgr.startManager();

		ConfigUtil cfgUtil = ConfigUtil.getInstance();

		float nominalVal = cfgUtil.getFloat(ConfigConst.GATEWAY_DEVICE,   "nominalHumiditySetting");
		float lowVal     = cfgUtil.getFloat(ConfigConst.GATEWAY_DEVICE,   "triggerHumidifierFloor");
		float highVal    = cfgUtil.getFloat(ConfigConst.GATEWAY_DEVICE,   "triggerHumidifierCeiling");
		int   delay      = cfgUtil.getInteger(ConfigConst.GATEWAY_DEVICE, "humidityMaxTimePastThreshold");

		_Logger.info("Using humidity time threshold (seconds): " + delay);

		generateAndProcessHumiditySensorDataSequence(
			devDataMgr, nominalVal, lowVal, highVal, delay);

		devDataMgr.stopManager();
	}

	private void generateAndProcessHumiditySensorDataSequence(
		DeviceDataManager ddm, float nominalVal, float lowVal, float highVal, int delay)
	{
		SensorData sd = new SensorData();
		sd.setName("My Test Humidity Sensor");
		sd.setLocationID("constraineddevice001");
		sd.setTypeID(ConfigConst.HUMIDITY_SENSOR_TYPE);

		// two nominal readings - no actuation expected
		sd.setValue(nominalVal);
		ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
		waitForSeconds(2);

		sd.setValue(nominalVal);
		ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
		waitForSeconds(2);

		// low readings past the time threshold - expect ON actuation
		sd.setValue(lowVal - 2);
		ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
		waitForSeconds(delay + 1);

		sd.setValue(lowVal - 1);
		ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
		waitForSeconds(delay + 1);

		// back into range - expect OFF actuation
		sd.setValue(lowVal + 1);
		ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
		waitForSeconds(delay + 1);

		sd.setValue(nominalVal);
		ddm.handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, sd);
		waitForSeconds(delay + 1);
	}

	private void waitForSeconds(int seconds)
	{
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			// ignore
		}
	}
}
