/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package util;

import java.util.ArrayList;

public class Min {

	/**
	 * @return the index of the minimum value in arr
	 */
	@SuppressWarnings("unused")
	public static ArrayList<Integer> getMinIndex(int[] arr ){
		ArrayList<Integer> minIndex = new ArrayList<>();
		minIndex.add(0);
	    for(int i=0; i<arr.length; i++){
	            if(arr[i] < arr[minIndex.get(0)]){
	            	minIndex.clear();
	            	minIndex.add(i);
	            }
	            else if (arr[i] == arr[ minIndex.get(0)]){
	            	minIndex.add(i);
	            }
	        }

	        return minIndex;
	    }

//	/**
//	 *  the element in list is the index of arr
//	 * @param arr
//	 * @return the index of minimum value in arr among the index list
//	 * list contains the availible index of arr
//	 */

//	public static ArrayList<Integer> getMinIndex(int[] arr, ArrayList<Integer> list){
//		ArrayList<Integer> minIndex = new ArrayList<Integer>();
//		minIndex.add(list.get(0));
//	    for(int i=1; i<list.size(); i++){
//	        if(arr[list.get(i)] > arr[minIndex.get(0)]){
//	        	minIndex.clear();
//	            minIndex.add(list.get(i));
//	        }else if(arr[list.get(i)] == arr[minIndex.get(0)]){
//	        	minIndex.add(list.get(i));
//	        }
//	    }
//	    return minIndex;
//	  }

	/**
	 * @return the minimum value of list
	 */
	@SuppressWarnings("unused")
	public static int getMinValue (ArrayList<Integer> list){
		int minimum = list.get(0);
		for (Integer aList : list) {
			if (aList < minimum) {
				minimum = aList;
			}
		}
	    return minimum;
	}

	/**
	 *
	 * @param arr is the range or index of List
	 *
	 * @return the minimum value
	 * list contains the availible index of arr
	 */
	@SuppressWarnings("unused")
	public static int getMinValue(int[] arr,  ArrayList<Integer> list){
	    int minimum = arr[ list.get(0)];
	    for(int i=0; i<list.size(); i++){
	        if(arr[list.get(i)] < minimum){
	        	minimum = arr[i];
	        }
	    }
	    return minimum;
	}
	@SuppressWarnings("unused")
	static double getMinValue(double[] arr) {
		double minimum = arr[0];
		for (double anArr : arr) {
			if (anArr < minimum) {
				minimum = anArr;
			}
		}
	    return minimum;

	}

}
