package org.tom.vd.mina;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.tom.vd.config.Config;

import com.effecia.mina.MinaCommandPo;

public class MinaGameInterface implements Runnable {
	private static Logger logger = Logger.getLogger(MinaGameInterface.class);

	public static IoSession ioSession;
	public static NioSocketConnector connector = new NioSocketConnector();

	public MinaGameInterface() {
		this.run();
	}

	public void sendCommand(MinaCommandPo command) {
		if (ioSession != null && !ioSession.isClosing()) {
			try {
				ioSession.write(command);
			} catch (Exception e) {
				logger.error(command.getCommandType() + " write error.");
				e.printStackTrace();
			}

		}
		// }
	}

	public void run() {
		logger.debug("Platform server checker running...");
		connector.getFilterChain().addLast("logger", new LoggingFilter());
		ObjectSerializationCodecFactory objectSerializationCodecFactory = new ObjectSerializationCodecFactory();
		objectSerializationCodecFactory
				.setDecoderMaxObjectSize(Integer.MAX_VALUE);
		objectSerializationCodecFactory
				.setEncoderMaxObjectSize(Integer.MAX_VALUE);
		connector.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(objectSerializationCodecFactory));

		connector.setConnectTimeoutCheckInterval(30);
		connector.getSessionConfig().setReadBufferSize(10240);
		connector.getFilterChain().addLast("threadPool",
				new ExecutorFilter(new OrderedThreadPoolExecutor()));
		connector.setHandler(new PlatformMinaHandler());
		ConnectFuture cf;

		while (true) {
			try {
				cf = connector.connect(new InetSocketAddress(Config.getCfg()
						.getString("minahost"), Integer.parseInt(Config
						.getCfg().getString("minaport"))));
				cf.awaitUninterruptibly();
				ioSession = cf.getSession();
			} catch (RuntimeIoException e) {
				e.printStackTrace();
				logger.warn("Get platform io session failed! Maybe the platform server is not ready yet.");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
			logger.debug("Platform server is ready. Ally-Platform connection ready.");
			break;
		}
		MinaCommandPo minaCommandPo = new MinaCommandPo();
		minaCommandPo.setRequestType("REQUEST");
		minaCommandPo.setModuleType("AP");
		minaCommandPo.setCommandType("LOGIN_PLATFORM");
		minaCommandPo.getParameter().put("passWord", "ssss");
		minaCommandPo.getParameter().put("hashCode", "sss");
		minaCommandPo.getParameter().put("web111SessionId", "abc");
		ioSession.write(minaCommandPo);
	}
}
