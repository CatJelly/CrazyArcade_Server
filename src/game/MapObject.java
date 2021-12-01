package game;
import javax.swing.*;

public abstract class MapObject extends Thread {
	protected static final int BLOCK_SIZE = 52 + 10;
	public int xPos;
	public int yPos;
	public int code;
	public String name;
	protected ImageIcon image = null;
	protected JPanel gamePanel = null;
	
	protected Map map;
	
	public MapObject(int xPos, int yPos, int code, String name, JPanel gamePanel) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.code = code;
		this.name = name;
		this.gamePanel = gamePanel;
	}
	
	public int [] getPos() {
		int [] pos = {xPos, yPos};
		return pos;
	}

	public void printObject() {
		JLabel label = new JLabel(image);
		label.setBounds(xPos * BLOCK_SIZE, yPos * BLOCK_SIZE + 10, BLOCK_SIZE, BLOCK_SIZE + 50);
		this.gamePanel.add(label);
		this.gamePanel.setComponentZOrder(label, xPos);
	}
}
