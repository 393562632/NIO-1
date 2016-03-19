package chapter2;

import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class TimeClientHandler extends ChannelHandlerAdapter{

	private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());
	private final ByteBuf firstMessage;
	
    public TimeClientHandler() {
		byte [] req = "QUERY TIME ORDER".getBytes();
		firstMessage = Unpooled.buffer(req.length);
		//往缓存中写入数据
		firstMessage.writeBytes(req);
	}

    /**
     * 当客户端和服务器TCP链路建立成功之后，Netty的Nio线程会回调channelActive方法，
     * 调用writeAndFlush把缓存中的请求数据发送出去。
     */
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(firstMessage);
	}
	/**
	 * 当服务器返回应答消息的时候，channelRead方法被调用
	 */
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		ByteBuf buf = (ByteBuf)msg;
		byte [] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		String body = new String(req,"UTF-8");
		System.out.println("Now is :"+body);
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		//释放资源
		logger.warning("Unexception exception from downstream :"+cause.getMessage());
		ctx.close();
	}
    
}
