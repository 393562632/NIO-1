package chapter7;

import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SubReqServer {
	public void bind(int port) throws Exception
	{
		//配置服务器的NIO线程组
		EventLoopGroup bossGroup =  new NioEventLoopGroup();
		EventLoopGroup workerGroup  = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup,workerGroup)
			//绑定管道
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 100)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChannelInitializer<Channel>() {

				protected void initChannel(Channel arg0) throws Exception {
					//ProtobufVarint32FrameDecoder负责半包处理
					arg0.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					//负责解码，不支持读半包,如同linux c写的服务器，根据数据结构的长度解码
					arg0.pipeline().addLast(new ProtobufDecoder(SubscribeReqProto.SubscribeReq.getDefaultInstance()));
					//编码的时候使用，根据数据结构的长度编码
					arg0.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					//编码
					arg0.pipeline().addLast(new ProtobufEncoder());
					arg0.pipeline().addLast(new SubReqServerHandler());
				}
				
			});
			//绑定端口
			ChannelFuture f = bootstrap.bind(port).sync();
			//等待服务器监听器端口关闭
			f.channel().closeFuture().sync();
			
			
		} finally{
			//优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String [] args) throws Exception{
		
		int port = 8080;
		if( args!=null && args.length > 0)
		{
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}
		new SubReqServer().bind(port);
	}
}
