import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;



public class Main
{

    /**
     * 一句话功能简述<p>
     * 功能详细描述
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException
    {
        try
        {
            File destFile = new File("F:/I9100_newbee_5.01.27.11_release_6ec3c923d3_4.1.2_V2_10002_V5.6.zip");

            Socket socket = new Socket("192.168.1.105",
                    9898);
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            byte[] paramData = ("token=认证信息&md5=" + Utils.getMd5ByFile(destFile)
                    + "&fileLength=" + destFile.length()).getBytes("utf-8");
            
            byte[] dataLen = new byte[4];
    		ByteBuffer.wrap(dataLen).putInt(paramData.length);
            
            os.write(dataLen);
            os.write(paramData);

            int command = readCommand(is);
            System.out.println("command >>> " + command);

            if (command == 2) // 开始上传
            {
                FileInputStream fis = new FileInputStream(destFile);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = fis.read(buffer)) != -1)
                {
                    os.write(buffer,
                            0,
                            len);
                }
                os.flush();
                fis.close();

                command = readCommand(is);
                System.out.println("command >>> " + command);

            }
            os.close();
            is.close();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static int readCommand(InputStream is)
    {
        byte[] buffer = new byte[4];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = -1;
        try
        {
            while ((len = is.read(buffer)) != -1)
            {
                baos.write(buffer,
                        0,
                        len);
                if (baos.toByteArray().length == 4)
                {
                    break;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        byte[] data = baos.toByteArray();
        if (data.length != 4)
        {
            return -1;
        }
        return byteArrayToInt(data);
    }

    public static int byteArrayToInt(byte[] b)
    {
        return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    }
}
