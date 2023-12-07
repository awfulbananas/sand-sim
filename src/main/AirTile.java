package main;

import java.awt.Color;

public class AirTile extends SimTile {
	public AirTile() {
		super(0.2, STATE_GAS, Color.white);
		data.put("id", 1.0);
		data.put("insulation", 0.1);
		data.put("heat transmission", 0.2);
		temp = DEFAULT_TEMP;
	}
	
	@Override
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		gasRise(prev);
		tempSpread(prev, sim.getSimTemp());
		return prev;
	}
	
	protected static boolean gasRise(SimTile[][] prev) {
		int start = (int)(3 * Math.random());
		int mod = 2 * (int)(Math.random() * 2) - 1;
		int i = start;
		do {
			if(gasSchwoof(i + 1, prev)) {
				return true;
			}
			i = (i + mod + 3) % 3;
		} while(i != start);
		return false;
	}
	
	protected static boolean gasSchwoof(int dir, SimTile[][] prev) {
		int nx = (dir==0||dir==1||dir==7)?2:(dir==3||dir==4||dir==5?0:1);
		int ny = (dir==1||dir==2||dir==3)?0:(dir==5||dir==6||dir==7?2:1);
		SimTile neighbor = prev[nx][ny];
		if(prev[1][ny] == null || prev[nx][1] == null) return false;
		SimTile center = prev[1][1];
		boolean pathClear;
		if(dir%2 == 0) {
			pathClear = true;
		} else {
			pathClear = prev[nx][1].state != STATE_SOLID || prev[1][ny].state != STATE_SOLID;
		}
		if(neighbor.state != STATE_SOLID && pathClear && prev[nx][ny].density >= prev[1][1].density && prev[nx][ny].temp < prev[1][1].temp) {
			prev[nx][ny] = center;
			prev[1][1] = neighbor;
			return true;
		}
		return false;
	}
}
