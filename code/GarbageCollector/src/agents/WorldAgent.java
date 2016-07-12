package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;

import java.awt.Point;

import assets.Assets;
import main.GarbageCollector;
import map.Map;
import elements.Road;
import elements.containers.Container;
import elements.trucks.GarbageTruck;
import elements.trucks.GlassTruck;
import elements.trucks.PaperTruck;
import elements.trucks.PlasticTruck;
import elements.trucks.Truck;
import exceptions.TruckFullException;

public class WorldAgent extends Agent {
	private static final long serialVersionUID = 1L;
	public static final int INFORM_CONTAINER_CAPACITY = 5,
			CONFIRM_REFUSE_MOVE = 6;

	// método setup
	@Override
	protected void setup() {
		// regista agente no DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("World");
		setEnabledO2ACommunication(true, 0);
		System.out.println("Created WorldAgent " + getName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		// adiciona behaviour ciclico (ler request lixo)
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					int requestType = -1;
					String[] args = null;
					try {
						args = msg.getContent().split("\\s+");
						requestType = Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
						return;
					}
					Map map = Map.INSTANCE;
					Point point;
					Container c;
					ACLMessage sendMsg = null;
					String toSend = null;
					Truck truck;
					switch (requestType) {
					case TruckAgent.INFORM_CREATE_TRUCK:
						// REQUEST_TYPE + TruckName + Capacity + X + Y +
						// TruckType
						try {
							int type = Integer.parseInt(args[5]);
							int capacity = Integer.parseInt(args[2]);
							String name = args[1];
							point = new Point(Integer.parseInt(args[3]),
									Integer.parseInt(args[4]));
							truck = null;
							switch (type) {
							case Assets.GARBAGE:
								truck = new GarbageTruck(point, capacity, null,
										name, Map.INSTANCE.mapMatrix, false);
								break;
							case Assets.PLASTIC:
								truck = new PlasticTruck(point, capacity, null,
										name, Map.INSTANCE.mapMatrix, false);
								break;
							case Assets.PAPER:
								truck = new PaperTruck(point, capacity, null,
										name, Map.INSTANCE.mapMatrix, false);
								break;
							case Assets.GLASS:
								truck = new GlassTruck(point, capacity, null,
										name, Map.INSTANCE.mapMatrix, false);
								break;
							}
							System.out.println("Added remote Truck " + name);
							DFAgentDescription dfd = new DFAgentDescription();
							dfd.setName(msg.getSender());
							ServiceDescription sd = new ServiceDescription();
							sd.setType(Integer.toString(type));
							sd.setName(getName());
							dfd.addServices(sd);
							DFService.register(myAgent, dfd);
							Map.INSTANCE.trucks.add(truck);
							Map.getElement(Road.class, point,
									Map.INSTANCE.mapMatrix).setTruck(truck);
							GarbageCollector.frame.mapComponent.repaint();
							return;
						} catch (StaleProxyException e) {
							e.printStackTrace();
						} catch (FIPAException e) {
							e.printStackTrace();
						}

						break;
					case TruckAgent.REQUEST_CONTAINER_CAPACITY:
						point = new Point(Integer.parseInt(args[1]),
								Integer.parseInt(args[2]));
						c = Map.getElement(Container.class, point,
								map.mapMatrix);
						sendMsg = new ACLMessage(ACLMessage.INFORM);
						// REQUEST_TYPE + X + Y + CAPACITY
						toSend = new String(
								WorldAgent.INFORM_CONTAINER_CAPACITY + " "
										+ point.x + " " + point.y + " "
										+ c.getUsedCapacity());
						break;
					case TruckAgent.REQUEST_MOVE:
						truck = Map.getTruckByAgentName(args[1], map.trucks);
						point = new Point(Integer.parseInt(args[2]),
								Integer.parseInt(args[3]));
						int moveDir = Integer.parseInt(args[4]);
						boolean canMove = true;
						for (Truck t : map.trucks) {
							Road road = Map.getElement(Road.class,
									t.getLocation(), map.mapMatrix);
							if (t.getLocation().equals(point)
									&& (t.getMoveDirection() == moveDir || !road
											.isTwoWay())) {
								canMove = false;
								/*
								 * System.out.println(args[1] +
								 * " couldn't move! " + moveDir + "|" +
								 * t.getMoveDirection() + " Trying to go from "
								 * + truck.getLocation() + " to " + point);
								 */
							}
						}
						if (canMove) {
							sendMsg = new ACLMessage(ACLMessage.CONFIRM);
							Point from = truck.getLocation();
							truck.moveTruck(point);
							Point to = truck.getLocation();
							Road roadFrom = Map.getElement(Road.class, from,
									map.mapMatrix);
							Road roadTo = Map.getElement(Road.class, to,
									map.mapMatrix);
							roadTo.setTruck(truck);
							// TODO: evitar que apague no inicio (v�rios na
							// mesma road)
							roadFrom.removeTruck();
							GarbageCollector.frame.mapComponent.repaintTruck(
									from, to);
						} else {
							sendMsg = new ACLMessage(ACLMessage.REFUSE);
						}

						// REQUEST_TYPE
						toSend = Integer
								.toString(WorldAgent.CONFIRM_REFUSE_MOVE);
						break;
					case TruckAgent.INFORM_EMPTIED_TRUCK:
						truck = Map.getTruckByAgentName(args[1],
								Map.INSTANCE.trucks);
						if (truck.remoteTruck) {
							truck.emptyTruck();
							truck.goingToDeposit = false;
						}
						break;
					case TruckAgent.INFORM_GOING_TO_DEPOSIT:
						truck = Map.getTruckByAgentName(args[1],
								Map.INSTANCE.trucks);
						if (truck.remoteTruck)
							truck.goingToDeposit = true;
						break;
					case TruckAgent.INFORM_CURRENT_DESTINATION:
						truck = Map.getTruckByAgentName(args[1],
								Map.INSTANCE.trucks);
						point = new Point(Integer.parseInt(args[2]),
								Integer.parseInt(args[3]));
						if (truck.remoteTruck) {
							truck.currentDestination = point;
							truck.getComponent().setCurrentDestination(truck);
						}
						break;
					case TruckAgent.INFORM_EMPTIED_CONTAINER:
						String name = args[1];
						point = new Point(Integer.parseInt(args[2]),
								Integer.parseInt(args[3]));
						truck = Map.getTruckByAgentName(name,
								Map.INSTANCE.trucks);
						if (truck.remoteTruck)
							try {
								int ammount = Integer.parseInt(args[4]);
								truck.addToTruck(ammount);
							} catch (TruckFullException e) {
								System.out
										.println("Sinchronization error in truck capacity!");
								e.printStackTrace();
							}
						c = Map.getElement(Container.class, point,
								map.mapMatrix);
						c.emptyContainer();
						GarbageCollector.frame.mapComponent.repaintElement(c,
								point);
						return; // no response to send
					default:
						System.out
								.println("(WorldAgent) GOT UNEXPECTED MESSAGE TYPE ("
										+ requestType + ")!");
						return;
					}
					if (sendMsg != null) {
						sendMsg.addReceiver(msg.getSender());
						sendMsg.setContent(toSend);
						send(sendMsg);
					}
				} else {
					block();
				}
			}
		});

	}

	// método takeDown
	@Override
	protected void takeDown() {
		// retira registo no DF
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
}
