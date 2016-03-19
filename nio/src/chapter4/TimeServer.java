package chapter4;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class TimeServer {
	
	private class ChildChannelHandler extends ChannelInitializer<SocketChannel>
	{

		@Override
		protected void initChannel(SocketChannel arg0) throws Exception {
			/**
			 * 依次遍历ByteBuf中的可读字节，判断看是否有"\n"或者"\r\n",如果有，
			 * 就以此位置为结束位置，从可读索引到结束位置区间的字节就组成了一行。
			 * 它是以换行符为结束标志的解码器，支持携带结束符或者不携带结束符两种解码方式，
			 * 同时支持配置单行的最大长度。
			 * 如果连续读取到最大长度后仍然没有发现换行符，就会抛出异常，同时忽略掉之前读到的异常码流。
			 */
			arg0.pipeline().addLast(new LineBasedFrameDecoder(1024));//解码器 
			/**
			 * 将接收到的对象转换成字符串，然后继续调用后面的handler。
			 * 
			 */
			arg0.pipeline().addLast(new StringDecoder());//解码器
			arg0.pipeline().addLast(new TimeServerHandler());
			
		}
		

	
	}
	public void bind(int port) throws Exception{
		//配置服务器的NIO线程组
		//用于服务器接受客户端的连接
		 EventLoopGroup  bossGroup = new NioEventLoopGroup();
		 //用于进行SocketChannel的网络读写
		 EventLoopGroup  workerGroup = new NioEventLoopGroup();
		 try {
			
			ServerBootstrap b = new ServerBootstrap();
			 //配置NioServerSocketChannel的TCP参数，设置IO事件处理者
			b.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG,1024).childHandler(new ChildChannelHandler());
			//同步等待绑定端口的完成
			ChannelFuture f = b.bind(port).sync();
			//等待服务器端链路关闭后main函数才退出。
			f.channel().closeFuture().sync();
			
			
		} finally{
			//优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static void main(String [] args)
	{
		int port = 8080;
		if(args != null && args.length > 0)
		{
			try{
				port = Integer.valueOf(args[0]);
				
			}catch (NumberFormatException e) {
			
				//采用默认值
			}
				
		}
		try {
			new TimeServer().bind(port);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	
	

}
