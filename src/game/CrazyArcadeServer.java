package game;
//JavaObjServer.java ObjectStream 湲곕컲 梨꾪똿 Server
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class CrazyArcadeServer extends JFrame {
	private static int playerNum = 0;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private ServerSocket socket; // �꽌踰꾩냼耳�
	private Socket client_socket; // accept() �뿉�꽌 �깮�꽦�맂 client �냼耳�
	private Vector UserVec = new Vector(); // �뿰寃곕맂 �궗�슜�옄瑜� ���옣�븷 踰≫꽣
	private static final int BUF_LEN = 128; // Windows 泥섎읆 BUF_LEN �쓣 �젙�쓽
	public Map map;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CrazyArcadeServer frame = new CrazyArcadeServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CrazyArcadeServer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // �꽌踰꾨�� �뜑�씠�긽 �떎�뻾�떆�궎吏� 紐� �븯寃� 留됰뒗�떎
				txtPortNumber.setEnabled(false); // �뜑�씠�긽 �룷�듃踰덊샇 �닔�젙紐� �븯寃� 留됰뒗�떎
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
		
		map = new Map("maps/map_1.txt");
	}

	// �깉濡쒖슫 李멸��옄 accept() �븯怨� user thread瑜� �깉濡� �깮�꽦�븳�떎.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // �궗�슜�옄 �젒�냽�쓣 怨꾩냽�빐�꽌 諛쏄린 �쐞�빐 while臾�
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept媛� �씪�뼱�굹湲� �쟾源뚯��뒗 臾댄븳 ��湲곗쨷
					AppendText("�깉濡쒖슫 李멸��옄 from " + client_socket);
					// User �떦 �븯�굹�뵫 Thread �깮�꽦
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // �깉濡쒖슫 李멸��옄 諛곗뿴�뿉 異붽�
					new_user.start(); // 留뚮뱺 媛앹껜�쓽 �뒪�젅�뱶 �떎�뻾
					AppendText("�쁽�옱 李멸��옄 �닔 " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					// System.exit(0);
				}
			}
		}
	}

	public void AppendText(String str) {
		// textArea.append("�궗�슜�옄濡쒕��꽣 �뱾�뼱�삩 硫붿꽭吏� : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void AppendObject(ChatMsg msg) {
		// textArea.append("�궗�슜�옄濡쒕��꽣 �뱾�뼱�삩 object : " + str+"\n");
		textArea.append("code = " + msg.code + "\n");
		textArea.append("id = " + msg.UserName + "\n");
		textArea.append("data = " + msg.data + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// User �떦 �깮�꽦�릺�뒗 Thread
	// Read One �뿉�꽌 ��湲� -> Write All
	class UserService extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		private Socket client_socket;
		private Vector user_vc;
		public String UserName = "";
		public String UserStatus;

		public UserService(Socket client_socket) {
			// TODO Auto-generated constructor stub
			// 留ㅺ컻蹂��닔濡� �꽆�뼱�삩 �옄猷� ���옣
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			try {
//				is = client_socket.getInputStream();
//				dis = new DataInputStream(is);
//				os = client_socket.getOutputStream();
//				dos = new DataOutputStream(os);

				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());

				// line1 = dis.readUTF();
				// /login user1 ==> msg[0] msg[1]
//				byte[] b = new byte[BUF_LEN];
//				dis.read(b);		
//				String line1 = new String(b);
//
//				//String[] msg = line1.split(" ");
//				//UserName = msg[1].trim();
//				UserStatus = "O"; // Online �긽�깭
//				Login();
			} catch (Exception e) {
				AppendText("userService error");
			}
		}

		public void Login() {
			AppendText("�깉濡쒖슫 李멸��옄 " + UserName + " �엯�옣.");
			WriteOne("Welcome to Java chat server\n");
			WriteOne(UserName + "�떂 �솚�쁺�빀�땲�떎.\n"); // �뿰寃곕맂 �궗�슜�옄�뿉寃� �젙�긽�젒�냽�쓣 �븣由�
			String msg = "[" + UserName + "]�떂�씠 �엯�옣 �븯���뒿�땲�떎.\n";
			WriteOthers(msg); // �븘吏� user_vc�뿉 �깉濡� �엯�옣�븳 user�뒗 �룷�븿�릺吏� �븡�븯�떎.
		}

		public void Logout() {
			String msg = "[" + UserName + "]�떂�씠 �눜�옣 �븯���뒿�땲�떎.\n";
			UserVec.removeElement(this); // Logout�븳 �쁽�옱 媛앹껜瑜� 踰≫꽣�뿉�꽌 吏��슫�떎
			WriteAll(msg); // �굹瑜� �젣�쇅�븳 �떎瑜� User�뱾�뿉寃� �쟾�넚
			AppendText("�궗�슜�옄 " + "[" + UserName + "] �눜�옣. �쁽�옱 李멸��옄 �닔 " + UserVec.size());
		}

		// 紐⑤뱺 User�뱾�뿉寃� 諛⑹넚. 媛곴컖�쓽 UserService Thread�쓽 WriteONe() �쓣 �샇異쒗븳�떎.
		public void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOne(str);
			}
		}
		// 紐⑤뱺 User�뱾�뿉寃� Object瑜� 諛⑹넚. 梨꾪똿 message�� image object瑜� 蹂대궪 �닔 �엳�떎
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOneObject(ob);
			}
		}
		public void WriteOthersObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.UserStatus == "O")
					user.WriteOneObject(ob);
			}
		}

		// �굹瑜� �젣�쇅�븳 User�뱾�뿉寃� 諛⑹넚. 媛곴컖�쓽 UserService Thread�쓽 WriteONe() �쓣 �샇異쒗븳�떎.
		public void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.UserStatus == "O")
					user.WriteOne(str);
			}
		}

		// Windows 泥섎읆 message �젣�쇅�븳 �굹癒몄� 遺�遺꾩� NULL 濡� 留뚮뱾湲� �쐞�븳 �븿�닔
		public byte[] MakePacket(String msg) {
			byte[] packet = new byte[BUF_LEN];
			byte[] bb = null;
			int i;
			for (i = 0; i < BUF_LEN; i++)
				packet[i] = 0;
			try {
				bb = msg.getBytes("euc-kr");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}

		// UserService Thread媛� �떞�떦�븯�뒗 Client �뿉寃� 1:1 �쟾�넚
		public void WriteOne(String msg) {
			try {
				// dos.writeUTF(msg);
//				byte[] bb;
//				bb = MakePacket(msg);
//				dos.write(bb, 0, bb.length);
				ChatMsg obcm = new ChatMsg("SERVER", "200", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
//					dos.close();
//					dis.close();
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // �뿉�윭媛��궃 �쁽�옱 媛앹껜瑜� 踰≫꽣�뿉�꽌 吏��슫�떎
			}
		}

		// 洹볦냽留� �쟾�넚
		public void WritePrivate(String msg) {
			try {
				ChatMsg obcm = new ChatMsg("洹볦냽留�", "200", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // �뿉�윭媛��궃 �쁽�옱 媛앹껜瑜� 踰≫꽣�뿉�꽌 吏��슫�떎
			}
		}
		public void WriteOneObject(Object ob) {
			try {
			    oos.writeObject(ob);
			} 
			catch (IOException e) {
				AppendText("oos.writeObject(ob) error");		
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout();
			}
		}
		public void login(ChatMsg cm) {
			UserName = cm.UserName;
			UserStatus = "O"; // Online �긽�깭
			Login();
			loginSuccess(cm);
		}
		public void loginSuccess(ChatMsg cm) {
			String msg = "loginSuccess";
			cm.data = msg;
			cm.code = "101";
			cm.playerNum = playerNum++;
			if(playerNum > 1) {
				playerNum = 0;
			}
			WriteOneObject(cm);
		}
		public void loginFail() {

		}
		public void logout() {
			Logout();
		}
		public void chatMsg(String msg, ChatMsg cm) {
			msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendText(msg); // server �솕硫댁뿉 異쒕젰
						String[] args = msg.split(" "); // �떒�뼱�뱾�쓣 遺꾨━�븳�떎.
						if (args.length == 1) { // Enter key 留� �뱾�뼱�삩 寃쎌슦 Wakeup 泥섎━留� �븳�떎.
							UserStatus = "O";
						} else if (args[1].matches("/exit")) {
							Logout();
						} else if (args[1].matches("/list")) {
							WriteOne("User list\n");
							WriteOne("Name\tStatus\n");
							WriteOne("-----------------------------\n");
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								WriteOne(user.UserName + "\t" + user.UserStatus + "\n");
							}
							WriteOne("-----------------------------\n");
						} else if (args[1].matches("/sleep")) {
							UserStatus = "S";
						} else if (args[1].matches("/wakeup")) {
							UserStatus = "O";
						} else if (args[1].matches("/to")) { // 洹볦냽留�
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if (user.UserName.matches(args[2]) && user.UserStatus.matches("O")) {
									String msg2 = "";
									for (int j = 3; j < args.length; j++) {// �떎�젣 message 遺�遺�
										msg2 += args[j];
										if (j < args.length - 1)
											msg2 += " ";
									}
									// /to 鍮쇨퀬.. [洹볦냽留�] [user1] Hello user2..
									user.WritePrivate(args[0] + " " + msg2 + "\n");
									//user.WriteOne("[洹볦냽留�] " + args[0] + " " + msg2 + "\n");
									break;
								}
							}
						} else if (args[1].matches("/start")) {
							cm.code = "900";
							cm.mapInfo = map.getMapInfo();
							WriteOneObject(cm);
						} else { // �씪諛� 梨꾪똿 硫붿떆吏�
							UserStatus = "O";
							//WriteAll(msg + "\n"); // Write All
							WriteAllObject(cm);
						}
		}
		public void emoticonMsg() {

		}
		public void gamePauseRequest() {

		}
		public void gmaePauseRequestAgree() {

		}
		public void gamePauseRequestDisagree() {

		}
		public void gameWin() {

		}
		public void gameLose() {

		}
		public void gameDraw() {

		}
		public void playerMove(ChatMsg cm) {
			int left_right = cm.left_right;
			int up_down = cm.up_down;
			int xPos = cm.p_xPos, yPos = cm.p_yPos;
			
			if(cm.data.equals("left")) {
				if(!map.collideCheck(xPos - 1, yPos, left_right, up_down)) {
					left_right -= 5;
					cm.left_right = left_right;
					cm.up_down = up_down;
					WriteAllObject(cm);
				}
			}
			else if(cm.data.equals("right")) {
				if(!map.collideCheck(xPos + 1, yPos, left_right, up_down)) {
					left_right += 5;
					cm.left_right = left_right;
					cm.up_down = up_down;
					WriteAllObject(cm);
				}
			}
			else if(cm.data.equals("up")) {
				if(!map.collideCheck(xPos, yPos - 1, left_right, up_down)) {
					up_down -= 5;
					cm.left_right = left_right;
					cm.up_down = up_down;
					WriteAllObject(cm);
				}
			}
			else if(cm.data.equals("down")) {
				if(!map.collideCheck(xPos, yPos + 1, left_right, up_down)) {
					up_down += 5;
					cm.left_right = left_right;
					cm.up_down = up_down;
					WriteAllObject(cm);
				}
			}
		}
		public void playerStatus(ChatMsg cm) {
			WriteAllObject(cm);
		}
		public void bombSet(ChatMsg cm) {
			WriteAllObject(cm);
		}
		public void bombExplodeEnd(ChatMsg cm) {
			map.mapInfo = cm.mapInfo;
			WriteAllObject(cm);
		}
		public void playerKill() {

		}
		public void playerDie() {

		}
		public void gameStart(ChatMsg cm) {
			cm.code = "900";
			cm.mapInfo = map.getMapInfo();
			WriteAllObject(cm);
		}
		public void mapChange() {

		}
		public void itemSet() {

		}
		
		public void run() {
			while (true) { // �궗�슜�옄 �젒�냽�쓣 怨꾩냽�빐�꽌 諛쏄린 �쐞�빐 while臾�
				try {
					// String msg = dis.readUTF();
//					byte[] b = new byte[BUF_LEN];
//					int ret;
//					ret = dis.read(b);
//					if (ret < 0) {
//						AppendText("dis.read() < 0 error");
//						try {
//							dos.close();
//							dis.close();
//							client_socket.close();
//							Logout();
//							break;
//						} catch (Exception ee) {
//							break;
//						} // catch臾� �걹
//					}
//					String msg = new String(b, "euc-kr");
//					msg = msg.trim(); // �븵�뮘 blank NULL, \n 紐⑤몢 �젣嫄�
					Object obcm = null;
					String msg = null;
					ChatMsg cm = null;
					if (socket == null)
						break;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						AppendObject(cm);
					} else
						continue;

					switch(cm.code) {
						case "100": //Login
						login(cm);
							break;
						case "101": //Login Success
						
							break;
						case "102": //Login Fail
						loginFail();
							break;
						case "200": //Logout
						logout();
							break;
						case "300": //Chatting Msg
						chatMsg(msg, cm);
							break;
						case "301": //Emoticon Msg
						emoticonMsg();
							break;
						case "400": //Game Pause Request
						gamePauseRequest();
							break;
						case "401": //Game Pause Request Agree
						gmaePauseRequestAgree();
							break;
						case "402": //Game Pause Request Disagree
						gamePauseRequestDisagree();
							break;
						case "500": //Game Win
						gameWin();
							break;
						case "501": //Game Lose
						gameLose();
							break;
						case "502": //Game Draw
						gameDraw();
							break;
						case "600": //Player Move
						playerMove(cm);
							break;
						case "601":
						playerStatus(cm);
							break;
						case "700": //Bomb Set
						bombSet(cm);
							break;
						case "702": //Bomb Set
						bombExplodeEnd(cm);
							break;
						case "800": //Player Kill
						playerKill();
							break;
						case "801": //Player Die
						playerDie();
							break;
						case "900":
						gameStart(cm);
							break;
						case "901": //Map Changed
						mapChange();
							break;
						case "902": //Item Set
						itemSet();
							break;
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
//						dos.close();
//						dis.close();
						ois.close();
						oos.close();
						client_socket.close();
						Logout(); // �뿉�윭媛��궃 �쁽�옱 媛앹껜瑜� 踰≫꽣�뿉�꽌 吏��슫�떎
						break;
					} catch (Exception ee) {
						break;
					} // catch臾� �걹
				} // 諛붽묑 catch臾몃걹
			} // while
		} // run
	}

}
