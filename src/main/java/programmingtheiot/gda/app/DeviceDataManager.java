/**
 * This class is part of the Programming the Internet of Things project.
 */

package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.BaseIotData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.SystemStateData;

import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.ICloudClient;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.CoapClientConnector;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.IRequestResponseClient;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;
import programmingtheiot.gda.connection.SmtpClientConnector;

import programmingtheiot.gda.system.SystemPerformanceManager;

public class DeviceDataManager implements IDataMessageListener
{
	// static

	private static final Logger _Logger =
		Logger.getLogger(DeviceDataManager.class.getName());

	// private var's

	private boolean enableMqttClient        = true;
	private boolean enableCoapServer        = false;
	private boolean enableCoapClient        = false;
	private boolean enableCloudClient       = false;
	private boolean enableSmtpClient        = false;
	private boolean enablePersistenceClient = false;
	private boolean enableSystemPerf        = false;

	private IActuatorDataListener actuatorDataListener = null;
	private IPubSubClient         mqttClient           = null;
	private ICloudClient          cloudClient          = null;
	private IPersistenceClient    persistenceClient    = null;
	private IRequestResponseClient smtpClient         = null;
	private CoapServerGateway     coapServer           = null;
	private CoapClientConnector   coapClient           = null;

	private SystemPerformanceManager sysPerfMgr = null;

	// PIOT-GDA-10-003: humidity threshold crossing state
	private ActuatorData   latestHumidifierActuatorData     = null;
	private ActuatorData   latestHumidifierActuatorResponse = null;
	private SensorData     latestHumiditySensorData         = null;
	private OffsetDateTime latestHumiditySensorTimeStamp    = null;

	private boolean handleHumidityChangeOnDevice = false;
	private int     lastKnownHumidifierCommand   = ConfigConst.OFF_COMMAND;

	private long    humidityMaxTimePastThreshold = 300;
	private float   nominalHumiditySetting       = 40.0f;
	private float   triggerHumidifierFloor       = 30.0f;
	private float   triggerHumidifierCeiling     = 50.0f;


	// constructors

	public DeviceDataManager()
	{
		super();

		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.enableMqttClient =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);

