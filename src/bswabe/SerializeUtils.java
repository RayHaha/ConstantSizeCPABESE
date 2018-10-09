package bswabe;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.ArrayList;

import SEFunction.KeywordIndex;
import SEFunction.SearchMasterKey;
import SEFunction.UserTuple;

public class SerializeUtils {

	/* Method has been test okay */
	public static void serializeElement(ArrayList<Byte> arrlist, Element e) {
		byte[] arr_e = e.toBytes();
		serializeUint32(arrlist, arr_e.length);
		byteArrListAppend(arrlist, arr_e);
	}

	/* Method has been test okay */
	public static int unserializeElement(byte[] arr, int offset, Element e) {
		int len;
		int i;
		byte[] e_byte;

		len = unserializeUint32(arr, offset);
		e_byte = new byte[(int) len];
		offset += 4;
		for (i = 0; i < len; i++)
			e_byte[i] = arr[offset + i];
		e.setFromBytes(e_byte);

		return (int) (offset + len);
	}	

	public static void serializeString(ArrayList<Byte> arrlist, String s) {
		byte[] b = s.getBytes();
		serializeUint32(arrlist, b.length);
		byteArrListAppend(arrlist, b);
	}

	/*
	 * Usage:
	 * 
	 * StringBuffer sb = new StringBuffer("");
	 * 
	 * offset = unserializeString(arr, offset, sb);
	 * 
	 * String str = sb.substring(0);
	 */
	public static int unserializeString(byte[] arr, int offset, StringBuffer sb) {
		int i;
		int len;
		byte[] str_byte;
	
		len = unserializeUint32(arr, offset);
		offset += 4;
		str_byte = new byte[len];
		for (i = 0; i < len; i++)
			str_byte[i] = arr[offset + i];
	
		sb.append(new String(str_byte));
		return offset + len;
	}

	public static byte[] serializeBswabePub(BswabePub pub) {
		ArrayList<Byte> arrlist = new ArrayList<Byte>();

		// 把int用string的方式存
		String stringN = Integer.toString(pub.n);
		serializeString(arrlist, stringN);
		
		
		for(int i=0; i<pub.vi.length; i++){
			serializeElement(arrlist, pub.vi[i]);
		}
		serializeElement(arrlist, pub.h);
		for(int i=0; i<pub.hi.length; i++){
			serializeElement(arrlist, pub.hi[i]);
		}
		serializeElement(arrlist, pub.e_gh);
	
		return Byte_arr2byte_arr(arrlist);
	}

	public static BswabePub unserializeBswabePub(byte[] b) {
		BswabePub pub;
		int offset;
	
		pub = new BswabePub();
		offset = 0;
		
		pub.pairing = PairingFactory.getPairing("a.properties");
		Pairing pairing = pub.pairing;
	
		pub.h = pairing.getG2().newElement();
		pub.e_gh = pairing.getGT().newElement();
		
		//  把string讀出來之後再轉成int
		StringBuffer sb = new StringBuffer("");
		offset = unserializeString(b, offset, sb);
		String stringN = sb.substring(0);
		pub.n = Integer.parseInt(stringN);
		
		pub.vi = new Element[pub.n+1];
		pub.hi = new Element[pub.n+1];
		
		for(int i=0; i<pub.vi.length; i++){
			pub.vi[i] = pairing.getG1().newElement();
			offset = unserializeElement(b, offset, pub.vi[i]);
			pub.vi[i].getImmutable();
		}
		offset = unserializeElement(b, offset, pub.h);
		pub.h.getImmutable();
		for(int i=0; i<pub.hi.length; i++){
			pub.hi[i] = pairing.getG2().newElement();
			offset = unserializeElement(b, offset, pub.hi[i]);
			pub.hi[i].getImmutable();
		}
		offset = unserializeElement(b, offset, pub.e_gh);
		pub.e_gh.getImmutable();
	
		return pub;
	}

	/* Method has been test okay */
	public static byte[] serializeBswabeMsk(BswabeMsk msk) {
		ArrayList<Byte> arrlist = new ArrayList<Byte>();
	
		serializeElement(arrlist, msk.alpha);
		serializeElement(arrlist, msk.g);
	
		return Byte_arr2byte_arr(arrlist);
	}

