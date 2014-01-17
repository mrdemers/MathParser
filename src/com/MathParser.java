package com;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MathParser extends Canvas implements Runnable{
	private static final long serialVersionUID = 1L;
	private boolean running = false;
	private Thread thread;
	public BufferedImage imageOriginal, image;
	public Image temp;
	public boolean[][] checked;
	public ArrayList<String> characters;
	public ArrayList<ArrayList<Point>> points;
	public static int WIDTH = 800, HEIGHT = 600;
	
	public MathParser() {
		this.setSize(800, 600);
		try {
			imageOriginal = ImageIO.read(this.getClass().getResource("/IMG_0306.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		temp = imageOriginal.getScaledInstance(WIDTH, HEIGHT, BufferedImage.SCALE_SMOOTH);
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.drawImage(temp, 0, 0, null);
		g.dispose();
		
		checked = new boolean[WIDTH][HEIGHT];
		points = new ArrayList<ArrayList<Point>>();
		characters = new ArrayList<String>();
	}
	
	// The color of lead on paper hopefully
	public int myColor = 0xff1e140a;
	public void analyzeImage() {
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		for (int y = 1; y < height-1; y++) {
			for (int x = 1; x < width-1; x++) {
				if (checked[x][y]) continue;
				if (getColorContrast(x, y)){
					minX = WIDTH;
					maxX = 0;
					minY = HEIGHT;
					maxY = 0;
					points.add(new ArrayList<Point>());
					createCharacter(x, y);
				}
			}
		}
	}
	
	int minX, maxX, minY, maxY;
	public void createCharacter(int x, int y) {
		if (x >= WIDTH-1 || x < 1 || y >= HEIGHT-1 || y < 1) return;
		if (checked[x][y]) return;
		if (getColorContrast(x, y)) {
			if (x < minX) minX = x;
			if (x > maxX) maxX = x;
			if (y < minY) minY = y;
			if (y > maxY) maxY = y;
			checked[x][y] = true;
			points.get(points.size()-1).add(new Point(x, y));
			createCharacter(x-1, y);
			createCharacter(x+1, y);
			createCharacter(x, y-1);
			createCharacter(x, y+1);
		}
	}
	
	public boolean getColorSimilar(int src, int compare, float fuzziness) {
		return false;
	}
	
	/// Returns true if the point contrasts a lot with surrounding points
	public boolean getColorContrast(int x, int y) {
		//Color values of the source color
		int src = image.getRGB(x, y);
		int rs = red(src);
		int gs = green(src);
		int bs = blue(src);
		//Color values of the surrounding colors
		int up = image.getRGB(x, y-1);
		int down = image.getRGB(x, y+1);
		int left = image.getRGB(x-1, y);
		int right = image.getRGB(x+1, y);
		
		//Threshold
		int tr = 30;
		
		if (Math.abs(rs - red(up)) > tr ||
			Math.abs(rs - red(down)) > tr ||
			Math.abs(rs - red(left)) > tr ||
			Math.abs(rs - red(right)) > tr ||
			Math.abs(gs - green(up)) > tr ||
			Math.abs(gs - green(down)) > tr ||
			Math.abs(gs - green(left)) > tr ||
			Math.abs(gs - green(right)) > tr ||
			Math.abs(bs - blue(up)) > tr ||
			Math.abs(bs - blue(down)) > tr ||
			Math.abs(bs - blue(left)) > tr ||
			Math.abs(bs - blue(right)) > tr) {
			float[] hsbvals = new float[3];
			Color.RGBtoHSB(rs, gs, bs, hsbvals);
			if (hsbvals[2] <= 40)
				return true;
		}
		return false;
	}
	
	public int red(int color) {
		return (color>>16)&0xff;
	}
	
	public int green(int color) {
		return (color>>8)&0xff;
	}
	
	public int blue(int color) {
		return color&0xff;
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		g.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
		g.setColor(Color.red);
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (checked[x][y])
					g.drawRect(x, y, 1, 1);
			}
		}
		g.dispose();
		bs.show();
	}
	
	@Override
	public void run() {
		while (running) {
			analyzeImage();
			render();
			try {
				System.out.println("Waiting...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void start() {
		if (running) return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop() {
		if (!running) return;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		MathParser parser = new MathParser();
		panel.add(parser);
		frame.add(panel, BorderLayout.CENTER);
		frame.setVisible(true);
		parser.start();
	}
}
