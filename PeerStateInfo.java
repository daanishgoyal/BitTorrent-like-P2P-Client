
import java.util.Date;

public class PeerStateInfo implements Comparable<PeerStateInfo>
{
	public String PID;
	public String port;
	public String address;
	public MessageBitField bitField;
	public Date start;
	public Date finish;
	public int isInvokedFirst;
	public int isInterested = 1;
	public int isChoked = 1;
	public int isOptimisticUnchoked = 0;
	public int isFavorableNbr = 0;
	public int isCompleted = 0;
	public int Index;
	public int state = -1;
	public int isHandShakeDone = 0;
	public double rate = 0;

	/**
	 
	 * @param id
	 * @param addr
	 * @param portNum
	 * @param ind
	 */
	public PeerStateInfo(String id, String addr, String portNum, int ind)
	{
		PID = id;
		address = addr;
		port = portNum;
		bitField = new MessageBitField();
		Index = ind;
	}

	/**
	 
	 * @param id
	 * @param addr
	 * @param portNum
	 * @param isInvokedFirst
	 * @param ind
	 */
	public PeerStateInfo(String id, String addr, String portNum, int isInvokedFirst, int ind)
	{
		PID = id;
		address = addr;
		port = portNum;
		this.isInvokedFirst = isInvokedFirst;
		bitField = new MessageBitField();
		Index = ind;
	}

	/**
	 
	 * @return peerId
	 */
	public String getPID() {
		return PID;
	}

	/**
	 
	 * @param PeerID
	 */
	public void setPID(String PID) {
		this.PID = PID;
	}

	/**
	
	 * @return IP
	 */
	public String getAddress() {
		return address;
	}

	/**
	
	 * @param IPaddress
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	
	 * @return portnumber
	 */
	public String getPort() {
		return port;
	}

	/**
	
	 * @param port number
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	
	 * @return
	 */
	public int getIsInvokedFirst() {
		return isInvokedFirst;
	}

	/**
	 
	 * @param isInvokedFirst
	 */
	public void setIsInvokedFirst(int isInvokedFirst) {
		this.isInvokedFirst = isInvokedFirst;
	}

	/**
	
	 * @param o1
	 * @return
	 */
	public int compareTo(PeerStateInfo o1) {
		if (this.rate == o1.rate) return 0;
		else if (this.rate > o1.rate) return 1;
		else return -1;
	}
}
