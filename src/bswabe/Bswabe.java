package bswabe;


import java.io.UnsupportedEncodingException;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class Bswabe {
	
	public static void Setup(BswabePub pub, BswabeMsk msk, int n){
		System.out.println("Bswabe_Setup");
		pub.pairing = PairingFactory.getPairing("a.properties");	// Type a properties
		Pairing pairing = pub.pairing;
		
		msk.g = pairing.getG1().newRandomElement().getImmutable();
		pub.h = pairing.getG2().newRandomElement().getImmutable();
		
		pub.e_gh = pairing.getGT().newElement();
		pub.e_gh = pairing.pairing(msk.g, pub.h);
		
		msk.alpha = pairing.getZr().newRandomElement().getImmutable();
		pub.n = n;
		
		pub.vi = new Element[n+1];
		pub.hi = new Element[n+1];
		pub.vi[0] = msk.g.duplicate();
		pub.hi[0] = pub.h.duplicate();
		for(int i=1; i<n+1; i++){
			Element alpha_i = msk.alpha.duplicate();
			if(i!=1){
				for(int j=1; j<i; j++){
					alpha_i.mul(msk.alpha);
				}
			}
			pub.vi[i] = msk.g.powZn(alpha_i);
			pub.hi[i] = pub.h.powZn(alpha_i);
			pub.vi[i].getImmutable();
			pub.hi[i].getImmutable();
		}
		
		pub.H1 = pairing.getZr().newElement();
		pub.H2 = pairing.getZr().newElement();
		pub.H3 = pairing.getZr().newElement();
		pub.H4 = pairing.getZr().newElement();
	}
	// 要先想一下attribute的方式
	// input用int的陣列 但是要寫一個function用來把string的陣列變成int的
	// for 1~n 如果有相同的就在那個index的int陣列變成1
	// 所以給attribute的時候只要給一個任意長度的字串陣列就可以
	
	// 還要想寫出連乘後多項式的function
	// 跟算多項式的值的
	// 之後再cscpabe那邊的keygen再寫把string的陣列變成int的
	// input universeAttributes, String array
	// output userAttributes
	public static BswabePrv KeyGen(BswabePub pub, BswabeMsk msk, int[] userAttributes)
			{
		System.out.println("Bswabe_KeyGen");
		Pairing pairing = pub.pairing;
		BswabePrv prv = new BswabePrv();
		prv.g_s_faA = pairing.getG1().newElement();
		prv.h_s1_a = pairing.getG2().newElement();
		
		prv.fxA = PolynomialOperate.PolynGen(pub,userAttributes);
		prv.fxALength = prv.fxA.length;
		Element faA = PolynomialOperate.PolynomialTotal(pub,prv.fxA,msk.alpha);
		Element s = pairing.getZr().newRandomElement().getImmutable();
		
		Element s_faA = s.duplicate();
		s_faA.div(faA);
		prv.g_s_faA = msk.g.duplicate();
		prv.g_s_faA.powZn(s_faA);
		
		Element s1a = s.duplicate();
		Element eOne = pairing.getZr().newElement();
		eOne.setToOne();
		s1a.sub(eOne);
		s1a.div(msk.alpha);
		prv.h_s1_a = pub.h.duplicate();
		prv.h_s1_a.powZn(s1a);
		
		return prv;
	}
	
	//	加密
	public static BswabeCph Encrypt(BswabePub pub, byte[] messagebyte, int[] accessStructure){
		System.out.println("Bswabe_Encrypt");
		Pairing pairing = pub.pairing;
		BswabeCph cph = new BswabeCph();
		
		// 先拿 r=H4(P,M,sigma)需要的三個參數
		// P
		int accessStructureSize = 0;
		// n-bits access structure
		byte[] accessP = new byte[accessStructure.length];
		for(int i=0; i<accessStructure.length; i++){
			if(accessStructure[i]==1){
				accessP[i] = 1;
				accessStructureSize++;
			}else{
				accessP[i] = 0;
			}
		}
		// sigma
		Element sigma = pairing.getZr().newRandomElement().getImmutable();
		// combine byte
		byte[] PMSigma = ByteCombine(accessP, messagebyte, sigma.toBytes());
		
		pub.H4 = pairing.getZr().newElement().setFromHash(PMSigma, 0, PMSigma.length).getImmutable();
		Element r = pub.H4.duplicate().getImmutable();
		
		// 得到多項式
		cph.fxP = PolynomialOperate.PolynGen(pub,accessStructure);
		cph.fxPLength = cph.fxP.length;
		
		// compute C1
		cph.C1 = pairing.getG2().newElement();
		for(int i=0; i<cph.fxP.length; i++){
			Element C1_calc = pairing.getG2().newElement();
			C1_calc = pub.hi[i].duplicate();
			C1_calc.powZn(cph.fxP[i]);
			cph.C1.mul(C1_calc);
		}
		cph.C1.powZn(r);
		
		
		// compute C2
		cph.C2 = new Element[pub.n-accessStructureSize];
		for(int i=0; i<pub.n-accessStructureSize; i++){
			cph.C2[i] = pub.vi[i+1].powZn(r);
		}
		cph.C2Length = cph.C2.length;
		
		// compute C3
		Element e_gh_r = pub.e_gh.duplicate();
		e_gh_r.powZn(r);
		byte[] sigmabyte = sigma.toBytes();
		pub.H2 = pairing.getZr().newElement().setFromHash(e_gh_r.toBytes(), 0, sigmabyte.length); 
		byte[] H2byte = pub.H2.toBytes();
		cph.C3 = new byte[sigmabyte.length];
		for(int i=0; i<cph.C3.length; i++){
			cph.C3[i] = (byte) (H2byte[i]^sigmabyte[i]);
		}
		
		// compute C4
		pub.H3 = pairing.getZr().newElement().setFromHash(sigma.toBytes(), 0, messagebyte.length); 
		cph.C4 = new byte[messagebyte.length];
		byte[] H3byte = pub.H3.toBytes();
		for(int i=0; i<cph.C4.length; i++){
			cph.C4[i] = (byte)(H3byte[i]^messagebyte[i]);
		}
		return cph;
	}
	
	// 解密
	public static byte[] Decrypt(BswabePub pub, BswabeCph cph, 
			BswabePrv prv) throws UnsupportedEncodingException{
		System.out.println("Bswabe_Decrypt");
		Pairing pairing = pub.pairing;
		
		Element[] fxAP = null;
		PolynDiv div = PolynomialOperate.divPolyn(pub, cph.fxP, prv.fxA);
		if(div.remainder.length==1 && div.remainder[0].isZero()){
			fxAP = new Element[div.result.length];
			for(int i=0; i<fxAP.length; i++){
				fxAP[i] = div.result[i].duplicate();
			}
		}else{
			String verificationerror = "Verification fail";
			return verificationerror.getBytes();
		}
		
		// compute U
		// 算連乘
		Element hmul_U = pairing.getG2().newElement();
		for(int i=0; i<fxAP.length-1; i++){
			Element h_calculate = pairing.getG2().newElement();
			h_calculate = pub.hi[i].duplicate();
			h_calculate.powZn(fxAP[i+1]);
			hmul_U.mul(h_calculate);
		}
		// 算U
		Element U = pairing.pairing(cph.C2[0], hmul_U);
		
		// compute V
		Element C2mul_V = pairing.getG1().newElement();
		for(int i=0; i<fxAP.length; i++){
			Element c_calculate = pairing.getG1().newElement();
			c_calculate = cph.C2[i].duplicate();
			c_calculate.powZn(fxAP[i]);
			C2mul_V.mul(c_calculate);
		}
		Element V = pairing.pairing(C2mul_V, prv.h_s1_a);
		
		// compute W
		Element W = pairing.pairing(prv.g_s_faA, cph.C1);
		
		// compute e(g,h)r
		Element eghr = W.duplicate();
		Element UV = U.duplicate();
		UV.mul(V);
		Element F0_inv = fxAP[0].duplicate();
		F0_inv.invert();
		eghr.div(UV);
		eghr.powZn(F0_inv);
		
		// compute sigma
		pub.H2 = pairing.getZr().newElement().setFromHash(eghr.toBytes(), 0, cph.C3.length);
		byte[] H2byte = pub.H2.toBytes();
		byte[] sigmabyte = new byte[cph.C3.length];
		for(int i=0; i<cph.C3.length; i++){
			sigmabyte[i] = (byte) (H2byte[i]^cph.C3[i]);
		}
		
		// compute messagebyte
		pub.H3 = pairing.getZr().newElement().setFromHash(sigmabyte, 0, cph.C4.length);
		byte[] H3byte = pub.H3.toBytes();
		byte[] messagebyte = new byte [cph.C4.length];
		for(int i=0; i<cph.C4.length; i++){
			messagebyte[i] = (byte)(H3byte[i]^cph.C4[i]);
		}
		return messagebyte;
	}
	
	
	
	
	public static byte[] ByteCombine(byte[] a, byte[] b, byte[]c){
		byte[] res = new byte[a.length+b.length+c.length];
		for(int i=0; i<a.length; i++){
			res[i] = a[i];
		}
		for(int i=a.length; i<a.length+b.length; i++){
			res[i] = b[i-a.length];
		}
		for(int i=a.length+b.length; i<a.length+b.length+c.length; i++){
			res[i] = c[i-a.length-b.length];
		}
		return res;
	}
}
