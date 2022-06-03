
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


public class PayLoad implements DefMessageConfig
{
    private String payloadType;
    private String payloadLen;
	private byte[] payload = null;
	private int messageLenght = typeOfDataMessage;
	private byte[] msgCategory = null;
	private byte[] size = null;


	public PayLoad() { }

	/**
	 
	 * @param msgBit
	 */
    public PayLoad(String msgBit) {
        try {
            if (Arrays.asList(Choke_data_message, unchoke_data_message, interested_data_message, nInterested_data_message).contains(msgBit))
            {
                this.setPayloadLen(1);
                this.setPayloadType(msgBit);
                this.payload = null;
            }
            else
                throw new Exception("error");
        } catch (Exception exp) {
            peerProcess.writeLog(exp.toString());
        }
    }

	/**
	 
	 * @param msgBit
	 * @param byteData
	 */
	public PayLoad(String msgBit, byte[] byteData)
	{
		try 
		{
			if (byteData != null)
			{
                this.setPayloadLen(byteData.length + 1);
                if (this.size.length > LENGTH_DATA_MSG)
                    throw new Exception("error");
                this.setPayload(byteData);
			} 
			else
			{
                if (Arrays.asList(Choke_data_message, unchoke_data_message, interested_data_message, nInterested_data_message).contains(msgBit))
                {
                    this.setPayloadLen(1);
                    this.payload = null;
                }
                else
                    throw new Exception("error");
			}
			this.setPayloadType(msgBit);
			if (this.getPayloadType().length > typeOfDataMessage)
				throw new Exception("error.");
		} catch (Exception e) {
			peerProcess.writeLog(e.toString());
		}
	}

	/**
	 * @param payloadLen
	 */
	public void setPayloadLen(int payloadLen) {
        this.messageLenght = payloadLen;
        this.payloadLen = ((Integer) payloadLen).toString();
        this.size = TransformDataUtil.ToBytes(payloadLen);
    }

	/**

	 * @param bytes
	 */
	public void setMessageLength(byte[] bytes) {
		Integer temp = TransformDataUtil.ToIntegerFromBytes(bytes);
		this.payloadLen = temp.toString();
		this.size = bytes;
		this.messageLenght = temp;
	}

	public byte[] getPayloadLen() {
		return size;
	}


	public int getMessageLengthInt() {
		return this.messageLenght;
	}


	public void setPayloadType(byte[] byteArr) {
		try {
			this.payloadType = new String(byteArr, characterSetMessage);
			this.msgCategory = byteArr;
		} catch (UnsupportedEncodingException e) {
			peerProcess.writeLog(e.toString());
		}
	}


	public void setPayloadType(String msgBit) {
		try {
			this.payloadType = msgBit.trim();
			this.msgCategory = this.payloadType.getBytes(characterSetMessage);
		} catch (UnsupportedEncodingException e) {
			peerProcess.writeLog(e.toString());
		}
	}

	
	public byte[] getPayloadType() {
		return msgCategory;
	}

	
	public void setPayload(byte[] message) {
		this.payload = message;
	}

	
	public byte[] getPayload() {
		return payload;
	}

	
	public String getPayloadTypeString() {
		return payloadType;
	}

	
	public String toString() {
		String tempString = null;
		try {
			tempString = "[Payload] : Payload Length - " + this.payloadLen + ", Payload Type - " + this.payloadType + ", Message - " + (new String(this.payload, characterSetMessage)).toString().trim();
		} catch (UnsupportedEncodingException e) {
			peerProcess.writeLog(e.toString());
		}
		return tempString;
	}

	/**
	 * @param msg
	 */
    public static byte[] toByteArray(PayLoad msg)
    {
        byte[] result = null;
        int msgBit;
        try
        {
            msgBit = Integer.parseInt(msg.getPayloadTypeString());
            if (msg.getPayloadLen().length > LENGTH_DATA_MSG)
                throw new Exception("Payload length not valid");
			else if (msg.getPayloadLen() == null)
				throw new Exception("Payload length not valid");
            else if (msgBit < 0 || msgBit > 7)
                throw new Exception("Payload type not valid");
            else if (msg.getPayloadType() == null)
                throw new Exception("Payload type not valid");

            if (msg.getPayload() != null) {
                result = new byte[LENGTH_DATA_MSG + typeOfDataMessage + msg.getPayload().length];
                System.arraycopy(msg.getPayloadLen(), 0, result, 0, msg.getPayloadLen().length);
                System.arraycopy(msg.getPayloadType(), 0, result, LENGTH_DATA_MSG, typeOfDataMessage);
                System.arraycopy(msg.getPayload(), 0, result, LENGTH_DATA_MSG + typeOfDataMessage, msg.getPayload().length);
            }
            else {
                result = new byte[LENGTH_DATA_MSG + typeOfDataMessage];
                System.arraycopy(msg.getPayloadLen(), 0, result, 0, msg.getPayloadLen().length);
                System.arraycopy(msg.getPayloadType(), 0, result, LENGTH_DATA_MSG, typeOfDataMessage);
            }
        }
        catch (Exception e)
        {
            peerProcess.writeLog(e.toString());
            result = null;
        }
        return result;
    }

	
	public static PayLoad fromByteArray(byte[] Message) {
		PayLoad message = new PayLoad();
		byte[] payLoad = null;
		byte[] payloadLen = new byte[LENGTH_DATA_MSG];
		byte[] payloadType = new byte[typeOfDataMessage];
		int l;
		try 
		{
			if (Message == null)
				throw new Exception("Message not valid");
			else if (Message.length < LENGTH_DATA_MSG + typeOfDataMessage)
				throw new Exception("Too small payload");
			System.arraycopy(Message, 0, payloadLen, 0, LENGTH_DATA_MSG);
			System.arraycopy(Message, LENGTH_DATA_MSG, payloadType, 0, typeOfDataMessage);
			message.setMessageLength(payloadLen);
			message.setPayloadType(payloadType);
			l = TransformDataUtil.ToIntegerFromBytes(payloadLen);
			if (l > 1)
			{
				payLoad = new byte[l-1];
				System.arraycopy(Message,
						LENGTH_DATA_MSG + typeOfDataMessage,
						payLoad, 0,
						Message.length - LENGTH_DATA_MSG - typeOfDataMessage);
				message.setPayload(payLoad);
			}
			payLoad = null;
		} 
		catch (Exception e) 
		{
			peerProcess.writeLog(e.toString());
			message = null;
		}
		return message;
	}

    public String getMessageTypeString() {
        return this.payloadType;
    }
}
