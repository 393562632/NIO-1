package chapter6;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public class EchoServer {
	
	public void bind(int port) throws Exception {
		
		//配置服务器的NIO线程组
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap  = new ServerBootstrap();
			bootstrap.group(bossGroup,workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 100)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel arg0) throws Exception {
				
					/**
					 * 固定长度解码器，按指定长度对消息自动解码,
					 * 无论一次接收到多少数据报，他都会按照构造函数中设置的固定长度进行解码，
					 * 如果是半包消息，FixedLengthFrameDecoder会缓存半包消息并等待下一个包到达后进行拼包，
					 * 直到读取到一个完整的包。
					 */
					arg0.pipeline().addLast(new FixedLengthFrameDecoder(20));
					/**
					 * 将接收到的对象转换成字符串，然后继续调用后面的handler。
					 * 
					 */
					arg0.pipeline().addLast(new StringDecoder());
					arg0.pipeline().addLast(new EchoServerHandler());
					
				}
				
			});
			//绑定端口，同步等待成功
			ChannelFuture f = bootstrap.bind(port).sync();
			//等待服务端监听端口关闭
			f.channel().closeFuture().sync();
			
		} finally{
			//优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static void main(String [] args) throws Exception{
		int port = 8080;
		if(args != null && args.length > 0)
		{
			try {
				port = Integer.valueOf(args[0]);	
			} catch (NumberFormatException e) {
				//采用默认值
			}
			
			
		}
		new EchoServer().bind(port);
	}

}
