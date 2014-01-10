package com.test.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.test.util.PropUtil;

public class MessageClient extends Thread {
	
	private volatile boolean running = true;
	private boolean connected = false;
	private SocketChannel socketChannel;
	private Selector selector;
	private SelectionKey key;
	

	
	public void startup(){
		running = true;
	}
	public void shutdown(){
		running = false;
	}
	public void run() {
		while (running) {
			try {
				reconnectInNeed(selector,socketChannel);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				try {
					key = keys.next();
					SocketChannel socketChannel = (SocketChannel) key.channel();
					
					if (key.isConnectable()) {
						if (socketChannel.finishConnect()) {
							System.out.println("... connected to server ...");
							socketChannel.register(selector,SelectionKey.OP_READ | SelectionKey.OP_WRITE);
							connected = true;
							continue;
						}
					}
					
					
					if (key.isReadable()) {
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						int i = 0 ;
						while ((i=socketChannel.read(buffer)) != -1){
							i ++ ;
						}
						socketChannel.write(buffer);
						
						
//						socketChannel.read(byteBuffer);
						System.out.println(new String(buffer.array(), 0, i));
						buffer.flip();
					}

					if (key.isWritable()) {
						ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
						byteBuffer.put("write to client message".getBytes());
						socketChannel.write(byteBuffer);
					}
				} catch (Exception e) {
					e.printStackTrace();
					connected = false;
					try {
					key.channel().close();
					key.cancel();
						Thread.sleep(2000);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} finally {
					keys.remove();
				}
			}
		}
	}
	private void reconnectInNeed(Selector selector2, SocketChannel socketChannel2) throws IOException {
		if(connected){ return ;}
		selector = Selector.open();
		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
		socketChannel.connect(new InetSocketAddress(PropUtil.get("serverIp"), PropUtil.getInt("serverPort")));
		selector.select();
		selector.isOpen();
	}

}
