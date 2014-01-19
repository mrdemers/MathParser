package com;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Symbol {
	private ArrayList<Point> points;
	private Rectangle boundingBox;
	private BufferedImage image;
	private String character;
	
	public Symbol() {
		points = new ArrayList<Point>();
		boundingBox = null;
		character = null;
	}
	
	public Symbol(ArrayList<Point> points) {
		this.points = points;
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (Point p : points) {
			if (p.x < minX) {
				minX = p.x;
			}
			if (p.x > maxX) {
				maxX = p.x;
			}
			if (p.y < minY) {
				minY = p.y;
			}
			if (p.y > maxY) {
				maxY = p.y;
			}
		}
		boundingBox = new Rectangle(minX, minY, maxX-minX+1, maxY-minY+1); 
	}
	
	public String getCharacter() {
		return character;
	}
	
	public int getX() {
		return boundingBox.x;
	}
	
	public int getY() {
		return boundingBox.y;
	}
	
	public Rectangle getBoundingBox() {
		return boundingBox;
	}
	
	public ArrayList<Point> getPoints() {
		return points;
	}
	
	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage newImage) {
		this.image = newImage;
	}
}
