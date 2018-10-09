package SEFunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import bswabe.BswabeCph;
import bswabe.BswabePub;
import bswabe.PolynomialOperate;
import bswabe.SerializeUtils;
import cscpabe.AESCoder;
import cscpabe.Common;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class CloudServer {
	public static void getUpload(String pubfile,String uploaddir,String cloudserverdir, 
			String keywordindex, String encname, String cphname
			) throws IOException, RowsExceededException, WriteException, BiffException{
		String keywordfile = uploaddir+keywordindex;
		String encfile = uploaddir+encname;
		String cphfile = uploaddir+cphname;
		String[] token = encname.split(".cpabe");
		// ���P�_���S���o���ɮצs�b
		String fidfile = cloudserverdir+"Fid.txt";
		File f = new File(fidfile);
		int fid = 0;
		if(f.exists()){
			FileReader fr = new FileReader(fidfile);
			BufferedReader br = new BufferedReader(fr);
			String fidstring = br.readLine();
			fr.close();
			fid = Integer.parseInt(fidstring);
		}
		fid++;	// fid �q1�}�l
		FileWriter fw = new FileWriter(fidfile);
		String fids = Integer.toString(fid);
		fw.write(fids);
		fw.flush();
		fw.close();
		// �o�Ǧ�m���᳣�n�H���ɮצW�٤��P�ӧ���
		// �����ɮ��ಾ��m
		File enc = new File(encfile);
		String toencs = cloudserverdir + "file/" + fid + ".cpabe";
		File toenc = new File(toencs);
		enc.renameTo(toenc);
		// ��cph�ಾ��m
		File cph = new File(cphfile);		
		String tocphs = cloudserverdir + "cph/" + fid + "_file";
		File tocph = new File(tocphs);
		cph.renameTo(tocph);
		
		// ���U�ӧ�keywordŪ�X�ӥ[�Jkeyword���
		
		/* get BswabePub from pubfile */
		byte[] pub_byte;
		BswabePub pub;
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);
		
		// Ūkeyword
		byte[] keyword_byte;
		KeywordIndex keyword;
		keyword_byte = Common.suckFile(keywordfile);
		keyword = SerializeUtils.unserializeKeywordIndex(pub, keyword_byte);
		
		String KeywordTable = cloudserverdir+"KeywordTable.xls";
		File file = new File(KeywordTable);
		WritableWorkbook book;
		WritableSheet sheet;
		int index = 0;
		if(file.exists()){
			// ������٨S����檺�a��
			Workbook wb = Workbook.getWorkbook(new File(KeywordTable));
			book = Workbook.createWorkbook(new File(KeywordTable),wb);
			sheet = book.getSheet(0);
			// ����즳�Ů檺��m
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
			// �p�G�Ĥ@���ت��
			book = Workbook.createWorkbook(new File(KeywordTable));
			sheet = book.createSheet("FileData",0);
		}
		// �Ĥ@��s (�ѧO�X)1, Fid, filepath, cphpath
		// 1
		jxl.write.Number number = new jxl.write.Number(0,index,1);
		sheet.addCell(number);
		// Fid
		number = new jxl.write.Number(1,index,fid);
		sheet.addCell(number);
		// filepath
		Label label = new Label(2,index,toencs);
		sheet.addCell(label);
		// cphpath
		label = new Label(3,index,tocphs);
		sheet.addCell(label);
		// filename
		label = new Label(4,index,token[0]);
		sheet.addCell(label);
				
		// �ĤG��}�l�s RandomNumber[i], Riki[i].length, Riki[i]...
		// 2, 3 ����s
		// 2, RandomNumber[i]
		// 3, Riki[i].length, Riki[i]...
		for(int i=0; i<keyword.RandomNumberLength; i++){
			// 2
			number = new jxl.write.Number(0,index+i*2+1,2);
			sheet.addCell(number);
			// RandomNumber[i].length
			Element a = keyword.RandomNumber[i].duplicate();
			byte[] b = a.toBytes();
			int[] bint = new int[b.length];
			number = new jxl.write.Number(1,index+i*2+1,b.length);
			sheet.addCell(number);
			// RandomNumber[i]
			for(int j=0; j<b.length; j++){
				bint[j] = b[j];
				number = new jxl.write.Number(j+2,index+i*2+1,bint[j]);
				sheet.addCell(number);
			}
			
			// 3
			number = new jxl.write.Number(0,index+i*2+2,3);
			sheet.addCell(number);
			// Riki[i].length
			number = new jxl.write.Number(1,index+i*2+2,keyword.Riki[i].length);
			sheet.addCell(number);
			// Riki[i]
			int[] rint = new int[keyword.Riki[i].length];
			for(int j=0; j<keyword.Riki[i].length; j++){
			rint[j] = keyword.Riki[i][j];
			number = new jxl.write.Number(j+2,index+i*2+2,rint[j]);
			sheet.addCell(number);
			}
		}
		
		// �g������4����
		index = index + 1 + 2*keyword.RandomNumberLength;
		number = new jxl.write.Number(0,index,4);
		sheet.addCell(number);
		
		// �̫��keywordfile�R��
		File filetodelete = new File(keywordfile);
		filetodelete.delete();
		
		book.write(); 
		book.close();
	}
	public static void Search(String pubfile, Element k, Element[] fxA, String decdir
			) throws Exception{
		// �ثe������2014��3�Ӻt��k
		// �ҥH���U�ӬO�����j�M
		// ���o��fid���Ȭݦ��X���ɮ�
		// �M��iKeywordTable���ѧO�X1�P�_�@���ɮת��}�l
		// �⦳���keyword���ΰ}�C��(fid),filepath�s�U��
		// ��user����ѱK��ۤv��
		// �n���T�{�v��
				
		/* get BswabePub from pubfile */
		byte[] pub_byte;
		BswabePub pub;
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);
		Pairing pairing = pub.pairing;
		
		String KeywordTable = "file/CloudServer/KeywordTable.xls";
		Workbook wb = Workbook.getWorkbook(new File(KeywordTable));
		WritableWorkbook book = Workbook.createWorkbook(new File(KeywordTable),wb);
		Sheet sheet = book.getSheet(0);
				
		ArrayList<String> namelist = new ArrayList<String>();
		ArrayList<String> filepathlist = new ArrayList<String>();
				
		boolean emptyfound = false;
		int index = 0;
		while(!emptyfound){
			Cell cell = sheet.getCell(0, index);
			if(cell.getType()==CellType.EMPTY){
				emptyfound = true;
			}else{
				String result = cell.getContents(); 
				if(result.equals("1")){
					int wherestart = index;
					cell = sheet.getCell(3,index);
					String cphfile = cell.getContents();
					/* get BswabeCph from cphfile */
					byte[] cph_byte;
					BswabeCph cph;
					cph_byte = Common.suckFile(cphfile);
					cph = SerializeUtils.bswabeCphUnserialize(pub, cph_byte);
					boolean verification = PolynomialOperate.AttributeVerification(pub, cph.fxP, fxA);
					if(verification){
						// �p�G���v������
						// ���T�{�o���ɮצb��椤���d��
						int checkindex = 1;
						boolean checkone = true;
						while(checkone){
							cell = sheet.getCell(0,index+checkindex);
							result = cell.getContents();
							if(cell.getType()==CellType.EMPTY || result.equals("4")){
								checkone = false;
							}else{
								checkindex++;
							}
						}
						checkindex = index+checkindex;
						
						boolean check = true;
						while(check){
						if(index>=checkindex-1){
							check = false;
						}else{
							// search
							// 2, RandomNumber[i]
							cell = sheet.getCell(1,index+1);
							result = cell.getContents();
							int randomlength = Integer.parseInt(result);
							int[] bint = new int[randomlength];
							byte[] b = new byte[randomlength];
							for(int i=0; i<randomlength; i++){
								cell = sheet.getCell(i+2, index+1);
								result = cell.getContents();
								bint[i] = Integer.parseInt(result);
								b[i] = (byte) bint[i];
							}
							Element e = pairing.getZr().newElement();
							e.setFromBytes(b);
							
							// 3, Riki[i]
							cell = sheet.getCell(1,index+2);
							result = cell.getContents();
							int rikilength = Integer.parseInt(result);
							int[] bint2 = new int[rikilength];
							byte[] b2 = new byte[rikilength];
							for(int i=0; i<rikilength; i++){
								cell = sheet.getCell(i+2, index+2);
								result = cell.getContents();
								bint2[i] = Integer.parseInt(result);
								b2[i] = (byte) bint2[i];
							}
							Element e2 = pairing.getZr().newElement();
							e2.setFromBytes(b2);
									
							// ��RandomNumeber�i��[�K
							byte[] Rik;
							Rik = AESCoder.encrypt(k.toBytes(), b);
							Element e3 = pairing.getZr().newElement();
							e3.setFromBytes(Rik);
							
							if(e3.isEqual(e2)){
								cell = sheet.getCell(4,wherestart);
								result = cell.getContents();
								namelist.add(result);
								cell = sheet.getCell(2,wherestart);
								result = cell.getContents();
								filepathlist.add(result);
							}
							index = index+2;
							// ��check�]�����ɭԤU�@�ӷ|�O1�ΪŮ�
						}
					}
				}
			}
			// �b�o��index++�|��U�@��
			index++;
		}// end �S���empty������
	}// end emptyfound
	book.write(); 
	book.close();
	
	String SearchResultName = decdir+"SearchResultName.txt";
	String SearchResultFilepath= decdir+"SearchResultFilepath.txt";
	String SearchResultNumber = decdir+"SearchResultNumber.txt";
	
	ArrayListToTxT(namelist,SearchResultName);
	ArrayListToTxT(filepathlist,SearchResultFilepath);
	IntToTxT(namelist.size(),SearchResultNumber);
	
	}
	
	public static void AndOrSearch(String decdir, String decdir2, 
			int AndOr) throws Exception{
		// AndOr = 1(And)  AndOr = 2(Or)
		
		String SearchResultFilepath= decdir+"SearchResultFilepath.txt";
		String SearchResultName = decdir + "SearchResultName.txt";
		String SearchResultNumber = decdir + "SearchResultNumber.txt";
		
		String[] path1 = getFile(SearchResultFilepath,SearchResultNumber);
		String[] name1 = getFile(SearchResultName,SearchResultNumber);
		
		String SearchResultFilepath2= decdir2+"SearchResultFilepath.txt";
		String SearchResultName2 = decdir2 + "SearchResultName.txt";
		String SearchResultNumber2 = decdir2 + "SearchResultNumber.txt";
		
		String[] path2 = getFile(SearchResultFilepath2,SearchResultNumber2);
		String[] name2 = getFile(SearchResultName2,SearchResultNumber2);
		
		ArrayList<String> PathList = new ArrayList<String>();
		ArrayList<String> NameList = new ArrayList<String>();
		
		if(AndOr==1){	// And
			for(int i=0; i<path1.length; i++){
				boolean found = false;
				for(int j=0; j<path2.length; j++){
					if(!found){
						if(path1[i].equals(path2[j])){
							PathList.add(path1[i]);
							NameList.add(name1[i]);
							found = true;
						}
					}
				}
			}
		}else{	// Or
			for(int i=0; i<path1.length; i++){
				PathList.add(path1[i]);
				NameList.add(name1[i]);
			}
			for(int i=0; i<path2.length; i++){
				boolean found = false;
				for(int j=0; j<path1.length; j++){
					if(path2[i].equals(path1[j])){
						found = true;
					}
				}
				if(!found){
					PathList.add(path2[i]);
					NameList.add(name2[i]);
				}
			}
		}
		
		// �s�^�h
		ArrayListToTxT(NameList,SearchResultName);
		ArrayListToTxT(PathList,SearchResultFilepath);
		IntToTxT(PathList.size(),SearchResultNumber);
		
	}
	
	public static void ArrayListToTxT(ArrayList<String> array, String path) throws IOException{
		FileWriter fw = new FileWriter(path);
		for(int i=0; i<array.size(); i++){
			String value = array.get(i);
			fw.write(value);
			fw.write("\r\n");
		}
		fw.flush();
		fw.close();
	}
	
	public static void IntToTxT(int number, String path) throws IOException{
		String NumberString = Integer.toString(number);
		FileWriter fwnumber = new FileWriter(path);
		fwnumber.write(NumberString);
		fwnumber.write("\r\n");
		fwnumber.flush();
		fwnumber.close();
	}
	
	public static String[] getFile(String SearchResultFile, 
			String SearchResultNumber) throws IOException{
		FileReader fr = new FileReader(SearchResultFile);
		BufferedReader br = new BufferedReader(fr);
		FileReader fr2 = new FileReader(SearchResultNumber);
		BufferedReader br2 = new BufferedReader(fr2);
		
		String snumber = br2.readLine();
		int filesize = Integer.parseInt(snumber);
		String[] result = new String[filesize];
		
		int index = 0;
		while(br.ready()){
			result[index] = br.readLine();
			index++;
		}
		fr.close();
		fr2.close();
		
		return result;
	}
	
}
