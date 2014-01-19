package com;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SymbolParser {
	private BufferedImage image;
	private SymbolFinder finder;
	private float fuzziness = .3f;
	private float threshold = .2f;
	
	public SymbolParser() {
		this(null);
	}
	
	public SymbolParser(BufferedImage image) {
		this.image = image;
	}
	
	public void loadImage(String fileName) {
		try {
			this.image = ImageIO.read(this.getClass().getResource("/" + fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		finder = new SymbolFinder(image, fuzziness, threshold);
		SymbolMap symbols = finder.findSymbols();
		for (Symbol s : symbols.getSymbols()) {
			s.setImage(createImage(s));
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
	/*
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
	*/
}
