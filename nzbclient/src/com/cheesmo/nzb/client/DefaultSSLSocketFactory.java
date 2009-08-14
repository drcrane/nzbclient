package com.cheesmo.nzb.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.net.DefaultSocketFactory;

public class DefaultSSLSocketFactory extends DefaultSocketFactory {
	private static DefaultSSLSocketFactory socketFact;

	private DefaultSSLSocketFactory() {

	}

	public static synchronized DefaultSSLSocketFactory getInstance() {
		if (socketFact == null) {
			socketFact = new DefaultSSLSocketFactory();
		}
		return socketFact;
	}

	@Override
	public Socket createSocket(String host, int port)
	throws UnknownHostException, IOException
	{
		return SSLSocketFactory.getDefault().createSocket(host, port);
	}
}