	/* Method has been test okay */
	public static BswabeMsk unserializeBswabeMsk(BswabePub pub, byte[] b) {
		int offset = 0;
		BswabeMsk msk = new BswabeMsk();
	
		msk.alpha = pub.pairing.getZr().newElement();
		msk.g = pub.pairing.getG1().newElement();
	
		offset = unserializeElement(b, offset, msk.alpha);
		offset = unserializeElement(b, offset, msk.g);
		msk.alpha.getImmutable();
		msk.g.getImmutable();
	
		return msk;
	}
	
	public static byte[] serializeSearchMasterKey(SearchMasterKey smk){
		ArrayList<Byte> arrlist = new ArrayList<Byte>();
		serializeElement(arrlist, smk.K_mk);
		return Byte_arr2byte_arr(arrlist);
	}
	
	public static SearchMasterKey unserialSearchMasterKey(BswabePub pub, byte[] b){
		int offset = 0;
		SearchMasterKey smk = new SearchMasterKey();
		smk.K_mk = pub.pairing.getZr().newElement();
		offset = unserializeElement(b, offset, smk.K_mk);
		smk.K_mk.getImmutable();
		return smk;
	}
	
	public static byte[] serializeKeywordIndex(BswabePub pub, KeywordIndex keyword){
		ArrayList<Byte> arrlist = new ArrayList<Byte>();
		
		String stringL = Integer.toString(keyword.RandomNumberLength);
		serializeString(arrlist, stringL);
		for(int i=0; i<keyword.RandomNumberLength; i++){
			serializeElement(arrlist, keyword.RandomNumber[i]);
		}
		// byte[] 用 element 的方式存起來
		for(int i=0; i<keyword.RandomNumberLength; i++){
			Element e = pub.pairing.getZr().newElement();
			e.setFromBytes(keyword.Riki[i]);
			serializeElement(arrlist, e);
		}
		return Byte_arr2byte_arr(arrlist);
	}
	
	public static KeywordIndex unserializeKeywordIndex(BswabePub pub, byte[] b){
		int offset = 0;
		KeywordIndex keyword = new KeywordIndex();
		
		StringBuffer sb = new StringBuffer("");
		offset = unserializeString(b, offset, sb);
		String stringL = sb.substring(0);
		keyword.RandomNumberLength = Integer.parseInt(stringL);
		
		keyword.RandomNumber = new Element[keyword.RandomNumberLength];
		keyword.Riki = new byte[keyword.RandomNumberLength][];
		
		for(int i=0; i<keyword.RandomNumberLength; i++){
			keyword.RandomNumber[i] = pub.pairing.getZr().newElement();
			offset = unserializeElement(b, offset, keyword.RandomNumber[i]);
		}
		
		for(int i=0; i<keyword.RandomNumberLength; i++){
			Element e = pub.pairing.getZr().newElement();
			offset = unserializeElement(b, offset, e);
			keyword.Riki[i] = e.toBytes();
		}
		
		return keyword;
	}
	
	public static byte[] serializeUserTuple(UserTuple user){
		ArrayList<Byte> arrlist = new ArrayList<Byte>();
		String stringL = Integer.toString(user.Uid);
		serializeString(arrlist, stringL);
		serializeElement(arrlist, user.A_Uid);
		serializeElement(arrlist, user.g_s_faA);
		serializeElement(arrlist, user.h_s1_a);
		
		stringL = Integer.toString(user.fxALength);
		serializeString(arrlist, stringL);
		
		for(int i=0; i<user.fxA.length; i++){
			serializeElement(arrlist, user.fxA[i]);
		}
		return Byte_arr2byte_arr(arrlist);
	}
	
