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
		 * input return data, mark with id
		 * 
		 * @param data
		 *            list of data
		 * @param id
		 */
		void onReceive(String data, String id);
	}
}


