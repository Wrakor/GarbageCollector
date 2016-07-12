package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import map.Map;
import assets.Assets;

public class MapJFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Dimension mapDim;
	public MapJComponent mapComponent;
	public Menu menu;

	public MapJFrame(String title) {
		super(title);

		Map map = Map.INSTANCE;
		mapDim = new Dimension(Assets.imgDim.width
				* map.mapMatrix.get(0).size(), Assets.imgDim.height
				* map.mapMatrix.size());

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		menu = new Menu();
		menu.setPreferredSize(new Dimension(400, mapDim.height));
		mapComponent = new MapJComponent(map);
		mapComponent.setPreferredSize(mapDim);
		this.getContentPane().add(mapComponent);
		this.getContentPane().add(menu, BorderLayout.EAST);

		this.setResizable(false);
		// Display the window.
		this.pack();
		this.setVisible(true);
	}
}
