package server;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import server.cache.Resource;
import server.cache.SizeBasedCache;

public class MultiThreadedServer implements Runnable {

	private Socket socket;
	protected SizeBasedCache sizeBasedCache;

	private final float[] edges = {
			-1.0f, -1.0f, -1.0f,
			-1.0f, 8.0f, -1.0f,
			-1.0f, -1.0f, -1.0f
	};
	private final float[] sharpen = {
			0.0f, -1.0f, 0.0f,
			-1.0f, 5.0f, -1.0f,
			0.0f, -1.0f, 0.0f
	};
	private final float[] blur = {
			0.111f, 0.111f, 0.111f, 
			0.111f, 0.111f, 0.111f, 
			0.111f, 0.111f, 0.111f, 
	};

	public MultiThreadedServer(Socket socket, SizeBasedCache sizeBasedCache) {
		super();
		this.socket = socket;
		this.sizeBasedCache = sizeBasedCache;
	}

	public void run()
	{
		try
		{
			System.out.println("Just connected to "
					+ socket.getRemoteSocketAddress());

			// PARSING IMAGE
			DataInputStream in =
					new DataInputStream(socket.getInputStream());
			int lengthX = Integer.parseInt(in.readUTF());
			int lengthY = Integer.parseInt(in.readUTF());
			int[][] result = new int[lengthX][lengthY];
			String resourceName = ""+lengthX*lengthY;
			System.out.println("Reading image : "+lengthX);
			if(sizeBasedCache.isElement(resourceName)){
				System.out.println("ALREADY IN THE CACHE");
				long startCalculationTime = System.currentTimeMillis();
				result = sizeBasedCache.getResource(resourceName).getImage().clone();
				System.out.println("Calculation time : "+(System.currentTimeMillis()-
						startCalculationTime)/1000.0+"s");
				for(int i = 0 ; i<lengthX ; i++){
					in.readUTF();
				}
			}
			else{
				int imageArray[][] = new int[lengthX][lengthY];
				for(int i = 0 ; i<lengthX ; i++){
					imageArray[i] = Utils.stringToIntArray(in.readUTF().split(","));
				}

				// CONVOLUTION
				long startCalculationTime = System.currentTimeMillis();
				BufferedImage image = Utils.arrayToImage(imageArray);
				Kernel kernel = new Kernel(3, 3, sharpen);
				ConvolveOp op = new ConvolveOp(kernel);
				BufferedImage dstImage = op.filter(image, null);
				result = Utils.imageTo2DArray(dstImage);
				sizeBasedCache.addElement(new Resource(resourceName, lengthX*lengthY, result.clone()));
				System.out.println("Calculation time : "+(System.currentTimeMillis()-
						startCalculationTime)/1000.0+"s");
			}

			// SENDING RESULT
			DataOutputStream out =
					new DataOutputStream(socket.getOutputStream());
			String messageToClient = "";
			out.writeUTF(""+result.length);
			out.writeUTF(""+result[0].length);
			for(int i = 0 ; i < result.length ; i++){
				for(int j = 0 ; j < result[0].length ; j++){
					if(j < result[0].length-1)
						messageToClient += result[i][j]+",";
					else
						messageToClient += result[i][j]+"";
				}

				out.writeUTF(messageToClient);
				messageToClient = "";
			}

			socket.close();
		}catch(SocketTimeoutException s)
		{
			System.out.println("Socket timed out!");
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String [] args) throws IOException
	{
		int port = Integer.parseInt(args[0]);
		SizeBasedCache sizeBasedCache = new SizeBasedCache(10000000, 0);
		ServerSocket serverSocket = new ServerSocket(port, 100);
		System.out.println("Listening on port " +
				serverSocket.getLocalPort() + "...");
		while(true){
			Socket socket = serverSocket.accept();
			new Thread(new MultiThreadedServer(socket, sizeBasedCache)).start();
		}
	}
}
