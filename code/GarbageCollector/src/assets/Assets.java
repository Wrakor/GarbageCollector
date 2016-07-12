package assets;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class Assets {
	public static BufferedImage grass, asphalt, glassContainer,
			glassContainerFull, paperContainer, paperContainerFull,
			plasticContainer, plasticContainerFull, garbageContainer,
			garbageContainerFull, deposit, glassTruck, paperTruck,
			plasticTruck, garbageTruck;

	public static Dimension imgDim;

	// directions for truck movement
	public static final int TOP = 0, BOTTOM = 1, LEFT = 2, RIGHT = 3,
			PLASTIC = 0, PAPER = 1, GLASS = 2, GARBAGE = 3;

	public static void loadAssets() {
		try {
			System.out.println("Loading assets...");
			grass = ImageIO.read(new File("img/grass.png"));
			asphalt = ImageIO.read(new File("img/asphalt.png"));
			glassContainer = ImageIO.read(new File("img/glasscontainer.png"));
			paperContainer = ImageIO.read(new File("img/papercontainer.png"));
			plasticContainer = ImageIO
					.read(new File("img/plasticcontainer.png"));
			garbageContainer = ImageIO
					.read(new File("img/garbagecontainer.png"));
			glassContainerFull = ImageIO.read(new File(
					"img/glasscontainerfull.png"));
			paperContainerFull = ImageIO.read(new File(
					"img/papercontainerfull.png"));
			plasticContainerFull = ImageIO.read(new File(
					"img/plasticcontainerfull.png"));
			garbageContainerFull = ImageIO.read(new File(
					"img/garbagecontainerfull.png"));
			deposit = ImageIO.read(new File("img/deposit.png"));
			glassTruck = ImageIO.read(new File("img/glasstruck.png"));
			paperTruck = ImageIO.read(new File("img/papertruck.png"));
			plasticTruck = ImageIO.read(new File("img/plastictruck.png"));
			garbageTruck = ImageIO.read(new File("img/garbagetruck.png"));
			imgDim = new Dimension(grass.getWidth() / 2, grass.getHeight() / 2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int reverseDirection(int dir) {
		switch (dir) {
		case Assets.TOP:
			return Assets.BOTTOM;
		case Assets.BOTTOM:
			return Assets.TOP;
		case Assets.LEFT:
			return Assets.RIGHT;
		case Assets.RIGHT:
			return Assets.LEFT;
		}
		throw new IllegalArgumentException();
	}

	public static int getMoveDirection(Point source, Point dest) {
		if (source.x > dest.x)
			return Assets.LEFT;
		if (source.x < dest.x)
			return Assets.RIGHT;
		if (source.y > dest.y)
			return Assets.BOTTOM;
		if (source.y < dest.y)
			return Assets.TOP;
		return -1;
	}
}
