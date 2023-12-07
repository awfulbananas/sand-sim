package main;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
//id guide:
//0: edge tile
//1: air tile
//2: powder tile
//3: water tile
public class SimTile {
	public static final int STATE_SOLID = 0;
	public static final int STATE_LIQUID = 1;
	public static final int STATE_GAS = 2;
	public static final int STATE_PLASMA = 3;
	
	//degrees C
	public static final double DEFAULT_TEMP = 20.0;
	public static final double DEFAULT_HEAT_TRANSMISSION = 0.25;
	public static final double DEFAULT_HEAT_INSULATION = 1;
	
	public final double density;
	public final int state;
	protected double temp;
	public Color c;
	private int UID;
	protected Map<String, Double> data;
	protected Function<SimTile[][], SimTile[][]> behaviour;
	
	public SimTile() {
		this(1, STATE_SOLID, Color.yellow);
	}
	
	public int getUID() {
		return UID;
	}
	
	public void setUID(int id) {
		UID = id;
	}
	
	public SimTile(double density, int state, Color c) {

		this.density = density;
		this.state = state;
		this.c = c;
		data = new HashMap<>();
	}
	
	public SimTile[][] updateTiles(SimTile[][] prev, Simulation sim) {
		return behaviour.apply(prev);
	}
	
	protected static boolean gasSchwoof(int dir, SimTile[][] prev, boolean checkDensity) {
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
		if(neighbor.state != STATE_SOLID && pathClear && (!checkDensity || prev[nx][ny].density >= prev[1][1].density)) {
			prev[nx][ny] = center;
			prev[1][1] = neighbor;
			return true;
		}
		return false;
	}
	
	protected static boolean gasRise(SimTile[][] prev, boolean checkDensity) {
		boolean rose = false;
		int start = (int)(3 * Math.random());
		int mod = 2 * (int)(Math.random() * 2) - 1;
		int i = start;
		do {
			if(gasSchwoof(i + 1, prev, checkDensity)) {
				return true;
			}
			i = (i + mod + 3) % 3;
		} while(i != start);
		return rose;
	}
	
	protected static boolean powderFall(boolean isLeft, SimTile[][] prev) {
		int nx = isLeft?0:2;
		SimTile center = prev[1][1];
		SimTile neighbor = prev[nx][1];
		SimTile belowNeighbor = prev[nx][2];
		if(belowNeighbor != null && neighbor.state != STATE_SOLID && belowNeighbor.state != STATE_SOLID && belowNeighbor.density < center.density) {
			prev[nx][2] = center;
			prev[1][1] = belowNeighbor;
			return true;
		}
		return false;
	}
	
	protected static boolean liquidSlide(boolean isLeft, SimTile[][] prev) {
		int nx = isLeft?0:2;
		SimTile center = prev[1][1];
		SimTile neighbor = prev[nx][1];
		if(neighbor.state > 0) {
			prev[nx][1] = center;
			prev[1][1] = neighbor;
			return true;
		}
		return false;
	}
	
	protected static boolean liquidFall(boolean isLeft, SimTile[][] prev) {
		int nx = isLeft?0:2;
		SimTile center = prev[1][1];
		SimTile neighbor = prev[nx][1];
		SimTile belowNeighbor = prev[nx][2];
		if(belowNeighbor != null && neighbor.state > 1 && belowNeighbor.state > 1 && belowNeighbor.density < center.density) {
			prev[nx][2] = center;
			prev[1][1] = belowNeighbor;
			return true;
		}
		return false;
	}
	
	protected static void tempSpread(SimTile[][] prev, boolean simTemp) {
		if(!simTemp) return;
		SimTile center = prev[1][1];
		double cTemp;
		double specHeat = center.data.containsKey("specific heat")?center.data.get("specific heat"):1;
		cTemp = center.temp;
		double tempLost = 0;
		for(int x = 0; x < 3; x++) {
			for(int y = 0; y < 3; y++) {
				if(prev[x][y] == center || prev[x][y] == null) continue;
				SimTile cur = prev[x][y];
				double insulation = cur.data.containsKey("insulation")?cur.data.get("insulation"):DEFAULT_HEAT_INSULATION;
				double heatTransmission = cur.data.containsKey("heat transmission")?cur.data.get("heat transmission"):DEFAULT_HEAT_TRANSMISSION;
				double heatTransferred = (cTemp - cur.temp) * insulation * heatTransmission;
				cur.temp += heatTransferred * specHeat;
				tempLost += heatTransferred;
			}
		}
		center.temp -= tempLost / specHeat;
	}
}
