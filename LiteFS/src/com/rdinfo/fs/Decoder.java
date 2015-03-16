package com.rdinfo.fs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
/**
 * 文件信息(格式：token=认证信息&md5=文件摘要信息&fileLength=文件长度)
 * 0：格式非法
 * 1：文件已存在
 * 2：可以开始上传
 * 3：上传成功
 * 4：上传失败
 * @author hiphonezhu@gmail.com
 * @version [LiteFS, 2015-3-15]
 */
public class Decoder implements ProtocolDecoder
{
    private boolean saveSuccess = true;
    private Map<String, String> basicMap = new HashMap<String, String>();
    private String destDirPath = "C:/FS"; // 文件保存目录
    boolean hasReachBasicInfo;
    int receivedFileLength;
    
    public Decoder()
    {
        File dir = new File(destDirPath);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
    }
    
    @Override
    public void decode(IoSession session, IoBuffer buffer,
            ProtocolDecoderOutput out) throws Exception
    {
        int startPosition = buffer.position();
        while(buffer.hasRemaining())
        {
            if (hasReachBasicInfo)
            {
                // 保存文件
                readStream(buffer);
            }
            else
            {
                byte b = buffer.get();
                if (b == '\n') // 读取提交的文件信息
                {
                    int currentPosition = buffer.position();
                    int limit = buffer.limit();
                    buffer.position(startPosition);
                    buffer.limit(currentPosition - 1);
                    
                    IoBuffer in = buffer.slice();
                    byte[] dest = new byte[in.limit()];
                    in.get(dest);
                    String basicInfo = new String(dest, "utf-8");
                    
                    buffer.limit(limit);
                    buffer.position(currentPosition);
                    
                    // 检测文件信息是否合法
                    System.out.println("received:" + basicInfo);
                    readBasicInfo(basicInfo);
                    final String md5 = basicMap.get("md5");
                    if (!checkToken() || md5 == null || basicMap.get("fileLength") == null)
                    {
                        System.out.println("invalid msg:" + basicInfo);
                        
                        saveSuccess = false;
                        out.write(0);
                        return;
                    }
                    
                    // 检测服务器是否已经存在该文件
                    String[] fileNames = new File(destDirPath).list(new FilenameFilter()
                    {
                        @Override
                        public boolean accept(File dir, String name)
                        {
                            return md5.equals(name);
                        }
                    });
                    if (fileNames != null && fileNames.length > 0) // 服务器已存在此文件, 直接返回成功
                    {
                        out.write(1);
                        return;
                    }
                    out.write(2);
                    hasReachBasicInfo = true;
                }
            }
        }
        if (hasReachBasicInfo && receivedFileLength == Integer.parseInt(basicMap.get("fileLength")))
        {
        	if (saveSuccess)
        	{
        		out.write(3);
        	}
        	else
        	{
        		out.write(4);
        	}
          
        	// close
        	if (bos != null)
        	{
        		bos.close();
        		bos = null;
        	}
        }
    }
    
    private void readStream(IoBuffer in) throws IOException
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
    
    BufferedOutputStream bos;
    private void saveData(byte[] data)
    {
        if (bos == null)
        {
            final String md5 = basicMap.get("md5");
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
     * 读取基础信息数据
     * @param basicInfo
     */
    private void readBasicInfo(String basicInfo)
    {
        if (basicInfo == null)
        {
            return;
        }
        String[] keyValues = basicInfo.split("&");
        if (keyValues != null)
        {
            for(String str : keyValues)
            {
                String[] keyValue = str.split("=");
                if (keyValue != null && keyValue.length > 1)
                {
                    basicMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }
    
    private boolean checkToken()
    {
        String token = basicMap.get("token");
        // 合法性检测
        return true;
    }
    
    @Override
    public void finishDecode(IoSession session,
            ProtocolDecoderOutput out) throws Exception
    {

    }

    @Override
    public void dispose(IoSession paramIoSession) throws Exception
    {

    }
}
