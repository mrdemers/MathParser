package com;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MathParser extends Canvas implements Runnable, KeyListener{
	private static final long serialVersionUID = 1L;
	public static int WIDTH = 800, HEIGHT = 600;
	private boolean running = false;
	private Thread thread;
	public BufferedImage imageOriginal, imageCopy;
	public static final boolean DEBUG = true;
	
	public SymbolParser creator;
	public HashMap<Point, Symbol> symbols;
	public EquationSolver solver;
	
	public MathParser() {
		this.setSize(WIDTH, HEIGHT);
		imageOriginal = readImage("Math.jpg");
		imageCopy = resize(imageOriginal, WIDTH, HEIGHT);
		creator = new SymbolParser(imageCopy);
		solver = new EquationSolver();
		addKeyListener(this);
		requestFocus();
		requestFocusInWindow();
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
	
	public static BufferedImage resize(BufferedImage img, int newWidth, int newHeight) {
		Image temp = img.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);
		BufferedImage ret = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics g = ret.getGraphics();
		g.drawImage(temp, 0, 0, null);
		g.dispose();
		return ret;
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
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gg = img.createGraphics();
		gg.drawImage(imageCopy, 0, 0, null);
		gg.setColor(Color.green);
		for (Symbol s : symbols.values()) {
			gg.drawImage(s.getImage(), s.getX(), s.getY(), null);
			if (DEBUG) {
				Rectangle r = s.getBoundingBox();
				gg.drawRect(r.x, r.y, r.width, r.height);
			}
		}
		if (DEBUG) {
			Collection<Symbol> syms = symbols.values();
			ArrayList<Symbol> list = new ArrayList<Symbol>(syms);
			for (int i = 0; i < list.size(); i++) {
				gg.drawImage(list.get(i).getImage(), i * 40, 0, null);
				for (int j = 0; j < creator.resizedImages.get(i).size(); j++) {
					gg.drawImage(creator.resizedImages.get(i).get(j), i*40, 40 + j*40, null);
				}
			}
		}
		gg.dispose();
		g.drawImage(img, 0, 0, WIDTH, HEIGHT, null);
		g.dispose();
		bs.show();
	}
	
	String equation;
	public void update() {	
		symbols = creator.createSymbols();
		render();
		this.requestFocusInWindow();
		equation = getEquation();
		System.out.println("Input");
		System.out.println("----------");
		System.out.println(equation);
	}
	
	public void solveEquation() {
		solver.solveEquation(equation);		
	}
	
	public String getEquation() {
		Set<Point> set = symbols.keySet();
		ArrayList<Point> list = new ArrayList<Point>(set);
		Collections.sort(list, new Comparator<Point>(){
			@Override
			public int compare(Point p1, Point p2) {
				return p1.x - p2.x;
			}
		});
		String result = "";
		for (Point p : list) {
			result += symbols.get(p).getCharacter();
		}
		return result;
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
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JPanel bottomCenter = new JPanel(new BorderLayout());
		JPanel bottomLeft = new JPanel(new BorderLayout());
		//Fuzziness slider
		JSlider fuzziness = new JSlider(0, 100, 25);
		fuzziness.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				parser.creator.setFuzziness(source.getValue()/100f);
				parser.update();
			}
		});
		bottomCenter.add(fuzziness, BorderLayout.NORTH);
		//Threshold Slider
		JSlider threshold = new JSlider(0, 100, 20);
		threshold.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				parser.creator.setThreshold(source.getValue()/100f);
				parser.update();
			}
		});
		bottomCenter.add(threshold, BorderLayout.SOUTH);
		//Labels
		bottomLeft.add(new JLabel("Fuzziness"), BorderLayout.NORTH);
		bottomLeft.add(new JLabel("Threshold"), BorderLayout.SOUTH);
		
		bottomPanel.add(bottomCenter, BorderLayout.CENTER);
		bottomPanel.add(bottomLeft, BorderLayout.WEST);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setFocusable(false);
		frame.add(panel, BorderLayout.CENTER);
		frame.setName("Frame");
		bottomPanel.setName("bottomPanel");
		panel.setName("mainPanel");
		parser.setName("The parser");
		frame.setVisible(true);
		parser.start();
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			this.solveEquation();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
}
