package elements;

import java.awt.image.BufferedImage;

import assets.Assets;

public class Deposit extends MapElement implements DrawableElement {

	@Override
	public BufferedImage getImg() {
		return Assets.deposit;
	}

	@Override
	public Deposit copy() {
		return this;
	}
}
