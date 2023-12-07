package main;

import java.awt.Color;

public class SandTile extends SimTile {
	
	public SandTile() {
		super(1.2, STATE_SOLID, new Color((int)(210 + 40 * Math.random()),
													  (int)(210 + 40 * Math.random()),
													  (int)(120 + 30 * Math.random())));
		data.put("id", 2.0);
		temp = 20;
	}
	
	@Override
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		SimTile belowTile = prev[1][2];
		tempSpread(prev, sim.getSimTemp());
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
}
