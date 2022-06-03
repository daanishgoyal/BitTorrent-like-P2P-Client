

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;


public class MessageBitField implements DefMessageConfig
{

	public DataPiece[] pieceList;
	public int pieceSize;


	public MessageBitField()
	{
		pieceSize = (int) Math.ceil(((double) configCommon.FileSize / (double) configCommon.pieceSize));
		this.pieceList = new DataPiece[pieceSize];

		for (int k = 0; k < this.pieceSize; k++){
			this.pieceList[k] = new DataPiece();
		}
			

	}

	public int getPiecesSize() {
		return pieceSize;
	}

	public void setPiecesSize(int pieceSize) {
		this.pieceSize = pieceSize;
	}


	public DataPiece[] getPiecesList() {
		return pieceList;
	}


	public void setPiecesList(DataPiece[] pieceList) {
		this.pieceList = pieceList;
	}

	public byte[] toBytes()
	{
		return this.fetchDataBytes();
	}

	/**
	 * @param byteList
	 * @return MessageBitField
	 */
	public static MessageBitField fromBytes(byte[] byteList)
	{
		int numMsgTypes=7;
		MessageBitField bitFieldMsg = new MessageBitField();
		for(int i = 0 ; i < byteList.length; i ++)
		{
			while(numMsgTypes >= 0)
			{
				int leftShift = 1 << numMsgTypes;
				if(i * 8 + (8-numMsgTypes-1) < bitFieldMsg.pieceSize)
				{
					if((byteList[i] & (leftShift)) != 0)
						bitFieldMsg.pieceList[i * 8 + (8-numMsgTypes-1)].doesPieceExist = 1;
					else
						bitFieldMsg.pieceList[i * 8 + (8-numMsgTypes-1)].doesPieceExist = 0;
				}
				numMsgTypes--;
			}
		}
		
		return bitFieldMsg;
	}

	/**
	 * @param MessageBitField obj
	 * @return boolean
	 */
	public synchronized boolean compare(MessageBitField bFieldMsg) {
		int bFieldMsgsizePiece = bFieldMsg.getPiecesSize();
		

		for (int i = 0; i < bFieldMsgsizePiece; i++) {
			if (bFieldMsg.getPiecesList()[i].getPieceExist() == 1
					&& this.getPiecesList()[i].getPieceExist() == 0) {
				return true;
			} else
				continue;
		}

		return false;
	}

	/**
	 * @param MessageBitField obj
	 * @return int
	 */
	public synchronized int firstDiff(MessageBitField bFieldMsg)
	{
		int sizePiece1 = this.getPiecesSize();
		int sizePiece2 = bFieldMsg.getPiecesSize();

		if (sizePiece1 >= sizePiece2) {
			for (int i = 0; i < sizePiece2; i++) {
				if (bFieldMsg.getPiecesList()[i].getPieceExist() == 1
						&& this.getPiecesList()[i].getPieceExist() == 0) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < sizePiece1; i++) {
				if (bFieldMsg.getPiecesList()[i].getPieceExist() == 1
						&& this.getPiecesList()[i].getPieceExist() == 0) {
					return i;
				}
			}
		}
		
		return -1;
	}

	/**
	 * @return byte[]
	 */
	public byte[] fetchDataBytes()
	{
		int subsizePiece = this.pieceSize / 8;
		if (pieceSize % 8 != 0)
			subsizePiece = subsizePiece + 1;
		int x = 0;
		byte[] bList = new byte[subsizePiece];
		int counter;
		int temp1 = 0;
		for (counter = 1; counter <= this.pieceSize; counter++)
		{
			int tPiece = this.pieceList[counter-1].doesPieceExist;
			temp1 = temp1 << 1;
			if (tPiece == 1)
			{
				temp1++;
			}

			if (counter % 8 == 0 && counter != 0) {
				bList[x] = (byte) temp1;
				x++;
				temp1 = 0;
			}
		}
		if ((counter-1) % 8 != 0)
		{
			int ts = ((pieceSize) - (pieceSize / 8) * 8);
			temp1 = temp1 << (8 - ts);
			bList[x] = (byte) temp1;
		}
		return bList;
	}

