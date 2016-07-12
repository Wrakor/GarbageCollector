package elements.containers;

import java.awt.image.BufferedImage;

import assets.Assets;
import elements.DrawableElement;

public class PaperContainer extends Container implements DrawableElement {

	public PaperContainer(int capacity) {
		super(capacity);
	}

	public PaperContainer(PaperContainer other) {
		super(null);
	}

	@Override
	public BufferedImage getImg() {
		if (this.isEmpty())
			return Assets.paperContainer;
		else
			return Assets.paperContainerFull;
	}

	@Override
	public int getType() {
		return Assets.PAPER;
	}

	@Override
	public PaperContainer copy() {
		return new PaperContainer(this);
	}
}