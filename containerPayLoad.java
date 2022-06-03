
public class containerPayLoad
{
	PayLoad messageData;
	String senderPID;


	public containerPayLoad()
	{
		messageData = new PayLoad();
		senderPID = null;
	}

	/**
	
	 * @param senderPID
	 */
    public void setSenderPID(String senderPID) {
        this.senderPID = senderPID;
    }

	/**
	 
	 * @param messageData
	 */
	public void setMessageData(PayLoad messageData) {
        this.messageData = messageData;
    }

	/**
	
	 * @return
	 */
	public PayLoad getMessageData() {
		return messageData;
	}

	/**
	 * @return
	 */
	public String getSenderPID() {
		return senderPID;
	}

}
