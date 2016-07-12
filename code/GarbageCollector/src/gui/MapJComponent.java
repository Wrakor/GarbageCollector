package gui;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JComponent;

import map.Map;
import assets.Assets;
import elements.MapElement;

public class MapJComponent extends JComponent {

	/**
	 * 
	 */
	private Map map;
	private static final long serialVersionUID = 1L;

	public MapJComponent(Map map) {
		this.map = map;
	}

	@Override
	protected void paintComponent(Graphics g) {
		for (int y = 0; y < map.mapMatrix.size(); y++) {
			ArrayList<MapElement> line = map.mapMatrix.get(y);
			for (int x = 0; x < line.size(); x++) {
				MapElement element = line.get(x);
				BufferedImage img = element.getImg();
				if (img != null)
					g.drawImage(img, x * Assets.imgDim.width, y
							* Assets.imgDim.height, Assets.imgDim.width,
							Assets.imgDim.height, null);
			}
		}
	}

	public void repaintElement(MapElement element, Point location) {
		repaint(MapJComponent.calcRepaintRectangle(location, null));
	}

	public void repaintTruck(Point from, Point to) {
		repaint(MapJComponent.calcRepaintRectangle(from, to));
	}

	private static Rectangle calcRepaintRectangle(Point from, Point to) {
		Rectangle toRepaint = new Rectangle(from.x * Assets.imgDim.width,
				from.y * Assets.imgDim.height, Assets.imgDim.width,
				Assets.imgDim.height);
		if (to != null) {
			Rectangle rectTo = new Rectangle(to.x * Assets.imgDim.width, to.y
					* Assets.imgDim.height, Assets.imgDim.width,
					Assets.imgDim.height);
			toRepaint.add(rectTo);
		}
		return toRepaint;
	}
}
