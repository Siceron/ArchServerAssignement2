package server;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.imageio.ImageIO;

public class Client extends Thread {

	// ARRAY OF DIFFICULTIES
	private String difficulties[] = {"100.png", "250.jpg", "500.jpg", "1000.jpg",
			"1500.jpg", "2000.jpg"};

	private String serverName;
	private int port;
	private int difficulty;

	public Client(String serverName, int port, int difficulty){
		this.serverName = serverName;
		this.port = port;
		this.difficulty = difficulty;
	}

	/**
	 * Parse the strings received for the server to an int array of pixels
	 * @param stringArray
	 * @return an int array representing the pixels of the image
	 */
	private int[] stringToIntArray(String stringArray[]) {
		int result[] = new int[stringArray.length];
		for(int i = 0 ; i<stringArray.length ; i++){
			result[i] = Integer.parseInt(stringArray[i]);
		}
		return result;
	}

	/**
	 * Get a 2d array of integers representing the pixels of the image
	 * @param image
	 * @return2d a 2d array of integers representing the pixels of the image
	 */
	private int[][] imageTo2DArray(BufferedImage image) {

		int w = image.getWidth();
		int h = image.getHeight();
		int[][] pixels = new int[w][h];

		for( int i = 0; i < w; i++ )
			for( int j = 0; j < h; j++ )
				pixels[i][j] = image.getRGB( i, j );

		return pixels;
	}

	/**
	 * Write an image to the disk from a 2d array of pixels
	 * @param arr := 2d array of pixels
	 */
	private void writeImage(int arr[][]){
		int xLength = arr.length;
		int yLength = arr[0].length;
		BufferedImage b = new BufferedImage(xLength, yLength, BufferedImage.TYPE_INT_ARGB);

		for(int x = 0; x < xLength; x++) {
			for(int y = 0; y < yLength; y++) {
				b.setRGB(x, y, arr[x][y]);
			}
		}
		try {
			ImageIO.write(b, "png", new File("result.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run()
	{
		// VARS USED FOR MEASUREMENT
		long startTime = System.currentTimeMillis();
		long networkTime = 0;
		long diskAccessTime = 0;

		try
		{
			long startDiskAccessTime = System.currentTimeMillis();
			String difficultyPath = difficulties[Utils.getRandomInteger(0, 5)];
			BufferedImage image = ImageIO.read(Client.class.getResource(difficultyPath));
			diskAccessTime += (System.currentTimeMillis() - startDiskAccessTime);
			int[][] result = imageTo2DArray(image);

			System.out.println("Connecting to " + serverName +
					" on port " + port);
			long startNetworkTime = System.currentTimeMillis();
			Socket client = new Socket(serverName, port);
			System.out.println("Just connected to " 
					+ client.getRemoteSocketAddress());
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);

			// SENDING IMAGE TO SERVER
			System.out.println("Sending image "+result.length);
			String messageToServer = "";
			out.writeUTF(""+result.length);
			out.writeUTF(""+result[0].length);
			for(int i = 0 ; i < result.length ; i++){
				for(int j = 0 ; j < result[0].length ; j++){
					if(j < result[0].length-1)
						messageToServer += result[i][j]+",";
					else
						messageToServer += result[i][j]+"";
				}
				out.writeUTF(messageToServer);
				messageToServer = "";
			}

			// RECEIVING IMAGE FROM SERVER
			startNetworkTime = System.currentTimeMillis();
			InputStream inFromServer = client.getInputStream();
			DataInputStream in =
					new DataInputStream(inFromServer);
			networkTime += System.currentTimeMillis() - startNetworkTime;
			int lengthX = Integer.parseInt(in.readUTF());
			int lengthY = Integer.parseInt(in.readUTF());
			int imageArray[][] = new int[lengthX][lengthY];
			for(int i = 0 ; i<lengthX ; i++){
				imageArray[i] = stringToIntArray(in.readUTF().split(","));
			}
			client.close();
			networkTime += System.currentTimeMillis() - startNetworkTime;
			startDiskAccessTime = System.currentTimeMillis();
			writeImage(imageArray); // WRITING IMAGE
			diskAccessTime += (System.currentTimeMillis() - startDiskAccessTime);
			System.out.println("Network time : "+networkTime/1000.0+"s");
			System.out.println("Disk access time : "+diskAccessTime/1000.0+"s");
			System.out.println("Total time : "+(System.currentTimeMillis()-startTime)/1000.0+"s");
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String [] args)
	{
		String serverName = args[0];
		int port = Integer.parseInt(args[1]);
		int numberOfClients = 10;
		for(int i = 0 ; i<numberOfClients ; i++){
			Thread t = new Client(serverName,port, Utils.getRandomInteger(0, 5));
			t.start();
			long currentTime = System.currentTimeMillis();
			while((System.currentTimeMillis()-currentTime)/1000.0 < Utils.expDistribRand()){
				// Wait an exponentially distributed time
			}
		}
	}
}
