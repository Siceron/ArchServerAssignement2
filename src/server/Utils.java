package server;

import java.awt.image.BufferedImage;
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
	
	/**
	 * Parse the strings received for the server to an int array of pixels
	 * @param stringArray
	 * @return an int array representing the pixels of the image
	 */
	public static int[] stringToIntArray(String stringArray[]) {
		int result[] = new int[stringArray.length];
		for(int i = 0 ; i<stringArray.length ; i++){
			result[i] = Integer.parseInt(stringArray[i]);
		}
		return result;
	}

	/**
	 * Transform an array of pixels to a BufferedImage
	 * @param imageArray
	 * @return the BufferedImage corresponding to the imageArray
	 */
	public static BufferedImage arrayToImage(int imageArray[][]){
		int xLength = imageArray.length;
		int yLength = imageArray[0].length;
		BufferedImage b = new BufferedImage(xLength, yLength, BufferedImage.TYPE_INT_ARGB);

		for(int x = 0; x < xLength; x++) {
			for(int y = 0; y < yLength; y++) {
				b.setRGB(x, y, imageArray[x][y]);
			}
		}

		return b;
	}

	/**
	 * Get a 2d array of integers representing the pixels of the image
	 * @param image
	 * @return2d a 2d array of integers representing the pixels of the image
	 */
	public static int[][] imageTo2DArray(BufferedImage image) {

		int w = image.getWidth();
		int h = image.getHeight();
		int[][] pixels = new int[w][h];

		for( int i = 0; i < w; i++ )
			for( int j = 0; j < h; j++ )
				pixels[i][j] = image.getRGB( i, j );

		return pixels;
	}
}
