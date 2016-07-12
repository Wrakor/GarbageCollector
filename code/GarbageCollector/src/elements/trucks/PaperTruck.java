package elements.trucks;

import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import assets.Assets;
import elements.DrawableElement;
import elements.MapElement;

public class PaperTruck extends Truck implements DrawableElement {

	public PaperTruck(Point initialLocation, int capacity,
			ContainerController containerController, String agentName,
			ArrayList<ArrayList<MapElement>> mapMatrix, boolean localInstance)
			throws StaleProxyException {
		super(initialLocation, capacity, containerController, agentName,
				Assets.PAPER, mapMatrix, localInstance);
	}

	@Override
	public BufferedImage getImg() {
		return Assets.paperTruck;
	}

	@Override
	public int getType() {
		return Assets.PAPER;
	}
}
