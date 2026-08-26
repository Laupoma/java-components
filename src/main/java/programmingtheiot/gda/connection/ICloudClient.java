/**
 * This class is part of the Programming the Internet of Things project.
 */

package programmingtheiot.gda.connection;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Interface contract for pub/sub clients.
 * 
 */
public interface ICloudClient
{
	public boolean connectClient();

	public boolean disconnectClient();

	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data);

	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data);

	public boolean subscribeToCloudEvents(ResourceNameEnum resource);

	public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource);

	public boolean setDataMessageListener(IDataMessageListener listener);
}
