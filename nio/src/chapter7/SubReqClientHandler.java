package chapter7;


import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class SubReqClientHandler extends ChannelHandlerAdapter{

	
	public SubReqClientHandler() {
		
	}

	private SubscribeReqProto.SubscribeReq subReq(int i) {
		
		SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
		builder.setSubReqID(i);
		builder.setUserName("LinWeiLiang");
		builder.setProductName("Netty Book for Protobuf");
		List<String> address = new ArrayList<String>();
		address.add("NanJing YuHuaTai");
		address.add("BeiJing LiuLiChang");
		address.add("ShenZhen HongShuLin");
		builder.addAllAddress(address);
		return builder.build();
		
	}
	
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		for (int i = 0; i < 10; i++) {
			ctx.write(subReq(i));
		}
		ctx.flush();
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		System.out.println("Receive server response : ["+ msg +"]");
	}

	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
	//	cause.printStackTrace();
		ctx.close();
	}
	
	
}
