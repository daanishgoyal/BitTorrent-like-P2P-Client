
import java.io.*;
import java.util.*;


public class RemotePeerInitiator
{
	public Vector<Process> peersList = new Vector<Process>();
	public Vector<PeerStateInfo> peersStateList = new Vector<PeerStateInfo>();

	public void getConfig() {
		try {
			int itr = 0;
			String tempStr;
			FileReader fr = new FileReader("PeerInfo.cfg");
			BufferedReader configReader = new BufferedReader(fr);

			while((tempStr = configReader.readLine()) != null) {
				 String[] configElements = tempStr.split("\\s+");
				 PeerStateInfo stateOfPeer = new PeerStateInfo(configElements[0], configElements[1], configElements[2], itr);
		         peersStateList.addElement(stateOfPeer);
		         itr++;
			}
			configReader.close();
		}
		catch (Exception err)
		{
			System.out.println("error:" +err.toString());
		}
	}

	public static synchronized boolean isFinish() {

		try {
			FileReader fr = new FileReader("PeerInfo.cfg");
			BufferedReader in = new BufferedReader(fr);

			int hasFileCount = 1;
			String record;

			while ((record = in.readLine()) != null) {
				int tempCount = Integer.parseInt(record.trim().split("\\s+")[3]);
				hasFileCount = hasFileCount * tempCount;
			}
			boolean res = false;
			if(hasFileCount != 0)
				res = true;

			in.close();
			return res;

		} catch (Exception err) {
			return false;
		}
	}

	/**
	
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try 
		{
			RemotePeerInitiator driver = new RemotePeerInitiator();
			driver.getConfig();

			String currDirPath = System.getProperty("user.dir");

			for (int i = 0; i < driver.peersStateList.size(); i++) {
				PeerStateInfo stateOfPeer = (PeerStateInfo) driver.peersStateList.elementAt(i);
				System.out.println("Start peer " + stateOfPeer.PID +  " residing at " + stateOfPeer.address);
				String command = "ssh " + stateOfPeer.address + " cd " + currDirPath + "; java peerProcess " + stateOfPeer.PID;
				driver.peersList.add(Runtime.getRuntime().exec(command));
				System.out.println(command);
			}		
			
			System.out.println("file is propogating" );
			
			boolean hasFinished = false;
			while(true) {
				hasFinished = isFinish();
				if (hasFinished) {
					System.out.println("All peers have been stopped");
					break;
				}
				else {
					try {
						int sleepTime = 5000;
						Thread.currentThread();
						Thread.sleep(sleepTime);
					} catch (InterruptedException err) {
					}
				}
			}
		}
		catch (Exception err) {
			System.out.println("Error occurred: " + err.toString());
		}
	}
}