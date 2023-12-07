package main;

import java.awt.Color;

public class WaterTile extends SimTile {
	public static final int MOMENTUM = 3;
	public static final double EXTRA_BOIL_ENERGY = 20;
	public static final double BOIL_TEMP = 100;
	public static Color C1 = new Color(0.1f,0.1f,1f);
	public static Color C2 = new Color(0.15f,0.15f,1f);
	private boolean movingLeft;
	private int swapTimer;
	
	public WaterTile() {
		super(1, STATE_LIQUID, Color.blue);
		data.put("id", 3.0);
		temp = DEFAULT_TEMP;
//		data.put("specific heat", 4.0);
		movingLeft = Math.random() < 0.5;
		swapTimer = MOMENTUM;
		c = movingLeft?C1:C2;
	}
	
	private boolean swapDir() {
		swapTimer--;
		if(swapTimer == 0) {
			movingLeft = !movingLeft;
			c = movingLeft?C1:C2;
			swapTimer = MOMENTUM;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		SimTile belowTile = prev[1][2];
		double curTemp = temp;
		boolean storingTemp = false;
		if(temp > BOIL_TEMP) {
			temp = BOIL_TEMP;
			storingTemp = true;
		}
		tempSpread(prev, sim.getSimTemp());
		if(storingTemp) temp += curTemp - BOIL_TEMP;
		if(temp > BOIL_TEMP + EXTRA_BOIL_ENERGY) {
			prev[1][1] = new SteamTile(temp - EXTRA_BOIL_ENERGY + 10);
			return prev;
		}
		if(belowTile != null && belowTile.density < density && belowTile.state != STATE_SOLID) {
			prev[1][2] = prev[1][1];
			prev[1][1] = belowTile;
			return prev;
		}
		if(!move(prev)) {
			if(swapDir())move(prev);
		}
		return prev;
	}
	
	private boolean move(SimTile[][] prev) {
		if(liquidFall(movingLeft, prev)) {
			return true;
		} else {
			return liquidSlide(movingLeft, prev);
		}
	}
	
	protected static boolean liquidSlide(boolean isLeft, SimTile[][] prev) {
		int nx = isLeft?0:2;
		SimTile center = prev[1][1];
		SimTile neighbor = prev[nx][1];
		if(neighbor.state > 1) {
			prev[nx][1] = center;
			prev[1][1] = neighbor;
			return true;
		}
		return false;
	}
}
