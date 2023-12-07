package main;
import java.awt.image.BufferedImage;

import fromics.Background;
import fromics.Frindow;
import fromics.Manager;

public class Game extends Manager {
	private Frindow win;

	public static void main(String[] args) {
		new Game(new Frindow(BufferedImage.TYPE_INT_RGB, 917, 640));
	}
	
	public Game(Frindow win) {
		super(win, 50, 5);
		
		this.win = win;
		screens = new Background[2];
		win.init(3, this);
		screen = 1;
		initScreen(1);
		startVariableLoop();
	}

	@Override
	protected void initScreen(int n) {
		switch(n) {
			case 0:
				screens[0] = new TitleScreen(win);
				link(screens[0]);
			case 1:
				screens[1] = new SimulationScreen(win);
				link(screens[1]);
		}
	}
}
