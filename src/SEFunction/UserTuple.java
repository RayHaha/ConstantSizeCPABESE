package SEFunction;

import it.unisa.dia.gas.jpbc.Element;

public class UserTuple {
	/*
	 *  ( Uid, A_Uid, SK )
	 */
	public int Uid;
	public Element A_Uid;			/* Z_r */
	// private key
	public Element g_s_faA;			/* G_1 */
	public Element h_s1_a;			/* G_2 */
	public int fxALength;
	public Element[] fxA;				/* Z_r */
}
