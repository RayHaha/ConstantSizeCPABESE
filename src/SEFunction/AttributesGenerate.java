package SEFunction;

public class AttributesGenerate {
	static String[] universeAttributes ={"Student","Ntu","OlderThan20","Teacher","Male"};
	static int AttributesLength = universeAttributes.length;
	// 在這邊要決定有沒有這個attribute
	public static int[] AttributesGen(String[] s){
		int[] Attributes = new int[AttributesLength];
		// initial
		for(int i=0; i<Attributes.length; i++){
			Attributes[i] = 0;
		}
		for(int i=0; i<s.length; i++){
			for(int j=0; j<Attributes.length; j++){
				if(s[i].equals(universeAttributes[j])){
					Attributes[j] = 1;
				}
			}
		}
		return Attributes;
	}
	
	public static int NSize(){
		return AttributesLength;
	}
}
