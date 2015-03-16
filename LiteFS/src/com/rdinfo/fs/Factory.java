package com.rdinfo.fs;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class Factory implements ProtocolCodecFactory
{
    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception
    {
    	Encoder encoder = (Encoder)session.getAttribute("Encoder");
    	if (encoder == null)
    	{
    		encoder = new Encoder();
    		session.setAttribute("Encoder", encoder);
    	}
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception
    {
    	Decoder decoder = (Decoder)session.getAttribute("Decoder");
    	if (decoder == null)
    	{
    		decoder = new Decoder();
    		session.setAttribute("Decoder", decoder);
    	}
        return decoder;
    }
}
