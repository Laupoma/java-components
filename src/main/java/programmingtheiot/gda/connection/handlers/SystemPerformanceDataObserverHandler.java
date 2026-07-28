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
 * CoAP observer handler specific to SystemPerformanceData updates.
 *
 */
public class SystemPerformanceDataObserverHandler implements CoapHandler
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(SystemPerformanceDataObserverHandler.class.getName());
	
	// params
	
	private IDataMessageListener dataMsgListener = null;
	
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SystemPerformanceDataObserverHandler()
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
		_Logger.info("Received CoAP response (payload should be SystemPerformanceData in JSON): " + response.getResponseText());
		
		// TODO: decode the payload to SystemPerformanceData and notify the listener via
		//       handleSystemPerformanceMessage(ResourceNameEnum, SystemPerformanceData) in a later exercise
	}
}
