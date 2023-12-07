package main;

import java.awt.Color;

public class SmokeTile extends SimTile {
	private int lifetime;
	
	public SmokeTile() {
		this(DEFAULT_TEMP, 30);
	}
	
	public SmokeTile(double startTemp, int life) {
		super(0.04, STATE_GAS, Color.gray);
		lifetime = life;
		data.put("id", 6.0);
		temp = DEFAULT_TEMP;
	}
	
	@Override
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		lifetime--;
		tempSpread(prev, sim.getSimTemp());
		if(lifetime == 0) {
			prev[1][1] = new AirTile();
			prev[1][1].temp = this.temp;
			return prev;
		}
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
					liquidSlide(true, prev);
				} else {
					liquidSlide(false, prev);
				}
			}
		}
		return prev;
	}
}
