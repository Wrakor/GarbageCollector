package elements.containers;

import java.awt.image.BufferedImage;

import assets.Assets;
import elements.DrawableElement;

public class GarbageContainer extends Container implements DrawableElement {

	public GarbageContainer(int capacity) {
		super(capacity);
	}

	public GarbageContainer(GarbageContainer other) {
		super(null);
	}

	@Override
	public BufferedImage getImg() {
		if (this.isEmpty())
			return Assets.garbageContainer;
		else
			return Assets.garbageContainerFull;
	}

	@Override
	public int getType() {
		return Assets.GARBAGE;
	}

	@Override
	public GarbageContainer copy() {
		return new GarbageContainer(this);
	}
}