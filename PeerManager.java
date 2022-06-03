
import java.net.*;
import java.io.*;
import java.util.Arrays;


public class PeerManager implements Runnable, DefMessageConfig
{
	private InputStream iStream;
	private Socket clientSkt = null;
	private int connectionType;
	private OutputStream oStream;

	private HandShake hndShkMsg;
	
	String myPID, remPID;
	
	final int CONNECTIONACTIVE = 1;
	final int CONNECTIONPASSIVE = 0;

	/**
	 * @param iStream
	 * @param skt
	 */
	public void openClose(InputStream iStream, Socket skt)
	{
		try {
			iStream = skt.getInputStream();
			iStream.close();
		} catch (IOException err) {
			err.printStackTrace();
		}
		
	}

	/**
	 * @param peerSkt
	 * @param connectionType
	 * @param myPID
	 */
	public PeerManager(Socket peerSkt, int connectionType, String myPID) {
		
		this.clientSkt = peerSkt;
		this.connectionType = connectionType;
		this.myPID = myPID;
		try 
		{
			oStream = peerSkt.getOutputStream();
			iStream = peerSkt.getInputStream();
		}
		catch (Exception err)
		{
			peerProcess.writeLog(this.myPID
								+ " Error : "
								+ err.getMessage());
		}
	}

	/**
	 
	 * @param address
	 * @param portNumber
	 * @param connectionType
	 * @param myPID
	 */
	public PeerManager(String address, int portNumber, int connectionType, String myPID)
	{	
		try {
			this.connectionType = connectionType;
			this.myPID = myPID;
			this.clientSkt = new Socket(address, portNumber);
		} 
		catch (UnknownHostException e) {
			peerProcess.writeLog(myPID + " Manager : " + e.getMessage());
		} 
		catch (IOException e) {
			peerProcess.writeLog(myPID + " Manager : " + e.getMessage());
		}
		this.connectionType = connectionType;
		try {
			iStream = clientSkt.getInputStream();
			oStream = clientSkt.getOutputStream();
		} 
		catch (Exception ex) {
			peerProcess.writeLog(myPID + "  Manager : " + ex.getMessage());
		}
	}

	/**
	 * Starts the handshake process and logs it.
	 * @return boolean
	 */
	public boolean InitiateHndShk()
	{
		try {
			HandShake hsObj = new HandShake(DefMessageConfig.headerHandshake, this.myPID);
			oStream.write(HandShake.ToByteArray(hsObj));
		} 
		catch (IOException err) {
			peerProcess.writeLog(this.myPID
					+ " Initiate Hand Shake : "
					+ err.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Thread method that is runnable.
	 */
	public void run() 
	{
		int bufferSize = 32;
		byte[] hndShkBuffer = new byte[bufferSize];
		byte[] noPayloadBufferData = new byte[LENGTH_DATA_MSG + typeOfDataMessage];
		byte[] messageLength;
		byte[] messageCategory;
		containerPayLoad containerPayLoad = new containerPayLoad();

		try{
			if(this.connectionType == CONNECTIONACTIVE) {
				if(!InitiateHndShk()) {
					peerProcess.writeLog(myPID
							+ " HANDSHAKE failed.");
					System.exit(0);
				}
				else
					peerProcess.writeLog(myPID + " Handshake initiated...");
				while(true) {
					iStream.read(hndShkBuffer);
					hndShkMsg = HandShake.ToHandShakeType(hndShkBuffer);
					if(hndShkMsg.getHeaderString().equals(DefMessageConfig.headerHandshake)) {
						remPID = hndShkMsg.getPeerIDString();
						peerProcess.writeLog(myPID
								+ " sending handshake 1 "
								+ remPID);
						peerProcess.writeLog(myPID
								+ " Received handshake " + remPID);

						peerProcess.pIDSktMap.put(remPID, this.clientSkt);
						break;
					}
					else continue;
				}
				
				// Here we are dispatching the Bit Field message
				PayLoad pl = new PayLoad(btField_dataMessage, peerProcess.myBFM.toBytes());
				byte[] bytePl = PayLoad.toByteArray(pl);
				oStream.write(bytePl);
				peerProcess.peerInfoMap.get(remPID).state = 8;
			}
			// Establishing a passive type socket connection
			else {
				while(true) {
					iStream.read(hndShkBuffer);
					hndShkMsg = HandShake.ToHandShakeType(hndShkBuffer);
					if(hndShkMsg.getHeaderString().equals(DefMessageConfig.headerHandshake)) {
						remPID = hndShkMsg.getPeerIDString();
						peerProcess.writeLog(myPID
								+ " sending handshake message to peer " + remPID);
						peerProcess.writeLog(myPID
								+ " got handshake from peer" + remPID);

						peerProcess.pIDSktMap.put(remPID, this.clientSkt);
						break;
					}
					else continue;
				}
				if(!InitiateHndShk()) {
					peerProcess.writeLog(myPID + " could not send handshake message.");
					System.exit(0);
				}
				else
					peerProcess.writeLog(myPID + " handshake has been acknowledged, TCP connection is established.");
				peerProcess.peerInfoMap.get(remPID).state = 2;
			}
			// receive data messages constantly from the peers
			while(true) {
				int headBytes = iStream.read(noPayloadBufferData);
				if(headBytes == -1)
					break;

				// creating message shell
				messageLength = new byte[LENGTH_DATA_MSG];
				messageCategory = new byte[typeOfDataMessage];
				System.arraycopy(noPayloadBufferData, 0, messageLength, 0, LENGTH_DATA_MSG);
				System.arraycopy(noPayloadBufferData, LENGTH_DATA_MSG, messageCategory, 0, typeOfDataMessage);

				// setting up the payload
				PayLoad dataPayload = new PayLoad();
				dataPayload.setPayloadType(messageCategory);
				dataPayload.setMessageLength(messageLength);

				if(Arrays.asList(DefMessageConfig.Choke_data_message, DefMessageConfig.unchoke_data_message,
						DefMessageConfig.interested_data_message, DefMessageConfig.nInterested_data_message)
						.contains(dataPayload.getMessageTypeString())){
					containerPayLoad.messageData = dataPayload;
					containerPayLoad.senderPID = this.remPID;
					peerProcess.appendToMessageQ(containerPayLoad);
				}

				else {
					int seen = 0;
					int seenBytes;
					byte[] dataBufferPl = new byte[dataPayload.getMessageLengthInt()-1];
					while(seen < dataPayload.getMessageLengthInt()-1){
						seenBytes = iStream.read(dataBufferPl, seen, dataPayload.getMessageLengthInt()-1-seen);
						if(seenBytes == -1)
							return;
						seen += seenBytes;
					}
					
					byte[] bufferWithPl = new byte [dataPayload.getMessageLengthInt() + LENGTH_DATA_MSG];
					System.arraycopy(noPayloadBufferData, 0, bufferWithPl, 0, LENGTH_DATA_MSG + typeOfDataMessage);
					System.arraycopy(dataBufferPl, 0, bufferWithPl, LENGTH_DATA_MSG + typeOfDataMessage, dataBufferPl.length);
					
					PayLoad msgWithPl = PayLoad.fromByteArray(bufferWithPl);
					containerPayLoad.messageData = msgWithPl;
					containerPayLoad.senderPID = remPID;
					peerProcess.appendToMessageQ(containerPayLoad);
					dataBufferPl = null;
					bufferWithPl = null;
					seen = 0;
					seenBytes = 0;
				}
			}
		}
		catch(IOException err){
			peerProcess.writeLog(myPID + " error: " + err);
		}
	}

}