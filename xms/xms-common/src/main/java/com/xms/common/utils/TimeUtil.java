package com.xms.common.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

public class TimeUtil {

	public static SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");

	public static SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");

	public static SimpleDateFormat format2 = new SimpleDateFormat("HHmmss");

	public static SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");

	public static SimpleDateFormat format4 = new SimpleDateFormat("yyyyMMddHHmmss");

	public static SimpleDateFormat format5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static SimpleDateFormat format6 = new SimpleDateFormat("MM/dd/yyyy");

	public static SimpleDateFormat format7 = new SimpleDateFormat("yyyyMMddHHmm");

	public static SimpleDateFormat format8 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	public static SimpleDateFormat format9 = new SimpleDateFormat("yyyy/MM/dd");

	public static SimpleDateFormat format10 = new SimpleDateFormat("yyyyMM");


	 /**
	 * 把字符串时间转换为date类型
	 * @param strDate：请求参数，字符串时间
	 * @param requestFormat，字符串时间的格式
	 * @return
	 */
	public static Date strToDate(String strDate, SimpleDateFormat requestFormat) {
		 ParsePosition pos = new ParsePosition(0);
		 Date strtodate = requestFormat.parse(strDate, pos);
		 return strtodate;
	}


	/**
	 * 获取某一天的日期，格式例如：20181115
	 * @return
	 */
	public static String getDayString() {
		Date date=new Date();
		return format1.format(date);
	}

	/**
	 * 获取当前月，格式例如：201811
	 * @return
	 */
	public static String getDayFormat10() {
		Date date=new Date();
		return format10.format(date);
	}

	/**
	 * 获取当天的日期，格式例如：2018-11-15
	 * @return
	 */
	public static String getDayFormat3(){
		Date date=new Date();
		return format3.format(date);
	}