	public static UserTuple unserializeUserTuple(BswabePub pub, byte[] b){
		int offset = 0;
		UserTuple user = new UserTuple();
		
		StringBuffer sb = new StringBuffer("");
		offset = unserializeString(b, offset, sb);
		String stringL = sb.substring(0);
		user.Uid = Integer.parseInt(stringL);
		
		user.A_Uid = pub.pairing.getZr().newElement();
		user.g_s_faA = pub.pairing.getG1().newElement();
		user.h_s1_a = pub.pairing.getG2().newElement();
		
		offset = unserializeElement(b, offset, user.A_Uid);
		offset = unserializeElement(b, offset, user.g_s_faA);
		offset = unserializeElement(b, offset, user.h_s1_a);
		user.g_s_faA.getImmutable();
		user.h_s1_a.getImmutable();
		
		sb = new StringBuffer("");
		offset = unserializeString(b, offset, sb);
		stringL = sb.substring(0);
		user.fxALength = Integer.parseInt(stringL);
		
		user.fxA = new Element[user.fxALength];
		for(int i=0; i<user.fxALength; i++){
			user.fxA[i] = pub.pairing.getZr().newElement();
			offset = unserializeElement(b, offset, user.fxA[i]);
			user.fxA[i].getImmutable();
		}
		return user;
	}

	/* Method has been test okay */
	public static byte[] serializeBswabePrv(BswabePrv prv) {
		ArrayList<Byte> arrlist = new ArrayList<Byte>();
		
		serializeElement(arrlist, prv.g_s_faA);
		serializeElement(arrlist, prv.h_s1_a);
		
		String stringL = Integer.toString(prv.fxALength);
		serializeString(arrlist, stringL);
		
		for(int i=0; i<prv.fxA.length; i++){
			serializeElement(arrlist, prv.fxA[i]);
		}
		return Byte_arr2byte_arr(arrlist);
	}

	/* Method has been test okay */
	public static BswabePrv unserializeBswabePrv(BswabePub pub, byte[] b) {
		int offset = 0;
		BswabePrv prv = new BswabePrv();
	
		prv.g_s_faA = pub.pairing.getG1().newElement();
		prv.h_s1_a = pub.pairing.getG2().newElement();
	
		offset = unserializeElement(b, offset, prv.g_s_faA);
		offset = unserializeElement(b, offset, prv.h_s1_a);
		prv.g_s_faA.getImmutable();
		prv.h_s1_a.getImmutable();
		
		StringBuffer sb = new StringBuffer("");
		offset = unserializeString(b, offset, sb);
		String stringL = sb.substring(0);
		prv.fxALength = Integer.parseInt(stringL);
		
		prv.fxA = new Element[prv.fxALength];
		for(int i=0; i<prv.fxALength; i++){
			prv.fxA[i] = pub.pairing.getZr().newElement();
			offset = unserializeElement(b, offset, prv.fxA[i]);
			prv.fxA[i].getImmutable();
		}
		return prv;
	}

	public static byte[] bswabeCphSerialize(BswabePub pub, BswabeCph cph) {
		ArrayList<Byte> arrlist = new ArrayList<Byte>();
		SerializeUtils.serializeElement(arrlist, cph.C1);
		
		String stringL = Integer.toString(cph.C2Length);
		serializeString(arrlist, stringL);
		for(int i=0; i<cph.C2.length; i++){
			serializeElement(arrlist, cph.C2[i]);
		}
		
		// byte 用 element的形式存
		Element e3 = pub.pairing.getZr().newElement();
		e3.setFromBytes(cph.C3);
		serializeElement(arrlist, e3);
		
		Element e4 = pub.pairing.getZr().newElement();
		e4.setFromBytes(cph.C4);
		serializeElement(arrlist, e4);
		
		stringL = Integer.toString(cph.fxPLength);
		serializeString(arrlist, stringL);
		
		for(int i=0; i<cph.fxP.length; i++){
			serializeElement(arrlist, cph.fxP[i]);
		}

		return Byte_arr2byte_arr(arrlist);
	}

