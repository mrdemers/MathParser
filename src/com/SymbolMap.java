package com;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

public class SymbolMap {
	private HashMap<Point, Symbol> symbols;
	
	public SymbolMap() {
		symbols = new HashMap<Point, Symbol>();
	}
	
	public void put(Point p, Symbol sym) {
		symbols.put(p, sym);
	}
	
	public Collection<Symbol> getSymbols() {
		return symbols.values();
	}
	
	public int size() {
		return symbols.size();
	}
	
	//Will return the string representation of the equation to use
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
}
