
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransformDataUtil
{
    /**
     * Convert to bytes
     * @param input
     * @return byte array
     */
	public static byte[] ToBytes(int input)
	{
	    int cnst = 4;
        byte[] bytesArray = new byte[cnst];
        for (int i = 0; i < cnst; i++)
        {
            int os = (bytesArray.length - 1 - i) * 8;
            bytesArray[i] = (byte) ((input >>> os) & 0xFF);
        }
        return bytesArray;
    }

    /**
     * @param byteArr
     * @return
     */
    public static int ToIntegerFromBytes(byte[] byteArr) {
	    int deflt = 0;
        return ToIntegerFromBytes(byteArr, deflt);
    }

    /**
     * @param byteArr, offset/mask
     * @return
     */
    public static int ToIntegerFromBytes(byte[] bytesArray, int os)
    {
        int cnst = 4;
        int typesLen = 8;
        int output = 0;
        for (int i = 0; i < cnst; i++)
        {
            int mask = (cnst - 1 - i) * typesLen;
            output += (bytesArray[i + os] & 0x000000FF) << mask;
        }
        return output;
    }

    /**
     * @return current system time
     */
    public static String getCurrentTime() {
        Calendar instance = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(instance.getTime());
    }
}
