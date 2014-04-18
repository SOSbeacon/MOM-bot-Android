package org.cnc.msrobot.InputOutput;


public interface Input {

	/**
	 * show input, mark with id
	 * 
	 * @param id
	 * @return
	 */
	public void show(String id);

	public void setReceiveCallback(InputReceiveCallback callback);
	
	public interface InputReceiveCallback {
		/**
		 * input callback data receiver, mark with id
		 * 
		 * @param data data receiver
		 * @param id
		 * @return true if data is result you want. Return false will call next callback for next data receiver
		 */
		public boolean onReceive(String data, String id);
		
		/**
		 * input callback onFail when all onReceive return false
		 * 
		 * @param id
		 */
		public void onFail(String data, String id);
	}
}


