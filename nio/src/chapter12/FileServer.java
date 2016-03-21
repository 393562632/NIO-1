package chapter12;

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
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
/**
 * 要确保开启了telnet
 * @author jonn
 *
 */
public class FileServer {
	
	public void run(int port) throws Exception {
		
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup,workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 100)
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					//把文件内容（字符串）编码
					sc.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8),
							//按照回车换行符对数据报进行解码
							new LineBasedFrameDecoder(1024),
							//解码成字符串
							new StringDecoder(CharsetUtil.UTF_8),
							new FileServerHandler());
				}
			});
			ChannelFuture future = bootstrap.bind(port).sync();
			System.out.println("Start file server at port :"+ port);
			future.channel().closeFuture().sync();
		} finally{
			//优雅停机
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	
	public static void main(String [] args) throws Exception
	{
		int port = 8080;
		if(args.length > 0 )
		{
				  try {
					port = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
				  e.printStackTrace();
				}	
		}
		new FileServer().run(port);
	}
}
