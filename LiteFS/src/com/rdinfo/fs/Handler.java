package com.rdinfo.fs;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class Handler extends IoHandlerAdapter
{
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception
    {
        System.out.println("exceptionCaught...");
        session.close(false);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception
    {
        System.out.println("messageReceived..." + message);
        int command = Integer.parseInt(message.toString());
        session.write(command);
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
        session.close(false);
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
        session.close(false);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception
    {
        System.out.println("sessionOpened...");
    }

}
