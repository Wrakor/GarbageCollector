package algorithms;

import java.awt.Point;
import java.util.List;

import map.Map;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import elements.Road;

public class MapGraph extends DefaultDirectedGraph<Point, DefaultEdge> {
	private static final long serialVersionUID = 1L;

	public MapGraph(Map map) {
		super(DefaultEdge.class);
		System.out.println("Creating graph...");
		int countVertex = 0, countEdge = 0;
		for (Point road : map.roadPoints) {
			this.addVertex(road);
			countVertex++;
		}
		for (Point road : map.roadPoints) {
			List<Point> adjacentRoads = Map.getAllAdjacentPoints(Road.class,
					road, map.mapMatrix);
			for (Point adjacent : adjacentRoads) {
				this.addEdge(road, adjacent);
				countEdge++;
			}
		}
		System.out.println("Processed " + countVertex + " vertexes and "
				+ countEdge + " edges");
	}
}