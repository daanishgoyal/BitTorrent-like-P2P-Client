
import java.io.*;

public class HandShake implements DefMessageConfig
{
	private byte[] handshakeHeader = new byte[handShakeHeaderLength];
	private byte[] zeroBits = new byte[handShakeZeroBitsLength];
	private byte[] PID = new byte[handShakePiDLength];
	private String msgHead;
	private String msgPID;

	public HandShake(){ }

	/**
	 * @param String header, String pid
	 */
	public HandShake(String header, String pid) {
		try {
			this.msgHead = header;
			this.handshakeHeader = header.getBytes(characterSetMessage);
			if (this.handshakeHeader.length > handShakeHeaderLength)
				throw new Exception("Header is too large");
			this.msgPID = pid;
			this.PID = pid.getBytes(characterSetMessage);
			if (this.PID.length > handShakeHeaderLength)
				throw new Exception("Peer ID is invalid");
			this.zeroBits = "0000000000".getBytes(characterSetMessage);
		} catch (Exception e) {
			peerProcess.writeLog(e.toString());
		}
	}

	/**
	 * @param handShakeHeader
	 */
	public void setHandshakeHeader(byte[] handShakeHeader) {
		try {
			this.msgHead = (new String(handShakeHeader, characterSetMessage)).toString().trim();
			this.handshakeHeader = this.msgHead.getBytes();
		} catch (UnsupportedEncodingException e) {
			peerProcess.writeLog(e.toString());
		}
	}

	/**
	 * @param PID
	 */
	public void setPID(byte[] PID) {
		try {
			this.msgPID = (new String(PID, characterSetMessage)).toString().trim();
			this.PID = this.msgPID.getBytes();

		} catch (UnsupportedEncodingException e) {
			peerProcess.writeLog(e.toString());
		}
	}

	/**
	 * @param pid
	 */
	public void setPID(String pid) {
		try {
			this.msgPID = pid;
			this.PID = pid.getBytes(characterSetMessage);
		} catch (UnsupportedEncodingException e) {
			peerProcess.writeLog(e.toString());
		}
	}

	/**
	 * @param mh
	 */
	public void setHeader(String mh) {
		try {
			this.msgHead = mh;
			this.handshakeHeader = mh.getBytes(characterSetMessage);
		} catch (UnsupportedEncodingException e) {
			peerProcess.writeLog(e.toString());
		}
	}
	
	public byte[] getHandshakeHeader() {
		return handshakeHeader;
	}

	public byte[] getPID() {
		return PID;
	}

	public byte[] getZeroBits() {
		return zeroBits;
	}

	public String getHeaderString() {
		return msgHead;
	}


	public String getPeerIDString() {
		return msgPID;
	}

	/**
	 * This method converts handshake Message byte array to simple handshake Message
	 * @param input
	 */
	public static HandShake ToHandShakeType(byte[] input) {
		HandShake handShake = null;
		byte[] PIDmsg = null;
		byte[] headerMsg = null;

		try {
			if (input.length != handShakeMessageLength)
				throw new Exception("error");

			handShake = new HandShake();
			headerMsg = new byte[handShakeHeaderLength];
			PIDmsg = new byte[handShakePiDLength];
			System.arraycopy(input, 0, headerMsg, 0, handShakeHeaderLength);
			System.arraycopy(input, handShakeHeaderLength + handShakeZeroBitsLength, PIDmsg, 0, handShakePiDLength);

			handShake.setHandshakeHeader(headerMsg);
			handShake.setPID(PIDmsg);

		} catch (Exception e) {
			peerProcess.writeLog(e.toString());
			handShake = null;
		}
		return handShake;
	}

	/**
	 * @param hsMsg
	 */
	public static byte[] ToByteArray(HandShake hsMsg) {

		byte[] result = new byte[handShakeMessageLength];

		try {
			if (hsMsg.getHandshakeHeader() == null) {
				throw new Exception("Handshake not valid");
			}
			if (hsMsg.getHandshakeHeader().length > handShakeHeaderLength || hsMsg.getHandshakeHeader().length == 0)
			{
				throw new Exception("Handshake not valid");
			} else {
				System.arraycopy(hsMsg.getHandshakeHeader(), 0, result, 0, hsMsg.getHandshakeHeader().length);
			}

			if (hsMsg.getZeroBits() == null) {
				throw new Exception("error");
			} 
			if (hsMsg.getZeroBits().length > handShakeZeroBitsLength || hsMsg.getZeroBits().length == 0) {
				throw new Exception("error");
			} else {
				System.arraycopy(hsMsg.getZeroBits(), 0, result, handShakeHeaderLength, handShakeZeroBitsLength - 1);
			}

			if (hsMsg.getPID() == null)
			{
				throw new Exception("error");
			} 
			else if (hsMsg.getPID().length > handShakePiDLength || hsMsg.getPID().length == 0)
			{
				throw new Exception("error");
			} 
			else 
			{
				System.arraycopy(hsMsg.getPID(), 0, result, handShakeHeaderLength + handShakeZeroBitsLength, hsMsg.getPID().length);
			}
		} 
		catch (Exception e) 
		{
			peerProcess.writeLog(e.toString());
			result = null;
		}
		return result;
	}
}
