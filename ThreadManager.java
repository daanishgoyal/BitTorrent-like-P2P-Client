
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadManager implements Runnable
{
	Thread sender;
	Socket clientSocket;
	private ServerSocket serverSocket;
	private String PID;

	/**

	 * @param skt
	 * @param pid
	 */
	public ThreadManager(ServerSocket skt, String pid)
	{
		this.PID = pid;
		this.serverSocket = skt;
	}


	public void run() 
	{
		while(true)
		{
			try
			{
				clientSocket = serverSocket.accept();
				sender = new Thread(new PeerManager(clientSocket,0, PID));
				peerProcess.writeLog(PID + " TCP connection established  successfully");
				peerProcess.sndThreadVector.add(sender);
				sender.start();
			}
			catch(Exception e)
			{
				peerProcess.writeLog(this.PID + "ConnectionError " + e.toString());
			}
		}
	}


	public void freeTheThread()
	{
		try 
		{
			if(!clientSocket.isClosed())
			clientSocket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}


