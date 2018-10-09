package bswabe;


import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;


public class PolynomialOperate {
		//加法 
    	public static Element[] addPolyn(BswabePub pub, Element[] a, Element[] b){ 
		Pairing pairing = pub.pairing;
    	int heigherlength, lowerlength;
    	int check = 0;	// 0->a = heigher, 1->b = heigher
    	if(a.length>=b.length){
    		heigherlength = a.length;
    		lowerlength = b.length;
    		check = 0;
    	}else{
    		heigherlength = b.length;
    		lowerlength = a.length;
    		check = 1;
    	}
        Element[] res=new Element[heigherlength];  
        for(int i=0;i<heigherlength;i++){  
        	res[i] = pairing.getZr().newElement();
            if(i<lowerlength){  
                res[i]=a[i].duplicate();
                res[i].add(b[i]);
            }  
            else{  
                if(check==0){
                	res[i] = a[i].duplicate();
                }else{
                	res[i] = b[i].duplicate();
                }
            }  
        }  
        return res;  
    }
    // 減法  a-b
    public static Element[] subPolyn(BswabePub pub, Element[] a, Element[] b){
		Pairing pairing = pub.pairing;
    	Element[] negateb = new Element[b.length];
    	for(int i=0; i<b.length; i++){
    		negateb[i] = pairing.getZr().newElement();
    		negateb[i] = b[i].duplicate();
    		negateb[i].negate();
    	}
    	Element[] res = addPolyn(pub, a, negateb);
    	
    	int newreslength = 0;
    	boolean found = false;
    	for(int k=res.length-1; k>=0; k--){
    		if(!found){
    			if(!res[k].isZero()){
    				found = true;
    				newreslength = k+1;
    			}
    		}
    	}
    	if(newreslength==0){
    		Element[] newres = new Element[1];
    		newres[0] = pairing.getZr().newElement().setToZero();
    		
    		return newres;
    	}else{
    		Element[] newres = new Element[newreslength];
        	for(int i=0; i<newreslength; i++){
        		newres[i] = pairing.getZr().newElement();
        		newres[i] = res[i].duplicate();
        	}
        	return newres;
    	}
    }
    // element 的多項式乘法
 	public static Element[] mulPolyn(BswabePub pub, Element[] a,Element[] b){  
		Pairing pairing = pub.pairing;
 		Element[] res=new Element[a.length+b.length-1];  
 		
 		// initial res
 		for(int i=0; i<res.length; i++){
 			res[i] = pairing.getZr().newElement();
 			res[i].setToZero();
 		}
 		
 		Element temp = pairing.getZr().newElement();
         for(int i=0;i<a.length;i++){  
             for(int j=0;j<b.length;j++){  
                     //res[i+j]+=a[i]*b[j]; 
             	Element temp_a = a[i].duplicate();
             	Element temp_b = b[j].duplicate();
                 temp = temp_a.mul(temp_b);
                 res[i+j].add(temp);
             }  
         }
         return res;  
     }
 	// 除法
 	public static PolynDiv divPolyn(BswabePub pub, Element[] dividend, Element[] divisor){
 		PolynDiv div = new PolynDiv();
 		Pairing pairing = pub.pairing;
 		if(dividend.length<divisor.length){
 			div.result = new Element[1];
 			div.result[0] = pairing.getZr().newElement();
 			div.result[0].setToZero();
 			div.remainder = new Element[dividend.length];
 			for(int i=0; i<div.remainder.length; i++){
 				div.remainder[i] = pairing.getZr().newElement();
 				div.remainder[i] = dividend[i].duplicate();
 			}
 		}else{
 			Element[] a = new Element[dividend.length];
 			for(int i=0; i<a.length; i++){
 				a[i] = dividend[i].duplicate();
 			}
 			Element[] b = new Element[divisor.length];
 			for(int i=0; i<b.length; i++){
 				b[i] = divisor[i].duplicate();
 			}
 			div.result = new Element[1];
 			div.result[0] = pairing.getZr().newElement();
 			div.result[0].setToZero();
 			//div
 			while(a.length>=b.length){
 				int num = a.length-b.length;
 				Element[] temp = new Element[num+1];
 				for(int i=0; i<num+1; i++){
 					temp[i] = pairing.getZr().newElement();
 				}
 				temp[num] = a[a.length-1].duplicate();
 				temp[num].div(b[b.length-1]);
 				div.result = addPolyn(pub, temp, div.result);
 				Element[] c = mulPolyn(pub, b,temp);
 				a = subPolyn(pub,a,c);
 			}
 			div.remainder = new Element[a.length];
 			for(int i=0; i<div.remainder.length; i++){
 				div.remainder[i] = a[i].duplicate();
 			}
 		}
 		return div;
 	}
 	
 	
 	// Attribute verification 判斷user有沒有權限
 	public static boolean AttributeVerification(BswabePub pub, Element[] fxP, Element[] fxA){
 		boolean VerificationResult = true;
 		PolynDiv div = divPolyn(pub, fxP, fxA);
 		if(div.remainder.length==1 && div.remainder[0].isZero()){
 			VerificationResult = true;
 		}else{
 			VerificationResult = false;
 		}
 		return VerificationResult;
 	}
 	// 用來算fxP fxA fxAP的
 	public static Element[] PolynGen(BswabePub pub, int[] attributes){
		Pairing pairing = pub.pairing;
 		Element[] fx = null;
 		for(int i=0; i<attributes.length; i++){
 			String iString = Integer.toString(i); 
 			byte[] w = iString.getBytes();
 			pub.H1= pairing.getZr().newElement().setFromHash(w, 0, w.length); 
 			if(i==0){
 				if(attributes[0]==1){
					// if a1==1, faA = 1
					fx = new Element[1];
					fx[0] = pairing.getZr().newElement();
					fx[0].setToOne();
				}else{
					// if a1==0, faA = a+H1i
					fx = new Element[2];
					fx[0] = pairing.getZr().newElement();
					fx[1] = pairing.getZr().newElement();
					fx[0] = pub.H1.duplicate();
					fx[1].setToOne();
				}
 			}else{
 				if(attributes[i]!=1){
					// if ai==1, function = 1, don't need to mul
 					// 先宣告這層的function
					Element fx2[] = new Element[2];
					fx2[0] = pairing.getZr().newElement();
					fx2[1] = pairing.getZr().newElement();
					fx2[0] = pub.H1.duplicate();
					fx2[1].setToOne();
					// 再把兩個相乘
					fx = mulPolyn(pub,fx,fx2);
 				}
 			}
 		}
 		return fx;
 	}
 	// 用來算faA faP faAP
 	public static Element PolynomialTotal(BswabePub pub, Element[] fx, Element x){
		Pairing pairing = pub.pairing;
 		Element total = pairing.getZr().newElement();
 		total.setToZero();
 		for(int i=0; i<fx.length; i++){
 			if(i==0){
 				total.add(fx[0]);
 			}else{
 				Element temp = pairing.getZr().newElement();
				for(int j=0; j<i; j++){	//用for乘alpha指數次
					if(j==0){
						temp = x.duplicate();
					}else{
						temp.mul(x);
					}
				}
				temp.mul(fx[i]);
				total.add(temp);
 			}
 		}
 		return total;
 	}
}
