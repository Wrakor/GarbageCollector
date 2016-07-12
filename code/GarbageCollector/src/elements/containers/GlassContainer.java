package elements.containers;

import java.awt.image.BufferedImage;

import assets.Assets;
import elements.DrawableElement;

public class GlassContainer extends Container implements DrawableElement {

	public GlassContainer(int capacity) {
		super(capacity);
	}

	public GlassContainer(GlassContainer other) {
		super(null);
	}

	@Override
	public BufferedImage getImg() {
		if (this.isEmpty())
			return Assets.glassContainer;
		else
			return Assets.glassContainerFull;
	}

	@Override
	public int getType() {
		return Assets.GLASS;
	}

	@Override
	public GlassContainer copy() {
		return new GlassContainer(this);
	}
}