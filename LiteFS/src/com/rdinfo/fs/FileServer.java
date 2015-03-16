package com.rdinfo.fs;

public class FileServer
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        Connector connector = new Connector();
        connector.start(9898);
    }
}
