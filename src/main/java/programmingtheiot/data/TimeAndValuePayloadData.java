/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * Simplified payload with only a timestamp and value, for cloud services
 * (e.g. Ubidots) where device/variable context is embedded in the topic.
 */

package programmingtheiot.data;

import java.io.Serializable;

import programmingtheiot.common.ConfigConst;

public class TimeAndValuePayloadData implements Serializable
{
	private String timeStamp = "";
	private float  value     = ConfigConst.DEFAULT_VAL;

	public TimeAndValuePayloadData()
	{
		super();
	}

	public TimeAndValuePayloadData(SensorData data)
	{
		super();

		if (data != null) {
			this.timeStamp = data.getTimeStamp();
			this.value     = data.getValue();
		}
	}

	public TimeAndValuePayloadData(ActuatorData data)
	{
		super();

		if (data != null) {
			this.timeStamp = data.getTimeStamp();
			this.value     = data.getValue();
		}
	}

	public String getTimeStamp()
	{
		return this.timeStamp;
	}

	public float getValue()
	{
		return this.value;
	}

	public void setTimeStamp(String timeStamp)
	{
		if (timeStamp != null) {
			this.timeStamp = timeStamp;
		}
	}

	public void setValue(float value)
	{
		this.value = value;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("timeStamp=").append(this.timeStamp);
		sb.append(',').append("value=").append(this.value);

		return sb.toString();
	}
}
