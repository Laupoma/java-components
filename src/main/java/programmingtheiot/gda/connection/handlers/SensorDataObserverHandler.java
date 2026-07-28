/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

import programmingtheiot.common.IDataMessageListener;

/**
 * CoAP observer handler specific to SensorData updates.
 *
 */
public class SensorDataObserverHandler implements CoapHandler
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(SensorDataObserverHandler.class.getName());
	
	// params
	
	private IDataMessageListener dataMsgListener = null;
	
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SensorDataObserverHandler()
	{
		super();
	}
	
	// public methods
	
	public void setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) {
			this.dataMsgListener = listener;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.californium.core.CoapHandler#onError()
	 */
	@Override
	public void onError()
	{
		_Logger.warning("Handling CoAP error...");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.californium.core.CoapHandler#onLoad(org.eclipse.californium.core.CoapResponse)
	 */
	@Override
	public void onLoad(CoapResponse response)
	{
		_Logger.info("Received CoAP response (payload should be SensorData in JSON): " + response.getResponseText());
		
		// TODO: decode the payload to SensorData and notify the listener via
		//       handleSensorMessage(ResourceNameEnum, SensorData) in a later exercise
	}
}
