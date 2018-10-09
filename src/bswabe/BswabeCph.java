package bswabe;

import it.unisa.dia.gas.jpbc.Element;

public class BswabeCph {
	/*
	 * A ciphertext
	 */
	public Element C1;							/* G_2  */
	int C2Length;
	public Element[] C2;							/* G_1 */
	public byte[] C3;
	public byte[] C4;
	// 下面是多項式的部分 還不確定會用到
	int fxPLength;
	public Element[] fxP;						/* Z_r */
	
}
