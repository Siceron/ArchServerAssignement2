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

public class Server extends Thread {

	private ServerSocket serverSocket;
	private SizeBasedCache sizeBasedCache;
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

	public Server(int port) throws IOException
	{
		serverSocket = new ServerSocket(port, 100);
		sizeBasedCache = new SizeBasedCache(10000000, 0);
		//serverSocket.setSoTimeout(60000);
	}

	private void measurement1(){
		while(true)
		{
			try
			{
				System.out.println("Listening on port " +
						serverSocket.getLocalPort() + "...");
				Socket server = serverSocket.accept();
				System.out.println("Just connected to "
						+ server.getRemoteSocketAddress());

				// PARSING IMAGE
				DataInputStream in =
						new DataInputStream(server.getInputStream());
				long startCalculationTime = System.currentTimeMillis();
				int lengthX = Integer.parseInt(in.readUTF());
				int lengthY = Integer.parseInt(in.readUTF());
				int imageArray[][] = new int[lengthX][lengthY];
				for(int i = 0 ; i<lengthX ; i++){
					imageArray[i] = Utils.stringToIntArray(in.readUTF().split(","));
				}

				// CONVOLUTION
				BufferedImage image = Utils.arrayToImage(imageArray);
				Kernel kernel = new Kernel(3, 3, sharpen);
				ConvolveOp op = new ConvolveOp(kernel);
				BufferedImage dstImage = op.filter(image, null);
				int[][] result = Utils.imageTo2DArray(dstImage);
				System.out.println("Calculation time : "+(System.currentTimeMillis()-
						startCalculationTime)/1000.0+"s");

				// SENDING RESULT
				DataOutputStream out =
						new DataOutputStream(server.getOutputStream());
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

				server.close();
			}catch(SocketTimeoutException s)
			{
				System.out.println("Socket timed out!");
				break;
			}catch(IOException e)
			{
				e.printStackTrace();
				break;
			}
		}
	}
	
	private void measurement3(){
		while(true)
		{
			try
			{
				System.out.println("Listening on port " +
						serverSocket.getLocalPort() + "...");
				Socket server = serverSocket.accept();
				System.out.println("Just connected to "
						+ server.getRemoteSocketAddress());
				
				// PARSING IMAGE
				DataInputStream in =
						new DataInputStream(server.getInputStream());
				long startCalculationTime = System.currentTimeMillis();
				int lengthX = Integer.parseInt(in.readUTF());
				int lengthY = Integer.parseInt(in.readUTF());
				int[][] result = new int[lengthX][lengthY];
				String resourceName = ""+lengthX*lengthY;
				System.out.println("Reading image : "+lengthX);
				if(sizeBasedCache.isElement(resourceName)){
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
						new DataOutputStream(server.getOutputStream());
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

				server.close();
			}catch(SocketTimeoutException s)
			{
				System.out.println("Socket timed out!");
				break;
			}catch(IOException e)
			{
				e.printStackTrace();
				break;
			}
		}
	}
	
	public void run()
	{
		//measurement1();
		measurement3();
	}
	
	public static void main(String [] args)
	{
		int port = Integer.parseInt(args[0]);
		try
		{
			Thread t = new Server(port);
			t.start();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
