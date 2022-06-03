
public interface DefMessageConfig {
	public static final String characterSetMessage = "UTF8";

	public static final String Choke_data_message = "0";
	public static final String unchoke_data_message = "1";

	public static final String interested_data_message = "2";
	public static final String nInterested_data_message = "3";


	public static final String has_dataMessage = "4";
	public static final String btField_dataMessage = "5";
	public static final String reqDataMessage = "6";
	public static final String pieceDataMessage = "7";
	public static final String headerHandshake = "CNTPROJ";



	public static final int LENGTH_DATA_MSG = 4;
	public static final int typeOfDataMessage = 1;
	public static final int dataPieceindexLength = 4;

	public static final int handShakeMessageLength = 32;
	public static final int handShakeHeaderLength = 18;
	public static final int handShakeZeroBitsLength = 10;
	public static final int handShakePiDLength = 4;
}