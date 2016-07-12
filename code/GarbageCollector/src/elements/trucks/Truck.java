package elements.trucks;

import jade.util.Event;
import jade.wrapper.AgentController;
import jade.wrapper.AgentState;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import main.GarbageCollector;
import map.Map;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import agents.TruckAgent;
import assets.Assets;
import elements.Deposit;
import elements.DrawableElement;
import elements.MapElement;
import elements.Road;
import elements.containers.Container;
import exceptions.TruckFullException;
import gui.TruckDetailsComponent;

public abstract class Truck extends Thread implements DrawableElement {

	public static int defaultCapacity = 20;

	Point currentLocation;

	public Point currentDestination;
	ArrayList<Point> pointsToVisit;
	private int pointIndex, moveDirection;
	private ArrayList<DefaultEdge> currentDestinationRoute;
	private HashMap<ArrayList<Point>, List<DefaultEdge>> routes;
	private LinkedList<Container> alreadyInformedContainers;
	ArrayList<ArrayList<MapElement>> mapMatrix;
	String agentName;
	public AgentController agentController;
	private int capacity, usedCapacity;
	TruckDetailsComponent component;
	private TruckInform waitInformThread;
	public boolean goingToDeposit = false;
	public boolean remoteTruck;
	// Statistics
	public int numberOfContainersAdded = 0, numberOfContainersEmptied = 0,
			totalAmountOfGarbage = 0, numberOfDepositVisits = 0;

	private static double emptyFactor = 0.75;
	private boolean go = true;
	static final int tickTime = 150; // in ms
	static final int waitTime = 500;
	private static final int vision = 5;

