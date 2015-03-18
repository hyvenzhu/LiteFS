package com.rdinfo.fs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 保存文件
 */
public class StreamReader 
{
	private String destDirPath = "C:/FS"; // 文件保存目录
	private ProtocolReader protocolReader; // 读取请求协议
	private long receivedFileLength; // 已经接收到的文件长度
	private BufferedOutputStream bos; // 文件输出流
	
	public StreamReader(ProtocolReader protocolReader) 
	{
		this.protocolReader = protocolReader;
		File dir = new File(destDirPath);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
	}
	
	/**
	 * 检测服务器是否已经存在该文件
	 * @return
	 */
	public boolean fileExist()
	{
        String[] fileNames = new File(destDirPath).list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return protocolReader.get("md5").equals(name);
            }
        });
        if (fileNames != null && fileNames.length > 0)
        {
        	return true;
        }
        return false;
	}
	
	/**
	 * 文件是否接收完毕
	 * @return
	 */
	public boolean readOver()
	{
		return receivedFileLength == Long.parseLong(protocolReader.get("fileLength"));
	}
	
	/**
	 * 文件md5校验是否合法
	 * @return
	 * @throws FileNotFoundException
	 */
	public boolean md5Legal() throws FileNotFoundException
	{
		return Utils.getMd5ByFile(new File(destDirPath, protocolReader.get("md5"))).equals(protocolReader.get("md5"));
	}
	
	/**
	 * 读取buffer中的数据
	 * @param in
	 * @throws IOException
	 */
	public void readStream(IoBuffer in) throws IOException
    {
        while(in.hasRemaining())
        {
            int positon = in.position();
            if (in.limit() - positon > 1024 * 4) // 数据大于4k
            {
                byte[] data = new byte[1024 * 4];
                in.get(data);
                saveData(data);
            }
            else
            {
                byte[] data = new byte[in.limit() - positon];
                in.get(data);
                saveData(data);
            }
        }
    }
    
    /**
     * 将数据保存到文件
     * @param data
     */
    private void saveData(byte[] data)
    {
        if (bos == null)
        {
            final String md5 = protocolReader.get("md5");
            try
            {
                File file = new File(destDirPath, md5);
                if (!file.exists())
                {
                    file.createNewFile();
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        try
        {
        	receivedFileLength += data.length;
            bos.write(data);
            bos.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 关闭文件流
     */
    public void close()
    {
    	// close
    	if (bos != null)
    	{
    		try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		bos = null;
    	}
    }
}
