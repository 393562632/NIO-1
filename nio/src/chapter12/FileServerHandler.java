package chapter12;

import java.io.File;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FileServerHandler extends SimpleChannelInboundHandler<String>{

	private static final String CR = System.getProperty("line.separator");
	
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, String msg)
			throws Exception {
		
		File file = new File(msg);
		
		
	}

	
}
