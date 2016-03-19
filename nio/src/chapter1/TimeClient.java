package chapter1;
import java.io.IOException;

/**
 * NIO时间服务器
 * @author jonn
 *
 */
public class TimeClient {

	public static void main(String [] args) throws IOException
	{
		
		int port = 8080;
		if(args != null && args.length > 0)
		{
			try {
				port = Integer.valueOf(args[0]);
				
			} catch (NumberFormatException e) {
				//采用默认值
			}
		}
		new Thread(new TimeClientHandle("127.0.0.1", port),"TimeClient-001").start();
	
		
	}
}
