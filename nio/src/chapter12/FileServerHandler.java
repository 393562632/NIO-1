package chapter12;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.RandomAccessFile;

public class FileServerHandler extends SimpleChannelInboundHandler<String>{

	private static final String CR = System.getProperty("line.separator");
	
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, String msg)
			throws Exception {
		
		File file = new File(msg);
		if(file.exists())
		{
			if(!file.isFile()){
				ctx.writeAndFlush("Not a file : "+ file + CR);
				return;
			}
		
		ctx.write(file + " " + file.length() + CR);
		RandomAccessFile randomAccessFile = new RandomAccessFile(msg, "r");
		//文件传输，FileChannel文件通道，Position文件操作的指针位置，Count操作的总字节数。
		FileRegion region = new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
		ctx.write(region);
		ctx.writeAndFlush(CR);
		randomAccessFile.close();
		}else {
			
			ctx.writeAndFlush("File not found : "+file + CR);
			
		}
		
		
		
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	
	
}
