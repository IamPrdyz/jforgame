package jforgame.server.cross.core.client;

import jforgame.commons.TimeUtil;
import jforgame.server.cross.core.server.CMessageDispatcher;
import jforgame.socket.mina.MinaMessageCodecFactory;
import jforgame.socket.support.MessageFactoryImpl;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class CCSession {

	private static Logger logger = LoggerFactory.getLogger(CCSession.class);

	private int id;

	private static java.util.concurrent.atomic.AtomicInteger idFactory = new AtomicInteger();

	private String ipAddr;
	/**
	 * remote 端口
	 */
	private int port;

	private CMessageDispatcher dispatcher;

	private IoSession wrapper;

	private long lastWriteTime;

	public static CCSession valueOf(String ip, int port, CMessageDispatcher dispatcher) {
		CCSession cSession = new CCSession();
		cSession.ipAddr = ip;
		cSession.port = port;
		cSession.id = idFactory.getAndIncrement();
		cSession.dispatcher = dispatcher;
		return cSession;
	}

	/**
	 * 建立与跨服端口的连接
	 */
	public void buildConnection() {
		NioSocketConnector connector = new NioSocketConnector();
		connector.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(new MinaMessageCodecFactory(MessageFactoryImpl.getInstance())));
		connector.setHandler(new IoHandlerAdapter() {
			@Override
			public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			}
			@Override
			public void messageReceived(IoSession session, Object data) throws Exception {
				dispatcher.clientDispatch(CCSession.this, data);
			}
		});

		logger.info("开始连接跨服服务器端口" + port);
		ConnectFuture future = connector.connect(new InetSocketAddress(port));

		future.awaitUninterruptibly();
		IoSession session = future.getSession();
		this.wrapper = session;
		this.lastWriteTime = System.currentTimeMillis();
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public int getPort() {
		return port;
	}

	public IoSession getWrapper() {
		return wrapper;
	}

	public int getId() {
		return id;
	}

	public void sendMessage(Object message) {
		WriteFuture future = this.wrapper.write(message);
		if (future.isWritten()) {
			this.lastWriteTime = System.currentTimeMillis();
		}
	}

	public void sendMessage(Object message, Runnable sentCallback) {
		WriteFuture future = this.wrapper.write(message);
		if (future.isWritten()) {
			this.lastWriteTime = System.currentTimeMillis();
			sentCallback.run();
		}
	}


	public boolean isExpired() {
		long now = System.currentTimeMillis();
		long diff = now - lastWriteTime;
		return diff > 30 * TimeUtil.MILLIS_PER_SECOND;
	}

	@Override
	public String toString() {
		return String.format("session[ip='%s',port='%s']", ipAddr, port);
	}
}
