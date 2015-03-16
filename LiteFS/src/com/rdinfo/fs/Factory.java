package com.rdinfo.fs;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class Factory implements ProtocolCodecFactory
{
    private Encoder encoder;
    private Decoder decoder;
    public Factory()
    {
        encoder = new Encoder();
        decoder = new Decoder();
    }
    @Override
    public ProtocolEncoder getEncoder(IoSession paramIoSession) throws Exception
    {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession paramIoSession) throws Exception
    {
        return decoder;
    }
}
