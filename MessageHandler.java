
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;

public class MessageHandler implements Runnable, DefMessageConfig
{
	public static int stateOfPeer = -1;
	private static String peerID = null;
	private static boolean isActive = true;
	RandomAccessFile accessFile;

	/**
	 * @param pid
	 */
	public MessageHandler(String pid) {
		peerID = pid;
	}

	public MessageHandler() { }


	public void metaShow(String dataType, int state) {
		peerProcess.writeLog("Message Processor  = "+ dataType + " State = "+state);
	}

	public void run() {
		String messageCategory;
		PayLoad pl;
		containerPayLoad containerPayLoad;
		String remotePID;

		while(isActive)
		{
			containerPayLoad  = peerProcess.deleteFromMessageQ();
			while(containerPayLoad == null) {
				Thread.currentThread();
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException err)
				{
				   err.printStackTrace();
				}
				containerPayLoad  = peerProcess.deleteFromMessageQ();
			}

			pl = containerPayLoad.getMessageData();
			messageCategory = pl.getMessageTypeString();
			remotePID = containerPayLoad.getSenderPID();

			int peerStateRemote = peerProcess.peerInfoMap.get(remotePID).state;

			if(messageCategory.equals(has_dataMessage) && peerStateRemote != 14) {
				peerProcess.writeLog(peerProcess.PID + " received the HAVE message by Peer " + remotePID);
				if(checkIfInterested(pl, remotePID)) {
					passInterestedMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
					peerProcess.peerInfoMap.get(remotePID).state = 9;
				}
				else {
					passNotInterestedMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
					peerProcess.peerInfoMap.get(remotePID).state = 13;
				}
			}
			else {
			 switch (peerStateRemote) {
			 case 2:
			   if (messageCategory.equals(btField_dataMessage))
			   {
		 		  peerProcess.writeLog(peerProcess.PID + " received a BITFIELD message from Peer " + remotePID);
	 			  passBitFieldMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
 				  peerProcess.peerInfoMap.get(remotePID).state = 3;
			   }
			   break;
			 case 3:
			   if (messageCategory.equals(nInterested_data_message))
			   {
					peerProcess.writeLog(peerProcess.PID + " received a NOT INTERESTED message from Peer " + remotePID);
					peerProcess.peerInfoMap.get(remotePID).isInterested = 0;
					peerProcess.peerInfoMap.get(remotePID).state = 5;
					peerProcess.peerInfoMap.get(remotePID).isHandShakeDone = 1;
			   }
			   else if (messageCategory.equals(interested_data_message))
			   {
					peerProcess.writeLog(peerProcess.PID + " received an INTERESTED message from Peer " + remotePID);
					peerProcess.peerInfoMap.get(remotePID).isInterested = 1;
					peerProcess.peerInfoMap.get(remotePID).isHandShakeDone = 1;
					if(!peerProcess.prefNbrMap.containsKey(remotePID) && !peerProcess.unchokedNbrMap.containsKey(remotePID))
					{
						passChokeMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
						peerProcess.peerInfoMap.get(remotePID).isChoked = 1;
						peerProcess.peerInfoMap.get(remotePID).state  = 6;
					}
					else {
						peerProcess.peerInfoMap.get(remotePID).isChoked = 0;
						passUnchokeMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
						peerProcess.peerInfoMap.get(remotePID).state = 4 ;
					}
			   }
			   break;
			 case 4:
				 if (messageCategory.equals(reqDataMessage)) {
            peerProcess.writeLog(peerProcess.PID + " received a REQUEST message from Peer " + remotePID);
						transferDataPiece(peerProcess.pIDSktMap.get(remotePID), pl, remotePID);
						if(!peerProcess.prefNbrMap.containsKey(remotePID)
								&& !peerProcess.unchokedNbrMap.containsKey(remotePID)) {
							passChokeMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
							peerProcess.peerInfoMap.get(remotePID).isChoked = 1;
							peerProcess.peerInfoMap.get(remotePID).state = 6;
						}
				 }
				 break;
			 case 8:
				 if (messageCategory.equals(btField_dataMessage)) {
						if(checkIfInterested(pl,remotePID)) {
							passInterestedMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
							peerProcess.peerInfoMap.get(remotePID).state = 9;
						}
						else {
							passNotInterestedMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
							peerProcess.peerInfoMap.get(remotePID).state = 13;
						}
				 }
				 break;
			 case 9:
				 if (messageCategory.equals(Choke_data_message)) {
						peerProcess.writeLog(peerProcess.PID + " CHOKED by Peer " + remotePID);
						peerProcess.peerInfoMap.get(remotePID).state = 14;
				 }
				 else if (messageCategory.equals(unchoke_data_message)) {
						peerProcess.writeLog(peerProcess.PID + " UN-CHOKED by Peer " + remotePID);
						int mismatchInd = peerProcess.myBFM.firstDiff(peerProcess.peerInfoMap.get(remotePID).bitField);
						if(mismatchInd != -1) {
							dispatchMessage(peerProcess.pIDSktMap.get(remotePID), mismatchInd, remotePID);
							peerProcess.peerInfoMap.get(remotePID).state = 11;
							peerProcess.peerInfoMap.get(remotePID).start = new Date();
						}
						else
							peerProcess.peerInfoMap.get(remotePID).state = 13;
				 }
				 break;
			 case 11:
				 if (messageCategory.equals(pieceDataMessage)) {
					    byte[] payloadBuff = pl.getPayload();
						peerProcess.peerInfoMap.get(remotePID).finish = new Date();
						long elapsedTime = peerProcess.peerInfoMap.get(remotePID).finish.getTime() - peerProcess.peerInfoMap.get(remotePID).start.getTime();
						int totalData = payloadBuff.length + LENGTH_DATA_MSG + typeOfDataMessage;
						peerProcess.peerInfoMap.get(remotePID).rate = ((double)(totalData)/(double)elapsedTime) * 100;

					
						DataPiece dataPiece = DataPiece.extractDataPieceFromPayload(payloadBuff);

						
						peerProcess.myBFM.modifyBitField(remotePID, dataPiece);

						int nextPieceToFetchInd = peerProcess.myBFM.firstDiff(peerProcess.peerInfoMap.get(remotePID).bitField);
						if(nextPieceToFetchInd != -1) {
							dispatchMessage(peerProcess.pIDSktMap.get(remotePID),nextPieceToFetchInd, remotePID);
							peerProcess.peerInfoMap.get(remotePID).state  = 11;

							peerProcess.peerInfoMap.get(remotePID).start = new Date();
						}
						else
							peerProcess.peerInfoMap.get(remotePID).state = 13;

						peerProcess.getPeerInfoRepeat();

						Enumeration<String> peerStatesList = peerProcess.peerInfoMap.keys();
						while(peerStatesList.hasMoreElements())
						{
							String pState = (String) peerStatesList.nextElement();
							PeerStateInfo stateOfPeer = peerProcess.peerInfoMap.get(pState);

							if(pState.equals(peerProcess.PID))
								continue;

							if (stateOfPeer.isCompleted == 0 && stateOfPeer.isChoked == 0 && stateOfPeer.isHandShakeDone == 1) {
								passHaveMsg(peerProcess.pIDSktMap.get(pState), pState);
								peerProcess.peerInfoMap.get(pState).state = 3;
							}
						}

						payloadBuff = null;
						pl = null;
				 }
				 else if (messageCategory.equals(Choke_data_message)) {
						peerProcess.writeLog(peerProcess.PID + " CHOKED by Peer " + remotePID);
						peerProcess.peerInfoMap.get(remotePID).state = 14;
				 }
				 break;
			 case 14:
				 if (messageCategory.equals(has_dataMessage)) {
						if(checkIfInterested(pl,remotePID)) {
							passInterestedMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
							peerProcess.peerInfoMap.get(remotePID).state = 9;
						}
						else {
							passNotInterestedMsg(peerProcess.pIDSktMap.get(remotePID), remotePID);
							peerProcess.peerInfoMap.get(remotePID).state = 13;
						}
				 }
				 else if (messageCategory.equals(unchoke_data_message)) {
						peerProcess.writeLog(peerProcess.PID + " UN-CHOKED by Peer " + remotePID);
						peerProcess.peerInfoMap.get(remotePID).state = 14;
				 }
				 break;
			 }
			}
		}
	}

	/**
	 * @param skt
	 * @param pl
	 * @param remPId
	 */
	private void transferDataPiece(Socket skt, PayLoad pl, String remPId)
	{
		byte[] byteArr = pl.getPayload();
		int piecePosition = TransformDataUtil.ToIntegerFromBytes(byteArr);

		peerProcess.writeLog(peerProcess.PID + " transferring the piece " + piecePosition + " to the Peer " + remPId);

		byte[] receivedBytes = new byte[configCommon.pieceSize];
		int totalReceivedBytes = 0;

		File f = new File(peerProcess.PID, configCommon.FileName);
		try {
			accessFile = new RandomAccessFile(f,"r");
			accessFile.seek(piecePosition* configCommon.pieceSize);
			totalReceivedBytes = accessFile.read(receivedBytes, 0, configCommon.pieceSize);
		}
		catch (IOException e) {
			peerProcess.writeLog(peerProcess.PID + " error while reading the byte " +  e.toString());
		}
		if( totalReceivedBytes == 0){
			peerProcess.writeLog(peerProcess.PID + " No bytes read from the file");
		}
		else if (totalReceivedBytes < 0){
			peerProcess.writeLog(peerProcess.PID + " improper file load");
		}

		byte[] dataBuff = new byte[totalReceivedBytes + DefMessageConfig.dataPieceindexLength];
		System.arraycopy(byteArr, 0, dataBuff, 0, DefMessageConfig.dataPieceindexLength);
		System.arraycopy(receivedBytes, 0, dataBuff, DefMessageConfig.dataPieceindexLength, totalReceivedBytes);

		PayLoad dispatchedMsg = new PayLoad(pieceDataMessage, dataBuff);
		byte[] byteArray =  PayLoad.toByteArray(dispatchedMsg);
		passPayload(skt, byteArray);

		receivedBytes = null;
		dispatchedMsg = null;
		byteArray = null;
		byteArr = null;
		dataBuff = null;

		try
		{
			accessFile.close();
		}
		catch(Exception e){
		}
	}

	/**
	 * @param skt
	 * @param pieceNumber
	 * @param remotePID
	 */
	private void dispatchMessage(Socket skt, int pieceNumber, String remotePID) {

		byte[] byteFormatPiece = new byte[DefMessageConfig.dataPieceindexLength];

		for (int i = 0; i < DefMessageConfig.dataPieceindexLength; i++) {
			byteFormatPiece[i] = 0;
		}

		byte[] byFormatPieceIndex = TransformDataUtil.ToBytes(pieceNumber);
		System.arraycopy(byFormatPieceIndex, 0, byteFormatPiece, 0, byFormatPieceIndex.length);
		PayLoad pl = new PayLoad(reqDataMessage, byteFormatPiece);
		byte[] plBytes = PayLoad.toByteArray(pl);
		passPayload(skt, plBytes);

		byFormatPieceIndex = null;
		byteFormatPiece = null;
		pl = null;
		plBytes = null;
	}

	/**
	 * @param skt
	 * @param remPId
	 */
	private void passNotInterestedMsg(Socket skt, String remPId)
	{
		peerProcess.writeLog(peerProcess.PID + " passing NOT INTERESTED message to the Peer " + remPId);
		PayLoad pl =  new PayLoad(nInterested_data_message);
		byte[] bytesPl = PayLoad.toByteArray(pl);
		passPayload(skt,bytesPl);
	}

	/**
	 * @param skt
	 * @param remPId
	 */
	private void passBitFieldMsg(Socket skt, String remPId) {
		peerProcess.writeLog(peerProcess.PID + " passing BITFIELD message to the Peer " + remPId);
		byte[] convertedByteArr = peerProcess.myBFM.toBytes();
		PayLoad pl = new PayLoad(btField_dataMessage, convertedByteArr);
		passPayload(skt, PayLoad.toByteArray(pl));
		convertedByteArr = null;
	}

	/**
	 * @param pl
	 * @param remPId
	 */
	private boolean checkIfInterested(PayLoad pl, String remPId) {
		MessageBitField bfm = MessageBitField.fromBytes(pl.getPayload());
		peerProcess.peerInfoMap.get(remPId).bitField = bfm;

		if (peerProcess.myBFM.compare(bfm))
		{
			return true;
		}
		return false;
	}

	/**
	 * @param skt
	 * @param remPId
	 */
	private void passUnchokeMsg(Socket skt, String remPId) {
		peerProcess.writeLog(peerProcess.PID + " passing UNCHOKE message to the Peer " + remPId);
		PayLoad pl = new PayLoad(unchoke_data_message);
		byte[] bytesPl = PayLoad.toByteArray(pl);
		passPayload(skt,bytesPl);
	}

	/**
	 * @param skt
	 * @param convertedByteArr
	 */
	private int passPayload(Socket skt, byte[] convertedByteArr) {
		try {
			OutputStream outputStream = skt.getOutputStream();
			outputStream.write(convertedByteArr);
		} catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	/**
	 * @param skt
	 * @param remPId
	 */
	private void passInterestedMsg(Socket skt, String remPId) {
		peerProcess.writeLog(peerProcess.PID + " passing INTERESTED message to the Peer " + remPId);
		PayLoad pl =  new PayLoad(interested_data_message);
		byte[] bytedPl = PayLoad.toByteArray(pl);
		passPayload(skt,bytedPl);
	}

	/** 
	 * @param skt
	 * @param remPId
	 */
	private void passChokeMsg(Socket skt, String remPId) {
		peerProcess.writeLog(peerProcess.PID + " passing CHOKE message to the Peer " + remPId);
		PayLoad pl = new PayLoad(Choke_data_message);
		byte[] bytesPl = PayLoad.toByteArray(pl);
		passPayload(skt,bytesPl);
	}

	/**
	 * @param remPId
	 * @param skt
	 */
	private void passHaveMsg(Socket skt, String remPId) {
		peerProcess.writeLog(peerProcess.PID + " passing HAVE message to the Peer " + remPId);
		byte[] convertedByteArr = peerProcess.myBFM.toBytes();
		PayLoad pl = new PayLoad(has_dataMessage, convertedByteArr);
		passPayload(skt, PayLoad.toByteArray(pl));
		convertedByteArr = null;
	}
}
