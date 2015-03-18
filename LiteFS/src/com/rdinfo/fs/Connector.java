package com.rdinfo.fs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class Connector
{
    private NioSocketAcceptor acceptor;
    public Connector()
    {
        acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new Handler());
        acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, 15); // 15秒如果client不发送数据则进入IDLE状态
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new Factory()));
        acceptor.getFilterChain().addLast("ThreadPool",new ExecutorFilter(Executors.newCachedThreadPool())); // 一定要配置在ProtocolCodecFilter后面，不然文件会错误
    }
    
    public void start(int port)
    {
        try
        {
            acceptor.bind(new InetSocketAddress(port));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void stop()
    {
    	if (acceptor != null)
    	{
    		acceptor.unbind();
    	}
    }
}
