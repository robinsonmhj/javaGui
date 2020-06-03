package javaGui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Grid extends JFrame {
	private Container panel;
	private JButton[][] buttonArray;
	private int[][] cells;
	private int threshold = 3;
	private int gridSize;
	private int imageSize;
	private ImageIcon[] ImageIconArray;
	private String dir = "Icon/";

	private LinkedList<Position> q = new LinkedList<Position>();

	class Position {
		int x;
		int y;
		int v;

		Position(int x, int y, int v) {
			this.x = x;
			this.y = y;
			this.v = v;
		}
		public int hashCode() {
			int result = (int) (x ^ (x >>> 32));
		    result = 31 * result + y;
		    result = 31 * result + v;
		    return result;
		}
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(!(obj instanceof Position))
				return false;
			Position o= (Position)obj;
			
			return o.x==this.x && o.y==this.y && o.v==this.v;
			
		}
		public String toString() {
			return "(" + x + "," + y + ")=" + v;
		}
	}

	class myEventListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JButton b = (JButton) e.getSource();
			int o = panel.getComponentZOrder(b);
			int x = o / gridSize;
			int y = o % gridSize;
			int v = cells[x][y];
			Position p = new Position(x, y, v);
			System.out.println(p + " was clicked");
			q.add(p);
		}

	}

	private void init(int gridSize, int imageSize, String gameType) {
		panel = getContentPane();
		panel.setLayout(new GridLayout(gridSize, gridSize));
		ImageIconArray = new ImageIcon[imageSize];

		// load the pic into memory
		for (int i = 1; i <= imageSize; i++) {
			String imageName = i + ".PNG";
			ImageIcon image = new ImageIcon(new ImageIcon(this.dir + "/" + imageName).getImage().getScaledInstance(80,
					80, java.awt.Image.SCALE_SMOOTH));
			ImageIconArray[i - 1] = image;
		}

		// add components in the panel
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				int random = (int) (Math.random() * imageSize);
				System.out.println("The random value is " + random);
				cells[i][j] = random;
				buttonArray[i][j] = new JButton(ImageIconArray[random]);
				buttonArray[i][j].addActionListener(new myEventListener());
				panel.add(buttonArray[i][j]);
			}
		}

		// create window
		setTitle(gameType + " Crush");
		setSize(600, 600);
		setResizable(true);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// send request to check if there is anything can be removed for every row and
		// column
		
		for (int i = 0; i < gridSize; i++) {
			q.add(new Position(-1, i, -1));
			q.add(new Position(i, -1, -1));
		}
		
	}

	public Grid(int gridSize, int imageSize) {
		this(gridSize, imageSize, "Candy", 3);
	}

	public Grid(int gridSize, int imageSize, String gameType, int threshold) {

		this.buttonArray = new JButton[gridSize][gridSize];
		this.cells = new int[gridSize][gridSize];
		this.gridSize = gridSize;
		this.imageSize = imageSize;
		this.threshold = threshold;
		this.dir = this.dir + gameType;
		init(gridSize, imageSize, gameType);

	}

	private void action(Position p) {
		/*
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		String type;
		int x = p.x;
		int y = p.y;
		if (x == -1) {
			type = "Column";
			checkAndRemove(type,y);
		} else if (y == -1) {
			type = "Row";
			checkAndRemove(type,x);
		}
	}

	private void sleep(int milisecond) {
		try {
			Thread.sleep(milisecond);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void exchange(Position p1, Position p2, boolean flag) {
		/*
		 * 
		 * flag: if true, then exchange the view and model of p1 and p2 else keep the
		 * same
		 * 
		 */

		if (flag) {
			// change the icon in the view
			buttonArray[p1.x][p1.y].setIcon(ImageIconArray[p2.v]);
			buttonArray[p2.x][p2.y].setIcon(ImageIconArray[p1.v]);
			repaint();
			// changed in the model
			int t = cells[p1.x][p1.y];
			cells[p1.x][p1.y] = cells[p2.x][p2.y];
			cells[p2.x][p2.y] = t;
		} else {
			// change the icon in the view
			buttonArray[p1.x][p1.y].setIcon(ImageIconArray[p1.v]);
			buttonArray[p2.x][p2.y].setIcon(ImageIconArray[p2.v]);
			repaint();
			// changed in the model
			cells[p1.x][p1.y] = p1.v;
			cells[p2.x][p2.y] = p2.v;
		}
		repaint();
	}

	private boolean checkAndRemove(String type, int n) {
		/*
		 * check if there any consecutive pic in the same row or column type: it
		 * represents "Column" or "Row" n: the column number or the row number
		 * 
		 * if there is any consecutive, return true else false
		 */
		int[] res;
		res = findLCI(type, n);
		System.out.println(type + n + ":start->" + res[0] + ",end->" + res[1]);
		int consecutiveNo = res[1] - res[0] + 1;
		if (consecutiveNo >= threshold) {
			for (int i = res[0]; i <= res[1]; i++) {
				if (type.equals("Column")) {
					buttonArray[i][n].setIcon(null);
					cells[i][n] = -1;
				} else {
					buttonArray[n][i].setIcon(null);
					cells[n][i] = -1;
				}
			}
			panel.repaint();
			sleep(1000);
			fill();
			return true;
		}
		return false;
	}

	private void action(Position p1, Position p2) {

		int rowDiff = Math.abs(p1.x - p2.x);
		int colDiff = Math.abs(p1.y - p2.y);
		int diff = rowDiff + colDiff;
		int cell1 = p1.v;
		int cell2 = p2.v;

		if (diff != 1) {
			System.out.print(p1 + " and " + p2 + " is not neighbors, skip");
		} else {
			if (cell1 == cell2) {
				System.out.print(p1 + " and " + p2 + " has the same value, skip");
			} else {
				exchange(p1, p2, true);
				boolean sprakFlag = false;

				if (rowDiff == 0) {
					String type = "Column";
					// check the column
					sprakFlag = checkAndRemove(type, p1.y)||sprakFlag;
					sprakFlag = checkAndRemove(type, p2.y)||sprakFlag;
					// check row
					sprakFlag = checkAndRemove("Row", p1.x)||sprakFlag;

				} else if (colDiff == 0) {
					String type = "Row";
					sprakFlag = checkAndRemove(type, p1.x)||sprakFlag;
					sprakFlag = checkAndRemove(type, p2.x)||sprakFlag;
					// check the column
					sprakFlag = checkAndRemove("Column", p1.y)||sprakFlag;
				}

				if (!sprakFlag) {
					sleep(500);
					System.out.println("Nothing can be removed, switch back,p1 is " + p1 + ",p2 is " + p2);
					exchange(p1, p2, false);
				}

			}
		}
	}

	private void fill() {
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				if (cells[i][j] == -1) {
					int random = (int) (Math.random() * imageSize);
					cells[i][j] = random;
					buttonArray[i][j].setIcon(ImageIconArray[random]);
					panel.repaint();
					// check the column if there is anything to be removed
					Position p = new Position(-1, j, -1);
					if(!q.contains(p))
						q.add(p);
					// send a request to check if there is anything to be removed in row
					p=new Position(i, -1, -1);
					if(!q.contains(p))
						q.add(p);
				}
			}
		}
		sleep(500);

	}

	// return the index of consective values
	private int[] findLCI(String type, int n) {
		/*
		 * 
		 * Find the the longest common integer in an array
		 * 
		 * type:"Column" or "Row"
		 * n: the row number or the column number
		 * return an array, first element is start index, 2nd element is the end point, 3rd is the value
		 * 
		 */
		int[] res = new int[3];
		int[] tempArray = new int[gridSize];
		for (int i = 0; i < gridSize; i++) {
			if (type.equals("Row"))
				tempArray[i] = cells[n][i];
			else
				tempArray[i] = cells[i][n];
		}

		int start = 0, end = 0, v = tempArray[0];
		int max = 0;
		res[0] = start;
		res[1] = end;
		res[2] = v;
		for (int i = 1; i < gridSize; i++) {
			if (tempArray[i] == v) {
				end = i;
				if (i == gridSize - 1) {
					int diff = end - start;
					if (diff > max) {
						res[0] = start;
						res[1] = end;
						res[2] = v;
					}
				}
			} else {
				int diff = end - start;
				if (diff > max) {
					res[0] = start;
					res[1] = end;
					res[2] = v;
					max = diff;
				}
				start = i;
				end = i;
				v = tempArray[i];
			}
		}
		return res;

	}

	public void run() {
		/*
		 * 
		 * The start point to run the game
		 * 
		 * 
		 * 
		 */

		int clickNum = 0;
		Position p1 = null, p2 = null;
		boolean need2Check = false;
		while (true) {
			if (!q.isEmpty()) {
				int reminder = clickNum % 2;
				Position p = q.poll();
				if (p.x == -1 || p.y == -1) {
					action(p);
					continue;
				}
				if (reminder == 0) {
					p1 = p;
					need2Check = true;
				} else
					p2 = p;
				clickNum++;
			} else {
				sleep(500);
			}
			if (clickNum % 2 == 0 && need2Check) {
				System.out.println("1st click is " + p1 + ", 2nd click is " + p2);
				action(p1, p2);
				need2Check = false;
			}
		}

	}

	public static void main(String[] args) throws InterruptedException {
		int gridSize = 4;
		int imageCount = 5;
		String gameType = "Anminal";
		int threshold = 3;
		Grid g = new Grid(gridSize, imageCount, gameType, threshold);

		g.run();

	}
}
