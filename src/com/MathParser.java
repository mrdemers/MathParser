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
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MathParser extends Canvas implements Runnable{
	private static final long serialVersionUID = 1L;
	private boolean running = false;
	private Thread thread;
	public BufferedImage imageOriginal, image;
	public BufferedImage one, two, three, four, five, six, seven, eight, nine, zero;
	public Image temp;
	public boolean[][] checked;
	public ArrayList<String> characters;
	public ArrayList<ArrayList<Point>> points;
	public float fuzziness = .25f;
	public static int WIDTH = 800, HEIGHT = 600;
	public ArrayList<BufferedImage> createdImages = new ArrayList<BufferedImage>();
	public ArrayList<ArrayList<BufferedImage>> resizedImages = new ArrayList<ArrayList<BufferedImage>>();
	
	public MathParser() {
		this.setSize(800, 600);
		imageOriginal = readImage("IMG_0306.jpg");
		one = readImage("one.png");
		two = readImage("two.png");
		three = readImage("three.png");
		four = readImage("four.png");
		five = readImage("five.png");
		six = readImage("six.png");
		seven = readImage("seven.png");
		eight = readImage("eight.png");
		nine = readImage("nine.png");
		zero = readImage("zero.png");
		
		image = resize(imageOriginal, WIDTH, HEIGHT);
		
		checked = new boolean[WIDTH][HEIGHT];
		points = new ArrayList<ArrayList<Point>>();
		characters = new ArrayList<String>();
	}
	
	public BufferedImage readImage(String fileName) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(this.getClass().getResource("/" + fileName)); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}
	public BufferedImage resize(BufferedImage img, int newWidth, int newHeight) {
		Image temp = img.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);
		BufferedImage ret = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics g = ret.getGraphics();
		g.drawImage(temp, 0, 0, null);
		g.dispose();
		return ret;
	}
	
	public void analyzeImage() {
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		String answer = "";
		for (int y = 2; y < height-2; y++) {
			for (int x = 2; x < width-2; x++) {
				if (checked[x][y]) continue;
				if (getColorContrast(x, y)){
					int color = image.getRGB(x, y);
					minX = WIDTH;
					maxX = 0;
					minY = HEIGHT;
					maxY = 0;
					System.out.println("Point: " + x + ", " + y);
					ArrayList<Point> ps = new ArrayList<Point>();
					points.add(ps);
					checked[x][y] = true;
					try {
						createCharacter(x, y, color);
						answer += checkImage();
					} catch (StackOverflowError e) {
						
					}
				}
			}
		}
		for (int i = 0; i < points.size(); ) {
			ArrayList<Point> ps = points.get(i);
			if (ps.size() > 700) {
				for (Point p : ps) {
					checked[p.x][p.y] = false;
				}
				points.remove(ps);
			} else {
				i++;
			}
		}
		System.out.println(answer);
	}
	
	static int num = 0;
	public String checkImage() {
		if (maxX <= minX || maxY <= minY) return "";
		int sizeX = maxX - minX;
		int sizeY = maxY - minY;
		BufferedImage image = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);
		BufferedImage imageToCheck = image;
		createdImages.add(image);
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (checked[x+minX][y+minY]) {
					image.setRGB(x, y, 0xff000000);
				} else {
					image.setRGB(x, y, 0x00ffffff);
				}
			}
		}
		ArrayList<BufferedImage> resized = new ArrayList<BufferedImage>();
		float[] similarity = new float[10];
		imageToCheck = resize(image, one.getWidth(), one.getHeight());
		resized.add(imageToCheck);
		similarity[0] = compareImage(imageToCheck, one);
		imageToCheck = resize(image, two.getWidth(), two.getHeight());
		resized.add(imageToCheck);
		similarity[1] = compareImage(imageToCheck, two);
		imageToCheck = resize(image, three.getWidth(), three.getHeight());
		resized.add(imageToCheck);
		similarity[2] = compareImage(imageToCheck, three);
		imageToCheck = resize(image, four.getWidth(), four.getHeight());
		resized.add(imageToCheck);
		similarity[3] = compareImage(imageToCheck, four);
		imageToCheck = resize(image, five.getWidth(), five.getHeight());
		resized.add(imageToCheck);
		similarity[4] = compareImage(imageToCheck, five);
		imageToCheck = resize(image, six.getWidth(), six.getHeight());
		resized.add(imageToCheck);
		similarity[5] = compareImage(imageToCheck, six);
		imageToCheck = resize(image, seven.getWidth(), seven.getHeight());
		resized.add(imageToCheck);
		similarity[6] = compareImage(imageToCheck, seven);
		imageToCheck = resize(image, eight.getWidth(), eight.getHeight());
		resized.add(imageToCheck);
		similarity[7] = compareImage(imageToCheck, eight);
		imageToCheck = resize(image, nine.getWidth(), nine.getHeight());
		resized.add(imageToCheck);
		similarity[8] = compareImage(imageToCheck, nine);
		imageToCheck = resize(image, zero.getWidth(), zero.getHeight());
		resized.add(imageToCheck);
		similarity[9] = compareImage(imageToCheck, zero);
		float highestNum = 0;
		int highest = 0;;
		for (int i = 0; i < similarity.length; i++) {
			if (similarity[i] > highestNum && similarity[i] > .3) {
				highestNum = similarity[i];
				highest = i+1;
				if (i == 9) {
					highest = 0;
				}
			}
			System.out.println("Similarity: " + similarity[i] + ", " + highestNum + ", " + highest);
		}
		resizedImages.add(resized);
		return "" + highest;
	}
	
	public float compareImage(BufferedImage src, BufferedImage comp) {
		float percent = 1.0f;
		float delta = 1.0f/(src.getWidth() * src.getHeight());
		for (int y = 0; y < src.getHeight(); y++) {
			for (int x = 0; x < src.getWidth(); x++) {
				if (src.getRGB(x, y) == 0xff000000 && comp.getRGB(x, y) != 0xffff0000
						|| src.getRGB(x, y) != 0xff000000 && comp.getRGB(x, y) == 0xffff0000) {
					percent -= delta;
				}
			}
		}
		return percent;
	}
	
	int minX, maxX, minY, maxY;
	public void createCharacter(int x, int y, int color) {
		if (x >= WIDTH-2 || x < 2 || y >= HEIGHT-2 || y < 2) return;
		int imageColor = image.getRGB(x, y);
		if (getColorSimilar(imageColor, color, fuzziness) || checked[x][y]) {
			if (x < minX) minX = x;
			if (x > maxX) maxX = x;
			if (y < minY) minY = y;
			if (y > maxY) maxY = y;
			checked[x][y] = true;
			points.get(points.size()-1).add(new Point(x, y));
			if (!checked[x-1][y]) {
				createCharacter(x-1, y, color);
			}
			if (!checked[x+1][y]) {
				createCharacter(x+1, y, color);
			}
			if (!checked[x][y-1]) {
				createCharacter(x, y-1, color);
			}
			if (!checked[x][y+1]) {
				createCharacter(x, y+1, color);
			}
		}
	}
	
	//Returns true if the source color is similar to the compare color by fuzziness percent
	public boolean getColorSimilar(int src, int compare, float fuzziness) {
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
	
	/// Returns true if the point contrasts a lot with surrounding points
	public boolean getColorContrast(int x, int y) {
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
		
		//Threshold
		float tr = .2f;
		
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
		if (vDeltaUp > tr && hsbUp[2] > hsbvals[2]) {
			return true;
		}
		if (vDeltaDown > tr && hsbDown[2] > hsbvals[2]) {
			return true;
		}
		if (vDeltaLeft > tr && hsbLeft[2] > hsbvals[2]) {
			return true;
		}
		if (vDeltaRight > tr && hsbRight[2] > hsbvals[2]) {
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
		for (ArrayList<Point> ps : points) {
			Point p = ps.get(0);
			g.drawString("" + p.x + ", " + p.y, p.x-50, p.y);
		}
		g.drawImage(one, 0, 0, null);
		g.drawImage(two, 40, 0, null);
		for (int i = 0; i < createdImages.size(); i++) {
			g.drawImage(createdImages.get(i), i * 40, 0, null);
			for (int j = 0; j < resizedImages.get(0).size(); j++) {
				g.drawImage(resizedImages.get(i).get(j), i*40, 40 + j*40,null);
			}
		}
		
		g.dispose();
		bs.show();
	}

	public void update() {	
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				checked[i][j] = false;
			}
		}
		points.clear();
		createdImages.clear();
		analyzeImage();
		render();
	}
	
	@Override
	public void run() {
		update();
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
		JPanel panel = new JPanel(new BorderLayout());
		final MathParser parser = new MathParser();
		panel.add(parser, BorderLayout.CENTER);
		JSlider slider = new JSlider(0, 100, 25);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				parser.fuzziness = source.getValue()/100f;
				parser.update();
			}
		});
		panel.add(slider, BorderLayout.SOUTH);
		frame.add(panel, BorderLayout.CENTER);
		frame.setVisible(true);
		parser.start();
	}
}
