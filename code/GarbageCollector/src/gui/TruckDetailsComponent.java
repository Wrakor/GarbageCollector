package gui;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import elements.trucks.Truck;

public class TruckDetailsComponent extends JPanel {
	private static final long serialVersionUID = 1L;
	JLabel name, currentDestination, currentUsage;

	public TruckDetailsComponent(Truck truck) {

		if (!(truck.getCurrentDestination() == null))
			this.currentDestination = new JLabel("<html>("
					+ (int) truck.getCurrentDestination().getX() + ", "
					+ (int) truck.getCurrentDestination().getY() + ")</html>");
		else
			this.currentDestination = new JLabel("<html>None yet</html>");

		this.currentUsage = new JLabel("<html>"
				+ Integer.toString(truck.getUsedCapacity()) + " / "
				+ truck.getCapacity() + "</html>");

		switch (truck.getType()) {
		case 0:
			this.name = new JLabel("<html><font color = 'orange'>"
					+ truck.getAgentName() + "</font> </html>");
			break;
		case 1:
			this.name = new JLabel("<html><font color = 'blue'>"
					+ truck.getAgentName() + "</font></html>");
			break;
		case 2:
			this.name = new JLabel("<html><font color = 'green'>"
					+ truck.getAgentName() + "</font></html>");
			break;
		case 3:
			this.name = new JLabel("<html><font color = 'black'>"
					+ truck.getAgentName() + "</font></html>");
			break;
		}
		initComponents();
	}

	private void initComponents() {

		Font font = this.name.getFont();
		Font unboldFont = new Font(font.getFontName(), Font.PLAIN,
				font.getSize());

		currentDestination.setFont(unboldFont);
		currentUsage.setFont(unboldFont);

		this.setLayout(new GridLayout(1, 3));
		this.add(name);
		this.add(currentDestination);
		this.add(currentUsage);
	}

	public void setCurrentDestination(Truck truck) {
		this.currentDestination.setText("<html>("
				+ (int) truck.getCurrentDestination().getX() + ", "
				+ (int) truck.getCurrentDestination().getY() + ")</html>");
		this.currentDestination.repaint();
	}

	public void setCurrentUsage(Truck truck) {
		this.currentUsage.setText("<html>"
				+ Integer.toString(truck.getUsedCapacity()) + " / "
				+ truck.getCapacity() + "</html>");
		this.currentUsage.repaint();
	}
}
