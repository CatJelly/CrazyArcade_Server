package game;
import java.awt.Image;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Player extends MapObject {
	private Socket socket;
	public int left_right = 0;
	public int up_down = 0;
	public int bomb;
	public int power;
	public int speed;
	private ImageIcon [] upImage = null;
	private ImageIcon [] downImage = null;
	private ImageIcon [] leftImage = null;
	private ImageIcon [] rightImage = null;
	
	public Player(int xPos, int yPos, int code, String name, JPanel gamePanel, Map map) {
		super(xPos, yPos, code, name, gamePanel);
		this.map = map;
		this.bomb = 1;
		this.power = 1;
		this.speed = 1;
		
		this.image = new ImageIcon("player/wait0.png");
		System.out.println(image);
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public void positionCheck() {
		if(left_right < -MapObject.BLOCK_SIZE) {
			left_right = 0;
			xPos -= 1;
		}
		else if(left_right > MapObject.BLOCK_SIZE) {
			left_right = 0;
			xPos += 1;
		}
		else if(up_down < -(MapObject.BLOCK_SIZE + 10)) {
			up_down = 0;
			yPos -= 1;
		}
		else if(up_down > MapObject.BLOCK_SIZE + 10) {
			up_down = 0;
			yPos += 1;
		}
	}
	
	@Override
	public void printObject() {
		JLabel label = new JLabel(image);
		label.setBounds(xPos * BLOCK_SIZE , yPos * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE + 50);
		this.gamePanel.add(label);
		this.gamePanel.setComponentZOrder(label, yPos);
	}
	
}