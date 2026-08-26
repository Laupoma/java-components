/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It implements ICloudClient and delegates the actual MQTT connectivity
 * to a locally instanced MqttClientConnector configured for the cloud
 * gateway service section of the configuration file.
 */

package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

public class CloudClientConnector implements ICloudClient
{
	private static final Logger _Logger =
		Logger.getLogger(CloudClientConnector.class.getName());

	private String topicPrefix = "";
	private MqttClientConnector mqttClient = null;
	private IDataMessageListener dataMsgListener = null;

	private int qosLevel = 1;

	public CloudClientConnector()
	{
		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.topicPrefix =
			configUtil.getProperty(ConfigConst.CLOUD_GATEWAY_SERVICE, ConfigConst.BASE_TOPIC_KEY);

		if (this.topicPrefix == null) {
			this.topicPrefix = "/";
		} else {
			if (! this.topicPrefix.endsWith("/")) {
				this.topicPrefix += "/";
			}
		}
	}

	@Override
	public boolean connectClient()
	{
		if (this.mqttClient == null) {
			this.mqttClient = new MqttClientConnector(ConfigConst.CLOUD_GATEWAY_SERVICE);
		}

		return this.mqttClient.connectClient();
	}

	@Override
	public boolean disconnectClient()
	{
		if (this.mqttClient != null && this.mqttClient.isConnected()) {
			return this.mqttClient.disconnectClient();
		}

		return false;
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data)
	{
		if (resource != null && data != null) {
			String payload = DataUtil.getInstance().sensorDataToTimeAndValueJson(data);

			return publishMessageToCloud(resource, data.getName(), payload);
		}

		return false;
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data)
	{
		if (resource != null && data != null) {
			SensorData cpuData = new SensorData();
			cpuData.updateData(data);
			cpuData.setName(ConfigConst.CPU_UTIL_NAME);
			cpuData.setValue(data.getCpuUtilization());

			boolean cpuDataSuccess = sendEdgeDataToCloud(resource, cpuData);

			if (! cpuDataSuccess) {
				_Logger.warning("Failed to send CPU utilization data to cloud service.");
			}

			SensorData memData = new SensorData();
			memData.updateData(data);
			memData.setName(ConfigConst.MEM_UTIL_NAME);
			memData.setValue(data.getMemoryUtilization());

			boolean memDataSuccess = sendEdgeDataToCloud(resource, memData);

			if (! memDataSuccess) {
				_Logger.warning("Failed to send memory utilization data to cloud service.");
			}

			return (cpuDataSuccess == memDataSuccess);
		}

		return false;
	}

	@Override
	public boolean subscribeToCloudEvents(ResourceNameEnum resource)
	{
		boolean success = false;

		String topicName = null;

		if (this.mqttClient != null && this.mqttClient.isConnected()) {
			topicName = createTopicName(resource);

			this.mqttClient.subscribeToTopic(topicName, this.qosLevel);

			success = true;
		} else {
			_Logger.warning("Subscription methods only available for MQTT. No MQTT connection to broker. Ignoring. Topic: " + topicName);
		}

		return success;
	}

	@Override
	public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource)
	{
		boolean success = false;

		String topicName = null;

		if (this.mqttClient != null && this.mqttClient.isConnected()) {
			topicName = createTopicName(resource);

			this.mqttClient.unsubscribeFromTopic(topicName);

			success = true;
		} else {
			_Logger.warning("Unsubscribe method only available for MQTT. No MQTT connection to broker. Ignoring. Topic: " + topicName);
		}

		return success;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) {
			this.dataMsgListener = listener;

			return true;
		}

		return false;
	}

	private String createTopicName(ResourceNameEnum resource)
	{
		return createTopicName(resource.getDeviceName(), resource.getResourceType());
	}

	private String createTopicName(String deviceName, String resourceTypeName)
	{
		return this.topicPrefix + deviceName + "/" + resourceTypeName;
	}

	private boolean publishMessageToCloud(ResourceNameEnum resource, String itemName, String payload)
	{
		String topicName = createTopicName(resource) + "-" + itemName;

		return publishMessageToCloud(topicName, payload);
	}

	private boolean publishMessageToCloud(String topicName, String payload)
	{
		try {
			_Logger.finest("Publishing payload value(s) to CSP: " + topicName);

			this.mqttClient.publishMessage(topicName, payload.getBytes(), this.qosLevel);

			return true;
		} catch (Exception e) {
			_Logger.warning("Failed to publish message to CSP: " + topicName);
		}

		return false;
	}
}
