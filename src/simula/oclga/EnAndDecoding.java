package simula.oclga;

import java.util.ArrayList;

import simula.ocl.distance.ValueElement4Search;
import simula.oclga.GeneValueScope.EncodedConstraintType;

public class EnAndDecoding {

	static ArrayList<GeneValueScope> geneValueList;
	
	public EnAndDecoding()
	{
		geneValueList = new ArrayList<GeneValueScope>();
	}
	
	/*
	public int[] removeNegativeValue(int v[], ArrayList<GeneValueScope> geneValueList)
	{
		ArrayList<GeneValueScope> cons = geneValueList;
		int value[] = new int[v.length];
		
		for(int i=0; i<v.length; i++)
		{
			if(v[i] < cons.get(i).getMinValue())
				value[i] = cons.get(i).getMinValue();
		    else if(v[i] > cons.get(i).getMaxValue())
		    	value[i] = cons.get(i).getMaxValue();
		    else
		    	value[i] = v[i];
		}
		
		return value;
	}
	*/
	
	public String[] decoding(int[] v, ValueElement4Search[] constraints)
	{
		String retString[] = new String[constraints.length];
		int index = 0; //indicate the index of the origin constraint
		
		for(int i=0; i<geneValueList.size(); i++)
		{
			GeneValueScope geneValue = geneValueList.get(i);
			
			if(geneValue.getEncodedConstraintType() == EncodedConstraintType.String)
			{
				retString[index] = decodingString(i, v[i], v);
				i = i + geneValue.maxValue;
			}
			else if(geneValue.getEncodedConstraintType() == EncodedConstraintType.Double)
			{
				retString[index] = decodingDouble(i, v);
				i = i + 1;
			}
			else
			{
				retString[index] = String.valueOf(v[i]);
			}
			
			index ++;
		}
		
		return retString;
	}
	
	public  String decodingDouble(int index, int[] v)
	{
		String doubleStr = new String();
		
		String realPart = String.valueOf(v[index]);
		String dotPart = String.valueOf(v[index+1]);
		
		doubleStr = realPart + "." + dotPart;
		return doubleStr;
	}
	
	
	public String decodingString(int index, int len, int[] v)
	{
		String str = new String();
		
		for(int i=1; i<len+1; i++)
		{
			char character = (char) v[index + i];
			str += character;
		}
		System.out.println(len +" after decoding: " + str);
		return str;
	}
	
	/*
	 * encoding the ValueElement4Search[] constraints
	 */
	public ArrayList<GeneValueScope> encoding(ValueElement4Search[] constraints)
	{
		return generateGeneValueList(constraints);
	}
	
	/*
	 * during the encoding, we encoding the value type of String to a array that with the max length
	 * for example, when the String length scope is [1, 20], then the arry is 20
	 */
	public static ArrayList<GeneValueScope> generateGeneValueList(ValueElement4Search[] constraints)
	{
		int individualLen = 0;
		
		for(int i=0; i<constraints.length; i++)
		{
			ValueElement4Search element = constraints[i];
			if(element.getType() == 2)
			{
				addCommonGene(element);               //add the gene of the length of string
				addStringGene(element.getMaxValue()); //add the gene of all the alphabet of the string
				individualLen = individualLen + element.getMaxValue() + 1;
			}
			else if(element.getType() == 3)
			{
				addDoubleGene(element);
				individualLen = individualLen +  2;
			}
			else
			{
				addCommonGene(element);
				individualLen ++;
			}
		}
		
		return getGeneValueList();
	}
	/*
    For the ValueElement4Search
	0 represent types of int
    1 represent types of boolean and enumeration
    2 represent types of string
    3 represent types of double
    
    but for GeneValueScope
    NUMERICAL_TYPE = 0;
	CATEGORICAL_TYPE = 1;
	
	When type of ValueElement4Search is 1 the type of GeneValueScope need to set to be 1 as a CATEGORICAL type
	or else, set type to 0, as a double type
    */
	public static void addCommonGene(ValueElement4Search element)
	{
		GeneValueScope gene = new GeneValueScope();
		gene.minValue = element.getMinValue();
		gene.maxValue = element.getMaxValue();
		
		switch( element.getType())
		{
		case 0:
			gene.type = Problem.NUMERICAL_TYPE;	
			break;
		case 1:
			gene.type = Problem.CATEGORICAL_TYPE;
		    break;
		case 2:
			gene.setEncodedConstraintType(EncodedConstraintType.String); 
			gene.type = Problem.NUMERICAL_TYPE;
			break;
		case 3:
			gene.setEncodedConstraintType(EncodedConstraintType.Double); 
			gene.type = Problem.NUMERICAL_TYPE;
			break;
		}
		
		geneValueList.add(gene);
	}
	
	
	public static void addStringGene(int len)
	{
	   for(int i=0; i<len; i++)
	   {
		   GeneValueScope gene = new GeneValueScope();
		   gene.minValue = 1;
		   gene.maxValue = 127;
		   gene.type = Problem.NUMERICAL_TYPE;
		   
		   geneValueList.add(gene);
	   }
	}
	
	public static void addDoubleGene(ValueElement4Search element)
	{
	   GeneValueScope BeforeDotGene = new GeneValueScope();
	   BeforeDotGene.minValue = element.getMinValue();
	   BeforeDotGene.maxValue = element.getMaxValue()-1;
	   BeforeDotGene.setEncodedConstraintType(EncodedConstraintType.Double);
	   BeforeDotGene.type = Problem.NUMERICAL_TYPE;
	   
	   geneValueList.add(BeforeDotGene);
	   
	   GeneValueScope afterDotGene = new GeneValueScope();
	   afterDotGene.minValue = 0;
	   afterDotGene.maxValue = 99999;
	   afterDotGene.type = Problem.NUMERICAL_TYPE;
		   
	   geneValueList.add(afterDotGene);
	}

	public static ArrayList<GeneValueScope> getGeneValueList() {
		return geneValueList;
	}

	public static void setGeneValueList(ArrayList<GeneValueScope> geneValueList) {
		EnAndDecoding.geneValueList = geneValueList;
	}
	
}
