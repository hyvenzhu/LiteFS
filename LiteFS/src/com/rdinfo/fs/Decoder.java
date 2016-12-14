package com.rdinfo.fs;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.io.FileNotFoundException;
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
        if (session.getAttribute("ParamLength") == null)
        {
            if(buffer.remaining() >= 4)
            {
                int paramLength = buffer.getInt();
                session.setAttribute("ParamLength", paramLength);
                return true;
            }
        }
        else
        {
            int paramLength = Integer.parseInt(session.getAttribute("ParamLength").toString());
            if (protocolReader.validate())
            {
                streamReader.readStream(buffer);
                if (vertifyFile()) // 上传成功
                {
                    out.write(3);

                    reset(session, buffer);

                    if (buffer.remaining() > 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return true;
            }
            else if (buffer.remaining() >= paramLength)
            {
                byte[] paramData = new byte[paramLength];
                buffer.get(paramData);

                String protocolInfo = new String(paramData, "utf-8");
                protocolReader.readProtocol(protocolInfo);
                if (!protocolReader.validate()) // 参数不合法
                {
                    System.out.println("invalid protocol:" + protocolInfo + ", correct format is 'token=认证信息&md5=文件摘要信息&fileLength=文件长度'");
                    out.write(0);

                    reset(session, buffer);
                }
                else if (streamReader.fileExist()) // 服务器已存在此文件
                {
                    out.write(1);

                    reset(session, buffer);
                }
                else // 可以开始上传
                {
                    out.write(2);
                }
                return true;
            }
        }
        return false;
    }

    void reset(IoSession session, IoBuffer buffer) {
        session.setAttribute("ParamLength", null);
        streamReader.reset();
    }

    @Override
    public void finishDecode(IoSession session,
                             ProtocolDecoderOutput out) throws Exception
    {
        System.out.println("finishDecode...");
        streamReader.close();
    }

    @Override
    public void dispose(IoSession paramIoSession) throws Exception
    {

    }
}