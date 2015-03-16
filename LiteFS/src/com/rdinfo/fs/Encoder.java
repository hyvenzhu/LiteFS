package com.rdinfo.fs;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class Encoder implements ProtocolEncoder
{
    
    @Override
    public void encode(IoSession session, Object object,
            ProtocolEncoderOutput output) throws Exception
    {
        IoBuffer ioBuffer = IoBuffer.allocate(8);
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
