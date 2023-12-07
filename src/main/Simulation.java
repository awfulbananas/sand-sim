package main;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import files.BezierSpline;
import fromics.Linkable;
import fromics.Mouse;
import fromics.Point;

public class Simulation extends Linkable {
	public static final Point DRAW_OFFSET = new Point(300, 0);
	public static final Point DRAW_SIZE = new Point(600, 600);
	public static final Point SIZE = new Point(150, 150);
	public static final int NUM_VIEWS = 2;
	public static final Function<SimTile, Color> DRAW_COLOR = (SimTile t) -> t.c;
	public static final double MIN_TEMP_SHOWN = 0;
	public static final double MAX_TEMP_SHOWN = 120;
	public static final Point[] TEMP_GRADIENT_POINTS = {new Point(0.545, 0.617, 1),  new Point(0.26, 0.236, 0.207), 
		    											new Point(1, 1, 0.234),  new Point(1, 0, 0)};
	public static final BezierSpline TEMP_COLOR_SPLINE = new BezierSpline(TEMP_GRADIENT_POINTS);
	public static final Function<SimTile, Color> DRAW_TEMP = (SimTile t) -> {
		Point col = TEMP_COLOR_SPLINE.get(clamp((t.temp - MIN_TEMP_SHOWN) / (MAX_TEMP_SHOWN - MIN_TEMP_SHOWN),0.0,1.0));
		return new Color((float)col.X(), (float)col.Y(), (float)col.Z());
	};
	public static final Map<Integer, TileType> KEY_MAPPINGS = new HashMap<>(); {{
		KEY_MAPPINGS.put(KeyEvent.VK_1, TileType.AIR);
		KEY_MAPPINGS.put(KeyEvent.VK_2, TileType.EDGE);
		KEY_MAPPINGS.put(KeyEvent.VK_3, TileType.SAND);
		KEY_MAPPINGS.put(KeyEvent.VK_4, TileType.WATER);
		KEY_MAPPINGS.put(KeyEvent.VK_5, TileType.POWDER);
		KEY_MAPPINGS.put(KeyEvent.VK_6, TileType.COAL);
		KEY_MAPPINGS.put(KeyEvent.VK_7, TileType.FLAME);
		KEY_MAPPINGS.put(KeyEvent.VK_8, TileType.SMOKE);
		KEY_MAPPINGS.put(KeyEvent.VK_9, TileType.STEAM);
	}};
	
	public enum TileType {
		AIR, EDGE, FLAME, POWDER, SAND, WATER, FIRE, SMOKE, STEAM, COAL;
	}
	
	private static SimTile getTile(TileType id, int variation) {
		switch(id) {
			case EDGE:
				return variation == 0?new EdgeTile(false):new EdgeTile(true);
			case AIR:
				return new AirTile();
			case SAND:
				return new SandTile();
			case WATER:
				return new WaterTile();
			case POWDER:
				return new PowderTile();
			case FLAME:
				return new FlameTile();
			case SMOKE:
				return new SmokeTile();
			case STEAM:
				return new SteamTile();
			case COAL:
				return new CoalTile();
			default:
				return new SandTile();
		}
	}
	
//	private static int maxUID = 0;
//	private static List<SimTile> tileList = new ArrayList<>();
	
	private SimTile[][] curData;
	private Point prevMouseLoc;
	private SimTile prevMouseTile;
	private int brushSize;
	private TileType curTile1;
	private TileType curTile2;
	private int view;
	private boolean simulateTemp;
	private boolean heatSink;
	
//	public static void assignUID(SimTile t) {
//		t.setUID(maxUID++);
//		tileList.add(t);
//	}
	
	private static double clamp(double val, double min, double max) {
		if(val < min) return min;
		if(val > max) return max;
		return val;
	}
	
	public Simulation() {
		super(0, 0);
		view = 0;
		simulateTemp = true;
		heatSink = true;
		brushSize = 1;
		curTile1 = TileType.SAND;
		curTile2 = TileType.WATER;
		prevMouseLoc = new Point(0,0);
		curData = new SimTile[(int)SIZE.X() + 2][(int)SIZE.Y() + 2];
		for(int i = 1; i < curData.length - 1; i++) {
			for(int j = 1; j < curData[0].length - 1; j++) {
				curData[i][j] = new AirTile();
			}
		}
		
		for(int i = 0; i < curData.length; i++) {
			curData[i][0] = new EdgeTile(true);
			curData[i][(int)SIZE.Y() - 1] = new EdgeTile(true);
		}
		for(int j = 1; j < curData.length - 1; j++) {
			curData[0][j] = new EdgeTile(true);
			curData[(int)SIZE.Y() - 1][j] = new EdgeTile(true);
		}
		
		prevMouseTile = curData[0][0];
	}
	
	public boolean getSimTemp() {
		return simulateTemp;
	}
	
	public int getBrushSize() {
		return brushSize;
	}
	
