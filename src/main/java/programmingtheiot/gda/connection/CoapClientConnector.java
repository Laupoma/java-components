/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.config.UdpConfig;
import org.eclipse.californium.elements.exception.ConnectorException;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.gda.connection.handlers.GenericCoapResponseHandler;
import programmingtheiot.gda.connection.handlers.SensorDataObserverHandler;

/**
 * CoAP client connector implementation using the Eclipse Californium library.
 *
 */
public class CoapClientConnector implements IRequestResponseClient
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(CoapClientConnector.class.getName());
	
	// params
	
	private String     protocol;
	private String     host;
	private int        port;
	private String     serverAddr;
	private CoapClient clientConn;
	private IDataMessageListener dataMsgListener;
	
	// constructors
	
	/**
	 * Default.
	 * 
	 * All config data will be loaded from the config file.
	 */
	public CoapClientConnector()
	{
		ConfigUtil config = ConfigUtil.getInstance();
		
		this.host = config.getProperty(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
		
		if (config.getBoolean(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY)) {
			this.protocol = ConfigConst.DEFAULT_COAP_SECURE_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_COAP_SECURE_PORT);
		} else {
			this.protocol = ConfigConst.DEFAULT_COAP_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_COAP_PORT);
		}
		
		// NOTE: URL does not have a protocol handler for "coap",
		// so we need to construct the URL manually
		this.serverAddr = this.protocol + "://" + this.host + ":" + this.port;
		
		initClient();
		
		_Logger.info("Using URL for server conn: " + this.serverAddr);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param host
	 * @param isSecure
	 * @param enableConfirmedMsgs
	 */
	public CoapClientConnector(String host, boolean isSecure, boolean enableConfirmedMsgs)
	{
		ConfigUtil config = ConfigUtil.getInstance();
		
		this.host = host;
		
		if (isSecure) {
			this.protocol = ConfigConst.DEFAULT_COAP_SECURE_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_COAP_SECURE_PORT);
		} else {
			this.protocol = ConfigConst.DEFAULT_COAP_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_COAP_PORT);
		}
		
		this.serverAddr = this.protocol + "://" + this.host + ":" + this.port;
		
		initClient();
		
		_Logger.info("Using URL for server conn: " + this.serverAddr);
	}
	
	// public methods
	
	@Override
	public boolean sendDiscoveryRequest(int timeout)
	{
		_Logger.info("Issuing discover...");
		
		try {
			Set<WebLink> wlSet = this.clientConn.discover();
			
			if (wlSet != null) {
				for (WebLink wl : wlSet) {
					_Logger.info(" --> URI: " + wl.getURI() + ". Attributes: " + wl.getAttributes());
				}
				
				return true;
			}
		} catch (ConnectorException | IOException e) {
			_Logger.log(Level.WARNING, "Failed to send DISCOVERY request.", e);
		}
		
				return false;
	}
	
	@Override
	public boolean sendDeleteRequest(ResourceNameEnum resource, String name, boolean enableCON, int timeout)
	{
		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}
		
		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());
		
		try {
			CoapResponse response = this.clientConn.delete();
			
			if (response != null) {
				_Logger.info("Handling DELETE. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
					response.getCode() + " - " + response.getResponseText());
				
				return true;
			} else {
				_Logger.warning("Handling DELETE. No response received.");
			}
		} catch (ConnectorException | IOException e) {
			_Logger.log(Level.WARNING, "Failed to send DELETE request.", e);
		}
		
		return false;
	}
	
	@Override
	public boolean sendGetRequest(ResourceNameEnum resource, String name, boolean enableCON, int timeout)
	{
		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}
		
		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());
		
		try {
			CoapResponse response = this.clientConn.get();
			
			if (response != null) {
				_Logger.info("Handling GET. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
					response.getCode() + " - " + response.getResponseText());
				
				return true;
			} else {
				_Logger.warning("Handling GET. No response received.");
			}
		} catch (ConnectorException | IOException e) {
			_Logger.log(Level.WARNING, "Failed to send GET request.", e);
		}
		
		return false;
	}
	
	@Override
	public boolean sendPostRequest(ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout)
	{
		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}
		
		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());
		
		try {
			CoapResponse response = this.clientConn.post(payload, MediaTypeRegistry.TEXT_PLAIN);
			
			if (response != null) {
				_Logger.info("Handling POST. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
					response.getCode() + " - " + response.getResponseText());
				
				return true;
			} else {
				_Logger.warning("Handling POST. No response received.");
			}
		} catch (ConnectorException | IOException e) {
			_Logger.log(Level.WARNING, "Failed to send POST request.", e);
		}
		
		return false;
	}
	
	@Override
	public boolean sendPutRequest(ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout)
	{
		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}
		
		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());
		
		try {
			CoapResponse response = this.clientConn.put(payload, MediaTypeRegistry.TEXT_PLAIN);
			
			if (response != null) {
				_Logger.info("Handling PUT. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
					response.getCode() + " - " + response.getResponseText());
				
				return true;
			} else {
				_Logger.warning("Handling PUT. No response received.");
			}
		} catch (ConnectorException | IOException e) {
			_Logger.log(Level.WARNING, "Failed to send PUT request.", e);
		}
		
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
	
	public void clearEndpointPath()
	{
	}
	
	public void setEndpointPath(ResourceNameEnum resource)
	{
	}
	
	@Override
	public boolean startObserver(ResourceNameEnum resource, String name, int ttl)
	{
		String uriPath = createUriPath(resource, name);
		
		_Logger.info("Observing resource [START]: " + uriPath);
		
		this.clientConn.setURI(uriPath);
		
		// NOTE: Per PIOT-GDA-09-008, use a resource-specific observer handler.
		// A more complete implementation would inspect the resource type and
		// choose between SensorDataObserverHandler and SystemPerformanceDataObserverHandler.
		SensorDataObserverHandler handler = new SensorDataObserverHandler();
		handler.setDataMessageListener(this.dataMsgListener);
		
		CoapObserveRelation cor = this.clientConn.observe(handler);
		
		return (! cor.isCanceled());
	}
	
	@Override
	public boolean stopObserver(ResourceNameEnum resourceType, String name, int timeout)
	{
		_Logger.info("Stopping observer for resource: " + createUriPath(resourceType, name));
		
		return true;
	}
	
	// private methods
	
	/**
	 * Creates the full URI path for the given resource and (optional) name.
	 * 
	 * @param resource The ResourceNameEnum used to build the base path.
	 * @param name Optional additional path segment.
	 * @return String The full URI path (protocol://host:port/resource[/name]).
	 */
	private String createUriPath(ResourceNameEnum resource, String name)
	{
		String uriPath = this.serverAddr;
		
		if (resource != null) {
			uriPath = uriPath + "/" + resource.getResourceName();
		}
		
		if (name != null && name.trim().length() > 0) {
			uriPath = uriPath + "/" + name;
		}
		
		return uriPath;
	}
	
	/**
	 * Initializes the CoAP client connection.
	 */
	private void initClient()
	{
		try {
			// NOTE: Californium 3.x requires the standard configuration to be
			// initialized with the relevant definitions before creating a client.
			// We set it directly rather than checking getStandard() first, because
			// getStandard() itself throws if no definitions are registered yet.
			CoapConfig.register();
			UdpConfig.register();
			
			Configuration config = new Configuration(CoapConfig.DEFINITIONS, UdpConfig.DEFINITIONS);
			Configuration.setStandard(config);
			
			_Logger.info("Initialized Californium configuration with standard definitions.");
			
			this.clientConn = new CoapClient(this.serverAddr);
			
			_Logger.info("Created client connection to server / resource: " + this.serverAddr);
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to connect to server: " + this.serverAddr, e);
		}
	}
}
