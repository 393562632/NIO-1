package chapter7;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class SubReqServerHandler extends ChannelHandlerAdapter {

	private SubscribeRespProto.SubscribeResp resp(int subReqID) {
		
		SubscribeRespProto.SubscribeResp.Builder builder = SubscribeRespProto.SubscribeResp.newBuilder();
		builder.setSubReqID(subReqID);
		builder.setRespCode(0);
		builder.setDesc("Netty book order succeed ,3 days later, sent to the designated address");
		return builder.build();
	}
	
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SubscribeReqProto.SubscribeReq req = (SubscribeReqProto.SubscribeReq)msg;
		if("LinWeiLiang".equalsIgnoreCase(req.getUserName()))
		{
			System.out.println("Service accept client subscribe req : ["+ req.toString() +"] ");
			ctx.writeAndFlush(resp(req.getSubReqID()));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		//cause.printStackTrace();
		ctx.close();//发生异常，关闭链路
		
		
	}
	
	

}
