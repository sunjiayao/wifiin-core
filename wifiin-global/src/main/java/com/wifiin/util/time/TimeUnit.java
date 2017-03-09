package com.wifiin.util.time;

import java.util.Calendar;

public enum TimeUnit {
	MILLISECOND(3){
		@Override
		public long currentUnitStartMillis(long millis) {
			return TimeUnit.currentUnitStartMillis(MILLISECOND, millis);
		}
		@Override
		public long nextUnitStartMillis(long millis) {
			return TimeUnit.nextUnitStartMillis(MILLISECOND, millis);
		}
		@Override
		public long nextNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(MILLISECOND,amount);
		}
		@Override
		public long beforeNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(MILLISECOND,-amount);
		}
		@Override
		public long nextNUnitMillisFromCurrent(int amount) {
			return nunitMillis(MILLISECOND,amount);
		}
		@Override
		public long beforeNUnitMillis(int amount) {
			return nunitMillis(MILLISECOND,-amount);
		}
		@Override
		public long nextNUnitMillis(long from, int amount) {
			return nunitMillis(MILLISECOND,from,amount);
		}
		@Override
		public long beforeNUnitMillis(long from, int amount) {
			return nunitMillis(MILLISECOND,from,-amount);
		}
		@Override
		public long nextNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(MILLISECOND,from,amount);
		}
		@Override
		public long beforeNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(MILLISECOND,from,-amount);
		}
	},SECOND(4){
		@Override
		public long currentUnitStartMillis(long millis) {
			return TimeUnit.currentUnitStartMillis(SECOND, millis);
		}
		@Override
		public long nextUnitStartMillis(long millis) {
			return TimeUnit.nextUnitStartMillis(SECOND, millis);
		}
		@Override
		public long nextNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(SECOND,amount);
		}
		@Override
		public long beforeNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(SECOND,-amount);
		}
		@Override
		public long nextNUnitMillisFromCurrent(int amount) {
			return nunitMillis(SECOND,amount);
		}
		@Override
		public long beforeNUnitMillis(int amount) {
			return nunitMillis(SECOND,-amount);
		}
		@Override
		public long nextNUnitMillis(long from, int amount) {
			return nunitMillis(SECOND,from,amount);
		}
		@Override
		public long beforeNUnitMillis(long from, int amount) {
			return nunitMillis(SECOND,from,-amount);
		}
		@Override
		public long nextNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(SECOND,from,amount);
		}
		@Override
		public long beforeNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(SECOND,from,-amount);
		}
	},MINUTE(5){
		@Override
		public long currentUnitStartMillis(long millis) {
			return TimeUnit.currentUnitStartMillis(MINUTE, millis);
		}
		@Override
		public long nextUnitStartMillis(long millis) {
			return TimeUnit.nextUnitStartMillis(MINUTE, millis);
		}
		@Override
		public long nextNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(MINUTE,amount);
		}
		@Override
		public long beforeNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(MINUTE,-amount);
		}
		@Override
		public long nextNUnitMillisFromCurrent(int amount) {
			return nunitMillis(MINUTE,amount);
		}
		@Override
		public long beforeNUnitMillis(int amount) {
			return nunitMillis(MINUTE,amount);
		}
		@Override
		public long nextNUnitMillis(long from, int amount) {
			return nunitMillis(MINUTE,from,-amount);
		}
		@Override
		public long beforeNUnitMillis(long from, int amount) {
			return nunitMillis(MINUTE,from,-amount);
		}
		@Override
		public long nextNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(MINUTE,from,amount);
		}
		@Override
		public long beforeNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(MINUTE,from,-amount);
		}
	},HOUR(6){
		@Override
		public long currentUnitStartMillis(long millis) {
			return TimeUnit.currentUnitStartMillis(HOUR, millis);
		}
		@Override
		public long nextUnitStartMillis(long millis) {
			return TimeUnit.nextUnitStartMillis(HOUR, millis);
		}
		@Override
		public long nextNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(HOUR,amount);
		}
		@Override
		public long beforeNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(HOUR,-amount);
		}
		@Override
		public long nextNUnitMillisFromCurrent(int amount) {
			return nunitMillis(HOUR,amount);
		}
		@Override
		public long beforeNUnitMillis(int amount) {
			return nunitMillis(HOUR,-amount);
		}
		@Override
		public long nextNUnitMillis(long from, int amount) {
			return nunitMillis(HOUR,from,amount);
		}
		@Override
		public long beforeNUnitMillis(long from, int amount) {
			return nunitMillis(HOUR,from,-amount);
		}
		@Override
		public long nextNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(HOUR,from,amount);
		}
		@Override
		public long beforeNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(HOUR,from,-amount);
		}
	},DAY(7){
		@Override
		public long currentUnitStartMillis(long millis) {
			return TimeUnit.currentUnitStartMillis(DAY, millis);
		}
		@Override
		public long nextUnitStartMillis(long millis) {
			return TimeUnit.nextUnitStartMillis(DAY, millis);
		}
		@Override
		public long nextNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(DAY,amount);
		}
		@Override
		public long beforeNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(DAY,-amount);
		}
		@Override
		public long nextNUnitMillisFromCurrent(int amount) {
			return nunitMillis(DAY,amount);
		}
		@Override
		public long beforeNUnitMillis(int amount) {
			return nunitMillis(DAY,-amount);
		}
		@Override
		public long nextNUnitMillis(long from, int amount) {
			return nunitMillis(DAY,from,amount);
		}
		@Override
		public long beforeNUnitMillis(long from, int amount) {
			return nunitMillis(DAY,from,-amount);
		}
		@Override
		public long nextNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(DAY,from,amount);
		}
		@Override
		public long beforeNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(DAY,from,-amount);
		}
	},WEEK(8){
		@Override
		public long currentUnitStartMillis(long millis) {
			return TimeUnit.currentUnitStartMillis(WEEK, millis);
		}
		@Override
		public long nextUnitStartMillis(long millis) {
			return TimeUnit.nextUnitStartMillis(WEEK, millis);
		}
		@Override
		public long nextNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(WEEK,amount);
		}
		@Override
		public long beforeNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(WEEK,-amount);
		}
		@Override
		public long nextNUnitMillisFromCurrent(int amount) {
			return nunitMillis(WEEK,amount);
		}
		@Override
		public long beforeNUnitMillis(int amount) {
			return nunitMillis(WEEK,-amount);
		}
		@Override
		public long nextNUnitMillis(long from, int amount) {
			return nunitMillis(WEEK,from,amount);
		}
		@Override
		public long beforeNUnitMillis(long from, int amount) {
			return nunitMillis(WEEK,from,-amount);
		}
		@Override
		public long nextNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(WEEK,from,amount);
		}
		@Override
		public long beforeNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(WEEK,from,-amount);
		}
	},MONTH(9){
		@Override
		public long currentUnitStartMillis(long millis) {
			return TimeUnit.currentUnitStartMillis(MONTH, millis);
		}
		@Override
		public long nextUnitStartMillis(long millis) {
			return TimeUnit.nextUnitStartMillis(MONTH, millis);
		}
		@Override
		public long nextNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(MONTH,amount);
		}
		@Override
		public long beforeNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(MONTH,-amount);
		}
		@Override
		public long nextNUnitMillisFromCurrent(int amount) {
			return nunitMillis(MONTH,amount);
		}
		@Override
		public long beforeNUnitMillis(int amount) {
			return nunitMillis(MONTH,-amount);
		}
		@Override
		public long nextNUnitMillis(long from, int amount) {
			return nunitMillis(MONTH,from,amount);
		}
		@Override
		public long beforeNUnitMillis(long from, int amount) {
			return nunitMillis(MONTH,from,-amount);
		}
		@Override
		public long nextNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(MONTH,from,amount);
		}
		@Override
		public long beforeNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(MONTH,from,-amount);
		}
	},YEAR(11) {
		@Override
		public long currentUnitStartMillis(long millis) {
			return TimeUnit.currentUnitStartMillis(YEAR, millis);
		}
		@Override
		public long nextUnitStartMillis(long millis) {
			return TimeUnit.nextUnitStartMillis(YEAR, millis);
		}
		@Override
		public long nextNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(YEAR,amount);
		}
		@Override
		public long beforeNUnitStartMillisFromCurrent(int amount) {
			return nunitStartMillisFromCurrent(YEAR,-amount);
		}
		@Override
		public long nextNUnitMillisFromCurrent(int amount) {
			return nunitMillis(YEAR,amount);
		}
		@Override
		public long beforeNUnitMillis(int amount) {
			return nunitMillis(YEAR,-amount);
		}
		@Override
		public long nextNUnitMillis(long from, int amount) {
			return nunitMillis(YEAR,from,amount);
		}
		@Override
		public long beforeNUnitMillis(long from, int amount) {
			return nunitMillis(YEAR,from,-amount);
		}
		@Override
		public long nextNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(YEAR,from,amount);
		}
		@Override
		public long beforeNUnitStartMillis(long from, int amount) {
			return nunitStartMillis(YEAR,from,-amount);
		}
	};
	private int value;
	private TimeUnit(int value){
		this.value=value;
	}
	public int getValue(){
		return value;
	}
	public static long nunitStartMillisFromCurrent(TimeUnit unit,int amount){
		return nunitMillis(unit,currentUnitStart(unit,System.currentTimeMillis()),amount);
	}
	public static long nunitStartMillis(TimeUnit unit,long from,int amount){
		return nunitMillis(unit,currentUnitStart(unit,from),amount);
	}
	
	public static long nunitMillis(TimeUnit unit, Calendar from, int amount){
		switch(unit){
		case YEAR:from.add(Calendar.YEAR, amount);break;
		case MONTH:from.add(Calendar.MONTH, amount);break;
		case WEEK:from.add(Calendar.WEEK_OF_YEAR, amount);break;
		case DAY:from.add(Calendar.DATE, amount);break;
		case HOUR:from.add(Calendar.HOUR, amount);break;
		case MINUTE:from.add(Calendar.MINUTE, amount);break;
		case SECOND:from.add(Calendar.SECOND, amount);break;
		case MILLISECOND:from.add(Calendar.MILLISECOND, amount);break;
		}
		return from.getTimeInMillis();
	}
	public static long nunitMillis(TimeUnit unit, long from, int amount){
		Calendar calendar=Calendar.getInstance();
		calendar.setTimeInMillis(from);
		return nunitMillis(unit,from,amount);
	}
	public static long nunitMillis(TimeUnit unit,int amount){
		return nunitMillis(unit,Calendar.getInstance(),amount);
	}
	
	public static TimeUnit getByValue(int unit){
		TimeUnit[] tus=TimeUnit.values();
		for(int i=0,l=tus.length;i<l;i++){
			TimeUnit tu=tus[i];
			if(tu.value==unit){
				return tu;
			}
		}
		return null;
	}
	public static Calendar currentUnitStart(TimeUnit unit,long millis){
		Calendar calendar=Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		TimeUnit tu=unit;
		switch(tu){
		case YEAR:
			calendar.set(Calendar.MONTH, Calendar.JANUARY);
		case MONTH:
			calendar.set(Calendar.DATE, 1);
			tu=DAY;
			break;
		case WEEK:
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			tu=DAY;
		}
		switch(tu){
		case DAY:
			calendar.set(Calendar.HOUR_OF_DAY,0);
		case HOUR:
			calendar.set(Calendar.MINUTE, 0);
		case MINUTE:
			calendar.set(Calendar.SECOND, 0);
		case SECOND:
			calendar.set(Calendar.MILLISECOND, 0);
		}
		return calendar;
	}
	public static long currentUnitStartMillis(TimeUnit unit,long millis){
		return currentUnitStart(unit,millis).getTimeInMillis();
	}
	public static long nextUnitStartMillis(TimeUnit unit,long millis){
		Calendar calendar=currentUnitStart(unit,millis);
		switch(unit){
		case YEAR:
			calendar.add(Calendar.YEAR, 1);
			break;
		case MONTH:
			calendar.add(Calendar.MONTH, 1);
			break;
		case WEEK:
			calendar.add(Calendar.DATE, Calendar.SATURDAY-calendar.get(Calendar.DAY_OF_WEEK)+1);
			break;
		case DAY:
			calendar.add(Calendar.DAY_OF_MONTH,1);
			break;
		case HOUR:
			calendar.add(Calendar.HOUR, 1);
			break;
		case MINUTE:
			calendar.add(Calendar.MINUTE, 1);
			break;
		case SECOND:
			calendar.add(Calendar.SECOND, 1);
			break;
		case MILLISECOND:
			calendar.add(Calendar.MILLISECOND, 1);
			break;
		}
		return calendar.getTimeInMillis();
	}
	public abstract long currentUnitStartMillis(long millis);
	public abstract long nextUnitStartMillis(long millis);
	public abstract long nextNUnitStartMillisFromCurrent(int amount);
	public abstract long beforeNUnitStartMillisFromCurrent(int amount);
	public abstract long nextNUnitStartMillis(long from,int amount);
	public abstract long beforeNUnitStartMillis(long from,int amount);
	public abstract long nextNUnitMillis(long from,int amount);
	public abstract long beforeNUnitMillis(long from,int amount);
	public abstract long nextNUnitMillisFromCurrent(int amount);
	public abstract long beforeNUnitMillis(int amount);
}
