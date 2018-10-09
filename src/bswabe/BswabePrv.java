package bswabe;

import it.unisa.dia.gas.jpbc.Element;

public class BswabePrv {
	/*
	 * A user private key
	 */
	public Element g_s_faA;			/* G_1 */
	public Element h_s1_a;			/* G_2 */
	// 下面是多項式的部分 還不確定會用到
	public int fxALength;
	public Element[] fxA;				/* Z_r */
}
