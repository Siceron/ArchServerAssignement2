package server;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

public class Client {

	public Client(){
		// Client
	}

	private static int[] stringToIntArray(String stringArray[]) {
		int result[] = new int[stringArray.length];
		for(int i = 0 ; i<stringArray.length ; i++){
			result[i] = Integer.parseInt(stringArray[i]);
		}
		return result;
	}
	
	private static int[][] imageTo2DArray(BufferedImage image) {

		int w = image.getWidth();
		int h = image.getHeight();
		int[][] pixels = new int[w][h];

		for( int i = 0; i < w; i++ )
			for( int j = 0; j < h; j++ )
				pixels[i][j] = image.getRGB( i, j );

		return pixels;
	}

	private static void writeImage(int arr[][]){
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

	public static void main(String [] args)
	{
		String serverName = args[0];
		int port = Integer.parseInt(args[1]);
		try
		{
			BufferedImage image = ImageIO.read(Client.class.getResource("image.png"));
			int[][] result = imageTo2DArray(image);

			System.out.println("Connecting to " + serverName +
					" on port " + port);
			Socket client = new Socket(serverName, port);
			System.out.println("Just connected to " 
					+ client.getRemoteSocketAddress());
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			
			// SENDING IMAGE TO SERVER
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
			InputStream inFromServer = client.getInputStream();
			DataInputStream in =
					new DataInputStream(inFromServer);
			int lengthX = Integer.parseInt(in.readUTF());
			int lengthY = Integer.parseInt(in.readUTF());
			int imageArray[][] = new int[lengthX][lengthY];
			for(int i = 0 ; i<lengthX ; i++){
				imageArray[i] = stringToIntArray(in.readUTF().split(","));
			}
			writeImage(imageArray); // WRITING IMAGE
			
			client.close();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
