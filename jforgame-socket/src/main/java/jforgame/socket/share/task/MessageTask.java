package jforgame.socket.share.task;

import java.lang.reflect.Method;

import jforgame.socket.IdSession;
import jforgame.socket.client.Traceful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 将用户的消息请求封装成一个命令
 */
public class MessageTask extends BaseGameTask {

	private static Logger logger = LoggerFactory.getLogger(MessageTask.class);

	private IdSession session;

	/** message controller */
	private Object handler;
	/** target method of the controller */
	private Method method;
	/** arguments passed to the method */
	private Object[] params;

	public static MessageTask valueOf(IdSession session, long dispatchKey, Object handler,
									  Method method, Object[] params) {
		MessageTask msgTask = new MessageTask();
		msgTask.dispatchKey = dispatchKey;
		msgTask.session = session;
		msgTask.handler = handler;
		msgTask.method  = method;
		msgTask.params  = params;

		return msgTask;
	}

	@Override
	public void action() {
		try{
			Object response = method.invoke(handler, params);
			if (response != null) {
//				if (response instanceof Traceful) {
//					Traceful traceful = (Traceful) response;
//				}
				session.sendPacket(response);
			}
		}catch(Exception e){
			logger.error("message task execute failed ", e);
		}
	}

	public Object getHandler() {
		return handler;
	}

	public Method getMethod() {
		return method;
	}

	public Object[] getParams() {
		return params;
	}

	@Override
	public String toString() {
		return  "[" + handler.getClass().getName() + "@" + method.getName() + "]";
	}

}
