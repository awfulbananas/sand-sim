package main;

import java.awt.Color;

public class CoalTile extends SimTile {
	public static final double COMBUSTION_ENERGY = 700;
	public static final double FLASH_POINT = 750;
	public static final double EXTENGUISH_TEMP = 200;
	public static final double BURN_RATE = 0.5;
	
	private int fuel;
	private boolean ignited;
	
	public CoalTile() {
		super(2, STATE_SOLID, new Color((int)(5 + 10 * Math.random()),
													(int)(5 + 10 * Math.random()),
													(int)(5 + 10 * Math.random())));
		fuel = 10;
		ignited = false;
		data.put("id", 7.0);
		temp = DEFAULT_TEMP;
	}
	
	@Override
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		if(Math.random() < (temp - FLASH_POINT) / 100 || ignited) {
			if(checkIgnition(prev)){
				ignited = true;
				burn(prev);
			} else {
				ignited = false;
			}
		}
		tempSpread(prev, sim.getSimTemp());
		SimTile belowTile = prev[1][2];
		if(belowTile != null && belowTile.density < density && belowTile.state != STATE_SOLID) {
			prev[1][2] = prev[1][1];
			prev[1][1] = belowTile;
			return prev;
		}
		if(Math.random() < 0.5) {
			if(!powderFall(true, prev)) {
				powderFall(false, prev);
			}
		} else {
			if(!powderFall(false, prev)) {
				powderFall(true, prev);
			}
		}
		return prev;
	}
	
	private boolean checkIgnition(SimTile[][] prev) {
		if(Math.random() < (EXTENGUISH_TEMP - temp) / 10) return false;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(prev[i][j].data.get("id") == 1.0) {
					return true;
				}
			}
		}
		return false;
	}

	private void burn(SimTile[][] prev) {
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(i==1&&j==1) continue;
				if(prev[i][j].data.get("id") == 1.0 && Math.random() < BURN_RATE) {
					fuel--;
					if(fuel == 0) {
						prev[1][1] = new FlameTile(temp + COMBUSTION_ENERGY, 50);
					} else {
						prev[i][j] = new FlameTile(prev[i][j].temp + COMBUSTION_ENERGY, 50);
					}
				}
			}
		}
	}
}
