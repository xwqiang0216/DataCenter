package com.test.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import com.test.util.PropUtil;

public class MessageServer extends Thread{
	private boolean running = true;
	private final int port = PropUtil.getInt("serverPort");
	
	

	public void run(){
		Selector selector = null;
		
		try{
			selector = Selector.open();
			
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			ssc.socket().bind(new InetSocketAddress(port));
			
			System.out.println(" ---------- < server is start up! > ----------- ");
			
			while(running){
				int selectedKeyNum = selector.select();
				System.out.println("selectedKey num : "+ selectedKeyNum);
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				System.out.println(selector.selectedKeys().size());
				SelectionKey selectedKey = null;
				
				while(keys.hasNext()){
					try{
						selectedKey = keys.next();
						 switch (selectedKey.readyOps()) {
							 case SelectionKey.OP_ACCEPT:
								 System.out.println("OP_ACCEPT");
								 break;
							 case SelectionKey.OP_READ:
								 System.out.println("OP_READ");
								 break;
							 case SelectionKey.OP_WRITE:
								 System.out.println("OP_WRITE");
								 break;
							 default : break;
						 }
						
					}catch(Exception e){
						e.printStackTrace();
						freeSelectedKey(selectedKey);
					}finally{
						keys.remove();
					}
				}
			}
			
			ssc.close();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	private void freeSelectedKey(SelectionKey selectedKey) {
		if (selectedKey != null) {
            try {
            	selectedKey.channel().close();
            	selectedKey.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
	}

}
