package com.rdinfo.fs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class Encoder implements ProtocolEncoder
{
    CharsetEncoder charsetEncoder;
    public Encoder()
    {
        charsetEncoder = Charset.forName("utf-8").newEncoder();
    }
    
    @Override
    public void encode(IoSession session, Object object,
            ProtocolEncoderOutput output) throws Exception
    {
        IoBuffer ioBuffer = IoBuffer.allocate(10);
        ioBuffer.setAutoExpand(true);
        int command = Integer.parseInt(object.toString());
        ioBuffer.putInt(command);
        ioBuffer.flip();
        output.write(ioBuffer);
    }

    @Override
    public void dispose(IoSession paramIoSession) throws Exception
    {

    }
}
