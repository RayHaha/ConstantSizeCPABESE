package SEFunction;



import java.io.File;

import bswabe.BswabePub;
import bswabe.SerializeUtils;
import cscpabe.AESCoder;
import cscpabe.Common;
import cscpabe.csCpabe;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class DataOwner {
	public static void KeywordIndexBuildingDO(String pubfile,String userfile,
			String keywordfile,String[] W) throws Exception{
		
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
		
		Pairing pairing = pub.pairing;
		
		int l = W.length;
		Element[] Ti = new Element[l];
		Element[] ri = new Element[l];
		for(int i=0; i<Ti.length; i++){
			Ti[i] = pairing.getG1().newElement();
			ri[i] = pairing.getZr().newRandomElement();
			byte[] w = W[i].getBytes();
			Ti[i] = pairing.getG1().newElement().setFromHash(w, 0, w.length); 
			Ti[i].powZn(ri[i]);
		}
		Element[] Vi = ProxyServer.KeywordIndexBuildingPS(pubfile, user.Uid, Ti);
		Element[] ki = new Element[l];
		Element[] expo = new Element[l];
		for(int i=0; i<l; i++){
			expo[i] = pairing.getZr().newElement();
			expo[i] = user.A_Uid.duplicate();
			expo[i].div(ri[i]);
			Vi[i].powZn(expo[i]);
			byte[] w = Vi[i].toBytes();
			ki[i] = pairing.getGT().newElement();
			ki[i] = pairing.getGT().newElement().setFromHash(w, 0, w.length); 
		}
		KeywordIndex keyword = new KeywordIndex();
		keyword.RandomNumber = new Element[l];
		keyword.RandomNumberLength = l;
		keyword.Riki = new byte[l][];
		for(int i=0; i<l; i++){
			keyword.RandomNumber[i] = pairing.getZr().newRandomElement();
			keyword.Riki[i] = AESCoder.encrypt(ki[i].toBytes(), keyword.RandomNumber[i].toBytes());
		}
		
		/* store KeywordIndex into keywordfile */
		byte[] keyword_byte;
		keyword_byte = SerializeUtils.serializeKeywordIndex(pub, keyword);
		Common.spitFile(keywordfile, keyword_byte);
	}
	
	public static void DataUpload(String pubfile, String userfile,
			String cloudserverdir,String dataownerdir,String inputpath ,
			String inputname,String encname,String cphname,String keywordindex,int[] accessStructure,
			String[] W) throws Exception{
		
		String encfile = dataownerdir+encname;
		String cphfile = dataownerdir+cphname;
		String keywordfile = dataownerdir+keywordindex;
		//¥[±K
		csCpabe cscpabe = new csCpabe();
		cscpabe.encrypt(pubfile, inputpath, encfile, cphfile, accessStructure);
		
		KeywordIndexBuildingDO(pubfile, userfile, keywordfile, W);
		
		File keyword = new File(keywordfile);
		File enc = new File(encfile);
		File cph = new File(cphfile);
		
		String uploaddir = cloudserverdir+"Upload/";
		String tokeywords = uploaddir+keywordindex;
		String toencs = uploaddir+encname;
		String tocphs = uploaddir+cphname;
		
		File tokeyword = new File(tokeywords);
		File toenc = new File(toencs);
		File tocph = new File(tocphs);
		
		keyword.renameTo(tokeyword);
		enc.renameTo(toenc);
		cph.renameTo(tocph);
		
		CloudServer.getUpload(pubfile,uploaddir,cloudserverdir,keywordindex,encname,cphname);
	}
}
