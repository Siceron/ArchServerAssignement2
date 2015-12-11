package server;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
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
		int xLenght = arr.length;
		int yLength = arr[0].length;
		BufferedImage b = new BufferedImage(xLenght, yLength, BufferedImage.TYPE_INT_ARGB);

		for(int x = 0; x < xLenght; x++) {
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

	static final float[] blur = {
		0.111f, 0.111f, 0.111f, 
		0.111f, 0.111f, 0.111f, 
		0.111f, 0.111f, 0.111f, 
	};

	static final float[] sharpen = {
		0.0f, -1.0f, 0.0f,
		-1.0f, 5.0f, -1.0f,
		0.0f, -1.0f, 0.0f
	};

	static final float[] edges = {
		-1.0f, -1.0f, -1.0f,
		-1.0f, 8.0f, -1.0f,
		-1.0f, -1.0f, -1.0f
	};

	public static void main(String [] args)
	{
		String serverName = args[0];
		int port = Integer.parseInt(args[1]);
		try
		{
			BufferedImage hugeImage = ImageIO.read(Client.class.getResource("image.png"));

			Kernel kernel = new Kernel(3, 3, edges);
			ConvolveOp op = new ConvolveOp(kernel);
			BufferedImage dstImage = op.filter(hugeImage, null);
			int[][] result = imageTo2DArray(dstImage);
			writeImage(result);

			System.out.println("Connecting to " + serverName +
					" on port " + port);
			Socket client = new Socket(serverName, port);
			System.out.println("Just connected to " 
					+ client.getRemoteSocketAddress());
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			out.writeUTF("Hello from "
					+ client.getLocalSocketAddress());
			InputStream inFromServer = client.getInputStream();
			DataInputStream in =
					new DataInputStream(inFromServer);
			System.out.println("Server says " + in.readUTF());
			client.close();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
