/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 */ 

package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;

/**
 * Main GDA application.
 *
 */
public class GatewayDeviceApp
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(GatewayDeviceApp.class.getName());
	
	public static final long DEFAULT_TEST_RUNTIME = 600000L;
	
	// private var's
	
	private DeviceDataManager dataMgr = null;
	
	
	// constructors
	
	public GatewayDeviceApp(String[] args)
	{
		super();
		
		_Logger.info("Initializing GDA...");
		
		parseArgs(args);
	}
	
	
	// static
	
	public static void main(String[] args)
	{
		GatewayDeviceApp gwApp = new GatewayDeviceApp(args);
		
		gwApp.startApp();
		
		try {
			Thread.sleep(DEFAULT_TEST_RUNTIME);
		} catch (InterruptedException e) {
			// ignore
		}
		
		gwApp.stopApp(0);
	}
	
	
	// public methods
	
	public void startApp()
	{
		_Logger.info("Starting GDA...");
		
		try {
			if (! ConfigUtil.getInstance().getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.TEST_EMPTY_APP_KEY)) {
				this.dataMgr = new DeviceDataManager();
			}
			
			if (this.dataMgr != null) {
				this.dataMgr.startManager();
			}
			
			_Logger.info("GDA started successfully.");
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to start GDA. Exiting.", e);
			
			stopApp(-1);
		}
	}
	
	public void stopApp(int code)
	{
		_Logger.info("Stopping GDA...");
		
		try {
			if (this.dataMgr != null) {
				this.dataMgr.stopManager();
			}
			
			_Logger.log(Level.INFO, "GDA stopped successfully with exit code {0}.", code);
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to cleanly stop GDA. Exiting.", e);
		}
	}
	
	
	// private methods
	
	private void initConfig(String configFile)
	{
		_Logger.log(Level.INFO, "Attempting to load configuration: {0}", (configFile != null ? configFile : "Default."));
	}
	
	private void parseArgs(String[] args)
	{
		if (args != null && args.length > 0) {
			_Logger.log(Level.INFO, "Parsing {0} command line args.", args.length);
		} else {
			_Logger.info("No command line args to parse.");
		}
		
		initConfig(null);
	}
}
