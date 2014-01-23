package com;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class SymbolFinder {
	private boolean[][] checked;
	private BufferedImage image;
	private int WIDTH, HEIGHT;
	private int minX, maxX, minY, maxY;
	public float fuzziness;
	public float threshold;
	
	public SymbolFinder(BufferedImage image) {
		this(image, .25f, .2f);
	}
	
	public SymbolFinder(BufferedImage image, float fuzziness, float threshold) {
		this.image = image;
		this.fuzziness = fuzziness;
		this.threshold = threshold;
		WIDTH = image.getWidth();
		HEIGHT = image.getHeight();
		checked = new boolean[WIDTH][HEIGHT];
	}
	
	public SymbolMap findSymbols() {
		if (image == null) {
			throw new RuntimeException("No image to look at");
		}
		SymbolMap symbols = new SymbolMap();
		
		//Finds the beginning point for each symbol, and
		//finds all points in that symbol
		for (int y = 2; y < HEIGHT-2; y++) {
			for (int x = 2; x < WIDTH-2; x++) {
				if (checked[x][y]) continue;
				if (doesColorContrast(image, x, y)){
					int color = image.getRGB(x, y);
					//System.out.println("Point: " + x + ", " + y);
					ArrayList<Point> ps = new ArrayList<Point>();
					Symbol nextSymbol;
					checked[x][y] = true;
					try {
						boolean add = true;
						findSymbol(ps, x, y, color);
						nextSymbol = new Symbol(ps);
						//Merge symbols if intersecting
						Rectangle r = nextSymbol.getBoundingBox();
						for (Symbol s : symbols.getSymbols()) {
							Rectangle bb = s.getBoundingBox();
							if (r.intersects(bb) || bb.contains(r) || r.contains(bb)) {
								System.out.println("Merging");
								s.merge(nextSymbol);
								r = s.getBoundingBox();
								add = false;
							}
						}
						if (r.width < 3 || r.height < 10 || ps.size() > 600) {
							add = false;
						}
						if (add) {
							symbols.put(new Point(nextSymbol.getX(), nextSymbol.getY()), nextSymbol);
						}
					} catch (StackOverflowError e) {
						//Sometimes find symbol looks too far, and causes a stack overflow
						//Just ignore it for now
					}
				}
			}
		}
		
		return symbols;
	}
	
	private void findSymbol(ArrayList<Point> points, int x, int y, int color) {
		if (x >= WIDTH-2 || x < 2 || y >= HEIGHT-2 || y < 2) return;
		int imageColor = image.getRGB(x, y);
		if (isColorSimilar(imageColor, color, fuzziness) || checked[x][y]) {
			if (x < minX) minX = x;
			if (x > maxX) maxX = x;
			if (y < minY) minY = y;
			if (y > maxY) maxY = y;
			checked[x][y] = true;
			points.add(new Point(x, y));
			if (!checked[x-1][y]) {
				findSymbol(points, x-1, y, color);
			}
			if (!checked[x+1][y]) {
				findSymbol(points, x+1, y, color);
			}
			if (!checked[x][y-1]) {
				findSymbol(points, x, y-1, color);
			}
			if (!checked[x][y+1]) {
				findSymbol(points, x, y+1, color);
			}
		}
	}
	
	//Returns true if the compare color is similar to the source
	private boolean isColorSimilar(int src, int compare, float fuzziness) {
		int rd = red(src) - red(compare);
		int gd = green(src) - green(compare);
		int bd = blue(src) - blue(compare);
		double maxDist = 255*fuzziness;
		double dist = Math.sqrt(rd*rd + gd*gd + bd*bd);
		if (dist < maxDist) {
			return true;
		}
		return false;
	}

	//Returns true if the brightness value of pixels
	//two units away is some value brighter than this one
	private boolean doesColorContrast(BufferedImage image, int x, int y) {
		//Color values of the source color
		int src = image.getRGB(x, y);
		int rs = red(src);
		int gs = green(src);
		int bs = blue(src);
		//Color values of the surrounding colors
		int up = image.getRGB(x, y-2);
		int down = image.getRGB(x, y+2);
		int left = image.getRGB(x-2, y);
		int right = image.getRGB(x+2, y);

		float[] hsbvals = new float[3];
		Color.RGBtoHSB(rs, gs, bs, hsbvals);

		float[] hsbUp = new float[3];
		float[] hsbDown = new float[3];
		float[] hsbLeft = new float[3];
		float[] hsbRight = new float[3];
		Color.RGBtoHSB(red(up), green(up), blue(up), hsbUp);
		Color.RGBtoHSB(red(down), green(down), blue(down), hsbDown);
		Color.RGBtoHSB(red(left), green(left), blue(left), hsbLeft);
		Color.RGBtoHSB(red(right), green(right), blue(right), hsbRight);

		float vDeltaUp = Math.abs(hsbvals[2] - hsbUp[2]);
		float vDeltaDown = Math.abs(hsbvals[2] - hsbDown[2]);
		float vDeltaLeft = Math.abs(hsbvals[2] - hsbLeft[2]);
		float vDeltaRight = Math.abs(hsbvals[2] - hsbRight[2]);
		//System.out.println("VDelta: " + vDeltaUp + ", " + vDeltaDown + ", " + vDeltaLeft + ", " + vDeltaRight);
		if (vDeltaUp > threshold && hsbUp[2] > hsbvals[2]) {
			return true;
		}
		if (vDeltaDown > threshold && hsbDown[2] > hsbvals[2]) {
			return true;
		}
		if (vDeltaLeft > threshold && hsbLeft[2] > hsbvals[2]) {
			return true;
		}
		if (vDeltaRight > threshold && hsbRight[2] > hsbvals[2]) {
			return true;
		}
		return false;
	}
	
	//Returns the red value of color
	private int red(int color) {
		return (color>>16)&0xff;
	}
	//Returns the green value of color
	private int green(int color) {
		return (color>>8)&0xff;
	}
	//Returns the blue value of color
	private int blue(int color) {
		return color&0xff;
	}
}
