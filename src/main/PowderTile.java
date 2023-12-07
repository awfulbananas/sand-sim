package main;

import java.awt.Color;

public class PowderTile extends SimTile {
	public static final double FLASH_POINT = 200.0;
	public static final int COMBUSTION_ENERGY = 1000;
	
	public PowderTile() {
		super(1.2, STATE_SOLID, new Color((float)(0.1*(Math.random()-0.5)+0.8), 
													   (float)(0.1*(Math.random()-0.5)+0.8), 
													   (float)(0.1*(Math.random()-0.5)+0.8)));
		data.put("id", 4.0);
		temp = DEFAULT_TEMP;
	}
	
	@Override
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		if(Math.random() < (temp - FLASH_POINT) / 1500 && checkO2(prev)) {
			
			prev[1][1] = new FlameTile(temp + COMBUSTION_ENERGY, 20);
			return prev;
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
	
	private boolean checkO2(SimTile[][] prev) {
		for(int x = 0; x < 3; x++) {
			for(int y = 0; y < 3; y++) {
				if(x == 1 && y == 1) continue;
				if(prev[x][y].data.get("id") == 1.0) {
					return true;
				}
			}
		}
		return false;
	}
}
