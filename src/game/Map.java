package game;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JPanel;

public class Map {
	public int [][] mapInfo = new int[13][15];
	
	public Map(String path) {
		try {
			int row = 0;
			BufferedReader reader = new BufferedReader(new FileReader(path));
			//BufferedReader reader = new BufferedReader(new FileReader("map_1.txt"));
			String temp;
			while((temp = reader.readLine()) != null) {
				String [] temp_arr = temp.split(" ");
				for(int i=0; i<mapInfo[row].length; i++) {
					mapInfo[row][i] = temp_arr[i].charAt(0) - '0';
				}
				row++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void mapLoad() {
		
	}
	
	
	
	public boolean collideCheck(int xPos, int yPos, int left_right, int up_down) {
		boolean check = true;
		int x = xPos + (int)(Math.ceil((double)left_right / MapObject.BLOCK_SIZE));
		int y = yPos + (int)(Math.ceil((double)up_down / MapObject.BLOCK_SIZE));
		if(xPos < 0 || yPos < 0 || xPos >= mapInfo[0].length || yPos >= mapInfo.length)
			check = true;
		else if(mapInfo[yPos][xPos] == 0 || mapInfo[yPos][xPos] == 5)
			check = false;
		else if(mapInfo[y][x] == 0 || mapInfo[y][x] == 5)
			check = true;
		else
			check = true;
		
		return check;
	}
	public int [][] getMapInfo() { return mapInfo; }
}
