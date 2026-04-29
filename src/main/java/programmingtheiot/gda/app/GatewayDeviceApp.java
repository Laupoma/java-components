/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 */ 

package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;
import programmingtheiot.gda.system.SystemPerformanceManager;

/**
 * Main GDA application.
 * */
public class GatewayDeviceApp
{
    // static
    
    private static final Logger _Logger =
        Logger.getLogger(GatewayDeviceApp.class.getName());
    
    // CAMBIO 1: Se actualizó a 65000L (65 segundos) según la instrucción.
    public static final long DEFAULT_TEST_RUNTIME = 65000L;
    
	private SystemPerformanceManager sysPerfMgr = null;
    // constructors
    
    /**
     * Constructor.
     * * @param args
     */
    public GatewayDeviceApp(String[] args)
    {
        super();
        
        _Logger.info("Initializing GDA...");
        
		this.sysPerfMgr = new SystemPerformanceManager();

        parseArgs(args);
    }
    
    
    // static
    
    /**
     * Main application entry point.
     * * @param args
     */
    public static void main(String[] args)
    {
        GatewayDeviceApp gwApp = new GatewayDeviceApp(args);
        
        gwApp.startApp();
        
        try {
            // El hilo principal dormirá por el tiempo definido en la constante
            Thread.sleep(DEFAULT_TEST_RUNTIME);
        } catch (InterruptedException e) {
            // ignore
        }
        
        gwApp.stopApp(0);
    }
    
    
    // public methods
    
    /**
     * Initializes and starts the application.
     * */
    public void startApp()
    {
        _Logger.info("Starting GDA...");
        
        try {
            if (this.sysPerfMgr.startManager()) {
			_Logger.info("GDA started successfully.");
		} else {
			_Logger.warning("Failed to start system performance manager!");
			
			stopApp(-1);
		}
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to start GDA. Exiting.", e);
            
            stopApp(-1);
        }
    }
    
    /**
     * Stops the application.
     * * @param code The exit code to pass to {@link System.exit()}
     */
    public void stopApp(int code)
	{
    	_Logger.info("Stopping GDA...");
    
    	try {
        // Solo llamamos al stop, no logueamos éxito aquí todavía
        if (!this.sysPerfMgr.stopManager()) {
            _Logger.warning("Failed to stop system performance manager!");
        }
    	} catch (Exception e) {
        _Logger.log(Level.SEVERE, "Failed to cleanly stop GDA. Exiting.", e);
    	}
    
    	// ESTA debe ser la última línea de log, fuera del try/catch
    	_Logger.log(Level.INFO, "GDA stopped successfully with exit code {0}.", code);
    
    	System.exit(code);
	}
    
    
    // private methods
    
    /**
     * Load the config file.
     * * @param configFile The name of the config file to load.
     */
    private void initConfig(String configFile)
    {
        _Logger.log(Level.INFO, "Attempting to load configuration: {0}", (configFile != null ? configFile : "Default."));
        
        // TODO: Your code here
    }
    
    /**
     * Parse any arguments passed in on app startup.
     * * @param args The non-null and non-empty args array.
     */
    private void parseArgs(String[] args)
    {
        // CAMBIO 4: La instrucción dice que por ahora los argumentos pueden ignorarse
        // y se debe llamar a initConfig(null) antes de salir.
        
        if (args != null && args.length > 0) {
            _Logger.log(Level.INFO, "Parsing {0} command line args.", args.length);
        } else {
            _Logger.info("No command line args to parse.");
        }
        
        initConfig(null);
    }

}