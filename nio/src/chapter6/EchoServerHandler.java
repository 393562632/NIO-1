package chapter6;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class EchoServerHandler extends ChannelHandlerAdapter{

	
	private int counter = 0;

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
	     String body =  (String) msg;
	     System.out.println("This is "+ ++counter +" times receive client : [" + body + "]");
	     ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
	     ctx.writeAndFlush(echo);
	     
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
	    //cause.printStackTrace();
	    ctx.close();//发生异常
	}
	
	
}
