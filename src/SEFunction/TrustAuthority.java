package SEFunction;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import bswabe.BswabeMsk;
import bswabe.BswabePrv;
import bswabe.BswabePub;
import bswabe.SerializeUtils;
import cscpabe.Common;
import cscpabe.csCpabe;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import jxl.Cell;
import jxl.CellType;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class TrustAuthority {
	public static void SystemSetup(String pubfile, String mskfile, String kmkfile,
			int n) throws ClassNotFoundException, IOException, RowsExceededException, WriteException{
		
		// 創建各資料夾
		String[] dirtobuild = new String[9];
		dirtobuild[0] = "file/";
		dirtobuild[1] = dirtobuild[0] + "CloudServer/";
		dirtobuild[2] = dirtobuild[0] + "DataOwner/";
		dirtobuild[3] = dirtobuild[0] + "User/";
		dirtobuild[4] = dirtobuild[0] + "ProxyServer/";
		dirtobuild[5] = dirtobuild[0] + "TrustAuthority/";
		dirtobuild[6] = dirtobuild[1] + "cph/";
		dirtobuild[7] = dirtobuild[1] + "file/";
		dirtobuild[8] = dirtobuild[1] + "Upload/";
		
		for(int i=0; i<9; i++){
			File build = new File(dirtobuild[i]);
			if(!build.exists()){
				build.mkdirs();
			}
		}
		
		// 創建登入表單
		String RegisterTable = "file/TrustAuthority/RegisterTable.xls";
		File registable = new File(RegisterTable);
		if(!registable.exists()){
			WritableWorkbook book;
			WritableSheet sheet;
			book = Workbook.createWorkbook(new File(RegisterTable));
			sheet = book.createSheet("RegisterData",0);
			Label label = new Label(0,0,"Trust");
			sheet.addCell(label);
			label = new Label(1,0,"trust");
			sheet.addCell(label);
			jxl.write.Number number = new jxl.write.Number(2,0,0);
			sheet.addCell(number);
			book.write(); 
			book.close();
		}
		
		csCpabe cscpabe = new csCpabe();
		cscpabe.setup(pubfile, mskfile, n);
		
		/* get BswabePub from pubfile */
		byte[] pub_byte;
		BswabePub pub;
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);
		
		Pairing pairing = pub.pairing;
		SearchMasterKey smk = new SearchMasterKey();
		smk.K_mk = pairing.getZr().newRandomElement();
		byte[] kmk_byte;
		kmk_byte = SerializeUtils.serializeSearchMasterKey(smk);
		Common.spitFile(kmkfile, kmk_byte);
	}
	public static void NewUserGrant(String pubfile, String mskfile, String kmkfile,
			String userdir, int[] userAttributes, int Uid) throws IOException, RowsExceededException, WriteException, BiffException{
		
		// 創新user的資料夾
		String NewUserPath = userdir+Uid+"/";
		File NewUserFile = new File(NewUserPath);
		if(!NewUserFile.exists()){
			NewUserFile.mkdirs();
		}
		String prvfile = NewUserPath + "private_key";
		String userfile = NewUserPath + "user_file";
		
		csCpabe cscpabe = new csCpabe();
		cscpabe.keyGen(pubfile, mskfile, prvfile, userAttributes);
		
		/* get BswabePub from pubfile */
		byte[] pub_byte;
		BswabePub pub;
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);
		
		/* get BswabeMsk from mskfile */
		byte[] msk_byte;
		BswabeMsk msk;
		msk_byte = Common.suckFile(mskfile);
		msk = SerializeUtils.unserializeBswabeMsk(pub,msk_byte);
		
		/* get SearchMasterKey from kmkfile */
		byte[] kmk_byte;
		SearchMasterKey kmk;
		kmk_byte = Common.suckFile(kmkfile);
		kmk = SerializeUtils.unserialSearchMasterKey(pub,kmk_byte);
		
		/* get BswabePrv from prvfile */
		byte[] prv_byte;
		BswabePrv prv;
		prv_byte = Common.suckFile(prvfile);
		prv = SerializeUtils.unserializeBswabePrv(pub,prv_byte);
		
		Pairing pairing = pub.pairing;
		Element A_Uid = pairing.getZr().newRandomElement().getImmutable();
		Element expo = pairing.getZr().newElement();
		expo = kmk.K_mk.duplicate();
		expo.div(A_Uid);
		Element B_Uid = pairing.getG1().newElement();
		B_Uid = msk.g.duplicate();
		B_Uid.powZn(expo);
		B_Uid.getImmutable();
		
		
		// (Uid, A_Uid, SK)用UserTuple的方次存起來
		UserTuple user = new UserTuple();
		user.Uid = Uid;
		user.A_Uid = pairing.getZr().newElement();
		user.A_Uid = A_Uid.duplicate();
		user.g_s_faA = pairing.getG1().newElement();
		user.g_s_faA = prv.g_s_faA.duplicate();
		user.h_s1_a = pairing.getG2().newElement();
		user.h_s1_a = prv.h_s1_a.duplicate();
		user.fxALength = prv.fxALength;
		user.fxA = new Element[user.fxALength];
		for(int i=0; i<user.fxALength; i++){
			user.fxA[i] = pairing.getZr().newElement();
			user.fxA[i] = prv.fxA[i].duplicate();
		}
		/* store UserTuple into userfile */
		byte[] user_byte;
		user_byte = SerializeUtils.serializeUserTuple(user);
		Common.spitFile(userfile, user_byte);
		
		// (Uid, B_Uid)用存excel的方式存起來
		// 第一格存Uid
		// 第二格存B_Uid轉byte[]之後的長度
		// 第三格開始存byte[]
		String ProxyTable = "file/ProxyServer/ProxyTable.xls";
		File file = new File(ProxyTable);
		WritableWorkbook book;
		WritableSheet sheet;
		int index = 0;
		// 先判斷有沒有這個表格
		if(file.exists()){
			Workbook wb = Workbook.getWorkbook(new File(ProxyTable));
			book = Workbook.createWorkbook(new File(ProxyTable),wb);
			sheet = book.getSheet(0);
			// 先找到有空格的位置
			boolean emptyfound = false;
			while(!emptyfound){
				Cell cell = sheet.getCell(0, index);
				if(cell.getType()==CellType.EMPTY){
					emptyfound = true;
				}else{
					index++;
				}
			}
		}else{
			// 如果是第一次 要創建檔案並存入第一筆資料
			book = Workbook.createWorkbook(new File(ProxyTable));
			sheet = book.createSheet("UserData",0);
		}
		// 找到之後
		// Uid
		jxl.write.Number number = new jxl.write.Number(0,index,Uid);
		sheet.addCell(number);
		// B_Uid.length
		Element a = B_Uid.duplicate();
		byte[] b = a.toBytes();
		int[] bint = new int[b.length];
		number = new jxl.write.Number(1,index,b.length);
		sheet.addCell(number);
		// B_Uid
		for(int i=0; i<b.length; i++){
			bint[i] = b[i];
			number = new jxl.write.Number(i+2,index,bint[i]);
			sheet.addCell(number);
		}
		book.write(); 
		book.close();
	}
	
	public static void UserRegister(String account, String password, String UID) throws BiffException, IOException, RowsExceededException, WriteException{
		String RegisterTable = "file/TrustAuthority/RegisterTable.xls";
		File RegisterFile = new File(RegisterTable);
		if(RegisterFile.exists()){
			Workbook wb = Workbook.getWorkbook(new File(RegisterTable));
			WritableWorkbook book = Workbook.createWorkbook(new File(RegisterTable),wb);
			WritableSheet sheet = book.getSheet(0);
			// 先找到有空格的位置
			int index = 0;
			boolean emptyfound = false;
			while(!emptyfound){
				Cell cell = sheet.getCell(0, index);
				if(cell.getType()==CellType.EMPTY){
					emptyfound = true;
				}else{
					index++;
				}
			}
			Label label = new Label(0,index,account);
			sheet.addCell(label);
			label = new Label(1,index,password);
			sheet.addCell(label);
			label = new Label(2,index,UID);
			sheet.addCell(label);
			
			book.write();
			book.close();
		}else{
			JOptionPane.showMessageDialog(null, "Can't find Register File", "Error", JOptionPane.INFORMATION_MESSAGE );
		}
	}
	
	public static void UserRevocationRegister(String account) throws BiffException, IOException, WriteException{
		String RegisterTable = "file/TrustAuthority/RegisterTable.xls";
		File file = new File(RegisterTable);
		WritableWorkbook book;
		WritableSheet sheet;
		if(file.exists()){
			Workbook wb = Workbook.getWorkbook(new File(RegisterTable));
			book = Workbook.createWorkbook(new File(RegisterTable),wb);
			sheet = book.getSheet(0);
			boolean accountfound = false;
			int index = 0;
			while(!accountfound){
				Cell cell = sheet.getCell(0, index);
				if(cell.getType()==CellType.EMPTY){
					System.out.println("Can't find account");
					accountfound = true;
				}else{
					String result = cell.getContents();
					if(result.equals(account)){
						sheet.removeRow(index);
						accountfound = true;
					}else{
						index++;
					}
				}
			}
			
			book.write(); 
			book.close();
		}else{
			System.out.println("account doesn't exist");
		}
	}
	
	public static void UserRevocation(int Uid) throws BiffException, IOException, WriteException{
		String ProxyTable = "file/ProxyServer/ProxyTable.xls";
		File file = new File(ProxyTable);
		WritableWorkbook book;
		WritableSheet sheet;
		if(file.exists()){
			Workbook wb = Workbook.getWorkbook(new File(ProxyTable));
			book = Workbook.createWorkbook(new File(ProxyTable),wb);
			sheet = book.getSheet(0);
			boolean uidfound = false;
			int index = 0;
			while(!uidfound){
				Cell cell = sheet.getCell(0, index);
				if(cell.getType()==CellType.EMPTY){
					System.out.println("Can't find Uid");
					uidfound = true;
				}else{
					String result = cell.getContents();
					int resnumber = Integer.parseInt(result);
					if(resnumber==Uid){
						sheet.removeRow(index);
						uidfound = true;
						String userdir = "file/User/"+Uid+"/";
						DeleteFolder.delFolder(userdir);
						File udir = new File(userdir);
						if(udir.exists()){
							udir.delete();
						}
					}else{
						index++;
					}
				}
			}
			book.write(); 
			book.close();
		}else{
			System.out.println("file doesn't exist");
		}
	}
}
