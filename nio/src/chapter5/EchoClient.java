package chapter5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.net.InetAddress;

public class EchoClient {

	public void connect(int port,String host) throws Exception {
		//配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel arg0) throws Exception {
					ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
					/**
					 * 1024表示单条消息的最大长度，当达到该长度后仍然没有查找到分隔符，
					 * 就抛出ToolLongFrameException异常，防止由于异常码流缺失分隔符导致的内存溢出，
					 * 这是Netty解码器的可靠性保护；第二个参数就是分隔符缓冲对象
					 */
					arg0.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
					arg0.pipeline().addLast(new StringDecoder());
					arg0.pipeline().addLast(new EchoClientHandler());
					
				}
			
			});
			
			//发起异步连接操作
			ChannelFuture f = bootstrap.connect(host,port).sync();
			//等待客户端链路关闭
			f.channel().closeFuture().sync();
		} finally{
			//优雅退出，释放Nio线程组
			group.shutdownGracefully();
		}
		
	}
	
	
	public static void main(String [] args) throws Exception
	{
		
		int port = 8080;
		if(args !=null && args.length > 0)
		{
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				//采用默认值
			}
		}
		//new EchoClient().connect(port, InetAddress.getLocalHost().getHostAddress());
		new EchoClient().connect(port, "127.0.0.1");
	}
}
