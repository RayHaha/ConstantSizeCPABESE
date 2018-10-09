package SEFunction;

import java.io.File;
import java.io.IOException;

import bswabe.BswabePub;
import bswabe.SerializeUtils;
import cscpabe.Common;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ProxyServer {
	public static Element[] KeywordIndexBuildingPS(String pubfile, int Uid, Element[] Ti
			) throws BiffException, IOException{
		
		/* get BswabePub from pubfile */
		byte[] pub_byte;
		BswabePub pub;
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);
		
		Pairing pairing = pub.pairing;
		
		Element B_Uid = pairing.getG1().newElement();
		B_Uid = FindBUid(pubfile, Uid);
		
		int TiLength = Ti.length;
		Element[] Vi = new Element[TiLength];
		for(int i=0; i<TiLength; i++){
			Vi[i] = pairing.getGT().newElement();
			Vi[i] = pairing.pairing(Ti[i], B_Uid);
		}
		return Vi;
	}
	
	public static Element TrapdoorGenPS(String pubfile, int Uid, Element Qw
			) throws IOException, BiffException{
		
		/* get BswabePub from pubfile */
		byte[] pub_byte;
		BswabePub pub;
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);
		
		Pairing pairing = pub.pairing;
		
		Element B_Uid = pairing.getG1().newElement();
		B_Uid = FindBUid(pubfile, Uid);
		
		Element temp = pairing.pairing(Qw, B_Uid);
		byte[] btemp = temp.toBytes();
		Element k = pairing.getGT().newElement().setFromHash(btemp, 0, btemp.length); 
		
		return k;
	}
	
	public static Element FindBUid(String pubfile, int Uid) throws IOException, BiffException{
		/* get BswabePub from pubfile */
		byte[] pub_byte;
		BswabePub pub;
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);
		
		Pairing pairing = pub.pairing;
		
		String ProxyTable = "file/ProxyServer/ProxyTable.xls";
		Workbook book = Workbook.getWorkbook(new File(ProxyTable));
		//獲得第一個工作表對象 
		Sheet sheet=book.getSheet(0);
		boolean uidfound = false;
		int index = 0;
		while(!uidfound){
			Cell cell = sheet.getCell(0, index);
			String result = cell.getContents();
			int res = Integer.parseInt(result);
			if(res==Uid){
				uidfound = true;
			}else{
				index++;
			}
		}
		// 找到uid的位置之後把B_Uid讀出來
		Cell cell = sheet.getCell(1, index);
		String result = cell.getContents();
		int buid_length = Integer.parseInt(result);
		byte[] b = new byte[buid_length];
		int[] bint = new int[buid_length];
		for(int i=0; i<buid_length; i++){
			cell = sheet.getCell(i+2, index);
			result = cell.getContents();
			bint[i] = Integer.parseInt(result);
			b[i] = (byte) bint[i];
		}
		Element B_Uid = pairing.getG1().newElement();
		B_Uid.setFromBytes(b);
		
		return B_Uid;
	}
}
