package main;

import java.awt.Color;

public class SteamTile extends SimTile {
	public static final double CONDENSE_TEMP = 100;
	public static final double CONDENSE_EXOTHERM = 20;
	
	private int life;
	
	public SteamTile() {
		this(DEFAULT_TEMP);
	}
	
	public SteamTile(double startTemp) {
		super(0.01, STATE_GAS, Color.LIGHT_GRAY);
		life = 40;
		data.put("id", 3.1);
		temp = DEFAULT_TEMP;
	}
	
	@Override
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		tempSpread(prev, sim.getSimTemp());
		if(temp < CONDENSE_TEMP) {
			life--;
			if(life <= 0 && Math.random() < 0.1) {
				prev[1][1] = new WaterTile();
				prev[1][1].temp = this.temp + CONDENSE_EXOTHERM - 10;
				return prev;
			}
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
					if(!liquidSlide(true, prev)) {;
					}
				} else {
					if(!liquidSlide(false, prev)) {
					}
				}
			}
		}
		return prev;
	}
}