		this.enableCoapServer =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);
		
		this.enableCoapClient =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_CLIENT_KEY);

		this.enableCloudClient =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);

		this.enablePersistenceClient =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);

		// PIOT-GDA-10-003: parse humidity threshold crossing rules from config
		this.handleHumidityChangeOnDevice =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, "handleHumidityChangeOnDevice");

		this.humidityMaxTimePastThreshold =
			configUtil.getInteger(
				ConfigConst.GATEWAY_DEVICE, "humidityMaxTimePastThreshold");

		this.nominalHumiditySetting =
			configUtil.getFloat(
				ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");

		this.triggerHumidifierFloor =
			configUtil.getFloat(
				ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");

		this.triggerHumidifierCeiling =
			configUtil.getFloat(
				ConfigConst.GATEWAY_DEVICE, "triggerHumidifierCeiling");

		// basic validation: keep the timer within a sane range
		if (this.humidityMaxTimePastThreshold < 10 || this.humidityMaxTimePastThreshold > 7200) {
			this.humidityMaxTimePastThreshold = 300;
		}

		initConnections();
	}

	public DeviceDataManager(
		boolean enableMqttClient,
		boolean enableCoapClient,
		boolean enableCloudClient,
		boolean enableSmtpClient,
		boolean enablePersistenceClient)
	{
		super();

		this.enableMqttClient        = enableMqttClient;
		this.enableCoapServer        = enableCoapClient;
		this.enableCloudClient       = enableCloudClient;
		this.enableSmtpClient        = enableSmtpClient;
		this.enablePersistenceClient = enablePersistenceClient;

		initConnections();
	}


	// public methods

	@Override
	public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
	{
		if (data != null) {
			_Logger.info("Handling actuator response: " + data.getName());

			if (data.hasError()) {
				_Logger.warning("Error flag set for ActuatorData instance.");
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
	{
		if (data != null) {
			_Logger.info("Handling actuator request: " + data.getName());

			if (data.hasError()) {
				_Logger.warning("Error flag set for ActuatorData instance.");
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
	{
		if (msg != null) {
			_Logger.info("Handling incoming generic message: " + msg);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
	{
		if (data != null) {
			_Logger.info("Handling sensor message: " + data.getName());

			if (data.hasError()) {
				_Logger.warning("Error flag set for SensorData instance.");
			}

			String jsonData = DataUtil.getInstance().sensorDataToJson(data);

			int qos = ConfigConst.DEFAULT_QOS;

			// PIOT-GDA-10-003: analyze the incoming data, then (stub) send upstream
			this.handleIncomingDataAnalysis(resourceName, data);

			this.handleUpstreamTransmission(resourceName, jsonData, qos);

			// send the SensorData to the cloud (last call - we still have the object here)
			if (this.cloudClient != null) {
				this.cloudClient.sendEdgeDataToCloud(resourceName, data);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
	{
		if (data != null) {
			_Logger.info("Handling system performance message: " + data.getName());

			if (data.hasError()) {
				_Logger.warning("Error flag set for SystemPerformanceData instance.");
			}

			// send the SystemPerformanceData to the cloud (last call)
			if (this.cloudClient != null) {
				this.cloudClient.sendEdgeDataToCloud(resourceName, data);
			}

			return true;
		} else {
			return false;
		}
	}

	public void setActuatorDataListener(String name, IActuatorDataListener listener)
	{
		if (listener != null) {
			this.actuatorDataListener = listener;
		}
	}

	public void startManager()
	{
		_Logger.info("Starting DeviceDataManager...");

		if (this.mqttClient != null) {
			if (this.mqttClient.connectClient()) {
				_Logger.info("Successfully connected MQTT client to broker.");

				int qos = ConfigConst.DEFAULT_QOS;

				// this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos);
				// this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
				// this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
				// this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
			} else {
				_Logger.severe("Failed to connect MQTT client to broker.");
			}
		}

		// NOTE: connect the cloud client BEFORE starting the SystemPerformanceManager
		// so no system performance data is generated before the cloud connection is up.
		if (this.cloudClient != null) {
			if (this.cloudClient.connectClient()) {
				_Logger.info("Successfully connected cloud client to CSP.");
			} else {
				_Logger.severe("Failed to connect cloud client to CSP.");
			}
		}

		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.startManager();
		}

		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.startServer()) {
				_Logger.info("CoAP server started.");
			} else {
				_Logger.severe("Failed to start CoAP server. Check log file for details.");
			}
		}
	}

	public void stopManager()
	{
		_Logger.info("Stopping DeviceDataManager...");

		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.stopManager();
		}

		if (this.mqttClient != null) {
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);

			if (this.mqttClient.disconnectClient()) {
				_Logger.info("Successfully disconnected MQTT client from broker.");
			} else {
				_Logger.severe("Failed to disconnect MQTT client from broker.");
			}
		}

		if (this.cloudClient != null) {
			if (this.cloudClient.disconnectClient()) {
				_Logger.info("Successfully disconnected cloud client from CSP.");
			} else {
				_Logger.severe("Failed to disconnect cloud client from CSP.");
			}
		}

		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.stopServer()) {
				_Logger.info("CoAP server stopped.");
			} else {
				_Logger.severe("Failed to stop CoAP server. Check log file for details.");
			}
		}
	}


	// private methods

	private void initConnections()
	{
		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.enableSystemPerf =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SYSTEM_PERF_KEY);

		if (this.enableSystemPerf) {
			this.sysPerfMgr = new SystemPerformanceManager();
			this.sysPerfMgr.setDataMessageListener(this);
		}

		if (this.enableMqttClient) {
			this.mqttClient = new MqttClientConnector();
			this.mqttClient.setDataMessageListener(this);
		}

		if (this.enableCoapServer) {
			this.coapServer = new CoapServerGateway(this);
		}

		if (this.enableCoapClient) {
			this.coapClient = new CoapClientConnector();
			this.coapClient.setDataMessageListener(this);
		}

		if (this.enableCloudClient) {
			this.cloudClient = new CloudClientConnector();
			this.cloudClient.setDataMessageListener(this);
		}

		if (this.enablePersistenceClient) {
			// TODO: implement this as an optional exercise in Lab Module 5
		}
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resource, SensorData data)
	{
		// route by sensor type - GDA handles humidity (CDA already handles temperature)
		if (data.getTypeID() == ConfigConst.HUMIDITY_SENSOR_TYPE) {
			handleHumiditySensorAnalysis(resource, data);
		}
	}

	private void handleHumiditySensorAnalysis(ResourceNameEnum resource, SensorData data)
	{
		_Logger.fine("Analyzing humidity data from CDA: " + data.getLocationID() + ". Value: " + data.getValue());

		boolean isLow  = data.getValue() < this.triggerHumidifierFloor;
		boolean isHigh = data.getValue() > this.triggerHumidifierCeiling;

		if (isLow || isHigh) {
			_Logger.fine("Humidity data from CDA exceeds nominal range.");

			if (this.latestHumiditySensorData == null) {
				// first out-of-range reading: start the timer, do nothing else yet
				this.latestHumiditySensorData = data;
				this.latestHumiditySensorTimeStamp = getDateTimeFromData(data);

				_Logger.fine(
					"Starting humidity nominal exception timer. Waiting for seconds: " +
					this.humidityMaxTimePastThreshold);

				return;
			} else {
				OffsetDateTime curHumiditySensorTimeStamp = getDateTimeFromData(data);

				long diffSeconds =
					ChronoUnit.SECONDS.between(
						this.latestHumiditySensorTimeStamp, curHumiditySensorTimeStamp);

				_Logger.fine("Checking humidity value exception time delta: " + diffSeconds);

				if (diffSeconds >= this.humidityMaxTimePastThreshold) {
					ActuatorData ad = new ActuatorData();
					ad.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);
					ad.setLocationID(data.getLocationID());
					ad.setTypeID(ConfigConst.HUMIDIFIER_ACTUATOR_TYPE);
					ad.setValue(this.nominalHumiditySetting);

					if (isLow) {
						ad.setCommand(ConfigConst.ON_COMMAND);
					} else if (isHigh) {
						ad.setCommand(ConfigConst.OFF_COMMAND);
					}

					_Logger.info(
						"Humidity exceptional value reached. Sending actuation event to CDA: " + ad);

					this.lastKnownHumidifierCommand = ad.getCommand();
					sendActuatorCommandtoCda(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, ad);

					// store command, reset sensor tracking for the next cycle
					this.latestHumidifierActuatorData = ad;
					this.latestHumiditySensorData = null;
					this.latestHumiditySensorTimeStamp = null;
				}
			}
		} else if (this.lastKnownHumidifierCommand == ConfigConst.ON_COMMAND) {
			// back in range while humidifier is ON - check whether to turn it OFF
			if (this.latestHumidifierActuatorData != null) {
				if (this.latestHumidifierActuatorData.getValue() >= this.nominalHumiditySetting) {
					this.latestHumidifierActuatorData.setCommand(ConfigConst.OFF_COMMAND);

					_Logger.info(
						"Humidity nominal value reached. Sending OFF actuation event to CDA: " +
						this.latestHumidifierActuatorData);

					sendActuatorCommandtoCda(
						ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, this.latestHumidifierActuatorData);

					this.lastKnownHumidifierCommand = this.latestHumidifierActuatorData.getCommand();
					this.latestHumidifierActuatorData = null;
					this.latestHumiditySensorData = null;
					this.latestHumiditySensorTimeStamp = null;
				} else {
					_Logger.fine("Humidifier is still on. Not yet at nominal levels (OK).");
				}
			} else {
				_Logger.warning(
					"ERROR: ActuatorData for humidifier is null (shouldn't be). Can't send command.");
			}
		}
	}

	private void sendActuatorCommandtoCda(ResourceNameEnum resource, ActuatorData data)
	{
		// path 1: direct listener (used with CoAP OBSERVE); guard against null ref
		if (this.actuatorDataListener != null) {
			this.actuatorDataListener.onActuatorDataUpdate(data);
		}

		// path 2: publish over MQTT to the CDA's actuator command topic
		if (this.enableMqttClient && this.mqttClient != null) {
			String jsonData = DataUtil.getInstance().actuatorDataToJson(data);

			if (this.mqttClient.publishMessage(resource, jsonData, ConfigConst.DEFAULT_QOS)) {
				_Logger.info(
					"Published ActuatorData command from GDA to CDA: " + data.getCommand());
			} else {
				_Logger.warning(
					"Failed to publish ActuatorData command from GDA to CDA: " + data.getCommand());
			}
		}
	}

	private OffsetDateTime getDateTimeFromData(BaseIotData data)
	{
		OffsetDateTime odt = null;

		try {
			odt = OffsetDateTime.parse(data.getTimeStamp());
		} catch (Exception e) {
			_Logger.warning(
				"Failed to extract ISO 8601 timestamp from IoT data. Using local current time.");

			odt = OffsetDateTime.now();
		}

		return odt;
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, ActuatorData data)
	{
		_Logger.fine("Handling incoming data analysis for ActuatorData...");
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SystemStateData data)
	{
		_Logger.fine("Handling incoming data analysis for SystemStateData...");
	}

	private boolean handleUpstreamTransmission(ResourceNameEnum resourceName, String jsonData, int qos)
	{
		_Logger.fine("Handling upstream transmission...");

		return true;
	}
}