	public static BswabeCph bswabeCphUnserialize(BswabePub pub, byte[] b) {
		BswabeCph cph = new BswabeCph();
		int offset = 0;
		
		cph.C1 = pub.pairing.getG2().newElement();
		offset = SerializeUtils.unserializeElement(b, offset, cph.C1);
		
		StringBuffer sb = new StringBuffer("");
		offset = unserializeString(b, offset, sb);
		String stringL = sb.substring(0);
		cph.C2Length = Integer.parseInt(stringL);
		
		cph.C2 = new Element[cph.C2Length];
		for(int i=0; i<cph.C2Length; i++){
			cph.C2[i] = pub.pairing.getG1().newElement();
			offset = unserializeElement(b, offset, cph.C2[i]);
			cph.C2[i].getImmutable();
		}
		
		Element e3 = pub.pairing.getZr().newElement();
		offset = unserializeElement(b, offset, e3);
		cph.C3 = e3.toBytes();
		
		Element e4 = pub.pairing.getZr().newElement();
		offset = unserializeElement(b, offset, e4);
		cph.C4 = e4.toBytes();
		
		sb = new StringBuffer("");
		offset = unserializeString(b, offset, sb);
		stringL = sb.substring(0);
		cph.fxPLength = Integer.parseInt(stringL);
		
		cph.fxP = new Element[cph.fxPLength];
		for(int i=0; i<cph.fxPLength; i++){
			cph.fxP[i] = pub.pairing.getZr().newElement();
			offset = unserializeElement(b, offset, cph.fxP[i]);
			cph.fxP[i].getImmutable();
		}
		return cph;
	}

	/* Method has been test okay */
	/* potential problem: the number to be serialize is less than 2^31 */
	private static void serializeUint32(ArrayList<Byte> arrlist, int k) {
		int i;
		byte b;
	
		for (i = 3; i >= 0; i--) {
			b = (byte) ((k & (0x000000ff << (i * 8))) >> (i * 8));
			arrlist.add(Byte.valueOf(b));
		}
	}

	/*
	 * Usage:
	 * 
	 * You have to do offset+=4 after call this method
	 */
	/* Method has been test okay */
	private static int unserializeUint32(byte[] arr, int offset) {
		int i;
		int r = 0;
	
		for (i = 3; i >= 0; i--)
			r |= (byte2int(arr[offset++])) << (i * 8);
		return r;
	}

	/* private static void serializePolicy(ArrayList<Byte> arrlist, BswabePolicy p) {
		serializeUint32(arrlist, p.k);
	
		if (p.children == null || p.children.length == 0) {
			serializeUint32(arrlist, 0);
			serializeString(arrlist, p.attr);
			serializeElement(arrlist, p.c);
			serializeElement(arrlist, p.cp);
		} else {
			serializeUint32(arrlist, p.children.length);
			for (int i = 0; i < p.children.length; i++)
				serializePolicy(arrlist, p.children[i]);
		}
	} */

	/* private static BswabePolicy unserializePolicy(BswabePub pub, byte[] arr,
			int[] offset) {
		int i;
		int n;
		BswabePolicy p = new BswabePolicy();
		p.k = unserializeUint32(arr, offset[0]);
		offset[0] += 4;
		p.attr = null;
	
		// children
		n = unserializeUint32(arr, offset[0]);
		offset[0] += 4;
		if (n == 0) {
			p.children = null;
	
			StringBuffer sb = new StringBuffer("");
			offset[0] = unserializeString(arr, offset[0], sb);
			p.attr = sb.substring(0);
	
			p.c = pub.p.getG1().newElement();
			p.cp = pub.p.getG1().newElement();
	
			offset[0] = unserializeElement(arr, offset[0], p.c);
			offset[0] = unserializeElement(arr, offset[0], p.cp);
		} else {
			p.children = new BswabePolicy[n];
			for (i = 0; i < n; i++)
				p.children[i] = unserializePolicy(pub, arr, offset);
		}
	
		return p;
	} */

	private static int byte2int(byte b) {
		if (b >= 0)
			return b;
		return (256 + b);
	}

	private static void byteArrListAppend(ArrayList<Byte> arrlist, byte[] b) {
		int len = b.length;
		for (int i = 0; i < len; i++)
			arrlist.add(Byte.valueOf(b[i]));
	}

	private static byte[] Byte_arr2byte_arr(ArrayList<Byte> B) {
		int len = B.size();
		byte[] b = new byte[len];
	
		for (int i = 0; i < len; i++)
			b[i] = B.get(i).byteValue();
	
		return b;
	}

}
