package com.rdinfo.fs;

import java.io.FileNotFoundException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
/**
 * 文件信息(格式：token=认证信息&md5=文件摘要信息&fileLength=文件长度)
 * 0：格式非法
 * 1：文件已存在
 * 2：可以开始上传
 * 3：上传成功
 * 4：IDLE连接断开
 * 5：服务器内部错误
 * 6：文件校验不正确(收到的长度不对或md5校验不正确)
 * @author hiphonezhu@gmail.com
 * @version [LiteFS, 2015-3-15]
 */
public class Decoder extends CumulativeProtocolDecoder
{
	private StreamReader streamReader;
    private ProtocolReader protocolReader;
    
    public Decoder()
    {
        protocolReader = new ProtocolReader();
        streamReader = new StreamReader(protocolReader);
    }
    
    public boolean vertifyFile() throws FileNotFoundException
    {
    	return streamReader.readOver() && streamReader.md5Legal();
    }
    
    @Override
	protected boolean doDecode(IoSession session, IoBuffer buffer,
			ProtocolDecoderOutput out) throws Exception {
    	if (protocolReader.validate())
    	{
    		streamReader.readStream(buffer);
            if (vertifyFile())
            {
      		   out.write(3);
            
      		   streamReader.close();
            }
    		return true;
    	}
    	else
    	{
    		while(buffer.hasRemaining())
            {
    			byte b = buffer.get();
                if (b == '\n') // 读取提交的文件信息
                {
                	int currentPosition = buffer.position();
                    buffer.position(0);
                    
                    byte[] dest = new byte[currentPosition - 1];
                    buffer.get(dest);
                    String protocolInfo = new String(dest, "utf-8");
                    
                    buffer.position(currentPosition);
                    
                    // 检测文件信息是否合法
                    protocolReader.readProtocol(protocolInfo);
                    if (protocolReader.validate())
                    {
                        System.out.println("invalid protocol:" + protocolInfo + ", correct format is 'token=认证信息&md5=文件摘要信息&fileLength=文件长度'");
                        out.write(0);
                        return true;
                    }
                    
                    if (streamReader.fileExist()) // 服务器已存在此文件, 直接返回成功
                    {
                        out.write(1);
                        return true;
                    }
                    out.write(2);
                	return true;
                }
            }
    		return false;
    	}
	}
    
    @Override
    public void finishDecode(IoSession session,
            ProtocolDecoderOutput out) throws Exception
    {
    	streamReader.close();
    }

    @Override
    public void dispose(IoSession paramIoSession) throws Exception
    {

    }
}
