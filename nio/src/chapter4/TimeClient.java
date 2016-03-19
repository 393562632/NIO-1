package chapter4;

import java.net.InetAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
/**
 * 支持TCP粘包的TimeClient
 * @author jonn
 *
 */
public class TimeClient {
	
	public void connect(int port,String host) throws Exception
	{
		//配置客户端NIO线程组，用于处理IO读写。
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			//辅助启动类
			Bootstrap bootstrap = new Bootstrap();
			//设置辅助启动类
			bootstrap.group(group)
			//设置Channel为NioSocketChannel，用于网络IO读写
			.channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, true)
			.handler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel arg0) throws Exception {
					arg0.pipeline().addLast(new LineBasedFrameDecoder(1024));//
					arg0.pipeline().addLast(new StringDecoder());//
					arg0.pipeline().addLast(new TimeClientHandler());
				}
			});
			
			//发起异步连接操作
			ChannelFuture f = bootstrap.connect(host,port).sync();
			//等待客户端链路关闭
			f.channel().closeFuture().sync();
			
			
		}finally{
			//优雅退出，释放NIO线程组
			group.shutdownGracefully();
		}
	}
	
	public static void main(String [] args) throws Exception{
		int port = 8080;
		if(args != null && args.length > 0 )
		{
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				//采用默认值
			}
		}
		new TimeClient().connect(port, InetAddress.getLocalHost().getHostAddress());
	}

}
