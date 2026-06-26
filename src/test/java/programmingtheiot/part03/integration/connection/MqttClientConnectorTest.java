/**
 * This class is part of the Programming the Internet of Things project.
 */

package programmingtheiot.part03.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.*;

public class MqttClientConnectorTest
{
	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnectorTest.class.getName());

	private MqttClientConnector mqttClient = null;

	@Before
	public void setUp() throws Exception
	{
		this.mqttClient = new MqttClientConnector();
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testConnectAndDisconnect()
	{
		int delay = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);

		assertTrue(this.mqttClient.connectClient());
		assertFalse(this.mqttClient.connectClient());

		try {
			Thread.sleep(delay * 1000 + 5000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.disconnectClient());
		assertFalse(this.mqttClient.disconnectClient());
	}

//	@Test
	public void testPublishAndSubscribe()
	{
		int qos = 0;
		int delay = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);

		assertTrue(this.mqttClient.connectClient());
		assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos));
		assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos));
		assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos));
		assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos));

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 1.", qos));
		assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 2.", qos));
		assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 3.", qos));

		try {
			Thread.sleep(25000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE));
		assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE));
		assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE));
		assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE));

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			// ignore
		}

		try {
			Thread.sleep(delay * 1000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.disconnectClient());
	}

//	@Test
	public void testPublishAndSubscribeTwoClients()
	{
		int qos = 0;

		IDataMessageListener listener = new MqttPublishDataMessageListener(ResourceNameEnum.GDA_MGMT_STATUS_CMD_RESOURCE, true);

		this.mqttClient.setDataMessageListener(listener);

		assertTrue(this.mqttClient.connectClient());

		assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos));
		assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_CMD_RESOURCE, 0));

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 1.", qos));
		assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 2.", qos));
		assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 3.", qos));

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE));
		assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_CMD_RESOURCE));

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.disconnectClient());
	}

//	@Test
	public void testIntegrateWithCdaPublishCdaCmdTopic()
	{
		int qos = 1;

		assertTrue(this.mqttClient.connectClient());
		assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.CDA_MGMT_STATUS_CMD_RESOURCE, "TEST: This is the CDA command payload.", qos));

		try {
			Thread.sleep(60000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.disconnectClient());
	}

//	@Test
	public void testIntegrateWithCdaSubscribeCdaMgmtTopic()
	{
		int qos = 1;
		int delay = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);

		assertTrue(this.mqttClient.connectClient());
		assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, qos));

		try {
			Thread.sleep(delay * 1000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE));

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			// ignore
		}

		assertTrue(this.mqttClient.disconnectClient());
	}
}
