package cscpabe;

import java.io.IOException;

import bswabe.Bswabe;
import bswabe.BswabeCph;
import bswabe.BswabeMsk;
import bswabe.BswabePrv;
import bswabe.BswabePub;
import bswabe.SerializeUtils;
import cscpabe.AESCoder;
import cscpabe.Common;
import it.unisa.dia.gas.jpbc.Element;

public class csCpabe {

	public void setup(String pubfile, String mskfile, int n) throws IOException,
			ClassNotFoundException{
		byte[] pub_byte, msk_byte;
		BswabePub pub = new BswabePub();
		BswabeMsk msk = new BswabeMsk();
		Bswabe.Setup(pub, msk, n);
		
		/* store BswabePub into pubfile */
		pub_byte = SerializeUtils.serializeBswabePub(pub);
		Common.spitFile(pubfile, pub_byte);
		
		/* store BswabeMsk into mskfile */
		msk_byte = SerializeUtils.serializeBswabeMsk(msk);
		Common.spitFile(mskfile, msk_byte);
		
	}
	
	public void keyGen(String pubfile, String mskfile, String prvfile, int[] userAttributes) throws IOException{
		byte[] pub_byte, msk_byte, prv_byte;
		BswabePub pub = new BswabePub();
		BswabeMsk msk = new BswabeMsk();
		
		/* get BswabePub from pubfile */
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);

		/* get BswabeMsk from mskfile */
		msk_byte = Common.suckFile(mskfile);
		msk = SerializeUtils.unserializeBswabeMsk(pub, msk_byte);
		
		/* store BswabePrv into prvfile */
		BswabePrv prv = Bswabe.KeyGen(pub, msk, userAttributes);
		prv_byte = SerializeUtils.serializeBswabePrv(prv);
		Common.spitFile(prvfile, prv_byte);
	}
	
	public void encrypt(String pubfile, String inputfile, String encfile, String cphfile, 
			int[] accessStructure) throws Exception{
		byte[] pub_byte, aesBuf, cphBuf, plt;
		BswabePub pub = new BswabePub();
		
		/* get BswabePub from pubfile */
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);

		Element m = pub.pairing.getZr().newRandomElement();
		BswabeCph cph = Bswabe.Encrypt(pub, m.toBytes(), accessStructure);
		
		if (cph == null) {
			System.out.println("Error happed in enc");
			System.exit(0);
		}
		
		cphBuf = SerializeUtils.bswabeCphSerialize(pub, cph);
		Common.spitFile(cphfile, cphBuf);
		
		/* read file to encrypted */
		plt = Common.suckFile(inputfile);
		aesBuf = AESCoder.encrypt(m.toBytes(), plt);
				
		Common.writeCpabeFile(encfile, cphBuf, aesBuf);
	}
	public void decrypt(String pubfile, String prvfile, String encfile, 
			String decfile) throws Exception{
		byte[] aesBuf, cphBuf;
		byte[] plt;
		byte[] prv_byte;
		byte[] pub_byte;
		byte[][] tmp;
		BswabeCph cph;
		BswabePrv prv;
		BswabePub pub;
		
		/* get BswabePub from pubfile */
		pub_byte = Common.suckFile(pubfile);
		pub = SerializeUtils.unserializeBswabePub(pub_byte);
		
		/* read ciphertext */
		tmp = Common.readCpabeFile(encfile);
		aesBuf = tmp[0];
		cphBuf = tmp[1];
		cph = SerializeUtils.bswabeCphUnserialize(pub, cphBuf);
		
		/* get BswabePrv form prvfile */
		prv_byte = Common.suckFile(prvfile);
		prv = SerializeUtils.unserializeBswabePrv(pub, prv_byte);
		
		/* decrypt */
		byte[] decryptbyte = Bswabe.Decrypt(pub, cph, prv);
		plt = AESCoder.decrypt(decryptbyte, aesBuf);
		Common.spitFile(decfile, plt);
	}
}
