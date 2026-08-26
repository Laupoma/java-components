/**
 * This class is part of the Programming the Internet of Things project.
 */

package programmingtheiot.part02.unit.data;

import static org.junit.Assert.*;

import org.junit.Test;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.TimeAndValuePayloadData;

public class TimeAndValuePayloadDataTest
{
	@Test
	public void testDefaultConstructor()
	{
		TimeAndValuePayloadData tvpd = new TimeAndValuePayloadData();

		assertNotNull(tvpd);
		assertEquals(0.0f, tvpd.getValue(), 0.0f);
		assertNotNull(tvpd.getTimeStamp());
	}

	@Test
	public void testConstructionFromSensorData()
	{
		SensorData sd = new SensorData();
		sd.setValue(22.5f);

		TimeAndValuePayloadData tvpd = new TimeAndValuePayloadData(sd);

		assertNotNull(tvpd);
		assertEquals(22.5f, tvpd.getValue(), 0.0f);
		assertEquals(sd.getTimeStamp(), tvpd.getTimeStamp());
	}

	@Test
	public void testConstructionFromActuatorData()
	{
		ActuatorData ad = new ActuatorData();
		ad.setValue(60.0f);

		TimeAndValuePayloadData tvpd = new TimeAndValuePayloadData(ad);

		assertNotNull(tvpd);
		assertEquals(60.0f, tvpd.getValue(), 0.0f);
		assertEquals(ad.getTimeStamp(), tvpd.getTimeStamp());
	}

	@Test
	public void testSetters()
	{
		TimeAndValuePayloadData tvpd = new TimeAndValuePayloadData();

		tvpd.setValue(99.9f);
		tvpd.setTimeStamp("2026-01-01T00:00:00.000Z");

		assertEquals(99.9f, tvpd.getValue(), 0.0f);
		assertEquals("2026-01-01T00:00:00.000Z", tvpd.getTimeStamp());
	}
}
