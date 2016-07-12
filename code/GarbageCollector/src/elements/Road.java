package elements;

import java.awt.image.BufferedImage;

import assets.Assets;
import elements.trucks.Truck;

public class Road extends MapElement implements DrawableElement {
	private boolean twoWay;
	private Truck truck;

	public Road(boolean twoWay) {
		this.twoWay = twoWay;
	}

	@Override
	public BufferedImage getImg() {
		if (truck == null)
			return Assets.asphalt;
		else
			return truck.getImg();
	}

	@Override
	public Road copy() {
		return this;
	}

	public Truck getTruck() {
		return truck;
	}

	public void setTruck(Truck truck) {
		this.truck = truck;
	}

	public void removeTruck() {
		this.truck = null;
	}

	public boolean isTwoWay() {
		return twoWay;
	}
}
