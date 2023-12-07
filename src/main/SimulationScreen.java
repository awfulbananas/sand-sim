package main;
import java.awt.Color;
import java.awt.Graphics;

import fromics.Background;
import fromics.Frindow;
import fromics.TextElement;

public class SimulationScreen extends Background {
	
	private Simulation sim;

	public SimulationScreen(Frindow observer) {
		super(observer);
		setX(0);
		setY(0);
		sim = new Simulation();
		TextElement tooltips = new TextElement(10, 10, "tooltips.txt");
		tooltips.setColor(Color.white);
		link(sim);
		link(tooltips);
	}
	
	protected void draw(Graphics g, double xOff, double yOff, double angOff) {
		g.drawString("", getScreenWidth(), getScreenHeight());
	}

}
