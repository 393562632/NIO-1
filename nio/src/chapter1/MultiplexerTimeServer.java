package chapter1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable{

	private Selector selector;
	private ServerSocketChannel servChannel;
	private volatile boolean stop;
	
	
	
	
	/**
	 * 初始化多路复用器，绑定监听端口
	 * @param port
	 */
	public MultiplexerTimeServer(int port){
		try {
			
			selector = Selector.open();
			//打开管道，监听客户端连接
			servChannel = ServerSocketChannel.open();
			//设置为非阻塞模式
			servChannel.configureBlocking(false);
			//绑定监听端口
			servChannel.socket().bind(new InetSocketAddress(port),1024);
			//将ServerSocketChannel注册到Reactor线程的多路复用器Selector上，监听ACCEPT事件
			servChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			System.out.println("the time server is start in port : " + port);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			System.exit(1);
		}		
		
	}

	public void stop() {
		this.stop  = true;
	}
	
	




	@Override
	public void run() {
		while(!stop)
		{
			try {
				//每隔一秒唤醒一次,遍历所有通道,观察read是否读到数据，没有数据继续往下执行
				if (selector.select() == 0) 
				{
					continue;
				}
			
				
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				SelectionKey key = null;
				while(iterator.hasNext())
				{
					key = iterator.next();
					iterator.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if(key != null)
						{
							key.cancel();
							if(key.channel() != null)
							{
								key.channel().close();
							}
						}
					}
				}
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
		}
		
		/**
		 * 多路复用器关闭后，所有注册在上面的Channel和pipe等资源都会被自动去注册并关闭，所以不需要重新释放资源
		 */
		if(selector != null)
		{
			  try {
				selector.close();
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		
	}
	
	private void handleInput(SelectionKey key) throws IOException{
		if(key.isValid())
		{
			//处理新接入的请求信息
			if(key.isAcceptable())
			{
				//Accept the new connection
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc  = ssc.accept();
				sc.configureBlocking(false);
				//Add the new connection to the selector
				sc.register(selector, SelectionKey.OP_READ);
				
				
			}
			
			if(key.isReadable())
			{
				//read the data
				SocketChannel sc  =  (SocketChannel)key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				//非阻塞的
				int readBytes = sc.read(readBuffer);
				/**
				 * 返回值大于0，读到了字节，对字节进行编码；返回值等于0，没有读到字节，正常场景，忽略；
				 * 返回值为-1，链路已经关闭，需要关闭SocketChannel，释放资源。
				 */
				if(readBytes > 0)
				{
					/**
					 * flip的作用是将缓冲区当前的limit设置为position，position设置为0，用于后续对缓冲区的读取操作。
					 */
				readBuffer.flip();
				byte [] bytes = new byte[readBuffer.remaining()];
				readBuffer.get(bytes);
				String body = new String(bytes,"UTF-8");
				System.out.println("The time server receive order : " + body);
				String currentTime =  "QUERY TIME ORDER".equalsIgnoreCase(body)?new java.util.Date(System.currentTimeMillis()).toString():"Bad Order";
				doWrite(sc, currentTime);
				
				
				
				}else if (readBytes < 0) {
					//对端链路关闭
				
					key.cancel();
					sc.close();
				}else {
					//读到0字节，忽略掉
				}
			}
		}
	}
	
	
	private void doWrite(SocketChannel channel ,String response) throws IOException	{
		
		if(response != null && response.trim().length() > 0)
		{
			byte [ ] bytes  = response.getBytes();
			ByteBuffer writeBuffer   =  ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer);
		}
	}

}
