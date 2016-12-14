package com.rdinfo.fs;

public class FileServer
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            Connector connector = new Connector();
            connector.start(5599);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
