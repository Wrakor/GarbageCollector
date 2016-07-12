package gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.util.Map;

import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;

import algorithms.MapGraph;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;

public class JGraphFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
	// private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);
	//
	private JGraphModelAdapter<Point, DefaultEdge> m_jgAdapter;

	public JGraphFrame(MapGraph graph) {
		super();
		m_jgAdapter = new JGraphModelAdapter<Point, DefaultEdge>(graph);
		JGraph jgraph = new JGraph(m_jgAdapter);
		JGraphFacade jgf = new JGraphFacade(jgraph);
		JGraphFastOrganicLayout layoutifier = new JGraphFastOrganicLayout();
		layoutifier.run(jgf);
		System.out.println("Layout complete");

		final Map nestedMap = jgf.createNestedMap(true, true);
		jgraph.getGraphLayoutCache().edit(nestedMap);

		jgraph.getGraphLayoutCache().update();
		jgraph.refresh();
		this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);
		adjustDisplaySettings(jgraph);
		this.getContentPane().add(jgraph);
		this.setVisible(true);
	}

	private void adjustDisplaySettings(JGraph jg) {
		jg.setPreferredSize(this.getSize());

		Color c = DEFAULT_BG_COLOR;
		/*
		 * String colorStr = null;
		 * 
		 * try { colorStr = getParameter( "bgcolor" ); } catch( Exception e ) {}
		 * 
		 * if( colorStr != null ) { c = Color.decode( colorStr ); }
		 */

		jg.setBackground(c);
	}

	/*
	 * private void positionVertexAt( Object vertex, int x, int y ) {
	 * DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex ); Map attr =
	 * cell.getAttributes( ); Rectangle b = GraphConstants.getBounds( attr );
	 * 
	 * GraphConstants.setBounds( attr, new Rectangle( x, y, b.width, b.height )
	 * );
	 * 
	 * Map cellAttr = new HashMap( ); cellAttr.put( cell, attr );
	 * m_jgAdapter.edit( cellAttr, null, null, null, null ); }
	 */
}
