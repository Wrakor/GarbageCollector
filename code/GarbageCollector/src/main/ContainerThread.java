package main;

import java.util.ArrayList;

import map.Map;
import elements.containers.Container;
import exceptions.ContainerFullException;

public class ContainerThread extends Thread {
	private boolean go = true;
	private static final int tickTime = 10000; // in ms
	private ArrayList<Container> containers;

	public ContainerThread() {
		this.containers = Map.INSTANCE.containers;
	}

	@Override
	public synchronized void start() {
		System.out.println("Starting container thread...");
		super.start();
	}

	@Override
	public void run() {
		while (go) {
			try {
				for (Container c : containers) {
					int added = addRandomToContainer(c);
					if (c.getUsedCapacity() - added == 0) // was empty
						GarbageCollector.frame.mapComponent.repaintElement(c,
								Map.findElement(Container.class, c,
										Map.INSTANCE.mapMatrix));
				}
				Thread.sleep(tickTime);
			} catch (InterruptedException e) {
				System.out.println("Container thread interrupted, exiting");
			}
		}
	}

	private int addRandomToContainer(Container c) {
		try {
			int toAdd = GarbageCollector.randGenerator.nextInt(2);
			c.addToContainer(toAdd);
			return toAdd;
		} catch (ContainerFullException e) {
			// do nothing
		}
		return 0;
	}

	@Override
	public void interrupt() {
		this.go = false;
		super.interrupt();
	}
}
