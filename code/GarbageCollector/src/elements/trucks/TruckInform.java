package elements.trucks;

import jade.util.Event;
import jade.wrapper.StaleProxyException;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import main.GarbageCollector;
import map.Map;

import org.jgrapht.alg.DijkstraShortestPath;

import agents.TruckAgent;
import elements.Road;

public class TruckInform extends Thread {
	private boolean go = true;
	private Truck truck;
	private Point container;
	private ArrayList<Integer> distances;

	public TruckInform(Truck truck) {
		super(truck.agentName + " inform");
		this.truck = truck;
		this.container = null;
		this.distances = new ArrayList<Integer>();
	}

	public synchronized void gotInform(Point container) {
		if (this.container == null)
			this.container = container;
		// else
		// System.out.println(truck.getAgentName()
		// + " currently processing another container!");
	}

	public synchronized void gotDistance(Point container, int distance) {
		if (this.container != null) {
			if (container.equals(this.container)) {
				this.distances.add(distance);
			}// else {
				// System.out.println(truck.getAgentName()
				// + " got INFORM_DISTANCE for a different container!!!");
				// }
		}
	}

	@Override
	public void run() {
		try {
			while (go) {
				if (container != null) {
					// processo de decisão (qual dos trucks vai lá)
					List<Point> adjacentRoads = Map.getAllAdjacentPoints(
							Road.class, container, truck.mapMatrix);
					boolean contains = false;
					for (Point road : adjacentRoads)
						if (truck.pointsToVisit.contains(road)) {
							contains = true;
							break;
						}
					Event sendInformDist = new Event(
							TruckAgent.INFORM_DISTANCE, this);
					if (!contains) {
						// TODO: optimizar qual das estradas escolher
						Point toVisit = adjacentRoads.get(0);
						int distance = DijkstraShortestPath.findPathBetween(
								Map.INSTANCE.graph, truck.currentLocation,
								toVisit).size();
						sendInformDist.addParameter(truck.getType() + " "
								+ container.x + " " + container.y + " "
								+ distance);
						truck.agentController.putO2AObject(sendInformDist,
								false);
						Thread.sleep(Truck.waitTime);
						// Make a copy
						ArrayList<Integer> distances = new ArrayList<Integer>(
								this.distances);
						boolean mineIsCloser = true;
						for (int receivedDistance : distances) {
							if (receivedDistance < distance) {
								mineIsCloser = false;
								break;
							}
						}

						if (mineIsCloser) {
							// TODO: interface para opções
							if (GarbageCollector.addToRoute) {
                                truck.pointsToVisit.add(toVisit); // permanente
                                truck.numberOfContainersAdded++;
                            }
							else if (!truck.goingToDeposit)
								truck.currentDestination = toVisit;
							/*System.out.println(truck.agentName
									+ " added point to visit: " + toVisit.x
									+ "|" + toVisit.y);*/
						}
					} else { // already on my route
						sendInformDist.addParameter(truck.getType() + " "
								+ container.x + " " + container.y + " -1");
						truck.agentController.putO2AObject(sendInformDist,
								false);
					}
					this.container = null;
					this.distances.clear();
				}
				Thread.sleep(Truck.tickTime);
			}
		} catch (InterruptedException e) {
			System.out.println("Inform thread interrupted!");
			this.go = false;
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
};
