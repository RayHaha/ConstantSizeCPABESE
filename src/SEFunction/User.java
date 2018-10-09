package SEFunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import bswabe.BswabePrv;
import bswabe.BswabePub;
import bswabe.SerializeUtils;
import cscpabe.Common;
import cscpabe.csCpabe;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class User {
	public static void TrapdoorGenUser(String pubfile,String userfile,String prvfile,String userdir,String w) throws Exception{
		/* get BswabePub from pubfile */
		byte[] pub_byte;
		BswabePub pub;
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);
		
		/* get UserTuple from userfile */
		byte[] user_byte;
		UserTuple user;
		user_byte = Common.suckFile(userfile);
		user = SerializeUtils.unserializeUserTuple(pub, user_byte);
		
		/* get BswabePrv from prvfile */
		byte[] prv_byte;
		BswabePrv prv;
		prv_byte = Common.suckFile(prvfile);
		prv = SerializeUtils.unserializeBswabePrv(pub,prv_byte);
		
		Pairing pairing = pub.pairing;
		Element Qw = pairing.getG1().newElement();
		byte[] b = w.getBytes();
		Qw = pairing.getG1().newElement().setFromHash(b, 0, b.length); 
		Qw.powZn(user.A_Uid);
		
		Element k = ProxyServer.TrapdoorGenPS(pubfile, user.Uid, Qw);
		
		CloudServer.Search(pubfile, k, prv.fxA, userdir);
	}
	
	public static void UserAndOrSearch(ArrayList<String> KeywordList, ArrayList<Integer> AndOrList,
			String pubfile,String userfile,String prvfile,String userdir) throws Exception{
		
		// 先拿到第一個keyword
		String w = KeywordList.get(0);
		TrapdoorGenUser(pubfile,userfile,prvfile,userdir,w);
		
		// 後續處理
		// keyword從1開始
		int theSize = AndOrList.size();
		
		String userdir2 = userdir + "search/";
		File U2File = new File(userdir2);
		if(!U2File.exists()){
			U2File.mkdirs();
		}
		
		for(int i=0; i<theSize; i++){
			w = KeywordList.get(i+1);
			TrapdoorGenUser(pubfile,userfile,prvfile,userdir2,w);
			CloudServer.AndOrSearch(userdir, userdir2, AndOrList.get(i));
		}
		
	}
	
	public static void FileDecryption(String pubfile, String prvfile,
			String userdir) throws Exception{
		csCpabe cscpabe = new csCpabe();
		String SearchResultFilepath= userdir+"SearchResultFilepath.txt";
		FileReader fr = new FileReader(SearchResultFilepath);
		BufferedReader br = new BufferedReader(fr);
		String SearchResultName = userdir + "SearchResultName.txt";
		FileReader fr2 = new FileReader(SearchResultName);
		BufferedReader br2 = new BufferedReader(fr2);
		while(br.ready() && br2.ready()){
			String path = br.readLine();
			String name  = br2.readLine();
			cscpabe.decrypt(pubfile, prvfile, path, userdir+name);
		}
		fr.close();
		fr2.close();
	}
	
	public static void FileDecryptionConfirm(String pubfile, String prvfile,
			String encpath, String decpath) throws Exception{
		csCpabe cscpabe = new csCpabe();
		cscpabe.decrypt(pubfile, prvfile, encpath, decpath);
	}
	
	public static boolean CheckSearch(String userdir) throws Exception{
		String SearchResultName = userdir + "SearchResultName.txt";
		FileReader fr = new FileReader(SearchResultName);
		BufferedReader br = new BufferedReader(fr);
		boolean check = false;
		while(br.ready()){
			String name = br.readLine();
			if(!name.equals("")){
				check = true;
			}
		}
		fr.close();
		return check;
	}
}
