package bswabe;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class BswabePub {
	/*
	 * A public key
	 */
	public Pairing pairing;		
	public int n;
	public Element[] vi;			/* G_1 */
	public Element h;				/* G_2 */
	public Element[] hi;			/* G_2 */
	public Element e_gh;		/* G_T */
	public Element H1;			/* Z_r */
	public Element H2;			/* Z_r */
	public Element H3;			/* Z_r */
	public Element H4;			/* Z_r */
	
}