	/**
	 * @param myPID
	 * @param isFilePresent
	 * */
	public void createMyBitField(String myPID, int isFilePresent) {

		// File does not exist.
		if (isFilePresent != 1) {
			for (int i = 0; i < this.pieceSize; i++) {
				this.pieceList[i].setDoesPieceExist(0);
				this.pieceList[i].setSenderPID(myPID);
			}

		}
		else {
			for (int i = 0; i < this.pieceSize; i++) {
				this.pieceList[i].setDoesPieceExist(1);
				this.pieceList[i].setSenderPID(myPID);
			}

		}

	}

	/**
	 * @param PID
	 * @param pieceObj
	 * */
	public synchronized void modifyBitField(String PID, DataPiece pieceObj) {
		try 
		{
			if (peerProcess.myBFM.pieceList[pieceObj.dataPieceIndex].doesPieceExist == 1) {
				peerProcess.writeLog(PID + " Piece already arrived ");
			} 
			else 
			{
				String fn = configCommon.FileName;
				File props = new File(peerProcess.PID, fn);
				int checkpoint = pieceObj.dataPieceIndex * configCommon.pieceSize;
				RandomAccessFile rand = new RandomAccessFile(props, "rw");
				byte[] bw;
				bw = pieceObj.DataPiece;
				rand.seek(checkpoint);
				rand.write(bw);
				this.pieceList[pieceObj.dataPieceIndex].setDoesPieceExist(1);
				this.pieceList[pieceObj.dataPieceIndex].setSenderPID(PID);
				rand.close();
				
				peerProcess.writeLog(peerProcess.PID + " recieved the piece " + pieceObj.dataPieceIndex + " from Peer " + PID + ". Number of pieces currently holding:  " + peerProcess.myBFM.myCurrentPieces());

				if (peerProcess.myBFM.isFinish())
				{
					peerProcess.peerInfoMap.get(peerProcess.PID).isInterested = 0;
					peerProcess.peerInfoMap.get(peerProcess.PID).isCompleted = 1;
					peerProcess.peerInfoMap.get(peerProcess.PID).isChoked = 0;
					modifyInfoConfig(peerProcess.PID, 1);
					peerProcess.writeLog(peerProcess.PID + " File transfer done. All pieces recieved.");
				}
			}
		} catch (Exception exp) {
			peerProcess.writeLog(peerProcess.PID + " Could not modify bitfield " + exp.getMessage());
		}
	}

    public int myCurrentPieces()
    {
        int ct = 0;
        for (int i = 0; i < this.pieceSize; i++) {
			if (this.pieceList[i].doesPieceExist == 1) {
				ct++;
			}
		}
        return ct;
    }
    public boolean isFinish() {
        for (int i = 0; i < this.pieceSize; i++) {
            if (this.pieceList[i].doesPieceExist == 0) {
                return false;
            }
        }
        return true;
    }

    /**
	 * @param PID
	 * @param isFilePresent
	 * */
	public void modifyInfoConfig(String PID, int isFilePresent)
	{
		BufferedReader iBuffer = null;
		BufferedWriter oBuffer = null;
		try
		{
			String record;
			StringBuffer sb = new StringBuffer();
			FileReader fr = new FileReader("PeerInfo.cfg");
			iBuffer = new BufferedReader(fr);

			while((record = iBuffer.readLine()) != null)
			{
				if(record.trim().split("\\s+")[0].equals(PID))
					sb.append(record.trim().split("\\s+")[0]
							+ " " + record.trim().split("\\s+")[1]
							+ " " + record.trim().split("\\s+")[2]
							+ " " + isFilePresent);
				else
					sb.append(record);
				sb.append("\n");
			}
			
			iBuffer.close();
			FileWriter fw = new FileWriter("PeerInfo.cfg");
			oBuffer= new BufferedWriter(fw);
			oBuffer.write(sb.toString());
			oBuffer.close();
		} 
		catch (Exception exp)
		{
			peerProcess.writeLog(PID + "cannot modify file " +  exp.getMessage());
		}
	}
	
	
	
}
