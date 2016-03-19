package chapter8;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;



public class HttpFileServer {
	
	private static final String DEFAULT_URL = "/src";
	private static String SERVER_URL;
	{
		try {
			SERVER_URL =  InetAddress.getLocalHost().getHostAddress();
//			System.out.println(SERVER_URL);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(final int port,final String url) throws Exception
	{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap  = new ServerBootstrap();
			bootstrap.group(bossGroup,workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {

					/**
					 * 发送的消息都要编码，接受的消息都要解码
					 */
					@Override
					protected void initChannel(SocketChannel arg0) throws Exception {
						//消息解码器
						arg0.pipeline().addLast("http-decoder",new HttpRequestDecoder());
						/**
						 * HttpObjectAggregator消息解码器,将多个消息转换成为单一的FullHttpRequest或者FullHttpResponse
						 * 因为HTTP解码器在每一个HTTP消息中会生成多个消息对象
						 * 1.HttpRequest/HttpResponse
						 * 2.HttpContent
						 * 3.LastHttpContent
						 */
						arg0.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65536));
						//响应编码器
						arg0.pipeline().addLast("http-encoder",new HttpResponseEncoder());
						//支持异步发送大的码流,但不占过多的内存，防止发生java内存溢出错误
						arg0.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
						//文件服务器的业务逻辑处理
						arg0.pipeline().addLast("fileServerHandler",new HttpFileServerHandler(url));
						
					}
				});
			ChannelFuture future = bootstrap.bind(SERVER_URL,port).sync();
			System.out.println("HTTP 文件目录服务器启动，网址是 ："+SERVER_URL+":"+port+url);
			future.channel().closeFuture().sync();
		} finally{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	
	public static void main(String [] args) throws Exception {
		
		int port = 8080;
		if ( args.length > 0 )
		{
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		String url = DEFAULT_URL ;
		if(args.length > 1)
		{
			url = args[1];
		}
		new HttpFileServer().run(port, url);
	}
	
}
