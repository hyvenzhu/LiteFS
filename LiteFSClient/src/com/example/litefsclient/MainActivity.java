package com.example.litefsclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    int i;

    public void click(View v)
    {
        System.out.println("sent...");
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    File sdcardDir = Environment.getExternalStorageDirectory();
                    File destFile = new File(sdcardDir,
                            "4.0_usb_tools.zip");

                    Socket socket = new Socket("192.168.1.104",
                            9898);
                    OutputStream os = socket.getOutputStream();
                    InputStream is = socket.getInputStream();
                    os.write(("token=认证信息&md5=" + Thread.currentThread().getId()
                            + ".zip&fileLength=" + destFile.length() + "\n").getBytes("utf-8"));

                    int command = readCommand(is);
                    System.out.println("command >>> " + command);

                    if (command == 2) // 开始上传
                    {
                        FileInputStream fis = new FileInputStream(destFile);
                        int len = -1;
                        byte[] buffer = new byte[1024];
                        while ((len = fis.read(buffer)) != -1)
                        {
                            os.write(buffer,
                                    0,
                                    len);
                        }
                        os.flush();
                        fis.close();

                        command = readCommand(is);
                        System.out.println("command >>> " + command);

                    }
                    os.close();
                    is.close();
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public int readCommand(InputStream is)
    {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = -1;
        try
        {
            while ((len = is.read(buffer)) != -1)
            {
                baos.write(buffer,
                        0,
                        len);
                if (baos.toByteArray().length >= 4)
                {
                    break;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        byte[] data = baos.toByteArray();
        if (data.length != 4)
        {
            return -1;
        }
        return byteArrayToInt(data);
    }

    public int byteArrayToInt(byte[] b)
    {
        return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    }
}