	public static Date getDayFormat3(String date){
		try {
			return format3.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getDayFormat8(){
		Date date=new Date();
		return format8.format(date);
	}

	public static String getDayFormat9(){
		Date date=new Date();
		return format9.format(date);
	}

	/**
	 * 获取时间 HHmmss
	 * @return
	 */
	public static String getTimeString(){
		return format2.format(new Date());
	}


	/**
	 * 获取某一天的某个具体时间，精确到秒，格式例如：20181115161512
	 * @return
	 */
	public static String getDate() {
		Date date=new Date();
		return format4.format(date);
	}


	/**
	 * 获取某一天的某个具体时间，精确到分，格式例如：201811151615
	 * @return
	 */
	public static String getPid() {
		Date date=new Date();
		return format7.format(date);
	}


	/**
	 * 字符串时间20190410000000转换为时间戳
	 * @param strDate
	 * @return
	 */
	public static final long getTimeString(String strDate) {
		ParsePosition pos = new ParsePosition(0);
		 Date strtodate = format4.parse(strDate, pos);
		 return strtodate.getTime();
	}

	/**
	 * 字符串时间yyyy-MM-dd HH:mm:ss转换为时间戳
	 * @param strDate
	 * @return
	 */
	public static final long getTimeStringFormat5(String strDate) {
		ParsePosition pos = new ParsePosition(0);
		 Date strtodate = format5.parse(strDate, pos);
		 return strtodate.getTime();
	}


	/**
	 * 时间戳转日期
	 * @param s
	 * @return
	 */
    public static String stampToDate(String timeStamp){
         return format1.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
    }


    /**
     * 时间戳转时间点
     * @param timeStamp
     * @return
     */
    public static String stampToTime(String timeStamp){
        return format2.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
   }





	/**
	 * 获取某一天的某个具体时间，精确到毫秒，格式例如：2018111516151111
	 * @param date
	 * @return
	 */
	public static String format1(Date date){
	     String firstDay = format1.format(date);
	     return firstDay;
	}

	public static String format2(Date date){
	     String firstDay = format2.format(date);
	     return firstDay;
	}

	public static String get_format4(Date date){
	     String firstDay = format4.format(date);
	     return firstDay;
	}

	public static String get_format5(Date date){
	     String firstDay = format5.format(date);
	     return firstDay;
	}


	/**
	 * 把字符串时间转换为date类型
	 * @param strDate
	 * @return
	 */
	public static Date strToDateLong(String strDate) {
		 ParsePosition pos = new ParsePosition(0);
		 Date strtodate = format5.parse(strDate, pos);
		 System.out.println(strtodate);
		 return strtodate;
	}


	/**
	 * 获得当前月的第一天
	 * @param date
	 * @return yyyyMMdd
	 */
	public static String getFirstDayOfMonth(){
		 Calendar calendar=Calendar.getInstance();//获取当前日期
		 calendar.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
		 return format1.format(calendar.getTime());
	}

	/**
	 * 获得某一个月的第一天
	 * @param date
	 * @return yyyyMMdd
	 * @throws ParseException
	 */
	public static String getFirstDayOfMonth(String date) throws ParseException{
		 Calendar calendar=Calendar.getInstance();//获取当前日期
		 calendar.setTime(format10.parse(date));
		 calendar.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
		 return format1.format(calendar.getTime());
	}

	/**
	 * 获得某当前月的最后一天
	 * @param date yyyyMM
	 * @return yyyyMMdd
	 */
	public static String getLastDayOfMonth(){
		 Calendar calendar=Calendar.getInstance();//获取当前日期
		 calendar.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
		 calendar.add(Calendar.MONTH, 1);
		 calendar.add(Calendar.DAY_OF_YEAR,-1);
		 return format1.format(calendar.getTime());
	}

	/**
	 * 获得某一个月的最后一天
	 * @param date yyyyMM
	 * @return yyyyMMdd
	 * @throws ParseException
	 */
	public static String getLastDayOfMonth(String date) throws ParseException{
		 Calendar calendar=Calendar.getInstance();//获取当前日期
		 calendar.setTime(format10.parse(date));
		 calendar.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
		 calendar.add(Calendar.MONTH, 1);
		 calendar.add(Calendar.DAY_OF_YEAR,-1);
		 return format1.format(calendar.getTime());
	}



	/**
	 * 获取一天的开始时间，格式例如：2018-11-15 00:00:00
	 * @param date
	 * @return
	 */
	public static String dateStart(String date){
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(new Date(date));
    	return format5.format(cal0.getTime());
	}


	/**
	 * 获取一天的结束时间，格式例如：2018-11-15 23:59:59
	 * @param date
	 * @return
	 */
	public static String dateEnd(String date){
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(new Date(date));
    	cal0.set(Calendar.HOUR_OF_DAY, 23);
    	cal0.set(Calendar.MINUTE, 59);
    	cal0.set(Calendar.SECOND, 59);
    	return format5.format(cal0.getTime());
	}


	 /**
     * 查询日期格式转换
     * @param date
     * @return
     */
    public static String dateSelect(String date)
	{
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(new Date(date));
    	return format1.format(cal0.getTime());
	}


	/**
	 * 获取搜索的时间范围
	 * @param map
	 * @return
	 */
	public static Map<String, Object> getSearchTime(Map<String, Object> map){
		if(null!=(map.get("selectTime"))) {
        	String[] times= map.get("selectTime").toString().split(" - ");
        	map.put("beginTime",dateStart(times[0]));
        	map.put("endTime",dateEnd(times[1]));
        }
		return map;
	}


	/**
	 * 获取搜索的时间范围
	 * @param map
	 * @return
	 */
	public static Map<String, Object> getSelectTime(Map<String, Object> map){
		if(null!=(map.get("selectTime"))) {
			String[] times = map.get("selectTime").toString().split(" - ");
			map.put("beginTime", TimeUtil.dateSelect(times[0]));
			map.put("endTime", TimeUtil.dateSelect(times[1]));
        }
		return map;
	}


	/**
	 * 审核时间间隔
	 * @param auditDate
	 * @param minu
	 * @return
	 */
	public static boolean TimeCompare(Date auditDate,Integer minu) {
		//将审核时间date类型转换为Calendar并且加上minu分钟
		Calendar cal=Calendar.getInstance();
		cal.setTime(auditDate);
		cal.add(Calendar.MINUTE, minu);
		//再将更改后的审核时间还原为date类型
		Date auditDate1=cal.getTime();
		//当前时间
		Date currentDate=new Date();
		//当前时间小于审核时间加上的分钟
		if(currentDate.before(auditDate1)) {
			return false;
		}else {
			return true;
		}
	}


	/**
	 * 判断time是否在from，to之内
	 * @param time：
	 * @param from：起始时间
	 * @param to：结束时间
	 * @return
	 */
	public static boolean belongCalendar(Date time, Date from, Date to) {
		if (time.getTime()==from.getTime()||time.getTime()==to.getTime()) {
            return true;
        }
		//当前时间
        Calendar date = Calendar.getInstance();
        date.setTime(time);
        //起始时间
        Calendar after = Calendar.getInstance();
        after.setTime(from);
        //结束时间
        Calendar before = Calendar.getInstance();
        before.setTime(to);
        //判断
        if (date.after(after) && date.before(before)) {
            return true;
        } else {
            return false;
        }
    }


	/**
	 * 返回昨天日期
	 * @return yyyyMMdd
	 */
	public static String getYesterday(){
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(new Date());
    	cal0.add(Calendar.DAY_OF_MONTH, -1);  //设置为前一天
    	return format1.format(cal0.getTime());
	}


	/**
	 * 获得多少天之后的时间戳
	 * @param date
	 * @param day
	 * @return
	 */
	public static long getAfterDayChuo(Date date, int day) {
		Calendar c = Calendar.getInstance();
	    c.setTime(date);
	    c.add(c.DAY_OF_YEAR, day);
	    return new Timestamp(c.getTimeInMillis()).getTime();
	}




	/**
	 * 如果时间为空，则默认时间为当天，否则返回时间
	 * @param timeStr
	 * @return
	 */
	public static String getDateString1(String timeStr){
		if(StringUtils.isEmpty(timeStr)) {
			return format1.format(new Date());
		}else {
			return timeStr;
		}
	}


	/**
	 * 获取当月第一天日期，如：20181001
	 * @return
	 */
	public static String getMonthBeginDateString(){
		return format1.format(getBeginDayOfMonth());
	}

	/**
	 * 前30天
	 * @return
	 */
	public static String getThirtyBeforeDateString(){
		Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(c.DAY_OF_YEAR,-30);
        return format1.format(c.getTime());
	}

	/**
	 * 获得指定年月的第一天日期，若未指定则默认查当月的第一天日期
	 * @param month
	 * @return
	 */
	public static String getOneMonthBeginDateString(String year,String month){
		//如果年份为空，则默认查所有
		if(StringUtils.isEmpty(year)) {
			//year=getNowYear().toString();//默认本年
			return null;
		}
		//如果月份为空，则默认第一月
		if(StringUtils.isEmpty(month)) {
			//month=getNowMonth().toString();//默认当月
			month="1";
		}
		return format1.format(getBeginDayOfOneMonth(Integer.parseInt(year),Integer.parseInt(month)));
	}


	/**
	 * 获取当月最后一天日期，例如：20181031
	 * @return
	 */
	public static String getMonthEndDateString(){
		return format1.format(getEndDayOfMonth());
	}


	/**
	 * 获得指定年月的最后一天日期，若未指定则默认查当月的最后一天日期
	 * @param month
	 * @return
	 */
	public static String getOneMonthEndDateString(String year,String month){
		//如果年份为空，则默认所有
		if(StringUtils.isEmpty(year)) {
			//year=getNowYear().toString();//默认本年
			return null;
		}
		//如果月份为空，则默认最后一个月
		if(StringUtils.isEmpty(month)) {
			//month=getNowMonth().toString();//默认当月
			month="12";
		}
		return format1.format(getEndDayOfOneMonth(Integer.parseInt(year),Integer.parseInt(month)));
	}


	/**
	 * 时间处理，将20180101101201换为2018/01/01 10:12:01
	 * @param timeStr
	 * @return
	 */
	public static String timeDeal(String timeStr) {
		String pattern = "(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})";
		String timeType="$1-$2-$3 $4:$5:$6";
		return timeStr.replaceFirst(pattern, timeType);
	}


	/**
	 * 前30天
	 * @param date
	 * @return
	 */
	public static Date getSevenDayBefore(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(c.DAY_OF_YEAR,-7);
        return TimeUtil.getDayEndTime(c.getTime());

    }


	/**
	 * 得到当天凌晨时间
	 * @return
	 */
	public static String dateStart(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(new Date());
    	cal0.set(Calendar.HOUR_OF_DAY, 0);
    	cal0.set(Calendar.MINUTE, 0);
    	cal0.set(Calendar.SECOND, 0);
    	return dateFormat.format(cal0.getTime());
	}


	/**
	 * 得到当天最后一秒时间
	 * @return
	 */
	public static String dateEnd(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(new Date());
    	cal0.set(Calendar.HOUR_OF_DAY, 23);
    	cal0.set(Calendar.MINUTE, 59);
    	cal0.set(Calendar.SECOND, 59);
    	return dateFormat.format(cal0.getTime());
	}


	/**
	 * 得到当前时间日期
	 * @return
	 */
	public static String getCurrentDay(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(date);
    	return dateFormat.format(cal0.getTime());
	}


	public static String dateStart1(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(date);
    	cal0.set(Calendar.HOUR_OF_DAY, 0);
    	cal0.set(Calendar.MINUTE, 0);
    	cal0.set(Calendar.SECOND, 0);
    	return dateFormat.format(cal0.getTime());
	}


	public static String dateEnd1(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(date);
    	cal0.set(Calendar.HOUR_OF_DAY, 23);
    	cal0.set(Calendar.MINUTE, 59);
    	cal0.set(Calendar.SECOND, 59);
    	return dateFormat.format(cal0.getTime());
	}


	/**
	 * 得到昨天最后一秒时间
	 * @return
	 */
	public static String dateYesEnd(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Calendar cal0 = Calendar.getInstance();
    	cal0.setTime(new Date());
    	cal0.add(Calendar.DAY_OF_MONTH, -1);  //设置为前一天
    	cal0.set(Calendar.HOUR_OF_DAY, 23);
    	cal0.set(Calendar.MINUTE, 59);
    	cal0.set(Calendar.SECOND, 59);
    	return dateFormat.format(cal0.getTime());
	}


	//============================借助Calendar类获取今天、昨天、本周、上周、本年及特定时间的开始时间和结束时间（返回类型为date类型）========================

	/**
	 * 获取当天开始时间
	 * @return
	 */
	public static Date getDayBegin(){
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);//0点
		cal.set(Calendar.MINUTE, 0);//0分
		cal.set(Calendar.SECOND, 0);//0秒
		cal.set(Calendar.MILLISECOND, 0);//0毫秒
		return cal.getTime();
	}


	/**
	 * 获取当天结束时间
	 * @return
	 */
	public static Date getDayEnd(){
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);//23点
		cal.set(Calendar.MINUTE, 59);//59分
		cal.set(Calendar.SECOND, 59);//59秒
		return cal.getTime();
	}


