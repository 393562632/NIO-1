package chapter4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
/**
 * 使用LineBasedFrameDecoder+StringDecoder来解决TCP的粘包/拆包问题
 * @author jonn
 *
 */
public class TimeServerHandler extends ChannelHandlerAdapter{
	private int counter;

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		String body = (String)msg;//是String，表示ByteBuf
		System.out.println("The time server receive order :" + body +" ; the counter is : " + ++counter);
		String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)?new java.util.Date(System.currentTimeMillis()).toString():"BAD ORDER";
		currentTime = currentTime + System.getProperty("line.separator");
		ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
		ctx.writeAndFlush(resp);
		
	}

	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	    ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ctx.close();
	}

	
	
}
