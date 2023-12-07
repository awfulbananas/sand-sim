package main;

import java.awt.Color;

public class EdgeTile extends SimTile {
	private boolean heatSink;
	
	public EdgeTile(boolean heatSink) {
		super(1000, STATE_SOLID, Color.black);
		this.heatSink = heatSink;
		data.put("id", 0.0);
		temp = DEFAULT_TEMP;
		data.put("insulation", 1.2);
	}
	
	@Override
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		if(sim.getSimTemp() && heatSink && temp != DEFAULT_TEMP && sim.hasHeatSink()) {
			temp -= (temp - DEFAULT_TEMP) * 0.5;
		} else {
			tempSpread(prev, sim.getSimTemp());
		}
		return prev;
	}
}
