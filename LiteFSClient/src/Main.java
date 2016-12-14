import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;


public class Main {

    /**
     * 一句话功能简述<p>
     * 功能详细描述
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File destDir = new File("/Users/hiphonezhu/Desktop/D/software");
        File[] destFiles = destDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });


        // 模拟destFiles.length个用户并发
        for (final File destFile : destFiles) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket("127.0.0.1", 5599);
                        OutputStream os = socket.getOutputStream();
                        InputStream is = socket.getInputStream();

                        // 模拟每个用户发送完整数据包5次
                        for(int i = 0; i < 2; i++) {
                            if (i == 0) {
                                sendProtocol(os, destFile, i);
                            }

                            int command = readCommand(is);
                            System.out.println("command >>> " + command);

                            if (command == 2) // 开始上传
                            {
                                FileInputStream fis = new FileInputStream(destFile);
                                int len = -1;
                                byte[] buffer = new byte[1024]; // 每次最多发送1K数据
                                while ((len = fis.read(buffer)) != -1) {
                                    os.write(buffer,
                                            0,
                                            len);
                                }
                                sendProtocol(os, destFile, 1);
                                os.flush();
                                fis.close();

                                command = readCommand(is);
                                System.out.println("command >>> " + command);
                            }
                        }

                        os.close();
                        is.close();
                    } catch (Exception ex) {

                    }

                }
            }).start();
        }
    }

    public static void sendProtocol(OutputStream os, File destFile, int i) throws IOException {
        // fileName字段可选
        byte[] paramData = ("token=认证信息&md5=" + Utils.getMd5ByFile(destFile)
                + "&fileLength=" + destFile.length() + "&fileName=" + "(" + (i + 1) + ")" + destFile.getName()).getBytes("utf-8");

        byte[] dataLen = new byte[4];
        ByteBuffer.wrap(dataLen).putInt(paramData.length);

        os.write(dataLen);
        os.write(paramData);
    }

    public static int readCommand(InputStream is) {
        byte[] buffer = new byte[4];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = -1;
        try {
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer,
                        0,
                        len);
                if (baos.toByteArray().length == 4) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] data = baos.toByteArray();
        if (data.length != 4) {
            return -1;
        }
        return byteArrayToInt(data);
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    }
}
