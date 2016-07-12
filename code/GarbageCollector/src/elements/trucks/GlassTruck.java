package elements.trucks;

import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import assets.Assets;
import elements.DrawableElement;
import elements.MapElement;

public class GlassTruck extends Truck implements DrawableElement {

	public GlassTruck(Point initialLocation, int capacity,
			ContainerController containerController, String agentName,
			ArrayList<ArrayList<MapElement>> mapMatrix, boolean localInstance)
			throws StaleProxyException {
		super(initialLocation, capacity, containerController, agentName,
				Assets.GLASS, mapMatrix, localInstance);
	}

	@Override
	public BufferedImage getImg() {
		return Assets.glassTruck;
	}

	@Override
	public int getType() {
		return Assets.GLASS;
	}
}
