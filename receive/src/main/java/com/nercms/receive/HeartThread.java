/**
 * HeartThread.java Nov 25, 2009
 * 
 * Copyright 2009 xwz, Inc. All rights reserved.
 */
package com.nercms.receive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class HeartThread implements Runnable {
	private DatagramSocket ds;
	private DatagramPacket p;

	public HeartThread(DatagramSocket ds, DatagramPacket p) {
		this.ds = ds;
		this.p = p;
	}

	public void run() {
		while (true) {
			try {
				ds.send(p);
				Thread.sleep(45000);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}
