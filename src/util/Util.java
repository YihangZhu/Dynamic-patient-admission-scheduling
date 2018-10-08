/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package util;

import dpas.Params;
import dpas.algoritm.SolCalculation;
import dpas.instance.Instance;
import dpas.instance.InstanceXMLReader;
import dpas.schedule.Problem;
import dpas.schedule.ScheduleResult;
import dpas.schedule.Solution;
import dpas.schedule.ValuesStorage;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;


public class Util{

	public static Random rand;
	public static double EPS = 1e-6;

    public static void initRandomGenerator(){
		rand = new Random(Params.SEED);
	}

    @SuppressWarnings("unused")
    public static float norm_Rand(float MU, float SIGMA){
		  double N = 12;
		  double x;
		  do{
			  x=0;
			  for(int i=0;i<N;i++)
				  x=x+(rand.nextFloat());
			  x=(x-N/2)/(Math.sqrt(N/12));
			  x=MU+x*Math.sqrt(SIGMA);
		  }while(x<=0);           
		  return (float) x;
	}

	@SuppressWarnings("unused")
	public static int[] sortList(int[] arr) {
		
	    int [][]arr_temp= new int[2][arr.length]; 
		for(int i=0;i<arr.length;i++){
			arr_temp[0][i] = i;
			arr_temp[1][i] = arr[i];
		}
		
		for(int i=0; i<arr.length; i++) {    
		    for(int j=i+1; j<=arr.length-1; j++) {
		        if(arr_temp[1][i] > arr_temp[1][j]) {        
		            int temp = arr_temp[1][i];                      
		            arr_temp[1][i] = arr_temp[1][j];                        
		            arr_temp[1][j] = temp;                                              
		        }
		    }
		}
		return arr_temp[0];
	}
	@SuppressWarnings("unused")
	public static int calculateDistance(int[] pointA, int[] pointB){
		return (int)Math.sqrt(Math.pow((pointA[0]-pointB[0]),2)+Math.pow(pointA[1] - pointB[1], 2));
	}
	@SuppressWarnings("unused")
	public static int [][] arrayCopy(int[][] arr1){
		int[][] arr2 = new int[arr1.length][];
		for(int i=0; i<arr1.length;i++)
			if(arr1[i] != null)
				arr2[i] = arr1[i].clone();
		return arr2;
	}

	@SuppressWarnings("unused")
	public static ArrayList<Integer> randomSerial(int begin, int end) {
		ArrayList<Integer> serial = new ArrayList<>(end-begin+1);
		for (int i=begin;i<(end-begin+1);i++)
			serial.add(i);
		Collections.shuffle(serial, rand);
		return serial;
	}