	@Override
	public void onFirstLink() {
		addKeystrokeFunction((KeyEvent e) -> {
			int n = e.getKeyCode();
			if(KEY_MAPPINGS.containsKey(n)) {
				if(!getKey(KeyEvent.VK_SHIFT)) {
					curTile1 = KEY_MAPPINGS.get(n);
				} else {
					curTile2 = KEY_MAPPINGS.get(n);
				}
				return;
			}
			switch(n) {
				case 192:
					TileType oldTile1 = curTile1;
					curTile1 = curTile2;
					curTile2 = oldTile1;
					return;
				case KeyEvent.VK_EQUALS:
					brushSize += 2;
					return;
				case KeyEvent.VK_MINUS:
					if(brushSize > 1) {
						 brushSize -= 2;
					}
					return;
				case KeyEvent.VK_T:
					simulateTemp = !simulateTemp;
					return;
				case KeyEvent.VK_H:
					heatSink = !heatSink;
					return;
				case KeyEvent.VK_OPEN_BRACKET:
					view = (view + NUM_VIEWS - 1) % NUM_VIEWS;
					return;
				case KeyEvent.VK_CLOSE_BRACKET:
					view = (view + 1) % NUM_VIEWS;
				
			}
		});
	}
	
	@Override
	public boolean update() {
		getInput();
		randomUpdateTiles();
		return false;
	}
	
	private void getInput() {
		Mouse m = getMouse();
		boolean mouse1 = m.getMouseButton(0);
		boolean mouse2 = m.getMouseButton(2);
		Point mLoc = m.getMouseLoc();
		if(mLoc == null) return;
		mLoc.sub(DRAW_OFFSET);
		mLoc.setX(mLoc.X()/DRAW_SIZE.X());
		mLoc.setY(mLoc.Y()/DRAW_SIZE.X());
		
		int dataX = (int)(mLoc.X() * SIZE.X()) - brushSize / 2;
		int dataY = (int)(mLoc.Y() * SIZE.Y()) - brushSize / 2;
		try {
			prevMouseTile = curData[dataX + brushSize / 2][dataY + brushSize / 2];
		} catch(IndexOutOfBoundsException e) {
			prevMouseTile = curData[0][0];
		}
		prevMouseLoc = new Point(dataX * DRAW_SIZE.X() / SIZE.X(), dataY * DRAW_SIZE.Y() / SIZE.Y());
		prevMouseLoc.add(DRAW_OFFSET);
		if(mouse1 || mouse2) {
			for(int x = Math.max(0, -dataX); x < Math.min(brushSize, SIZE.X() - dataX - 1); x++) {
				for(int y = Math.max(0, -dataY); y < Math.min(brushSize, SIZE.Y() - dataY - 1); y++) {
					if(dataY + y < 1 || dataX + x < 1) continue;
					curData[dataX + x][dataY + y] = mouse1?(getTile(curTile1, 0)):(getTile(curTile2, 0));
				}
			}
			
		}
	}
	
	private void randomUpdateTiles() {
		List<Point> toUpdate = new ArrayList<>();
		for(int i = 0; i <= (int)SIZE.X(); i++) {
			for(int j = 0; j <= (int)SIZE.Y(); j++) {
				toUpdate.add(new Point(i,j));
			}
		}
		Collections.shuffle(toUpdate);
		
		for(Point p : toUpdate) {
			int x = (int)p.X();
			int y = (int)p.Y();
			
			SimTile[][] neighborhood = new SimTile[3][3];
			for(int i = -1; i <= 1; i++) {
				if(x + i < 0) i++;
				for(int j = -1; j <= 1; j++) {
					if(y + j < 0) j++;
					neighborhood[1 + i][1 + j] = curData[x + i][y + j];
				}
			}
			SimTile[][] newNeighborhood = neighborhood[1][1].updateTiles(neighborhood, this);
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					if(neighborhood[i][j] == null) continue;
					curData[x + i - 1][y + j - 1] = newNeighborhood[i][j];
				}
			}
		}
	}
	
	public void drawImg(BufferedImage img, Function<SimTile, Color> colorFunc) {
		int imgX = img.getWidth();
		int imgY = img.getHeight();
		double imgRatio = (double)imgX / (double)imgY;
		double simRatio = SIZE.X() / SIZE.Y();
		
		int maxX;
		int maxY;
		if(imgRatio > simRatio) {
			maxX = (int)(SIZE.X() * ((double)imgY / SIZE.Y()));
			maxY = imgY;
		} else {
			maxX = imgX;
			maxY = (int)(SIZE.Y() * ((double)imgX / SIZE.X()));
		}
		
		double imgScale = SIZE.X() / (double)maxX;
		
		for(double x = 0; x < maxX; x++) {
			for(double y = 0; y < maxY; y++) {
				img.setRGB((int)x, (int)y, colorFunc.apply(curData[(int)(x * imgScale)][(int)(y * imgScale)]).getRGB());
			}
		}
	}

	@Override
	protected void draw(Graphics g, double xOff, double yOff, double angOff) {
		BufferedImage base =new BufferedImage((int)DRAW_SIZE.X(), (int)DRAW_SIZE.Y(), BufferedImage.TYPE_INT_RGB);
		switch(view) {
			case 0:
				drawImg(base, DRAW_COLOR);
				break;
			case 1:
				drawImg(base, DRAW_TEMP);
		}
		g.drawImage(base, (int)DRAW_OFFSET.X(), (int)DRAW_OFFSET.Y(), null);
		g.drawString("temp: " + prevMouseTile.temp, 10, 250);
		g.drawString("state: " + prevMouseTile.state, 10, 262);
		g.setColor(Color.black);
		if(prevMouseLoc != null) {
			g.drawRect((int)prevMouseLoc.X(), (int)prevMouseLoc.Y(), (int)(brushSize * DRAW_SIZE.X() / SIZE.X()), (int)(brushSize * DRAW_SIZE.Y() / SIZE.Y()));
		}
	}

	public boolean hasHeatSink() {
		return heatSink;
	}

}
