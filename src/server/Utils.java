package server;

import java.util.Random;

public class Utils {

	/**
	 * Method that get a random number
	 * @param min
	 * @param max
	 * @return a random number between min and max
	 */
	public static int getRandomInteger(int min, int max) {

	    Random rand = new Random();

	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	/**
	 * Generate a random number based on the exponential distribution
	 * (with the inversion method)
	 * @return a random number based on the exponential distribution
	 */
	public static double expDistribRand() {
		Random rand = new Random();
	    return  Math.log(1-rand.nextDouble())/(-0.5);
	}
}
