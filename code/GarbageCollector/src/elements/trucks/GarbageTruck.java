package elements.trucks;

import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import assets.Assets;
import elements.DrawableElement;
import elements.MapElement;

public class GarbageTruck extends Truck implements DrawableElement {

	public GarbageTruck(Point initialLocation, int capacity,
			ContainerController containerController, String agentName,
			ArrayList<ArrayList<MapElement>> mapMatrix, boolean localInstance)
			throws StaleProxyException {
		super(initialLocation, capacity, containerController, agentName,
				Assets.GARBAGE, mapMatrix, localInstance);
	}

	@Override
	public BufferedImage getImg() {
		return Assets.garbageTruck;
	}

	@Override
	public int getType() {
		return Assets.GARBAGE;
	}

}
