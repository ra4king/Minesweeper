package com.ra4king.minesweeper;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Minesweeper extends JApplet {
	private static final long serialVersionUID = 3559893803121454469L;
	
	private JButton button;
	private JButton buttons[][];
	private JButton bombs[];
	private ImageIcon images[];
	private final int rowNum = 20, colNum = 20, bombNum = 50;
	private volatile int pressedCount = 0;
	private volatile boolean defaultLAF = true;
	
	public void init() {
		setSize(805,608);
		((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(2,2,3,3));
		
		images = new ImageIcon[13];
		for(int a = 0; a < images.length; a++) {
			try{
				images[a] = new ImageIcon(getClass().getResource("/images/" + a + ".jpg"));
				while(images[a].getImage().getWidth(null) < 0 || images[a].getImage().getHeight(null) < 0) {
					Thread.sleep(100);
				}
			}
			catch(Exception exc) {}
		}
		
		images[10].setDescription("flag");
		
		buttons = new JButton[rowNum][colNum];
		ButtonActions ba = new ButtonActions();
		
		JPanel panel = new JPanel(new GridLayout(rowNum,colNum),true);
		
		for(int a = 0; a < rowNum; a++) {
			for(int b = 0; b < colNum; b++) {
				button = null;
				button = new JButton(images[0]);
				button.setHorizontalAlignment(SwingConstants.LEFT);
				button.setVerticalAlignment(SwingConstants.TOP);
				button.addActionListener(ba);
				button.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent me) {
						JButton button = (JButton)me.getSource();
						
						if(button.isEnabled() && me.getButton() == MouseEvent.BUTTON3) {
							if(((ImageIcon)button.getIcon()).getDescription().equals("flag"))
								button.setIcon(images[0]);
							else
								button.setIcon(images[10]);
						}
					}
				});
				panel.add(button);
				buttons[a][b] = button;
			}
		}
		
		newGame();
		
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		menu.add("New Game").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int i = JOptionPane.showConfirmDialog(Minesweeper.this,"Are you sure you want to start a new game?","Are you sure?",JOptionPane.YES_NO_OPTION);
				if(i == JOptionPane.YES_OPTION) newGame();
			}
		});
		menu.add("Change theme").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try{
					if(defaultLAF)
						UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
					else
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					
					SwingUtilities.updateComponentTreeUI(Minesweeper.this);
					defaultLAF = !defaultLAF;
				}
				catch(Exception exc) {System.out.println("ERROR!");}
			}
		});
		menu.add("Options (coming soon!)");
		JMenu help = new JMenu("Help");
		help.add("About").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JOptionPane.showMessageDialog(Minesweeper.this,"<html>Minesweeper ver. 1.1<br>by Roi Atalla","About",JOptionPane.WARNING_MESSAGE);
			}
		});
		menubar.add(menu);
		menubar.add(help);
		setJMenuBar(menubar);
		
		add(panel);
	}
	
	public void newGame() {
		for(int a = 0; a < rowNum; a++)
			for(int b = 0; b < colNum; b++) {
				buttons[a][b].setEnabled(true);
				buttons[a][b].setIcon(images[0]);
				buttons[a][b].setDisabledIcon(null);
			}
		
		bombs = new JButton[bombNum];
		int a = 0;
		while(a < bombNum) {
			int numBombs = (int)(Math.random()*(rowNum*colNum));
			boolean isRedundant = false;
			for(int b = 0; b < bombNum; b++) {
				if(bombs[b] == buttons[(numBombs-(numBombs%rowNum))/rowNum][numBombs%colNum]) {
					isRedundant = true;
					break;
				}
			}
			
			if(isRedundant) continue;
			
			bombs[a] = buttons[(numBombs-(numBombs%rowNum))/rowNum][numBombs%colNum];
			bombs[a].setDisabledIcon(images[9]);
			a++;
		}
		
		pressedCount = 0;
	}
	
	private boolean arrayContains(Object[] ob1, Object ob2) {
		for(int a = 0; a < ob1.length; a++) {
			if(ob1[a] == ob2) return true;
		}
		return false;
	}
	
	private class ButtonActions implements ActionListener {
		public Point getXY(JButton button) {
			int x = 0, y = 0;
			
			for(int a = 0; a < rowNum; a++) {
				for(int b = 0; b < colNum; b++) {
					if(button == buttons[a][b]) {
						x = a;
						y = b;
					}
				}
			}
			
			return new Point(x,y);
		}
		
		private int getBombCount(JButton button) {
			int count = 0;
			
			Point p = getXY(button);
			int x = p.x;
			int y = p.y;
			
			for(int a = -1; a < 2; a++) {
				for(int b = -1; b < 2; b++) {
					if(a == 0 && b == 0) continue;
					
					try{
						if(arrayContains(bombs,buttons[x+a][y+b])) count++;
					}
					catch(Exception exc) {}
				}
			}
			
			return count;
		}
		
		public void actionPerformed(ActionEvent ae) {
			JButton button = (JButton)ae.getSource();
			
			if(((ImageIcon)button.getIcon()).getDescription().equals("flag")) return;
			
			boolean isBomb = arrayContains(bombs,button);
			
			int bx = getXY(button).x;
			int by = getXY(button).y;
			
			if(getBombCount(button) > 0 && !isBomb) {
				button.setDisabledIcon(images[getBombCount(button)]);
				button.setEnabled(false);
				pressedCount++;
			}
			else if(!isBomb) {
				button.setEnabled(false);
				pressedCount++;
				
				for(int a = -1; a < 2; a++) {
					for(int b = -1; b < 2; b++) {
						try{
							if(buttons[bx+a][by+b].isEnabled())
								pressedCount++;
							buttons[bx+a][by+b].setEnabled(false);
							buttons[bx+a][by+b].setDisabledIcon(images[getBombCount(buttons[bx+a][by+b])]);
						}
						catch(Exception exc) {}
					}
				}
				
				for(int level = 2; level < Math.max(rowNum,colNum)+1; level++) {
					for(int x = -level; x <= level; x++) {
						for(int y = -level; y <= level; y++) {
							try{
								if(!buttons[bx+x][by+y].isEnabled() && getBombCount(buttons[bx+x][by+y]) == 0) {
									for(int a = -1; a < 2; a++) {
										for(int b = -1; b < 2; b++) {
											try{
												if(buttons[bx+x+a][by+y+b].isEnabled())
													pressedCount++;
												buttons[bx+x+a][by+y+b].setEnabled(false);
												buttons[bx+x+a][by+y+b].setDisabledIcon(images[getBombCount(buttons[bx+x+a][by+y+b])]);
											}
											catch(Exception exc) {}
										}
									}
								}
							}
							catch(Exception exc) {}
						}
					}
					Image i = createImage(getWidth(),getHeight());
					paint(i.getGraphics());
					getGraphics().drawImage(i,0,0,null);
				}
			}
			
			boolean hasWon = false;
			
			if(isBomb || (hasWon = pressedCount >= (rowNum*colNum)-bombNum)) {
				for(int a = 0; a < rowNum; a++) {
					for(JButton b : buttons[a]) {
						if(((ImageIcon)b.getIcon()).getDescription().equals("flag")) {
							if(arrayContains(bombs,b)) {
								b.setDisabledIcon(images[11]);
								b.setEnabled(false);
							}
							else {
								b.setDisabledIcon(images[12]);
								b.setEnabled(false);
							}
						}
					}
				}
				
				button.setEnabled(false);
				
				for(JButton bomb : bombs) {
					bomb.setEnabled(false);
					
					Image i = createImage(getWidth(),getHeight());
					paint(i.getGraphics());
					getGraphics().drawImage(i,0,0,null);
				}
				
				try{
						Thread.sleep(50);
					}
					catch(Exception exc) {}
				
				final JDialog dialog = new JDialog();
				dialog.setTitle("Game Over!");
				dialog.setModal(true);
				
				JButton button1 = new JButton("New Game");
				button1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						newGame();
						dialog.dispose();
					}
				});
				
				JLabel label;
				if(hasWon) {
					label = new JLabel("You win!");
				}
				else label = new JLabel("You lose!");
				
				label.setFont(new Font(Font.SANS_SERIF,Font.BOLD,15));
				
				label.setHorizontalAlignment(SwingConstants.CENTER);
				
				dialog.setSize(250,115);
				dialog.setLayout(new GridLayout(2,1));
				dialog.add(label);
				JPanel panel = new JPanel();
				panel.add(button1);
				dialog.add(panel);
				
				int x = getLocationOnScreen().x+(getSize().width/2-125);
				int y = getLocationOnScreen().y+(getSize().height/2-57);
				
				dialog.setLocation(x,y);
				dialog.setResizable(false);
				
				dialog.setVisible(true);
				
				if(!dialog.isVisible()) newGame();
				
				return;
			}
		}
	}
}