    @SuppressWarnings("unused")
	public static ArrayList<Integer> selection(int num, ArrayList<double[]> weights){
		ArrayList<Integer> selectedRooms = new ArrayList<>();
		int sumWeights = 0;
		for (double [] i : weights){
			sumWeights += i[1];
		}
		for (int i=0; i<num;i++){
			double lb = 0;
			double ub = weights.get(0)[1];
			double randi = rand.nextDouble() * sumWeights;
			for (int j=0; j<weights.size();j++) {
				if (randi > lb && randi <= ub) {
					selectedRooms.add((int) weights.get(j)[0]);
					sumWeights -= weights.get(j)[1];
					weights.remove(j);
					break;
				}else {
					lb += weights.get(j)[1];
					ub += weights.get(j+1)[1];
				}
			}
		}
		return selectedRooms;
	}
    @SuppressWarnings("unused")
	public static double [] normalization(double [] data){
		double min = Min.getMinValue(data);
		double max = Max.getMaxValue(data);
		double [] normalizedData = new double[data.length];
		for (int i = 0; i<data.length; i++){
			normalizedData[i] = (data[i] - min)/(max - min);
		}
		return normalizedData;
	}
    @SuppressWarnings("unused")
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(HashMap<K, V> map) {
		return map.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(e1, e2) -> e1,
						LinkedHashMap::new
				));
	}

    @SuppressWarnings("unused")
	public static void printResults(Solution solution){
		for (int i = 0; i < solution.getCostContainer().length; i++) {
			if (i==0){
				System.out.print(
						Params.getCostNames()[i] + ":\t" +
								solution.getCostContainer()[i]+"\t");
//				for (int j = 0; j < solution.getRoomCostContainer().length; j++) {
//					System.out.print(Params.getRoomCostNames()[j] + ":\t" + solution.getRoomCostContainer()[j] + "\t");
//				}
//				System.out.println();
			}else {
				System.out.print(
						Params.getCostNames()[i] + ":\t" +
								solution.getCostContainer()[i]+"\t");
			}
		}
		System.out.println(
//				"Violations:\t" + solution.getViolations()
						 "\tTotal ObjVal:\t" + solution.getObjectiveValue()
						+ "\tRuntime:\t" + solution.getRuntime()
						+ "\tRoom occupancy ratio:\t"+solution.getRoomOccupancyRatio()
						+ "\tOR occupancy ratio:\t"+solution.getOROccupancyRatio()
        );
	}


        public static void printArray(double[] arr, int precision) {
        for (double v : arr) {
            if (precision == 0) {
                System.out.print((int)(v) + "\t");
            } else {
                System.out.print(Util.round(v, precision) + "\t");
            }
        }
        System.out.println();
    }

    public static void printArray(double[][] arr, int precision) {
        int i = 0;
        for (double[] v : arr) {
            System.out.print(i++ +":\t");
            if (v!=null) {
                printArray(v,precision);
            }else {
                System.out.println();
            }
        }
    }
	@SuppressWarnings("all")
	public static void printArray(int[] values){
		for (int i : values) {
			System.out.print(i+"\t");
		}
		System.out.println();
	}

	@SuppressWarnings("all")
	public static void printArray(int[][] arr){
        int i = 0;
        for (int[] v : arr) {
            System.out.print(i++ +":\t");
            if (v!=null) {
                printArray(v);
            }else {
                System.out.println();
            }
        }
    }

	public static Solution readSolution(Instance instance, String filePath){
		ScheduleResult scheduleResult = new ScheduleResult(instance);
		return SolutionXMLReader.readXMLFile(filePath, scheduleResult, instance);
	}

	public static Instance readInstance(String filePath){
		InstanceXMLReader instanceXmlReader = new InstanceXMLReader();
		return instanceXmlReader.readData(filePath);
	}

	@SuppressWarnings("unused")
	public static void checkSolution(Problem problem, Solution solution){
		ValuesStorage vs = new ValuesStorage(problem.getInstance());
		SolCalculation cs = new SolCalculation(problem,vs);
		cs.assign(solution);
		cs.objectiveValueCalculation(solution,true);
	}

	public static boolean isUnequal(double val1, double val2, boolean throwException) {
		double diff = Math.abs(val1 - val2) - EPS;
		if (diff > 0){
			if (throwException) {
				throw new IllegalMonitorStateException("The two values are different!\t" + val1 + "\t" + val2);
			}else {
				System.out.println("The two values are different!\t" + val1 + "\t" + val2);
			}
			return true;
		}
		return false;
	}

    public static boolean isEqual(double val1, double val2) {
        double diff = Math.abs(val1 - val2) - EPS;
        return (diff <= 0);
    }

    public static double round(double value, int decimal){
        double x = Math.pow(10,decimal);
        return Math.round(value*x)/x;
    }

	public static void prettyFormat(String input, String output, int indent) {
		try {

			Source xmlInput = new StreamSource(new FileInputStream(new File(input)));

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));

//            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

			StreamResult result = new StreamResult(new File(output));



			// Output to console for testing
//             StreamResult result = new StreamResult(System.out);

			transformer.transform(xmlInput, result);



		} catch (Exception e) {
			e.printStackTrace(); // simple exception handling, please review it
		}
	}
}
