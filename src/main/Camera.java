package main;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import fromics.Linkable;

public class Camera extends Linkable {

	public Camera(double x, double y, double z) {
		super(x, y, z);
	}
	
	public boolean update() {
		if(getKey(KeyEvent.VK_RIGHT)) {
			setX(X() - 0.0003 * Z() * dt());
		}
		if(getKey(KeyEvent.VK_LEFT)) {
			setX(X() + 0.0003 * Z() * dt());
		}
		if(getKey(KeyEvent.VK_UP)) {
			setY(Y() + 0.0003 * Z() * dt());
		}
		if(getKey(KeyEvent.VK_DOWN)) {
			setY(Y() - 0.0003 * Z() * dt());
		}
		if(getKey(KeyEvent.VK_PAGE_UP)) {
			setZ(Z() * (1 + 0.000001 * dt()));
		}
		if(getKey(KeyEvent.VK_PAGE_DOWN)) {
			setZ(Z() / (1 + 0.000001 * dt()));
		}
		
		return false;
	}

	@Override
	protected void draw(Graphics g, double xOff, double yOff, double angOff) {}
	
}