	/**
	 * 获取昨天开始时间
	 * @return
	 */
	public static Date getBeginDayOfYesterday(){
		Calendar cal=Calendar.getInstance();
		cal.setTime(getDayBegin());//当天开始时间
		cal.add(Calendar.DAY_OF_MONTH, -1);//当天月份天数减1
		return cal.getTime();
	}


	/**
	 * 获取昨天结束时间
	 * @return
	 */
	public static Date getEndDayOfYesterday(){
		Calendar cal=Calendar.getInstance();
		cal.setTime(getDayEnd());//当天结束时间
		cal.add(Calendar.DAY_OF_MONTH, -1);//当天月份天数减1
		return cal.getTime();
	}


	/**
	 * 获取明天开始时间
	 * @return
	 */
	public static Date getBeginDayOfTomorrow(){
		Calendar cal=Calendar.getInstance();
		cal.setTime(getDayBegin());//当天开始时间
		cal.add(Calendar.DAY_OF_MONTH, 1);//当天月份天数加1
		return cal.getTime();
	}

	/**
	 *
	* @Title: getTomorrowString
	* @param:
	* @Description: 获取明天的日期
	* @return String
	 */
	public static String getTomorrowString(){
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DATE, 1);//当天月份天数加1
		return format1.format(cal.getTime());
	}

	/**
	 *
	* @Title: getTomorrowDay
	* @param:
	* @Description: 获取明天的日期
	* @return String
	 */
	public static String getTomorrowDay(){
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DATE, 1);//当天月份天数加1
		return format3.format(cal.getTime());
	}

	/**
	 * 获取明天结束时间
	 * @return
	 */
	public static Date getEndDayOfTomorrow(){
		Calendar cal=Calendar.getInstance();
		cal.setTime(getDayEnd());//当天结束时间
		cal.add(Calendar.DAY_OF_MONTH, 1);//当天月份天数加1
		return cal.getTime();
	}


	/**
	 * 获取某个日期的开始时间
	 * @param d
	 * @return
	 */
    public static Timestamp getDayStartTime(Date d) {
        Calendar calendar=Calendar.getInstance();
        if(null!=d){
        	calendar.setTime(d);
        }
        calendar.set(calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTimeInMillis());
    }


    /**
     * 获取某个日期的结束时间
     * @param d
     * @return
     */
    public static Timestamp getDayEndTime(Date d) {
        Calendar calendar=Calendar.getInstance();
        if(null!=d){
        	calendar.setTime(d);
        }
        calendar.set(calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTimeInMillis());
    }


    /**
     * 获取本周的开始时间
     * @return
     */
	@SuppressWarnings("unused")
	public static Date getBeginDayOfWeek(){
		Date date=new Date();
		if(date==null){
			return null;
		}
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		int dayOfWeek=cal.get(Calendar.DAY_OF_WEEK);
		if(dayOfWeek==1){
			dayOfWeek+=7;
		}
		cal.add(Calendar.DATE, 2-dayOfWeek);
		return getDayStartTime(cal.getTime());
	}


	/**
	 * 获取本周的结束时间
	 * @return
	 */
    public static Date getEndDayOfWeek(){
        Calendar cal=Calendar.getInstance();
        cal.setTime(getBeginDayOfWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime();
        return getDayEndTime(weekEndSta);
    }


    /**
     * 获取上周开始时间
     */
    @SuppressWarnings("unused")
    public static Date getBeginDayOfLastWeek() {
        Date date=new Date();
        if (date==null) {
            return null;
        }
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        int dayofweek=cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek==1) {
            dayofweek+=7;
        }
        cal.add(Calendar.DATE, 2-dayofweek-7);
        return getDayStartTime(cal.getTime());
    }


    /**
     * 获取上周的结束时间
     * @return
     */
    public static Date getEndDayOfLastWeek(){
        Calendar cal=Calendar.getInstance();
        cal.setTime(getBeginDayOfLastWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime();
        return getDayEndTime(weekEndSta);
    }


    /**
     * 获取今年是哪一年
     * @return
     */
    public static Integer getNowYear(){
    	Date date = new Date();
    	GregorianCalendar gc=(GregorianCalendar)Calendar.getInstance();
        gc.setTime(date);
        return Integer.valueOf(gc.get(1));
    }


    /**
     * 获取本月是哪一月
     * @return
     */
    public static Integer getNowMonth() {
    	Date date = new Date();
    	GregorianCalendar gc=(GregorianCalendar)Calendar.getInstance();
    	gc.setTime(date);
        return gc.get(2) + 1;
    }


    /**
     * 获取本月的开始时间
     * @return
     */
    public static Date getBeginDayOfMonth() {
        Calendar calendar=Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth()-1, 1);
        return getDayStartTime(calendar.getTime());
    }


    /**
     * 获得指定月的开始时间
     * @param month
     * @return
     */
    public static Date getBeginDayOfOneMonth(int year,int month) {
        Calendar calendar=Calendar.getInstance();
        calendar.set(year, month-1, 1);
        return getDayStartTime(calendar.getTime());
    }


    /**
     * 获取本月的结束时间
     * @return
     */
    public static Date getEndDayOfMonth() {
    	Calendar calendar=Calendar.getInstance();
    	calendar.set(getNowYear(), getNowMonth()-1, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth()-1, day);
        return getDayEndTime(calendar.getTime());
    }


    /**
     * 获得指定月的结束日期
     * @return
     */
    public static Date getEndDayOfOneMonth(int year,int month) {
    	Calendar calendar=Calendar.getInstance();
    	calendar.set(year, month-1, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(year, month-1, day);
        return getDayEndTime(calendar.getTime());
    }


    /**
     * 获取上月的开始时间
     * @return
     */
    public static Date getBeginDayOfLastMonth() {
        Calendar calendar=Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth()-2, 1);
        return getDayStartTime(calendar.getTime());
    }


    /**
     * 获取上月的结束时间
     * @return
     */
    public static Date getEndDayOfLastMonth() {
        Calendar calendar=Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth()-2, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth()-2, day);
        return getDayEndTime(calendar.getTime());
    }



    public static String getEndDayOfLastMonthString() {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date date = getEndDayOfLastMonth();
    	return dateFormat.format(date.getTime());
    }


    /**
     * 获取本年的开始时间
     * @return
     */
    public static Date getBeginDayOfYear() {
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear());
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        return getDayStartTime(cal.getTime());
    }


    /**
     * 获取本年的结束时间
     * @return
     */
    public static Date getEndDayOfYear() {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, getNowYear());
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DATE, 31);
        return getDayEndTime(cal.getTime());
    }


    /**
     * 两个日期相减得到的天数
     * @param beginDate
     * @param endDate
     * @return
     */
    public static int getDiffDays(Date beginDate, Date endDate) {
        if(beginDate==null||endDate==null) {
            throw new IllegalArgumentException("getDiffDays param is null!");
        }
        long diff=(endDate.getTime()-beginDate.getTime())/(1000*60*60*24);
        int days = new Long(diff).intValue();
        return days;
    }


    /**
     * 两个日期相减得到的毫秒数
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long dateDiff(Date beginDate, Date endDate) {
        long date1ms=beginDate.getTime();
        long date2ms=endDate.getTime();
        return date2ms-date1ms;
    }


    /**
     * 获取两个日期中的最大日起
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date max(Date beginDate, Date endDate) {
        if(beginDate==null) {
            return endDate;
        }
        if(endDate==null) {
            return beginDate;
        }
        if(beginDate.after(endDate)) {//beginDate日期大于endDate
           return beginDate;
        }
        return endDate;
    }


    /**
     * 获取两个日期中的最小日期
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date min(Date beginDate, Date endDate) {
        if(beginDate==null) {
            return endDate;
        }
        if(endDate==null) {
            return beginDate;
        }
        if(beginDate.after(endDate)) {
            return endDate;
        }
        return beginDate;
    }


    /**
     * 获取某月该季度的第一个月
     * @param date
     * @return
     */
    public static Date getFirstSeasonDate(Date date) {
    	final int[] SEASON={ 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4 };
    	Calendar cal=Calendar.getInstance();
    	cal.setTime(date);
    	int sean = SEASON[cal.get(Calendar.MONTH)];
    	cal.set(Calendar.MONTH, sean*3-3);
    	return cal.getTime();
    }


    /**
     * 返回某个日期下几天的日期
     * @param date
     * @param i
     * @return
     */
    public static Date getNextDay(Date date, int i) {
        Calendar cal=new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DATE,cal.get(Calendar.DATE)+i);
        return cal.getTime();
    }


    /**
     * 返回某个日期前几天的日期
     * @param date
     * @param i
     * @return
     */
    public static Date getFrontDay(Date date, int i) {
        Calendar cal=new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DATE, cal.get(Calendar.DATE)-i);
        return cal.getTime();
    }



    /**
   * 获取某年某月按天切片日期集合（某个月间隔多少天的日期集合）
   * @param beginYear
   * @param beginMonth
   * @param k
   * @return
   */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List getTimeList(int beginYear, int beginMonth, int k) {
    	List list = new ArrayList();
        Calendar begincal=new GregorianCalendar(beginYear,beginMonth, 1);
        int max = begincal.getActualMaximum(Calendar.DATE);
        for (int i = 1; i < max; i = i + k) {
        	list.add(begincal.getTime());
            begincal.add(Calendar.DATE, k);
        }
        begincal = new GregorianCalendar(beginYear, beginMonth, max);
        list.add(begincal.getTime());
        return list;
    }


    /**
     * 获取某年某月到某年某月按天的切片日期集合（间隔天数的集合）
     * @param beginYear
     * @param beginMonth
     * @param endYear
     * @param endMonth
     * @param k
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List getTimeList(int beginYear,int beginMonth,int endYear,int endMonth, int k) {
    	List list = new ArrayList();
    	if (beginYear==endYear){
    		for(int j=beginMonth;j<=endMonth;j++){
    			list.add(getTimeList(beginYear,j,k));
    		}
    	}else{
    			{
    				for(int j=beginMonth;j<12;j++){
    					list.add(getTimeList(beginYear,j,k));
    				}
                    for(int i=beginYear+1;i<endYear;i++) {
                    	for (int j=0; j<12; j++) {
                    		list.add(getTimeList(i,j,k));
                    	}
                    }
                    for (int j=0;j <= endMonth; j++) {
                    	list.add(getTimeList(endYear, j, k));
                    }
    			}
    		}
    	return list;
    }




    //=================================时间格式转换==========================

    /**
     * date类型进行格式化输出（返回类型：String）
     * @param date
     * @return
     */
    public static String dateFormat(Date date) {
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          String dateString = formatter.format(date);
          return dateString;
     }


    /**
     * 将"2015-08-31 21:08:06"型字符串转化为Date
     * @param str
     * @return
     * @throws ParseException
     */
    public static Date StringToDate(String str) throws ParseException{
    	SimpleDateFormat formatter1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	SimpleDateFormat formatter2=new SimpleDateFormat("yyyyMMddHHmmss");
    	str=formatter1.format(formatter2.parse(str));
        Date date = (Date) formatter1.parse(str);
        return date;
    }

    /**
     * 将"20150831210806"型字符串转化为"2015-08-31 21:08:06"
     * @param str
     * @return
     * @throws ParseException
     */
    public static String format4Toformat5(String str) throws ParseException{
    	return format5.format(format4.parse(str));
    }

    /**
     * 将CST时间类型字符串进行格式化输出
     * @param str
     * @return
     * @throws ParseException
     */
    public static String CSTFormat(String str) throws ParseException{
    	SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date date = (Date) formatter.parse(str);
        return dateFormat(date);
    }



    /**
     * 将long类型转化为Date
     * @param str
     * @return
     * @throws ParseException
     */
    public static Date LongToDare(long str) throws ParseException{
        return new Date(str * 1000);
    }




    //====================================其他常见日期操作方法======================

    /**
     * 获得当前的日期毫秒
     * @return
     */
    public static long nowTimeMillis() {
    	return System.currentTimeMillis();
    }


    /**
     * 获得当前的时间戳
     * @return
     */
    public static Timestamp nowTimeStamp() {
    	return new Timestamp(nowTimeMillis());
    }


    /**
     * 判断当前日期是否在[startDate, endDate]区间
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     *
     * @return
     */
    public static boolean isEffectiveDate(Date startDate, Date endDate){
        if(startDate == null || endDate == null){
            return false;
        }
        long currentTime = new Date().getTime();
        if(currentTime >= startDate.getTime()
                && currentTime <= endDate.getTime()){
            return true;
        }
        return false;
    }


    /**
     * 得到二个日期间的间隔天数
     * @param secondString：后一个日期
     * @param firstString：前一个日期
     * @return
     */
    public static String getTwoDay(String secondString, String firstString) {
    	SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
    	long day = 0;
    	try {
    		Date secondTime = myFormatter.parse(secondString);
    		Date firstTime = myFormatter.parse(firstString);
    		day = (secondTime.getTime() - firstTime.getTime()) / (24 * 60 * 60 * 1000);
    	} catch (Exception e) {
    		return "";
    	}
    	return day + "";
    }


    /**
     * 时间前推或后推分钟,其中JJ表示分钟.
     * @param StringTime：时间
     * @param minute：分钟（有正负之分）
     * @return
     */
    public static String getPreTime(String StringTime, String minute) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String mydate1 = "";
    	try {
    		Date date1 = format.parse(StringTime);
    		long Time = (date1.getTime() / 1000) + Integer.parseInt(minute) * 60;
    		date1.setTime(Time * 1000);
    		mydate1 = format.format(date1);
    	} catch (Exception e) {
    		return "";
    	}
    	return mydate1;
    }


    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     *
     * @param strDate
     * @return
     */
    public static Date strToDate(String strDate) {
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	ParsePosition pos = new ParsePosition(0);
    	Date strtodate = formatter.parse(strDate, pos);
    	return strtodate;
    }



    /**
     * 得到一个时间延后或前移几天的时间
     * @param nowdate：时间
     * @param delay：前移或后延的天数
     * @return
     */
    public static String getNextDay(String nowdate, String delay) {
    	try{
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    		String mdate = "";
    		Date d = strToDate(nowdate);
    		long myTime = (d.getTime() / 1000) + Integer.parseInt(delay) * 24 * 60 * 60;
    		d.setTime(myTime * 1000);
    		mdate = format.format(d);
    		return mdate;
    	}catch(Exception e){
    		return "";
    	}
    }


    /**
     * 判断是否闰年
     * @param ddate
     * @return
     */
    public static boolean isLeapYear(String ddate) {
    	/**
    	  * 详细设计： 1.被400整除是闰年，否则： 2.不能被4整除则不是闰年 3.能被4整除同时不能被100整除则是闰年
    	  * 3.能被4整除同时能被100整除则不是闰年
    	 */
    	Date d = strToDate(ddate);
    	GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
    	gc.setTime(d);
    	int year = gc.get(Calendar.YEAR);
    	if ((year % 400) == 0){
    		return true;
    	}else if ((year % 4) == 0){
    	   if ((year % 100) == 0){
    		   return false;
    	   }else{
    		   return true;
    	   }
    	}else{
    		return false;
    	}
    }


    /**
     * 返回美国时间格式
     * @param str
     * @return
     */
    public static String getEDate(String str) {
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	ParsePosition pos = new ParsePosition(0);
    	Date strtodate = formatter.parse(str, pos);
    	String j = strtodate.toString();
    	String[] k = j.split(" ");
    	return k[2] + k[1].toUpperCase() + k[5].substring(2, 4);
    }


    /**
     * 判断二个时间是否在同一个周
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameWeekDates(Date date1, Date date2) {
    	Calendar cal1 = Calendar.getInstance();
    	Calendar cal2 = Calendar.getInstance();
    	cal1.setTime(date1);
    	cal2.setTime(date2);
    	int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
    	if(0 == subYear) {
    		if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)){
    			return true;
    		}
    	}else if(1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
    		// 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
    		if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)){
    			return true;
    		}
    	}else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
    		if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)){
    			return true;
    		}
    	}
    	return false;
    }



    /**
     * 产生周序列,即得到当前时间所在的年度是第几周
     * @return
     */
    public static String getSeqWeek() {
    	Calendar c = Calendar.getInstance(Locale.CHINA);
    	String week = Integer.toString(c.get(Calendar.WEEK_OF_YEAR));
    	if (week.length() == 1)
    	week = "0" + week;
    	String year = Integer.toString(c.get(Calendar.YEAR));
    	return year +"年第"+ week+"周";
    }


    /**
     * 获得一个日期所在的周的星期几的日期，如要找出2002年2月3日所在周的星期一是几号
     * @param sdate：日期
     * @param num：星期几（星期天是一周的第一天）
     * @return
     */
    public static String getWeek(String sdate, String num) {
    	// 再转换为时间
    	Date dd = strToDate(sdate);
    	Calendar c = Calendar.getInstance();
    	c.setTime(dd);
    	if (num.equals("1")) // 返回星期一所在的日期
    	c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    	else if (num.equals("2")) // 返回星期二所在的日期
    	c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
    	else if (num.equals("3")) // 返回星期三所在的日期
    	c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
    	else if (num.equals("4")) // 返回星期四所在的日期
    	c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
    	else if (num.equals("5")) // 返回星期五所在的日期
    	c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
    	else if (num.equals("6")) // 返回星期六所在的日期
    	c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
    	else if (num.equals("0")) // 返回星期日所在的日期
    	c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
    	return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }


    /**
     * 根据一个日期，返回是星期几的字符串
     * @param sdate
     * @return
     */
    public static String getWeek(String sdate) {
    	// 再转换为时间
    	Date date = strToDate(sdate);
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	// int hour=c.get(Calendar.DAY_OF_WEEK);
    	// hour中存的就是星期几了，其范围 1~7
    	// 1=星期日 7=星期六，其他类推
    	return new SimpleDateFormat("EEEE").format(c.getTime());
    }


    /**
     * 根据一个日期，返回是星期几的字符串
     * @param sdate
     * @return
     */
    public static String getWeekStr(String sdate){
    	String str = "";
    	str = getWeek(sdate);
    	if("1".equals(str)){
    		str = "星期日";
    	}else if("2".equals(str)){
    		str = "星期一";
    	}else if("3".equals(str)){
    		str = "星期二";
    	}else if("4".equals(str)){
    		str = "星期三";
    	}else if("5".equals(str)){
    		str = "星期四";
    	}else if("6".equals(str)){
    		str = "星期五";
    	}else if("7".equals(str)){
    		str = "星期六";
    	}
    	return str;
    }


    /**
     * 两个时间之间的天数
     * @param date1
     * @param date2
     * @return
     */
    public static long getDays(String date1, String date2) {
    	if (date1 == null || date1.equals(""))
    		return 0;
    	if (date2 == null || date2.equals(""))
    		return 0;
    	// 转换为标准时间
    	SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
    	Date date = null;
    	Date mydate = null;
    	try {
    		date = myFormatter.parse(date1);
    		mydate = myFormatter.parse(date2);
    	} catch (Exception e) {
    	}
    	long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
    	return day;
    }


    /**
     * 形成如下的日历 ， 根据传入的一个时间返回一个结构 星期日 星期一 星期二 星期三 星期四 星期五 星期六 下面是当月的各个时间
     * 此函数返回该日历第一行星期日所在的日期
     * @param sdate
     * @return
     */
    public static String getNowMonth(String sdate) {
    	// 取该时间所在月的一号
    	sdate = sdate.substring(0, 8) + "01";

    	// 得到这个月的1号是星期几
    	Date date = strToDate(sdate);
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	int u = c.get(Calendar.DAY_OF_WEEK);
    	String newday = getNextDay(sdate, (1 - u) + "");
    	return newday;
    }


    /**
     * 根据用户传入的时间表示格式，返回当前时间的格式 如果是yyyyMMdd，注意字母y不能大写
     * @param sformat
     * @return
     */
    public static String getUserDate(String sformat) {
    	Date currentTime = new Date();
    	SimpleDateFormat formatter = new SimpleDateFormat(sformat);
    	String dateString = formatter.format(currentTime);
    	return dateString;
    }


    /**
     * 返回一个i位数的随机数
     * @param i
     * @return
     */
    public static String getRandom(int i) {
    	Random jjj = new Random();
    	// int suiJiShu = jjj.nextInt(9);
    	if (i == 0)
    		return "";
    	String jj = "";
    	for (int k = 0; k < i; k++) {
    		jj = jj + jjj.nextInt(9);
    	}
    	return jj;
    }


    /**
     * 取得数据库主键 生成格式为yyyymmddhhmmss+k位随机数
     * @param k：表示是取几位随机数，可以自己定
     * @return
     */
    public static String getNo(int k) {
    	return getUserDate("yyyyMMddhhmmss") + getRandom(k);
    }

    /**
     * 日期格式转换
     * yyyy-MM-dd HH:mm:ss 转换成 yyyyMMddHHmmss
     * @param date
     * @return
     */
    public static String get_format4(String str){
    	Date date;
		try {
			date = format5.parse(str);
			return format4.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
    }

    /**
     * date2比date1多的天数
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDays(Date date1,Date date2)
    {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
       int day1= cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if(year1 != year2)   //同一年
        {
            int timeDistance = 0 ;
            for(int i = year1 ; i < year2 ; i ++)
            {
                if(i%4==0 && i%100!=0 || i%400==0)    //闰年
                {
                    timeDistance += 366;
                }
                else    //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2-day1) ;
        }
        else    //不同年
        {
            System.out.println("判断day2 - day1 : " + (day2-day1));
            return day2-day1;
        }
    }

    /**
     * 计算周一与周日所在日期
     * @param date
     * @return
     */
    public static String getMondayAndSundayDate(String date){
		 try {
			   SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd"); //设置时间格式
			   Calendar cal = Calendar.getInstance();
			   Date time=sdf.parse(date);
			   cal.setTime(time);
			   //判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
			   int dayWeek = cal.get(Calendar.DAY_OF_WEEK);//获得当前日期是一个星期的第几天
			   if(1 == dayWeek) {
			      cal.add(Calendar.DAY_OF_MONTH, -1);
			   }
			  cal.setFirstDayOfWeek(Calendar.MONDAY);//设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
			  int day = cal.get(Calendar.DAY_OF_WEEK);//获得当前日期是一个星期的第几天
			  cal.add(Calendar.DATE, cal.getFirstDayOfWeek()-day);//根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
			  String monday = sdf.format(cal.getTime()).toString();
			  cal.add(Calendar.DATE, 6);
			  String sunday = sdf.format(cal.getTime()).toString();
			  return monday + "-" + sunday;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		 return null;
	}

    /**
     * 计算当前日期所属的年周数
     * @param date
     * @return
     */
    public static int getWeekOfYearOrder(String date){
    	Calendar calendar = null;
    	try {
    		calendar = Calendar.getInstance();
        	calendar.setFirstDayOfWeek(Calendar.MONDAY);
        	calendar.setTime(format1.parse(date));
    	} catch (ParseException e) {
    	    e.printStackTrace();
    	}
    	  return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
	 * 计算日期加减天数后的日期
	 * @return yyyyMMdd
	 */
	public static String getCountDay(String date, int num){
    	Calendar cal0 = Calendar.getInstance();
    	try {
			cal0.setTime(format1.parse(date));
			cal0.add(Calendar.DAY_OF_MONTH, num);
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return format1.format(cal0.getTime());
	}


	public static String format(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }

    /**
     * 计算距离现在多久，非精确
     *
     * @param date
     * @return
     */
    public static String getTimeBefore(Date date) {
        Date now = new Date();
        long l = now.getTime() - date.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        String r = "";
        if (day > 0) {
            r += day + "天";
        } else if (hour > 0) {
            r += hour + "小时";
        } else if (min > 0) {
            r += min + "分";
        } else if (s > 0) {
            r += s + "秒";
        }
        r += "前";
        return r;
    }

    /**
     * 计算距离现在多久，精确
     *
     * @param date
     * @return
     */
    public static String getTimeBeforeAccurate(Date date) {
        Date now = new Date();
        long l = now.getTime() - date.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        long hour = (l / (60 * 60 * 1000) - day * 24);
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        String r = "";
        if (day > 0) {
            r += day + "天";
        }
        if (hour > 0) {
            r += hour + "小时";
        }
        if (min > 0) {
            r += min + "分";
        }
        if (s > 0) {
            r += s + "秒";
        }
        r += "前";
        return r;
    }

    /**
     * 获取秒级的时间戳
     * @return
     */
    public static Long getNowTimestampSecond(){
    	String timestamp = String.valueOf(new Date().getTime());
    	int length = timestamp.length();
    	if (length > 3) {
    		return Long.valueOf(timestamp.substring(0,length-3));
    	} else {
    		return 0l;
    	}
    }

    /**
     * 对当前日期按日进行加减
     * @param num
     * @return yyyyMMdd
     */
    public static String getDateAddByDate(int num){
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(new Date());
    	calendar.add(calendar.DATE, num);
    	return format1.format(calendar.getTime());
    }

    /**
     * 获取直播过期时间
     * @param hour
     * @return
     */
    public static String getLiveExpirationTime(String hour) {
    	long now_time = new Date().getTime();
    	BigDecimal expirationTime = new BigDecimal(hour).multiply(new BigDecimal(3600000)).add(new BigDecimal(now_time)).setScale(0, BigDecimal.ROUND_DOWN);
    	return format5.format(new Date(Long.valueOf(expirationTime.toString())));
    }

    /**
     *
     * 描述:获取下一个月的第一天.
     *
     * @return
     */
    public static String getPerFirstDayOfMonth() {
        SimpleDateFormat dft = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return dft.format(calendar.getTime());
    }

    /**
     *
    * @Title: getAfterDay
    * @param:
    * @Description: 查询几天之后的时间
    * @return String
     */
    public static String getAfterDay(String day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, Integer.parseInt(day));
        return format5.format(calendar.getTime());
    }

    public static void main(String[] args) throws ParseException {
    	System.out.println(getTimeString(getPerFirstDayOfMonth()+"000000"));
	}
}
