package chapter1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandle implements Runnable{

	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop;
	
	
	
	public TimeClientHandle(String host, int port) {
		super();
		this.host = host == null ? "127.0.0.1" : host;
		this.port = port;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}



	@Override
	public void run() {
		
		try {
			doConnect();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		while(!stop)
		{
			try {
				//每隔一秒唤醒一次,遍历所有通道是否有可读的
				if (selector.select() == 0) 
				{
					continue;
				}
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectionKeys.iterator();
				SelectionKey key = null;
				while(iterator.hasNext())
				{
					key = iterator.next();
					iterator.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if(key!=null)
						{
							key.cancel();
							if(key.channel()!=null)
							{
								key.channel().close();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			
		}
		//多路复用器关闭后，所有注册在上面的Channel和pipe等资源都会被自动反注册并关闭，所以不需要重复释放资源
		if(selector!=null)
		{
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if(key.isValid())
		{
			//判断是否连接成功
			SocketChannel sChannel  = (SocketChannel)key.channel();
			if(key.isConnectable())
			{
				if(sChannel.finishConnect())
				{
					//注册接受响应的key
				     sChannel.register(selector, SelectionKey.OP_READ);	
				     //发送请求
				     doWrite(sChannel);
				}else {
					System.exit(1);
				}
			}
			if(key.isReadable())
			{
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				//非阻塞的
				int readBytes  =  sChannel.read(readBuffer);
				if(readBytes > 0)
				{
					readBuffer.flip();
					byte [] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes,"UTF-8");
					System.out.println("Now is : "+body);
					this.stop = true;
					
				}else if (readBytes < 0) {
					//对端链路关闭
					key.cancel();
					sChannel.close();
				}else {
					;//读取到0字节，忽略
				}
			}
		}
	}
	
	private void doConnect() throws IOException
	{
		//如果直接连接成功，则注册到多路复用器，发送请求信息，读应答
		if(socketChannel.connect(new InetSocketAddress(host,port)))
		{
			socketChannel.register(selector, SelectionKey.OP_READ);
			doWrite(socketChannel);
			
		}else {
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
			
		}
		
	}
	
	private void doWrite(SocketChannel sc) throws IOException
	{
		byte[] req  = "QUERY TIME ORDER".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		sc.write(writeBuffer);
		//如果没有待发送的剩余消息
		if (!writeBuffer.hasRemaining()) {
			System.out.println("Send order 2 server succeed.");
		
		}
		
	}
	
}
