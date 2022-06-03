
public class DataPiece
{
	public int doesPieceExist;
	public String senderPID;
	public byte [] DataPiece;
	public int dataPieceIndex;

	/**
	 * @return
	 */
	public int getPieceExist() {
		return doesPieceExist;
	}

	/**
	 * @param doesPieceExist
	 */
	public void setDoesPieceExist(int doesPieceExist) {
		this.doesPieceExist = doesPieceExist;
	}

	/**
	 * @return
	 */
	public String getSenderPID() {
		return senderPID;
	}

	/**
	 * @param senderPID
	 */
	public void setSenderPID(String senderPID) {
		this.senderPID = senderPID;
	}

	public DataPiece()
	{
		DataPiece = new byte[configCommon.pieceSize];
		dataPieceIndex = -1;
		doesPieceExist = 0;
		senderPID = null;
	}
	/**
	 * @param payloadData
	 * @return piece index
	 */
	public static DataPiece extractDataPieceFromPayload(byte[] payloadData) {
		byte[] bi = new byte[DefMessageConfig.dataPieceindexLength];
		DataPiece piece = new DataPiece();
		System.arraycopy(payloadData, 0, bi, 0, DefMessageConfig.dataPieceindexLength);
		piece.dataPieceIndex = TransformDataUtil.ToIntegerFromBytes(bi);
		piece.DataPiece = new byte[payloadData.length- DefMessageConfig.dataPieceindexLength];
		System.arraycopy(payloadData, DefMessageConfig.dataPieceindexLength, piece.DataPiece, 0, payloadData.length- DefMessageConfig.dataPieceindexLength);
		return piece;
	}
}
