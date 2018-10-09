package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import SEFunction.AttributesGenerate;
import SEFunction.DataOwner;
import SEFunction.DeleteFolder;
import SEFunction.TrustAuthority;
import SEFunction.User;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

public class GUI{
	public static void GraphicUI() throws BiffException, IOException{
		//file_dir
		String file_dir = "file/";
		
		String sCloudServer = "CloudServer/";
		String sDataOwner = "DataOwner/";
		String sUser = "User/";
		String sTrustAuthority = "TrustAuthority/";
		
		String pubkey = "public_key";
		String mskkey = "master_key";
		String prvkey = "private_key";
		String kmkkey = "kmk_key";
		String usertuple = "user_file";
		String keywordindex = "keyword_file";
		
		String pubfile = file_dir+sCloudServer+pubkey;
		String mskfile = file_dir+sTrustAuthority+mskkey;
		String kmkfile = file_dir+sTrustAuthority+kmkkey;

		String RegisterTable = "file/TrustAuthority/RegisterTable.xls";
		File RegisterFile = new File(RegisterTable);
		
		JFrame frame = new JFrame("csCPABE");

		// background picture
		String TAimagepath = "image/TAimage.png";
		ImageIcon TAbackground = new ImageIcon(TAimagepath);
		JLabel TAbackgroundlabel = new JLabel(TAbackground);
		TAbackgroundlabel.setBounds(0, 0, TAbackground.getIconWidth(),
						TAbackground.getIconHeight());
				
		frame.getLayeredPane().add(TAbackgroundlabel, new Integer(Integer.MIN_VALUE));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.setSize(960, 560);
		frame.setResizable(false);
		frame.setVisible(true);

		CardLayout card = new CardLayout();
		
		
		
		// 不需要有cloud server, proxy server
		// 這兩個只會被動執行
		JPanel MainPanel = new JPanel();
		JPanel ButtonPanel = new JPanel();
		JPanel AccountPanel = new JPanel();
		

		//JButton TAButton = new JButton("Trust Authority");
		JButton USButton = new JButton("User");
		JButton DOButton = new JButton("Data Owner");

		
		JButton DestroyButton = new JButton("BOOM!!!");
				
		MainPanel.setLayout(card);
		
		// AccountPanel 
		JLabel AccountLabel = new JLabel("Account");
		JLabel UidLabel = new JLabel("-1");
		AccountLabel.setVisible(false);
		UidLabel.setVisible(false);
		AccountPanel.add(AccountLabel);
		AccountPanel.add(UidLabel);
		AccountPanel.setVisible(false);
		
		// NewUserGrantPanel
		// background picture
		String NUGimagepath = "image/newusergrantimage.png";
		ImageIcon NUGbackground = new ImageIcon(NUGimagepath);

		JPanel NewUserGrantPanel = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
			    g.drawImage(NUGbackground.getImage(), 0, 0, this);
			   }
		};
		NewUserGrantPanel.setLayout(null);
		JTextArea acArea = new JTextArea();
		acArea.setFont(new   java.awt.Font("Dialog",   1,   40));
		acArea.setForeground(Color.WHITE);
		acArea.setOpaque(false);
		acArea.setSize(600, 50);
		acArea.setBounds(300, 60, 600, 50);
		JTextArea paArea = new JTextArea();
		paArea.setFont(new   java.awt.Font("Dialog",   1,   40));
		paArea.setForeground(Color.WHITE);
		paArea.setOpaque(false);
		paArea.setSize(585, 50);
		paArea.setBounds(315, 150, 585, 50);
		JTextArea idArea = new JTextArea();
		idArea.setFont(new   java.awt.Font("Dialog",   1,   40));
		idArea.setForeground(Color.WHITE);
		idArea.setOpaque(false);
		idArea.setSize(640, 50);
		idArea.setBounds(260, 230, 640, 50);
		JTextArea atArea = new JTextArea();
		atArea.setFont(new   java.awt.Font("Dialog",   1,   40));
		atArea.setForeground(Color.WHITE);
		atArea.setOpaque(false);
		atArea.setSize(570, 50);
		atArea.setBounds(330, 320, 570, 50);
		NewUserGrantPanel.add(acArea);
		NewUserGrantPanel.add(paArea);
		NewUserGrantPanel.add(idArea);
		NewUserGrantPanel.add(atArea);

		// home button
		JButton NUGHomeButton = new JButton();
		NUGHomeButton.setSize(165,85);
		NUGHomeButton.setBounds(760, 445, 165, 85);
		NUGHomeButton.setContentAreaFilled(false);
		NUGHomeButton.setBorderPainted(false); 
		NUGHomeButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "TA");	
				acArea.setText("");
				paArea.setText("");
				idArea.setText("");
				atArea.setText("");
			}
			
		});
		NewUserGrantPanel.add(NUGHomeButton);

		JButton NewUserConfirmButton = new JButton();
		NewUserConfirmButton.setSize(165, 85);
		NewUserConfirmButton.setBounds(500, 445, 165, 85);
		NewUserConfirmButton.setContentAreaFilled(false);
		NewUserConfirmButton.setBorderPainted(false); 
		NewUserGrantPanel.add(NewUserConfirmButton);
		
		NewUserConfirmButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String sUid = idArea.getText();
				String attri = atArea.getText();
				String[] attr = attri.split(",");
				if(sUid.length()==0){
					JOptionPane.showMessageDialog(null, "Please input Uid", "Error", JOptionPane.INFORMATION_MESSAGE );
				}else{
					int Uid = Integer.parseInt(sUid);
					try {
						int[] userA = AttributesGenerate.AttributesGen(attr);
						TrustAuthority.NewUserGrant(pubfile, mskfile, kmkfile, file_dir+sUser , userA, Uid);
						TrustAuthority.UserRegister(acArea.getText(), paArea.getText(), idArea.getText());
						JOptionPane.showMessageDialog(null, "Done!", "Done!", JOptionPane.INFORMATION_MESSAGE );
					} catch (WriteException | BiffException | IOException e1) {
						e1.printStackTrace();
					}
				}
				
			}
			
		});
		
		// User Revocation Panel
		String URimagepath = "image/userrevocationimage.png";
		ImageIcon URbackground = new ImageIcon(URimagepath);

		JPanel UserRevocationPanel = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
			    g.drawImage(URbackground.getImage(), 0, 0, this);
			   }
		};
		UserRevocationPanel.setLayout(null);
		JTextArea acArea2 = new JTextArea();
		JTextArea idArea2 = new JTextArea();
		UserRevocationPanel.add(acArea2);
		acArea2.setFont(new   java.awt.Font("Dialog",   1,   40));
		acArea2.setForeground(Color.WHITE);
		acArea2.setOpaque(false);
		acArea2.setSize(600, 50);
		acArea2.setBounds(300, 60, 600, 50);
		UserRevocationPanel.add(idArea2);
		idArea2.setFont(new   java.awt.Font("Dialog",   1,   40));
		idArea2.setForeground(Color.WHITE);
		idArea2.setOpaque(false);
		idArea2.setSize(640, 50);
		idArea2.setBounds(260, 230, 640, 50);

		// home button
		JButton URHomeButton = new JButton();
		URHomeButton.setSize(165,85);
		URHomeButton.setBounds(760, 445, 165, 85);
		URHomeButton.setContentAreaFilled(false);
		URHomeButton.setBorderPainted(false); 
		URHomeButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "TA");	
				acArea2.setText("");
				idArea2.setText("");
			}
			
		});
		UserRevocationPanel.add(URHomeButton);

		JButton UserRevocationConfirm = new JButton();
		UserRevocationConfirm.setSize(165, 85);
		UserRevocationConfirm.setBounds(500, 445, 165, 85);
		UserRevocationConfirm.setContentAreaFilled(false);
		UserRevocationConfirm.setBorderPainted(false); 
		UserRevocationPanel.add(UserRevocationConfirm);
		
		UserRevocationConfirm.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String sUid = idArea2.getText();
				String account = acArea2.getText();
				if(sUid.length()==0){
					JOptionPane.showMessageDialog(null, "Please input Uid", "Error", JOptionPane.INFORMATION_MESSAGE );
				}else{
					int Uid = Integer.parseInt(sUid);
					try {
						TrustAuthority.UserRevocation(Uid);
						TrustAuthority.UserRevocationRegister(account);
						JOptionPane.showMessageDialog(null, "Done!", "Done!", JOptionPane.INFORMATION_MESSAGE );
					} catch (BiffException | WriteException | IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			
		});
		
		// TA Panel
		JPanel TAPanel = new JPanel();
		JButton NewUserGrantButton = new JButton();
		NewUserGrantButton.setSize(775,125);
		NewUserGrantButton.setBounds(65, 100, 775, 125);
		NewUserGrantButton.setOpaque(false);
		JButton UserRevocationButton = new JButton();
		UserRevocationButton.setSize(775, 125);
		UserRevocationButton.setBounds(65, 300, 775, 125);
		UserRevocationButton.setOpaque(false);
		TAPanel.setLayout(null);
		TAPanel.add(TAbackgroundlabel, new Integer(Integer.MIN_VALUE));
		
		TAPanel.add(NewUserGrantButton);
		TAPanel.add(UserRevocationButton);
		
		
		// system setup button (for test don't use now)
		JButton SystemSetupButton = new JButton("System Setup");
		SystemSetupButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				int Nsize = AttributesGenerate.NSize();
				try {
					TrustAuthority.SystemSetup(pubfile, mskfile, kmkfile, Nsize);
				} catch (ClassNotFoundException | WriteException | IOException e1) {
					e1.printStackTrace();
				}
			}
			
		});
		
		NewUserGrantButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "NewUserGrant");
			}
			
		});
		
		UserRevocationButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "UserRevocation");
			}
			
		});
		
		// DO Panel
		// background picture
		String DOimagepath = "image/DOimage.png";
		ImageIcon DObackground = new ImageIcon(DOimagepath);

		JPanel DOPanel = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
			    g.drawImage(DObackground.getImage(), 0, 0, this);
			    //super.paintComponents(g);
			   }
		};
		JButton SelectFileButton = new JButton();
		SelectFileButton.setSize(300, 80);
		SelectFileButton.setBounds(10, 60, 300, 80);
		SelectFileButton.setContentAreaFilled(false);
		SelectFileButton.setBorderPainted(false); 
		JLabel FileName = new JLabel("");
		FileName.setSize(560, 50);
		FileName.setBounds(340, 80, 560, 50);
		FileName.setFont(new   java.awt.Font("Dialog",   1,   40));
		FileName.setForeground(Color.WHITE);
		FileName.setOpaque(false);
		JTextArea KeywordArea = new JTextArea();
		KeywordArea.setSize(600, 50);
		KeywordArea.setBounds(300, 200, 600, 50);
		KeywordArea.setFont(new   java.awt.Font("Dialog",   1,   40));
		KeywordArea.setForeground(Color.WHITE);
		KeywordArea.setOpaque(false);
		JTextArea AccessInputArea = new JTextArea();
		AccessInputArea.setSize(585, 50);
		AccessInputArea.setBounds(315, 335, 585, 50);
		AccessInputArea.setFont(new   java.awt.Font("Dialog",   1,   40));
		AccessInputArea.setForeground(Color.WHITE);
		AccessInputArea.setOpaque(false);
		JButton DataUploadButton = new JButton();
		DataUploadButton.setSize(165, 85);
		DataUploadButton.setBounds(500, 445, 165, 85);
		DataUploadButton.setContentAreaFilled(false);
		DataUploadButton.setBorderPainted(false); 
		DOPanel.setLayout(null);
		DOPanel.add(SelectFileButton);
		DOPanel.add(FileName);
		DOPanel.add(KeywordArea);
		DOPanel.add(AccessInputArea);
		DOPanel.add(DataUploadButton);
		
		// home button
		JButton DOHomeButton = new JButton();
		DOHomeButton.setSize(165,85);
		DOHomeButton.setBounds(760, 445, 165, 85);
		DOHomeButton.setContentAreaFilled(false);
		DOHomeButton.setBorderPainted(false); 
		DOHomeButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "US");	
				FileName.setText("");
				KeywordArea.setText("");
				AccessInputArea.setText("");
			}
				
		});
		DOPanel.add(DOHomeButton);
		
		JTextField FilePath = new JTextField();
		
		SelectFileButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();//宣告filechooser 
				int returnValue = fileChooser.showOpenDialog(null);//叫出filechooser 
				if (returnValue == JFileChooser.APPROVE_OPTION){
					File selectedFile = fileChooser.getSelectedFile();//指派給File 
					FileName.setText(selectedFile.getName());
					FilePath.setText(selectedFile.getAbsolutePath());
				}
			}
			
		});
		
		DataUploadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// compute time
				long time1, time2;
				time1 = System.currentTimeMillis();
				
				String AreaString = KeywordArea.getText();
				String[] keywordarray = {};
				String accessStructure = AccessInputArea.getText();
				String[] aStructure = accessStructure.split(",");
				int[] accessP = AttributesGenerate.AttributesGen(aStructure);
				boolean run = true;
				if(AreaString.length()==0){
					// 沒輸入關鍵字
					run = false;
					JOptionPane.showMessageDialog(null, "Please input keyword", "Error", JOptionPane.INFORMATION_MESSAGE );
				}else{
					keywordarray = AreaString.split(",");
				}
				
				if(accessStructure.length()==0){
					run = false;
					JOptionPane.showMessageDialog(null, "Please input access structure", "Error", JOptionPane.INFORMATION_MESSAGE );
				}
				
				String sUid = UidLabel.getText();
				int Uid = Integer.parseInt(sUid);
				String inputname = FileName.getText();
				
				if(inputname.equals("")){
					run = false;
					JOptionPane.showMessageDialog(null, "Please choose the file", "Error", JOptionPane.INFORMATION_MESSAGE );
				}
				if(run){
					String cphname = inputname+"_file";
					String encname = inputname+".cpabe";
					String userfile = file_dir+sUser+Uid+"/"+usertuple;
					String inputpath = FilePath.getText();
					File checkuid = new File(userfile);
					if(checkuid.exists()){
						try {
							DataOwner.DataUpload(pubfile, userfile, file_dir+sCloudServer , file_dir+sDataOwner,
									inputpath, inputname, encname, cphname, keywordindex, accessP, keywordarray);
							JOptionPane.showMessageDialog(null, "Done!", "Done!", JOptionPane.INFORMATION_MESSAGE );
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}else{
						JOptionPane.showMessageDialog(null, "You are not user", "Error", JOptionPane.INFORMATION_MESSAGE );
					}
					
				}
				time2 = System.currentTimeMillis();
				System.out.println("Data Upload cost：" + (time2-time1) + "ms");
			}
			
		});
		
		// Decrypt Panel
		// background picture
		String Decryptimagepath = "image/decryptimage.png";
		ImageIcon Decryptbackground = new ImageIcon(Decryptimagepath);
		JPanel DecryptPanel = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
		    g.drawImage(Decryptbackground.getImage(), 0, 0, this);
		    //super.paintComponents(g);
		   }
		};
		DecryptPanel.setLayout(null);
		Vector<String> SearchResultNameVector = new Vector<String>();
		Vector<String> SearchResultPathVector = new Vector<String>();
		JList<String> SearchResultList = new JList<String>(SearchResultNameVector);
		SearchResultList.setSize(960, 400);
		SearchResultList.setBounds(0, 0, 960, 400);
		SearchResultList.setFont(new   java.awt.Font("Dialog",   0,   20));
		SearchResultList.setForeground(Color.WHITE);
		SearchResultList.setOpaque(false);
		((JLabel)SearchResultList.getCellRenderer()).setOpaque(false);
		SearchResultList.setSelectionForeground(Color.RED);
		JScrollPane SearchResultScroll = new JScrollPane(SearchResultList);
		SearchResultScroll.setSize(960, 400);
		SearchResultScroll.setBounds(0, 0, 960, 400);
		SearchResultScroll.setOpaque(false);
		SearchResultScroll.getViewport().setOpaque(false);
		DecryptPanel.add(SearchResultScroll);
		JButton DecryptConfirm = new JButton();
		DecryptConfirm.setSize(165, 85);
		DecryptConfirm.setBounds(500, 445, 165, 85);
		DecryptConfirm.setContentAreaFilled(false);
		DecryptConfirm.setBorderPainted(false); 
		DecryptPanel.add(DecryptConfirm);
		
		// home button
		JButton DecryptHomeButton = new JButton();
		DecryptHomeButton.setSize(165,85);
		DecryptHomeButton.setBounds(760, 445, 165, 85);
		DecryptHomeButton.setContentAreaFilled(false);
		DecryptHomeButton.setBorderPainted(false); 
		DecryptPanel.add(DecryptHomeButton);
		DecryptHomeButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "US");	
				SearchResultPathVector.clear();
				SearchResultNameVector.clear();
			}
						
		});
		
		DecryptConfirm.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				
				String sUid = UidLabel.getText();
				int Uid = Integer.parseInt(sUid);
				String prvfile = file_dir+sUser+Uid+"/"+prvkey;
				String decfiledir = file_dir+sUser+Uid+"/";
				
				//  找出所選擇的index
				int[] Idx = SearchResultList.getSelectedIndices();
				String[] SelectPath = new String[Idx.length];
				String[] SelectName = new String[Idx.length];
				for(int i=0; i<Idx.length; i++){
					int theIdx = Idx[i];
					// 利用index找到選擇的路徑
					SelectPath[i] = (String) SearchResultPathVector.get(theIdx);
					SelectName[i] = (String) SearchResultNameVector.get(theIdx);
					try {
						User.FileDecryptionConfirm(pubfile, prvfile, SelectPath[i], decfiledir+SelectName[i]);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				SearchResultPathVector.clear();
				SearchResultNameVector.clear();
				JOptionPane.showMessageDialog(null, "Done!", "Done!", JOptionPane.INFORMATION_MESSAGE );
			}
			
		});
		
		// Search Panel
		// background picture
		String Searchimagepath = "image/searchimage.png";
		ImageIcon Searchbackground = new ImageIcon(Searchimagepath);

		JPanel SearchPanel = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
			    g.drawImage(Searchbackground.getImage(), 0, 0, this);
			    //super.paintComponents(g);
			   }
		};
		JButton AndButton = new JButton();
		AndButton.setSize(165, 85);
		AndButton.setBounds(100, 270, 165, 85);
		AndButton.setContentAreaFilled(false);
		AndButton.setBorderPainted(false); 
		JButton OrButton = new JButton();
		OrButton.setSize(165, 85);
		OrButton.setBounds(345, 270, 165, 85);
		OrButton.setContentAreaFilled(false);
		OrButton.setBorderPainted(false); 
		JButton BackButton = new JButton();
		BackButton.setSize(165, 85);
		BackButton.setBounds(585, 270, 165, 85);
		BackButton.setContentAreaFilled(false);
		BackButton.setBorderPainted(false); 
		JTextArea KeywordToSearch = new JTextArea();
		KeywordToSearch.setSize(725, 50);
		KeywordToSearch.setBounds(125, 110, 725, 50);
		KeywordToSearch.setFont(new   java.awt.Font("Dialog",   1,   40));
		KeywordToSearch.setForeground(Color.WHITE);
		KeywordToSearch.setOpaque(false);
		JButton SearchConfirm = new JButton();
		SearchConfirm.setSize(165, 85);
		SearchConfirm.setBounds(500, 445, 165, 85);
		SearchConfirm.setContentAreaFilled(false);
		SearchConfirm.setBorderPainted(false);
		SearchPanel.setLayout(null);
		SearchPanel.add(AndButton);
		SearchPanel.add(OrButton);
		SearchPanel.add(BackButton);
		SearchPanel.add(KeywordToSearch);
		SearchPanel.add(SearchConfirm);
		
		
		ArrayList<String> KeywordList = new ArrayList<String>();
		ArrayList<Integer> AndOrList = new ArrayList<Integer>();
		// 1 -> And    2 -> Or
		
		// home button
		JButton SearchHomeButton = new JButton();
		SearchHomeButton.setSize(165,85);
		SearchHomeButton.setBounds(760, 445, 165, 85);
		SearchHomeButton.setContentAreaFilled(false);
		SearchHomeButton.setBorderPainted(false); 
		SearchPanel.add(SearchHomeButton);
		SearchHomeButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "US");	
				KeywordList.clear();
				AndOrList.clear();
				KeywordToSearch.setText("");
			}
					
		});
		
		SearchConfirm.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// 計算時間
				long time1, time2;
				time1 = System.currentTimeMillis();
				// Search 結束跳decrypt頁面
				String AreaString = KeywordToSearch.getText();
				String sUid = UidLabel.getText();
				if(AreaString.length()==0){
					JOptionPane.showMessageDialog(null, "Please input keyword", "Error", JOptionPane.INFORMATION_MESSAGE );
				}else{
					int Uid = Integer.parseInt(sUid);
					String userfile = file_dir+sUser+Uid+"/"+usertuple;
					String prvfile = file_dir+sUser+Uid+"/"+prvkey;
					String decfiledir = file_dir+sUser+Uid+"/";
					File checkuid = new File(userfile);
					if(checkuid.exists()){
						try {
							if(AndOrList.size()==0){
								User.TrapdoorGenUser(pubfile, userfile, prvfile, decfiledir,AreaString);
							}else{
								KeywordList.add(AreaString);
								User.UserAndOrSearch(KeywordList, AndOrList, pubfile, userfile, prvfile, decfiledir);
							}
							
							time2 = System.currentTimeMillis();
							System.out.println("search cost：" + (time2-time1) + "ms");
							
							boolean SearchCheck = User.CheckSearch(decfiledir);
							if(SearchCheck){
								String SearchResultFilepath= decfiledir+"SearchResultFilepath.txt";
								FileReader fr = new FileReader(SearchResultFilepath);
								BufferedReader br = new BufferedReader(fr);
								String SearchResultName = decfiledir + "SearchResultName.txt";
								FileReader fr2 = new FileReader(SearchResultName);
								BufferedReader br2 = new BufferedReader(fr2);
								while(br.ready() && br2.ready()){
									String path = br.readLine();
									SearchResultPathVector.addElement(path);
									String name  = br2.readLine();
									SearchResultNameVector.addElement(name);
								}
								br.close();
								br2.close();
								SearchResultList.setListData(SearchResultNameVector);
								card.show(MainPanel, "Decrypt");
								KeywordList.clear();
								AndOrList.clear();
								KeywordToSearch.setText("");
								//User.FileDecryption(pubfile, prvfile, decfiledir);
							}else{
								JOptionPane.showMessageDialog(null, "Can't find result", "Error", JOptionPane.INFORMATION_MESSAGE );
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}else{
						JOptionPane.showMessageDialog(null, "You are not user", "Error", JOptionPane.INFORMATION_MESSAGE );
					}
					
				}
			}
			
		});
		
		AndButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String AreaString = KeywordToSearch.getText();
				if(AreaString.length()==0){
					JOptionPane.showMessageDialog(null, "Please input keyword", "Error", JOptionPane.INFORMATION_MESSAGE );
				}else{
					KeywordList.add(AreaString);
					AndOrList.add(1);
					KeywordToSearch.setText("");
				}
			}
			
		});
		
		OrButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String AreaString = KeywordToSearch.getText();
				if(AreaString.length()==0){
					JOptionPane.showMessageDialog(null, "Please input keyword", "Error", JOptionPane.INFORMATION_MESSAGE );
				}else{
					KeywordList.add(AreaString);
					AndOrList.add(2);
					KeywordToSearch.setText("");
				}
				
			}
			
		});
		
		BackButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				int theSize = KeywordList.size();
				if(theSize==0){
					JOptionPane.showMessageDialog(null, "Nothing to delete", "Error", JOptionPane.INFORMATION_MESSAGE );
				}else{
					String keyword = KeywordList.get(theSize-1);
					KeywordToSearch.setText(keyword);
					KeywordList.remove(theSize-1);
					AndOrList.remove(theSize-1);
				}
				
				
			}
			
		});
		
		// US panel
		// background picture
		String USimagepath = "image/USimage.png";
		ImageIcon USbackground = new ImageIcon(USimagepath);

		JPanel USPanel = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
			    g.drawImage(USbackground.getImage(), 0, 0, this);
			   }
		};
		JButton DataOwnerButton = new JButton();
		DataOwnerButton.setSize(775,125);
		DataOwnerButton.setBounds(45, 50, 775, 125);
		DataOwnerButton.setContentAreaFilled(false);
		DataOwnerButton.setBorderPainted(false); 
		JButton SearchButton = new JButton();
		SearchButton.setSize(775, 125);
		SearchButton.setBounds(45, 240, 775, 125);
		SearchButton.setContentAreaFilled(false);
		SearchButton.setBorderPainted(false); 
		USPanel.setLayout(null);
		USPanel.add(DataOwnerButton);
		USPanel.add(SearchButton);
		
		SearchButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "Search");
			}
			
		});
		
		DataOwnerButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "DO");
			}
			
		});
		
		JPanel LoginFail = new JPanel();
		JLabel LoginFailLabel = new JLabel("Login fail");
		LoginFail.add(LoginFailLabel);
		
		Container contentPane = frame.getContentPane();  
		
		ButtonPanel.add(DestroyButton);
		
		MainPanel.add(TAPanel,"TA");
		MainPanel.add(USPanel,"US");
		MainPanel.add(DOPanel,"DO");
		MainPanel.add(NewUserGrantPanel,"NewUserGrant");
		MainPanel.add(UserRevocationPanel,"UserRevocation");
		MainPanel.add(SearchPanel,"Search");
		MainPanel.add(LoginFail,"LoginFail");
		MainPanel.add(DecryptPanel,"Decrypt");
		
		USButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "US");	
			}
			
		});
		DOButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				card.show(MainPanel, "DO");
			}
			
		});
		DestroyButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				DeleteFolder.delFolder(file_dir);
			}
			
		});
		
		int mType = JOptionPane.INFORMATION_MESSAGE;
		
		
		// 讀帳號 判斷有沒有這個帳號以及是否是受信任的第三方
		int Uid = -1;
		String account = "";
		if(!RegisterFile.exists()){
			JOptionPane.showMessageDialog(null, "Setup First", "Warning", JOptionPane.INFORMATION_MESSAGE );
			int Nsize = AttributesGenerate.NSize();
			try {
				TrustAuthority.SystemSetup(pubfile, mskfile, kmkfile, Nsize);
			} catch (ClassNotFoundException | WriteException | IOException e1) {
				e1.printStackTrace();
			}
		}else{
			Workbook book=Workbook.getWorkbook(new File(RegisterTable));
			Sheet sheet=book.getSheet(0);
			int raw = sheet.getRows();
			int index = 0;
			boolean accountfound = false;
			account = JOptionPane.showInputDialog(frame,"Please input account","Account",mType);
			while(!accountfound && index<raw){
				Cell cell=sheet.getCell(0,index);
				String result=cell.getContents();
				if(result.equals(account)){
					accountfound = true;
				}else{
					index++;
				}
			}
			if(accountfound){
				String password = JOptionPane.showInputDialog(frame,"Please input password","Password",mType);
				Cell cell=sheet.getCell(1,index);
				String result=cell.getContents();
				System.out.println(result);
				if(result.equals(password)){
					cell = sheet.getCell(2,index);
					result = cell.getContents();
					Uid = Integer.parseInt(result);
				}else{
					JOptionPane.showMessageDialog(null, "Wrong password", "Error", JOptionPane.INFORMATION_MESSAGE );
				}
				
			}else{
				JOptionPane.showMessageDialog(null, "Can't find accont", "Error", JOptionPane.INFORMATION_MESSAGE );
			}
		}
		String stringUid = ""+Uid;
		UidLabel.setText(stringUid);
		contentPane.add(AccountPanel, BorderLayout.NORTH);
		//contentPane.add(ButtonPanel, BorderLayout.NORTH);
		//contentPane.add(forbackground);
		contentPane.add(MainPanel);
		if(Uid == -1){	// 沒讀到帳號
			card.show(MainPanel, "LoginFail");
			AccountLabel.setText("LoginFail");
			contentPane.add(ButtonPanel, BorderLayout.SOUTH);
		}else if(Uid == 0){	// 帳號為受信任的第三方
			card.show(MainPanel, "TA");
			AccountLabel.setText(account);
			//contentPane.add(HomeButton, BorderLayout.SOUTH);
		}else{	// 帳號為一般使用者
			card.show(MainPanel, "US");
			AccountLabel.setText(account);
			//contentPane.add(HomeButton, BorderLayout.SOUTH);
		}

		frame.getContentPane();
		frame.validate();
	}
}