	public Truck(Point initialLocation, int capacity,
			ContainerController containerController, String name,
			int agentType, ArrayList<ArrayList<MapElement>> matrix,
			boolean localInstance) throws StaleProxyException {
		super(name + " truck");
		this.currentLocation = initialLocation;
		this.capacity = capacity;
		this.usedCapacity = 0;
		this.mapMatrix = Map.cloneMapMatrix(matrix);
		this.agentName = name;
		this.remoteTruck = !localInstance;
		if (localInstance) {
			this.waitInformThread = new TruckInform(this);
			this.waitInformThread.start();
			this.agentController = containerController.createNewAgent(name,
					TruckAgent.class.getName(), new Object[] { agentType,
							this.waitInformThread });
			this.agentController.start();
			// wait until agent is started
			while (agentController.getState().getCode() != AgentState.cAGENT_STATE_IDLE) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!GarbageCollector.local) {
				createTruckInform();
			}
		}
		if (GarbageCollector.local) {
			this.component = new TruckDetailsComponent(this);
			GarbageCollector.frame.menu.addTruck(this);
		}
		this.alreadyInformedContainers = new LinkedList<Container>();
	}

	public ArrayList<Point> getPointsToVisit() {
		return pointsToVisit;
	}

	public TruckDetailsComponent getComponent() {
		return component;
	}

	public String getAgentName() {
		return agentName;
	}

	public abstract int getType();

	public Point getLocation() {
		return this.currentLocation;
	}

	public void addToTruck(int ammount) throws TruckFullException {
		if ((this.usedCapacity + ammount) > this.capacity)
			throw new TruckFullException();
		else {
			this.usedCapacity += ammount;
			if (GarbageCollector.local)
				this.component.setCurrentUsage(this);
		}

	}

	public void emptyTruck() {
		this.usedCapacity = 0;
		if (GarbageCollector.local)
			this.component.setCurrentUsage(this);
	}

	public void moveTruck(Point destination) {
		int moveDirection = Assets.getMoveDirection(this.getLocation(),
				destination);
		if (moveDirection > -1) // actual move
			this.setMoveDirection(moveDirection);
		this.currentLocation = destination;
	}

	public void setPointsToVisit(ArrayList<Point> route) {
		this.pointsToVisit = route;
		this.currentDestinationRoute = new ArrayList<>();
		this.routes = new HashMap<ArrayList<Point>, List<DefaultEdge>>();
		this.pointIndex = 0;
	}

	public boolean emptyAdjacentContainers(List<Container> adjacentContainers,
			List<Point> adjacentContainerPoints) {
		boolean emptiedAny = false;
		int garbageAmountBeforeEmptying = this.usedCapacity;
		try {
			for (int i = 0; i < adjacentContainers.size(); i++) {
				Container c = adjacentContainers.get(i);
				Point p = adjacentContainerPoints.get(i);
				if (c.truckCompatible(this)
						&& this.usedCapacity < this.capacity) {
					if (containerRequest(p.x, p.y)) {
						emptiedAny = true;
						numberOfContainersEmptied++;
						totalAmountOfGarbage += this.usedCapacity
								- garbageAmountBeforeEmptying;
					}
				}

			}
		} catch (StaleProxyException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return emptiedAny;
	}

	public Point getCurrentDestination() {
		return currentDestination;
	}

	public int getUsedCapacity() {
		return usedCapacity;
	}

	public int getCapacity() {
		return capacity;
	}

	public boolean emptyInDeposit(List<Point> adjacentDeposits) {
		if (adjacentDeposits.size() > 0) {
			this.emptyTruck();
			this.numberOfDepositVisits++;
			this.goingToDeposit = false;
			try {
				if (!GarbageCollector.local)
					emptiedTruckInform();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public void containerInform(int garbageType, int X, int Y)
			throws StaleProxyException {
		Event event = new Event(TruckAgent.INFORM_OTHER_TRUCKS, this);
		event.addParameter(new String(garbageType + " " + X + " " + Y));
		this.agentController.putO2AObject(event, false);
	}

	public void emptiedContainerInform(int X, int Y, int ammount)
			throws StaleProxyException {
		Event event = new Event(TruckAgent.INFORM_EMPTIED_CONTAINER, this);
		event.addParameter(new String(getAgentName() + " " + X + " " + Y + " "
				+ ammount));
		this.agentController.putO2AObject(event, false);
	}

	public void goingToDepositInform() throws StaleProxyException {
		Event event = new Event(TruckAgent.INFORM_GOING_TO_DEPOSIT, this);
		event.addParameter(new String(getAgentName()));
		this.agentController.putO2AObject(event, false);
	}

	public void currentDestinationInform() throws StaleProxyException {
		Event event = new Event(TruckAgent.INFORM_CURRENT_DESTINATION, this);
		event.addParameter(new String(getAgentName() + " "
				+ this.currentDestination.x + " " + this.currentDestination.y));
		this.agentController.putO2AObject(event, false);
	}

	public boolean containerRequest(int X, int Y) throws StaleProxyException,
			InterruptedException {
		Event event = new Event(TruckAgent.REQUEST_CONTAINER_CAPACITY, this);
		event.addParameter(new String(X + " " + Y));
		event.addParameter(new Point(X, Y));
		this.agentController.putO2AObject(event, false);

		int usedContainerCapacity = (int) event.waitUntilProcessed();
		if (usedContainerCapacity > 0) {
			try {
				this.addToTruck(usedContainerCapacity);
				emptiedContainerInform(X, Y, usedContainerCapacity);
				if (this.usedCapacity >= this.capacity * emptyFactor)
					goToClosestDeposit(X, Y);
				return true;
				// TODO: esvaziar o que puder (tem de avisar mundo)
				// TODO: ir esvaziar quando acima de x capacidade usada
			} catch (TruckFullException e) {
				containerInform(this.getType(), X, Y);
				this.currentDestination = getClosestDeposit();
				goToClosestDeposit(X, Y);
				if (GarbageCollector.local)
					this.component.setCurrentDestination(this);
				System.out.println(getAgentName() + " is full, going to "
						+ this.currentDestination.x + "|"
						+ this.currentDestination.y + " to empty...");
			}
		}
		return false;
	}

	private Point getClosestDeposit() {
		int minDistance = Integer.MAX_VALUE;
		Point closestDeposit = null;
		for (Point deposit : Map.INSTANCE.depositPoints) {
			// TODO: optimizar estrada a escolher
			Point road = Map.getAllAdjacentPoints(Road.class, deposit,
					mapMatrix).get(0);
			ArrayList<Point> points = new ArrayList<Point>(2);
			points.add(currentLocation);
			points.add(road);
			List<DefaultEdge> path;
			if (!this.routes.containsKey(points)) {
				path = DijkstraShortestPath.findPathBetween(Map.INSTANCE.graph,
						currentLocation, road);
				this.routes.put(points, path);
			} else
				path = this.routes.get(points);

			if (path.size() < minDistance)
				closestDeposit = road;
		}
		return closestDeposit;
	}

	public boolean moveRequest(Point destination) throws StaleProxyException {
		Event event = new Event(TruckAgent.REQUEST_MOVE, this);
		event.addParameter(new String(this.agentName + " " + destination.x
				+ " " + destination.y + " "
				+ Assets.getMoveDirection(this.getLocation(), destination)));
		this.agentController.putO2AObject(event, false);

		boolean result;
		try {
			result = (boolean) event.waitUntilProcessed(Truck.waitTime);
		} catch (InterruptedException e) {
			System.out.println(getAgentName() + " move request timed out!");
			result = false;
		}

		if (result)
			moveTruck(destination);

		return result;
	}

	public void createTruckInform() throws StaleProxyException {
		Event event = new Event(TruckAgent.INFORM_CREATE_TRUCK, this);
		event.addParameter(new String(this.agentName + " " + this.getCapacity()
				+ " " + this.currentLocation.x + " " + this.currentLocation.y
				+ " " + this.getType()));
		this.agentController.putO2AObject(event, true);
	}

	public void emptiedTruckInform() throws StaleProxyException {
		Event event = new Event(TruckAgent.INFORM_EMPTIED_TRUCK, this);
		event.addParameter(new String(this.agentName));
		this.agentController.putO2AObject(event, false);
	}

	// temporary
	public boolean moveRandomDirection(List<Point> possibleMoves) {
		try {
			return moveRequest(possibleMoves.get(GarbageCollector.randGenerator
					.nextInt(possibleMoves.size())));
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean goRoute() throws StaleProxyException, InterruptedException {
		if (pointsToVisit.size() < 1)
			return false;
		if (pointIndex == pointsToVisit.size())
			pointIndex = 0; // TODO: ESTRATEGIAS
		if (currentDestination == null) {
			currentDestination = pointsToVisit.get(pointIndex);
				
			if (GarbageCollector.local)
				this.component.setCurrentDestination(this);
			else
				currentDestinationInform();
		}
		if (currentDestinationRoute.isEmpty()) {
			ArrayList<Point> points = new ArrayList<Point>(2);
			points.add(currentLocation);
			points.add(currentDestination);
			if (!routes.containsKey(points))
				routes.put(points, DijkstraShortestPath
						.findPathBetween(Map.INSTANCE.graph, currentLocation,
								currentDestination));
			currentDestinationRoute = new ArrayList<DefaultEdge>(
					routes.get(points));
		}
		if (!currentDestinationRoute.isEmpty()) {
			DefaultEdge currentEdge = currentDestinationRoute.get(0);
			if (this.moveRequest(Map.INSTANCE.graph.getEdgeTarget(currentEdge))) {
				currentDestinationRoute.remove(0);
				if (currentLocation.equals(currentDestination)) {
					pointIndex++;
					currentDestination = null;
				}
				return true;
			}
		}
		return false;
	}

	/* THREAD */

	@Override
	public synchronized void start() {
		System.out.println("Starting " + agentName + " truck thread...");
		super.start();
	}

	@Override
	public void run() {
		int countVision = 0;
		while (go) {
			try {
				// System.out.println(getAgentName() + " tick!");
				if (this.goRoute()) {
					countVision++;
					emptyAdjacentContainers(Map.getAllAdjacentElements(
							Container.class, getLocation(), mapMatrix),
							Map.getAllAdjacentPoints(Container.class,
									getLocation(), mapMatrix));

					if (countVision == Truck.vision) { // evita spam // de //
						// mensagens // inform
						informContainersInVision();
						countVision = 0;
					}

					// TODO: esvaziar só acima de X???
					if (emptyInDeposit(Map.getAllAdjacentPoints(Deposit.class,
							getLocation(), mapMatrix))) {
						/*
						 * System.out.println(getAgentName() +
						 * " emptied the truck at: " + getLocation().x + "|" +
						 * getLocation().y);
						 */
					}
				}
				Thread.sleep(tickTime);
			} catch (InterruptedException e) {
				System.out.println("Truck thread interrupted, exiting");
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}

	private void informContainersInVision() throws StaleProxyException {
		ArrayList<Point> points = new ArrayList<Point>();
		ArrayList<Container> containers = new ArrayList<Container>();

		Map.getContainersWithinVision(points, containers, this.getLocation(),
				Truck.vision, this.getType(), mapMatrix);
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			Container c = containers.get(i);
			if (GarbageCollector.addToRoute) {
				if (!this.alreadyInformedContainers.contains(c)) {
					containerInform(c.getType(), p.x, p.y);
					this.alreadyInformedContainers.add(c);
				}

			} else
				containerInform(c.getType(), p.x, p.y);
		}
	}

	@Override
	public void interrupt() {
		this.go = false;
		super.interrupt();
	}

	public int getMoveDirection() {
		return moveDirection;
	}

	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}

	private void goToClosestDeposit(int X, int Y) throws StaleProxyException {
		containerInform(this.getType(), X, Y);
		this.currentDestination = getClosestDeposit();
		goingToDeposit = true;
		if (GarbageCollector.local)
			this.component.setCurrentDestination(this);
		else
			currentDestinationInform();
			goingToDepositInform();
		System.out.println(getAgentName() + " is full, going to "
				+ this.currentDestination.x + "|" + this.currentDestination.y
				+ " to empty...");
	}
}
