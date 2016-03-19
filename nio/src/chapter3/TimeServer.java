package chapter3;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TimeServer {
	
	private class ChildChannelHandler extends ChannelInitializer<SocketChannel>
	{

		@Override
		protected void initChannel(SocketChannel arg0) throws Exception {
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
