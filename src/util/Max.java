/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package util;

import java.util.ArrayList;
import java.util.Objects;

public class Max {

	/**
	 *
	 * @return the index of the maximum in arr.
	 */
	@SuppressWarnings("unused")
	public static ArrayList<Integer> getMaxIndex(int[] arr){
		ArrayList<Integer> maxIndex = new ArrayList<>();
	    maxIndex.add(0);
	    for(int i=0; i<arr.length; i++){
	        if(arr[i] > arr[maxIndex.get(0)]){
	            maxIndex.clear();
	            maxIndex.add(i);
	        }else if(arr[i] == arr[maxIndex.get(0)]){
	        	maxIndex.add(i);
	        }
	    }
	    return maxIndex;
	}
	@SuppressWarnings("unused")
	public static ArrayList<Integer> getMaxIndex(ArrayList<Integer> list){
		ArrayList<Integer> maxIndex = new ArrayList<>();
		maxIndex.add(0);
		for(int i=0; i<list.size(); i++){
			if(list.get(i)>list.get(maxIndex.get(0))){
				maxIndex.clear();
				maxIndex.add(i);
			}else if(Objects.equals(list.get(i), list.get(maxIndex.get(0)))){
				maxIndex.add(i);
			}
		}
		return maxIndex;
	}
//	/**
//	 *
//	 * @param arr the values
//	 * @param list the indexes corresponding to the values
//	 * @return the index of the maximum value in arr
//	 * list contains the availible index of arr
//	 */
//	public static ArrayList<Integer> getMaxIndex(int[] arr,  ArrayList<Integer> list){
//	    ArrayList<Integer> maxIndex = new ArrayList<Integer>();
//		maxIndex.add(list.get(0));
//	    for(int i=1; i<list.size(); i++){
//	        if(arr[list.get(i)] > arr[maxIndex.get(0)]){
//	        	maxIndex.clear();
//	            maxIndex.add(list.get(i));
//	        }else if(arr[list.get(i)] == arr[maxIndex.get(0)]){
//	        	maxIndex.add(list.get(i));
//	        }
//	    }
//	    return maxIndex;
//	}

	/**
	 *
	 * @return get the maximum value of list
	 */
	@SuppressWarnings("unused")
	public static int getMaxValue(ArrayList<Integer> list) {
		int maximum = list.get(0);
		for (Integer aList : list) {
			if (aList > maximum) {
				maximum = aList;
			}
		}
	    return maximum;

	}

	/**
	 *

	 * @return return the Maximum array arr
	 */
	static double getMaxValue(double[] arr) {
		double maximum = arr[0];
		for (double anArr : arr) {
			if (anArr > maximum) {
				maximum = anArr;
			}
		}
	    return maximum;

	}

}
