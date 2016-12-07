package com.rdinfo.fs;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Handler extends IoHandlerAdapter
{
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        cause.printStackTrace(ps);
        String errorMsg = new String(baos.toByteArray());

        System.out.println("exceptionCaught..." + errorMsg);
        session.write(5);
        session.close(false);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception
    {
        System.out.println("messageReceived..." + message);
        int command = Integer.parseInt(message.toString());
        session.write(command);
//        if (command != 2)
//        {
//            session.close(false);
//        }
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception
    {
        System.out.println("messageSent..." + message);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception
    {
        System.out.println("sessionClosed...");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception
    {
        System.out.println("sessionCreated...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception
    {
        System.out.println("sessionIdle...");
        Decoder decoder = (Decoder)session.getAttribute("Decoder");
        if (decoder != null)
        {
        	if (!decoder.vertifyFile())
        	{
        		session.write(6);
        	}
        	else
        	{
        		session.write(4);
        	}
        }
        else
        {
        	session.write(4);
        }
//        session.close(false);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception
    {
        System.out.println("sessionOpened...");
    }

}
