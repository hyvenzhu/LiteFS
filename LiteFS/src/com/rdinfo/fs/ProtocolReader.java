package com.rdinfo.fs;

import java.util.HashMap;
import java.util.Map;

/**
 * 分析文件协议
 */
public class ProtocolReader
{
    private Map<String, String> protocolMap = new HashMap<String, String>();

    /**
     * 读取文件协议
     * @param protocol
     */
    public void readProtocol(String protocol)
    {
        System.out.println("received:" + protocol);
        if (protocol == null)
        {
            return;
        }
        String[] keyValues = protocol.split("&");
        if (keyValues != null)
        {
            for(String str : keyValues)
            {
                String[] keyValue = str.split("=");
                if (keyValue != null && keyValue.length > 1)
                {
                    protocolMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    public String get(String protocolName)
    {
        return protocolMap.get(protocolName);
    }

    public boolean validate()
    {
        if (get("token") == null || get("md5") == null || get("fileLength") == null)
        {
            return false;
        }
        return true;
    }

    public void reset() {
        protocolMap.clear();
    }
}