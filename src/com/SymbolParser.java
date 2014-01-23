package com;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class SymbolParser {
	private BufferedImage image;
	private SymbolFinder finder;
	private float fuzziness = .3f;
	private float threshold = .2f;
	public BufferedImage one, two, three, four, five, six, seven, eight, nine, zero, plus, minus, times, divide;
	public ArrayList<ArrayList<BufferedImage>> resizedImages = new ArrayList<ArrayList<BufferedImage>>();
	
	public SymbolParser() {
		this(null);
	}
	
	public SymbolParser(BufferedImage image) {
		this.image = image;
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
		plus = readImage("plus.png");
		minus = readImage("minus.png");
		times = readImage("times.png");
		divide = readImage("divide.png");
	}
	
	public void loadImage(String fileName) {
		try {
			this.image = ImageIO.read(this.getClass().getResource("/" + fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
	public void setFuzziness(float f) {
		this.fuzziness = f;
	}
	
	public void setThreshold(float t) {
		this.threshold = t;
	}
	
	public SymbolMap createSymbols() {
		resizedImages.clear();
		finder = new SymbolFinder(image, fuzziness, threshold);
		SymbolMap symbols = finder.findSymbols();
		for (Symbol s : symbols.getSymbols()) {
			s.setImage(createImage(s));
			s.setCharacter(checkImage(s));
		}
		return symbols;
	}
	
	public BufferedImage createImage(Symbol s) {
		Rectangle r = s.getBoundingBox();
		BufferedImage img = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB);
		//System.out.println("Size: " + r.width + ", " + r.height);
		for (Point p : s.getPoints()) {
			int x = p.x - r.x;
			int y = p.y - r.y;
			//System.out.println("X: " + x + ", Y: " + y);
			img.setRGB(x, y, 0xff000000);
		}
		return img;
	}
	
	static int num = 0;
	public String checkImage(Symbol symbol) {
		BufferedImage imageToCheck;
		BufferedImage image = symbol.getImage();
		ArrayList<BufferedImage> resized = new ArrayList<BufferedImage>();
		float[] similarity = new float[14];
		
		//Special case for 1
		imageToCheck = image;
		if (imageToCheck.getWidth() < imageToCheck.getHeight()/2f && imageToCheck.getWidth() <= 10) {
			imageToCheck = MathParser.resize(image, one.getWidth(), one.getHeight());
			resized.add(imageToCheck);
			similarity[0] = compareImage(imageToCheck, one);
		}
		
		//Check 2
		imageToCheck = MathParser.resize(image, two.getWidth(), two.getHeight());
		resized.add(imageToCheck);
		similarity[1] = compareImage(imageToCheck, two);
		
		//Check 3
		imageToCheck = MathParser.resize(image, three.getWidth(), three.getHeight());
		resized.add(imageToCheck);
		similarity[2] = compareImage(imageToCheck, three);
		
		//Check 4
		imageToCheck = MathParser.resize(image, four.getWidth(), four.getHeight());
		resized.add(imageToCheck);
		similarity[3] = compareImage(imageToCheck, four);
		
		//Check 5
		imageToCheck = MathParser.resize(image, five.getWidth(), five.getHeight());
		resized.add(imageToCheck);
		similarity[4] = compareImage(imageToCheck, five);
		
		//Check 6
		imageToCheck = MathParser.resize(image, six.getWidth(), six.getHeight());
		resized.add(imageToCheck);
		similarity[5] = compareImage(imageToCheck, six);
		
		//Check 7
		imageToCheck = MathParser.resize(image, seven.getWidth(), seven.getHeight());
		resized.add(imageToCheck);
		similarity[6] = compareImage(imageToCheck, seven);
		
		//Check 8
		imageToCheck = MathParser.resize(image, eight.getWidth(), eight.getHeight());
		resized.add(imageToCheck);
		similarity[7] = compareImage(imageToCheck, eight);
		
		//Check 9
		imageToCheck = MathParser.resize(image, nine.getWidth(), nine.getHeight());
		resized.add(imageToCheck);
		similarity[8] = compareImage(imageToCheck, nine);
		
		//Check 0
		imageToCheck = MathParser.resize(image, zero.getWidth(), zero.getHeight());
		resized.add(imageToCheck);
		similarity[9] = compareImage(imageToCheck, zero);
		
		//Check +
		imageToCheck = MathParser.resize(image, plus.getWidth(), plus.getHeight());
		resized.add(imageToCheck);
		similarity[10] = compareImage(imageToCheck, plus);
		
		//Check -
		if (image.getWidth()/2 > image.getHeight()) {
			imageToCheck = MathParser.resize(image, minus.getWidth(), minus.getHeight());
			resized.add(imageToCheck);
			similarity[11] = compareImage(imageToCheck, minus);
		}
		
		//Check *
		imageToCheck = MathParser.resize(image, times.getWidth(), times.getHeight());
		resized.add(imageToCheck);
		similarity[12] = compareImage(imageToCheck, times);
		
		//Check /
		imageToCheck = MathParser.resize(image, divide.getWidth(), divide.getHeight());
		resized.add(imageToCheck);
		similarity[13] = compareImage(imageToCheck, divide);
		
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
		//	System.out.println("Similarity[" + i + "]: " + similarity[i] + ", " + highestNum + ", " + highest);
		}
		resizedImages.add(resized);
		String returnVal = "";
		if (highest < 10) {
			returnVal += highest;
		} else if (highest == 11) {
			returnVal = "+";
		} else if (highest == 12) {
			returnVal = "-";
		} else if (highest == 13) {
			returnVal = "*";
		} else if (highest == 14) {
			returnVal = "/";
		}
		return "" + returnVal;
	}
	
	public float compareImage(BufferedImage src, BufferedImage comp) {
		float percent = 1.0f;
		int amtOfPixels = 0;
		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y++) {
				amtOfPixels += src.getRGB(x, y) == 0xff000000?0:1;
			}
		}
		float delta = 1.0f/(src.getWidth() * src.getHeight());
		for (int y = 0; y < src.getHeight(); y++) {
			for (int x = 0; x < src.getWidth(); x++) {
				//Pixel where there is none in compare image
				if (src.getRGB(x, y) != 0 && comp.getRGB(x, y) != 0xffff0000) {
					percent -= delta * 2;
				}
				//No pixel where there is one in compare image
				if (src.getRGB(x, y) == 0 && comp.getRGB(x, y) == 0xffff0000) {
					percent -= delta;
				}
			}
		}
		return percent;
	}
	
}
