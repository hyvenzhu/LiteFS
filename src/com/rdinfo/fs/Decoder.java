package com.rdinfo.fs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
 * 文件信息(格式：token=认证信息&md5=文件摘要信息)
 * @author hiphonezhu@gmail.com
 * @version [LiteFS, 2015-3-15]
 */
public class Decoder implements ProtocolDecoder
{
    private boolean saveSuccess;
    private Map<String, String> basicMap = new HashMap<String, String>();
    private String destDirPath = "C:/FS"; // 文件保存目录
    
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
            byte b = buffer.get();
            if (b == '\n') // 读取提交的文件信息
            {
                int currentPosition = buffer.position();
                int limit = buffer.limit();
                buffer.position(startPosition);
                buffer.limit(currentPosition);
                
                IoBuffer in = buffer.slice();
                byte[] dest = new byte[in.limit()];
                in.get(dest);
                String basicInfo = new String(dest, "utf-8");
                
                buffer.position(currentPosition);
                buffer.limit(limit);
                
                // 检测文件信息是否合法
                System.out.println("received:" + basicInfo);
                readBasicInfo(basicInfo);
                final String md5 = basicMap.get("md5");
                if (!checkToken() || md5 == null)
                {
                    System.out.println("invalid msg:" + basicInfo);
                    
                    saveSuccess = false;
                    session.write("invalid msg:" + basicInfo);
                    session.close(false);
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
                    session.write("0");
                }
                else
                {
                    // 保存文件
                    IoBuffer fileBuffer = buffer.slice();
                    readStream(fileBuffer);
                }
            }
        }
        if (saveSuccess)
        {
            session.write("0");
        }
        else
        {
            session.write("-1");
        }
    }
    
    private void readStream(IoBuffer in)
    {
        try
        {
            while(in.hasRemaining())
            {
                if (in.limit() > 1024 * 4) // 数据大于4k
                {
                    byte[] data = new byte[1024 * 4];
                    in.get(data);
                    saveData(data);
                }
                else
                {
                    byte[] data = new byte[in.limit()];
                    in.get(data);
                    saveData(data);
                }
            }
            bos.flush();
            saveSuccess = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            // 关闭文件输出流
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    bos = null;
                }
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
                bos = new BufferedOutputStream(new FileOutputStream(destDirPath + "/" + md5));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            bos.write(data);
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
    public void finishDecode(IoSession paramIoSession,
            ProtocolDecoderOutput paramProtocolDecoderOutput) throws Exception
    {

    }

    @Override
    public void dispose(IoSession paramIoSession) throws Exception
    {

    }
}
