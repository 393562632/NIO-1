package chapter8;



import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpHeaderUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaderUtil.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
	private static final Pattern ALLOWED_FILE_NAME = Pattern
	    .compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
	private final String url;
	
	
	public HttpFileServerHandler(String url)
	{
						this.url = url	;
	}
	
	/**
	 * 返回请求失败
	 * @param ctx
	 * @param status
	 */
	private static void sendError(ChannelHandlerContext ctx,HttpResponseStatus status)
	{
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status,Unpooled.copiedBuffer("Failure: "+status.toString() + "\r\n",CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE,"text/plain;charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				
	}
	
	private static void setContentTypeHeader(HttpResponse response,File file)
	{
		MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
		response.headers().set(CONTENT_TYPE,mimetypesFileTypeMap.getContentType(file.getPath()));
		
		
	}
	
	
	private static void sendRedirect(ChannelHandlerContext ctx,String newUri)
	{
		System.out.println("1");
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
		System.out.println("2");
		response.headers().set(LOCATION,newUri);
		System.out.println("3");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		System.out.println("4");
	}
	/**
	 * 返回html列表
	 * @param ctx
	 * @param dir
	 */
	private static void sendListing(ChannelHandlerContext ctx,File dir)
	{
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
		response.headers().set(CONTENT_TYPE,"text/html;charset=UTF-8");
		StringBuilder buf = new StringBuilder();
		String dirPath = dir.getPath();
		buf.append("<!DOCTYPE html>\r\n");
		buf.append("<html><head><title>");
		buf.append(dirPath);
		buf.append(" 目录：");
		buf.append("</title></head><body>\r\n");
		buf.append("<h3>");
		buf.append(dirPath).append(" 目录：");
		buf.append("<h3>\r\n");
		buf.append("<ul>");
		buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
		for (File file : dir.listFiles()) {
			if(file.isHidden() || !file.canRead())
			{
			 continue;	
			}
			String name  = file.getName();
			if(!ALLOWED_FILE_NAME.matcher(name).matches())
			{
				continue;
			}
			buf.append("<li>链接:<a href=\"");
			buf.append(name);
			buf.append("\">");
			buf.append(name);
			buf.append("</a></li>\r\n");
			
		}
		buf.append("</ul></body></html>\r\n");
		ByteBuf buffer  = Unpooled.copiedBuffer(buf,CharsetUtil.UTF_8);
		response.content().writeBytes(buffer);
		//是buffer释放，不是response释放
		buffer.release();
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	/**
	 * 解析出服务器真实的文件路径
	 * @param uri
	 * @return
	 */
	private String sanitizeUri(String uri)
	{
		try {
			//解析URI
			    uri = URLDecoder.decode(uri,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			try {
				uri = URLDecoder.decode(uri,"ISO-8859-1");
			} catch (UnsupportedEncodingException e2) {
				throw new Error();
			}
		}
		if(!uri.startsWith(this.url)){
			return null;
		}
		if(!uri.startsWith("/"))
		{
			return null;
		}
		uri = uri.replace('/', File.separatorChar);
		if(uri.contains(File.separator + '.') || uri.contains('.' + File.separator) || uri.startsWith(".") || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches())
		{
			return null;
		}
		//当前程序的所在目录 +URI 
		return System.getProperty("user.dir")+ File.separator + uri;
	}
	

	/**要使用Netty5.x
	 * 	@Override
	 * @param ctx
	 * @param request
	 * @throws Exception
	 */
	@Override
	protected void messageReceived(ChannelHandlerContext ctx,
			FullHttpRequest request) throws Exception {
		if(!request.decoderResult().isSuccess())
		{
		 
			sendError(ctx, BAD_REQUEST);
			return;
		}
		
		if(request.method()!= GET)
		{
			sendError(ctx, METHOD_NOT_ALLOWED);
			return;
		}
		
		final String uri = request.uri();
		final String path = sanitizeUri(uri);
		if(path == null)
		{
			sendError(ctx, FORBIDDEN);
			return;
			
		}
		File  file = new File(path);
		//隐藏文件或者不存在
		if(file.isHidden() || !file.exists())
		{
			sendError(ctx, NOT_FOUND);
		}
		
		if(file.isDirectory())
		{
			if(uri.endsWith("/"))
			{
			 sendListing(ctx,file);	
			}else {
				sendRedirect(ctx, uri+'/');
			}
		
			return;
		}
		
		if(!file.isFile())
		{
			sendError(ctx, FORBIDDEN);
			return;
		}
		
		RandomAccessFile randomAccessFile = null;
		try {
			//以只读的方式打开文件
			randomAccessFile = new RandomAccessFile(file, "r");
			
		} catch (FileNotFoundException e) {
			sendError(ctx, NOT_FOUND);
			return;
		}
		long fileLength = randomAccessFile.length();
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		setContentLength(response, fileLength);
		setContentTypeHeader(response, file);
		if(isKeepAlive(request))
		{
			response.headers().set(CONNECTION,HttpHeaderNames.KEEP_ALIVE);
		}
		ctx.write(response);
		ChannelFuture sendFileFuture ;
		//通过ChunkedFile对象将文件写入到发送缓冲区中
		sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile,0,fileLength,8192),ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			
			@Override
			public void operationComplete(ChannelProgressiveFuture arg0)
					throws Exception {
				System.err.println("Transfer complete. ");
				
			}
			
			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress,
					long total) throws Exception {
				if(total < 0)
				{
					System.err.println("Transfer progress: "+ progress);
				}else {
					System.err.println("Transfer progress: "+progress + "/"+total);
				}
				
			}
			
			
		});
		//使用chunked编码，最后需要发送一个编码结束的空消息体，如果写入缓冲区完成，flush清空缓冲区，发送出去
		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		if(!isKeepAlive(request))
		{
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}




	
	

}
