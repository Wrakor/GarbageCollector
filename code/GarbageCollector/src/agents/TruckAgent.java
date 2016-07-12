package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.util.Event;

import java.awt.Point;

import main.GarbageCollector;
import elements.trucks.TruckInform;

public class TruckAgent extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Event event;
	private AID worldAgent;
	public static final int REQUEST_CONTAINER_CAPACITY = 1, REQUEST_MOVE = 2,
			INFORM_OTHER_TRUCKS = 3, INFORM_EMPTIED_CONTAINER = 4,
			GOT_INFORM_EVENT = 7, INFORM_DISTANCE = 8, INFORM_CREATE_TRUCK = 9,
			INFORM_EMPTIED_TRUCK = 10, INFORM_GOING_TO_DEPOSIT = 11,
			INFORM_CURRENT_DESTINATION = 12;
	private TruckInform truckInformThread;

	// método setup
	@Override
	protected void setup() {
		// regista agente no DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType(String.valueOf(this.getArguments()[0]));
		this.truckInformThread = (TruckInform) this.getArguments()[1];
		setEnabledO2ACommunication(true, 0);
		System.out.println("Created TruckAgent " + getName() + " with type: "
				+ sd.getType());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
			// find world agent
			if (GarbageCollector.local) {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd1 = new ServiceDescription();
				sd1.setType("World");
				template.addServices(sd1);
				DFService.search(this, template);
				this.worldAgent = DFService.search(this, template)[0].getName();
			} else {
				this.worldAgent = new AID("World@"
						+ GarbageCollector.remotePlatformName, true);
				this.worldAgent.addAddresses(GarbageCollector.remoteMTP);
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		// adiciona behaviour ciclico (ler mensagens)
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;
			TruckAgent myTruckAgent = (TruckAgent) this.myAgent;

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
					switch (requestType) {
					case TruckAgent.INFORM_DISTANCE:
						synchronized (myTruckAgent.truckInformThread) {
							myTruckAgent.truckInformThread.gotDistance(
									new Point(Integer.parseInt(args[1]),
											Integer.parseInt(args[2])), Integer
											.parseInt(args[3]));
						}
						break;
					case TruckAgent.INFORM_OTHER_TRUCKS:
						synchronized (myTruckAgent.truckInformThread) {
							myTruckAgent.truckInformThread.gotInform(new Point(
									Integer.parseInt(args[1]), Integer
											.parseInt(args[2])));
						}
						break;
					case WorldAgent.INFORM_CONTAINER_CAPACITY:
						Point expectedContainer = (Point) event.getParameter(1);
						if (expectedContainer.x == Integer.parseInt(args[1])
								&& expectedContainer.y == Integer
										.parseInt(args[2]))
							event.notifyProcessed(Integer.parseInt(args[3]));
						else
							System.out
									.println("(TruckAgent) GOT CAPACITY OF AN UNEXPECTED CONTAINER");
						event = null;
						break;
					case WorldAgent.CONFIRM_REFUSE_MOVE:
						if (msg.getPerformative() == ACLMessage.CONFIRM)
							event.notifyProcessed(true);
						else if (msg.getPerformative() == ACLMessage.REFUSE)
							event.notifyProcessed(false);
						event = null;
						break;
					default:
						System.out
								.println("(TruckAgent) GOT UNEXPECTED MESSAGE TYPE("
										+ requestType + ")!");
						return;
					}
				} else {
					block();
				}
			}
		});

		// adiciona behaviour ciclico (enviar mensagens)
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;
			TruckAgent myTruckAgent = (TruckAgent) this.myAgent;

			@Override
			public void action() {
				// get an object from the O2A mailbox
				Event event = (Event) myAgent.getO2AObject();

				// if we actually got one
				if (event != null) {
					try {
						String messageContent = (String) event.getParameter(0);
						String[] args = messageContent.split("\\s+");
						String toSend = null;

						ACLMessage msg = null;

						switch (event.getType()) {
						case TruckAgent.INFORM_DISTANCE:
							msg = new ACLMessage(ACLMessage.INFORM);
							if (findTrucksByType(args[0], msg))
								toSend = event.getType() + " " + args[1] + " "
										+ args[2] + " " + args[3];
							break;
						case TruckAgent.INFORM_CREATE_TRUCK:
							msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(worldAgent);
							// REQUEST_TYPE + TruckName + Capacity + X + Y +
							// TruckType
							toSend = event.getType() + " " + args[0] + " "
									+ args[1] + " " + args[2] + " " + args[3]
									+ " " + args[4];
							break;
						case TruckAgent.INFORM_EMPTIED_TRUCK:
						case TruckAgent.INFORM_GOING_TO_DEPOSIT:
							msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(worldAgent);
							// REQUEST_TYPE + TruckName
							toSend = event.getType() + " " + args[0];
							break;
						case TruckAgent.INFORM_CURRENT_DESTINATION:
							msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(worldAgent);
							// REQUEST_TYPE + TruckName + X + Y
							toSend = event.getType() + " " + args[0] + " "
									+ args[1] + " " + args[2];
							break;
						case TruckAgent.REQUEST_CONTAINER_CAPACITY:
							myTruckAgent.event = event;
							msg = new ACLMessage(ACLMessage.REQUEST);
							msg.addReceiver(worldAgent);
							// REQUEST_TYPE + X + Y
							toSend = event.getType() + " " + args[0] + " "
									+ args[1];
							break;
						case TruckAgent.INFORM_OTHER_TRUCKS:
							// pesquisa DF por agentes do tipo de lixo
							// respectivo
							msg = new ACLMessage(ACLMessage.INFORM);
							if (findTrucksByType(args[0], msg))
								// INFORM_TYPE + X + Y
								toSend = event.getType() + " " + args[1] + " "
										+ args[2];
							break;
						case TruckAgent.REQUEST_MOVE:
							myTruckAgent.event = event;
							msg = new ACLMessage(ACLMessage.REQUEST);
							msg.addReceiver(worldAgent);
							// REQUEST_TYPE + TRUCK_NAME + X + Y + MOVE_DIR
							toSend = event.getType() + " " + args[0] + " "
									+ args[1] + " " + args[2] + " " + args[3];
							break;
						case TruckAgent.INFORM_EMPTIED_CONTAINER:
							msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(worldAgent);
							// REQUEST_TYPE + X + Y + Used_Capacity
							toSend = event.getType() + " " + args[0] + " "
									+ args[1] + " " + args[2] + " " + args[3];
							break;
						default:
							System.out
									.println("(TruckAgent) GOT UNEXPECTED MESSAGE TYPE("
											+ event.getType() + ")!");
							return;

						}
						if (toSend != null) {
							msg.setContent(toSend);
							send(msg);
						}
					} catch (IndexOutOfBoundsException e) {
						System.out.println("(TruckAgent) INVALID ARGUMENTS");
						e.printStackTrace();
						return;
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

	// não retorna o próprio
	private boolean findTrucksByType(String type, ACLMessage msg) {
		try {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType(type);
			template.addServices(sd1);
			DFAgentDescription[] result;
			result = DFService.searchUntilFound(this, this.getDefaultDF(),
					template, new SearchConstraints(), 100);
			if (result != null) {
				// result = DFService.search(this, template);
				for (int i = 0; i < result.length; ++i) {
					// dont send messages to self
					if (!this.getAID().equals(result[i].getName()))
						msg.addReceiver(result[i].getName());
				}
				return true;
			}
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
