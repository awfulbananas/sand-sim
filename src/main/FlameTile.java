package main;

import java.awt.Color;

public class FlameTile extends SimTile {
	private int life;
	private int dirtiness;
	
	public FlameTile() {
		this(1250, 30);
	}
	
	public FlameTile(double startTemp, int dirtiness) {
		super(0.05, STATE_PLASMA, Color.orange);
		this.dirtiness = dirtiness;
		data.put("id", 5.0);
		temp = startTemp;
		life = 30;
	}
	
	@Override
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		life--;
		if(!checkO2(prev)) life /= 4;
		if(temp < 200 || life == 0) {
			prev[1][1] = Math.random() < 0.5 ? new SmokeTile(temp, dirtiness):new AirTile();
			prev[1][1].temp = temp;
			return prev;
		}
		tempSpread(prev, sim.getSimTemp());
		SimTile belowTile = prev[1][2];
		if(belowTile != null && belowTile.density < density && belowTile.state != STATE_SOLID) {
			prev[1][2] = prev[1][1];
			prev[1][1] = belowTile;
			return prev;
		}
		boolean fell = true;
		if(Math.random() < 0.5) {
			if(!powderFall(true, prev)) {
				fell = powderFall(false, prev);
			}
		} else {
			if(!powderFall(false, prev)) {
				fell = powderFall(true, prev);
			}
		}
		if(!fell) {
			if(!gasRise(prev, true)) {
				if(Math.random() < 0.5) {
					if(!liquidSlide(true, prev)) {
						fell = liquidSlide(false, prev);
					}
				} else {
					if(!liquidSlide(false, prev)) {
						fell = liquidSlide(true, prev);
					}
				}
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
