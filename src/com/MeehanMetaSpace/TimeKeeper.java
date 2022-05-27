package com.MeehanMetaSpace;

public class TimeKeeper {
	private long _prior, _reset;
	private String _taskName;
	public long reset(final String taskName) {
		_taskName=taskName;
		if (_prior > 0){
			stop();
		}
		_prior = System.currentTimeMillis();
		_reset=_prior;
		announce(TYPE.START, Basics.concat("\"", _taskName, "\""));
		return _prior;
	}

	public void stop() {
		announce(TYPE.STOP, _reset, Basics.concat("\"", _taskName, "\""));
		_prior = 0;
		
	}

	public long announce(final String clue) {
		return announce(TYPE.CHECKPOINT, clue);
	}
	public long announce(final TYPE type, final String clue) {
		if (_prior > 0) {
			return _announce(type, _prior, clue);
		}
		return 0;
	}
	
	public void announce(final long priorTime, final String clue){
		announce(TYPE.CHECKPOINT, priorTime, clue);
	}
	public void announce(TYPE type, final long priorTime, final String clue){
		_announce(type, priorTime,clue);
	}
	enum TYPE{
		START,
		STOP,
		CHECKPOINT
	};
	private long _announce(final TYPE type, final long priorTime, final String clue){
		final long newTime = System.currentTimeMillis();
		System.out.print("------>  TimeKeeper ");
		if (type==TYPE.START){
			System.out.print("START  TASK");
		} else if (type==TYPE.STOP){
			System.out.print("STOP  TASK");
		} else {
			System.out.print("CHECKPOINT");
		}
		System.out.print(":  ");
		System.out.print(newTime - priorTime);
		System.out.print(" millisecs;  \"");
		System.out.print(clue);
		System.out.println("\". ");
		_prior = newTime;
		return newTime;
	}

}
