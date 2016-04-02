import java.io.*;
import java.net.*;
import Commons.*;

public class HackathonSrv {
	public final int BUFFER_SIZE = 1024*1024;

	String imagePath = "/var/www/html/image.jpg";
	String ServerURL = "http://172.17.216.241/image.jpg";
	
	public int incomingImageSize = 0;

	void dumpImageToFile(byte[] imageBuffer, int imageLength, boolean append) {
		

		try {
			FileOutputStream fos = new FileOutputStream(imagePath, append);
			fos.write(imageBuffer,0,imageLength);
			fos.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	void sendNotificationToClient() {
		StringBuilder sb = new StringBuilder();
		sb.append("\"key\" : ");
		sb.append("\""+ServerURL+"\"");
		String str = sb.toString();

		GCMConnector gcmc = new GCMConnector();
		try {
			gcmc.Send(str);
		} catch(Exception e) {
			System.out.println("Could not send message to android device");
		}
		
	}

	void run() {
		try
		{
            ServerSocket serverSocket = new ServerSocket(Constants.PORT_NUMBER);


            while(true) {
	            
	            System.out.println("Waiting for client to connect");

	            Socket clientSocket = serverSocket.accept();

	            System.out.println("Client connected");

	            byte[] buffer = new byte[BUFFER_SIZE];
	            int read;
	            InputStream clientInputStream = clientSocket.getInputStream();

	            boolean firstBuffer = true;

	            //This while just reads one single image over multiple TCP socket reads
	            while(true) {

	            	//The first 4 bytes from the input stream indicate the length of incoming image

	            	incomingImageSize = 0;

		            for (int i=0; i<Constants.IMAGE_SIZE_LENGTH; i++) {
		            	read = clientInputStream.read();

		            	if(read == -1) {
		            		System.out.println("End of stream");
		            		return;
		            	}

		            	incomingImageSize = (incomingImageSize | ((read&0xFF)<<(i*8)));
		            }

		            if (incomingImageSize == Constants.IMAGE_END_MARKER) {
		            	break;
		            }

		            for(int i=0; i<incomingImageSize; i++) {
		            	read = clientInputStream.read();

		            	if(read == -1) {
		            		System.out.println("End of stream");
		            		return;
		            	}
		            	buffer[i] = (byte)read;
		            }

		            System.out.println("Chunk Read");

		            dumpImageToFile(buffer, incomingImageSize, !firstBuffer);


		            if (firstBuffer == true)
		            	firstBuffer = false;

		            //Clear the buffer. Let GC do it
		            buffer = new byte[BUFFER_SIZE];

	            }//End of single chunk while
	            sendNotificationToClient();
	            clientSocket.close();
	        	System.out.println("Image written");
            }
        }//End of try
        catch (IOException e)
        {
        	e.printStackTrace();
        }
	}
	public static void main(String args[])
	{
		HackathonSrv srv = new HackathonSrv();
		srv.run();
	}
}