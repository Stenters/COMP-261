import java.util.Collection;
import java.util.HashSet;

/**
 * Road represents ... a road ... in our graph, which is some metadata and a
 * collection of Segments. We have lots of information about Roads, but don't
 * use much of it.
 * 
 * @author tony
 */
public class Road {
	public final int roadID, speed;
	public final String name, city;
	public final Collection<Segment> components;
	public final boolean isOneway, notForCar, notForPede, notForBicy;
	public final RoadClass roadClass;

	public Road(int roadID, int type, String label, String city, int oneway,
			int speed, int roadclass, int notforcar, int notforpede,
			int notforbicy) {

		this.roadID = roadID;
		this.city = city;
		this.name = label;
		this.components = new HashSet<>();
		this.speed = convertSpeedClassToSpeedLimit(speed);
		this.roadClass = RoadClass.values()[roadclass];

		this.isOneway = oneway == 1;
		this.notForCar = notforcar == 1;
		this.notForPede = notforpede == 1;
		this.notForBicy = notforbicy == 1;


	}

	public void addSegment(Segment seg) {
		components.add(seg);
	}

	/*
	 * class
	 *    0 = Residential
	 *    1 = Collector
	 *    2 = Arterial
	 *    3 = Principal HW
	 *    4 = Major HW
	 */
	private enum RoadClass {RESIDENTIAL, COLLECTOR, ARTERIAL, PRINCIPAL_HW, MAJOR_HW}

	/*
	 * speed
	 *
	 *	  0 = 5km/h
	 *    1 = 20km/h
	 *    2 = 40km/h
	 *    3 = 60km/h
	 *    4 = 80km/h
	 *    5 = 100km/h
	 *    6 = 110km/h
	 *    7 = no limit
	 */
	public int convertSpeedClassToSpeedLimit(int speedClass){
		switch (speedClass) {
			case 0:
				return 5;
			case 1:
				return 20;
			case 2:
				return 40;
			case 3:
				return 60;
			case 4:
				return 80;
			case 5:
				return 100;
			case 6:
				return 110;
			default:
				return Integer.MAX_VALUE;
		}
	}

	public boolean equals(Object o) {
		return o instanceof Road && ((Road) o).name.equals(name);
	}
}

// code for COMP261 assignments