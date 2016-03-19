package chapter3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
/**
 * 缓存中集聚多个分包组成的粘包，即TCP粘包，注意，这个粘包大小一般小于缓存的大小
 * 这里并没有解决TCP粘包的问题
 * @author jonn
 *
 */
public class TimeServerHandler extends ChannelHandlerAdapter{

	private int counter;
	
	/**
	 * 注意回调的时机
	 */
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		//读取缓存,此处期望是接受100次，输出counter的值为100，但实际结果上并非如此，说明出现粘包
		ByteBuf buf = (ByteBuf)msg;
		byte [] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String body = new String(req,"UTF-8").substring(0,req.length - System.getProperty("line.separator").length());
		System.out.println("The time server receive order : "+body+" ; the counter is : "+ ++ counter);
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
