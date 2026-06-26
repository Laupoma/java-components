/**
 * This class is part of the Programming the Internet of Things project.
 */

package programmingtheiot.gda.connection;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
{
	// static

	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnector.class.getName());

	// params

	private boolean useAsyncClient = false;

	private MqttClient           mqttClient      = null;
	private MqttConnectOptions   connOpts        = null;
	private MemoryPersistence    persistence     = null;
	private IDataMessageListener dataMsgListener = null;

	private String clientID        = null;
	private String brokerAddr      = null;
	private String host            = ConfigConst.DEFAULT_HOST;
	private String protocol        = ConfigConst.DEFAULT_MQTT_PROTOCOL;
	private int    port            = ConfigConst.DEFAULT_MQTT_PORT;
	private int    brokerKeepAlive = ConfigConst.DEFAULT_KEEP_ALIVE;

	// constructors

	public MqttClientConnector()
	{
		super();

		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.host =
			configUtil.getProperty(
				ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);

		this.port =
			configUtil.getInteger(
				ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);

		this.brokerKeepAlive =
			configUtil.getInteger(
				ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);

		this.useAsyncClient =
			configUtil.getBoolean(
				ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.USE_ASYNC_CLIENT_KEY);

		this.clientID = MqttClient.generateClientId();

		this.persistence = new MemoryPersistence();
		this.connOpts    = new MqttConnectOptions();

		this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
		this.connOpts.setCleanSession(false);
		this.connOpts.setAutomaticReconnect(true);

		this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;

		_Logger.info("MQTT broker addr: " + this.brokerAddr);
		_Logger.info("MQTT client ID:   " + this.clientID);
	}

	// public methods

	@Override
	public boolean connectClient()
	{
		try {
			if (this.mqttClient == null) {
				this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, this.persistence);
				this.mqttClient.setCallback(this);
			}

			if (! this.mqttClient.isConnected()) {
				_Logger.info("MQTT client connecting to broker: " + this.brokerAddr);
				this.mqttClient.connect(this.connOpts);
				return true;
			} else {
				_Logger.warning("MQTT client already connected to broker: " + this.brokerAddr);
			}
		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "Failed to connect MQTT client to broker.", e);
		}

		return false;
	}

	@Override
	public boolean disconnectClient()
	{
		try {
			if (this.mqttClient != null) {
				if (this.mqttClient.isConnected()) {
					_Logger.info("Disconnecting MQTT client from broker: " + this.brokerAddr);
					this.mqttClient.disconnect();
					return true;
				} else {
					_Logger.warning("MQTT client not connected to broker: " + this.brokerAddr);
				}
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to disconnect MQTT client from broker: " + this.brokerAddr, e);
		}

		return false;
	}

	public boolean isConnected()
	{
		return (this.mqttClient != null && this.mqttClient.isConnected());
	}

	@Override
	public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos)
	{
		_Logger.info("publishMessage called. Not yet implemented.");
		return false;
	}

	@Override
	public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
	{
		_Logger.info("subscribeToTopic called. Not yet implemented.");
		return false;
	}

	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
	{
		_Logger.info("unsubscribeFromTopic called. Not yet implemented.");
		return false;
	}

	@Override
	public boolean setConnectionListener(IConnectionListener listener)
	{
		return false;
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

	// callbacks

	@Override
	public void connectComplete(boolean reconnect, String serverURI)
	{
		_Logger.info("MQTT connection complete. Reconnect: " + reconnect + " | URI: " + serverURI);
	}

	@Override
	public void connectionLost(Throwable t)
	{
		_Logger.warning("MQTT connection lost: " + t.getMessage());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		_Logger.info("MQTT message delivery complete. Token: " + token);
	}

	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception
	{
		_Logger.info("MQTT message arrived on topic: " + topic + " | Payload: " + new String(msg.getPayload()));
	}

	// private methods

	private void initClientParameters(String configSectionName)
	{
		// TODO: implement this
	}

	private void initCredentialConnectionParameters(String configSectionName)
	{
		// TODO: implement this
	}

	private void initSecureConnectionParameters(String configSectionName)
	{
		// TODO: implement this
	}
}
