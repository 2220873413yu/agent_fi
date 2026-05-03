package com.xms.app.plugin.preview;

/**
 * @author: GT63S
 * @createDate: 2024/9/12
 */

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;

public class UserTaskEvent {
	private Runnable task;

	public Runnable getTask() {
		return task;
	}

	public void setTask(Runnable task) {
		this.task = task;
	}
}

class UserTaskEventFactory implements EventFactory<UserTaskEvent> {
	@Override
	public UserTaskEvent newInstance() {
		return new UserTaskEvent();
	}
}

class UserTaskEventHandler implements EventHandler<UserTaskEvent> {
	@Override
	public void onEvent(UserTaskEvent event, long sequence, boolean endOfBatch) {
		if (event.getTask() != null) {
			event.getTask().run();
		}
	}
}


