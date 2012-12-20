package com.example.eventcalendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateStrCls {
	//�@���t�݂̂̃t�H�[�}�b�g
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	// �����݂̂̃t�H�[�}�b�g
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	// RFC822�ɏ]�����~���b�P�ʂ̎����t�H�[�}�b�g
	public static SimpleDateFormat RFC822MilliDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	// UTC�����i�^�C���]�[�������j�̃t�H�[�}�b�g
	public static SimpleDateFormat UTCDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	// �ꎞ�Ԃ̕���
	public final static int HOUR_BY_MINUTES = 60;
	// �ꕪ�̕b��
	public final static int MINUTE_BY_SECONDS = 60;
	//�@1�b�̃~���b��
	public final static int SECOND_BY_MILLI = 1000;
	// 1���̃~���b��
	public final static int MINUTE_BY_MILLI = MINUTE_BY_SECONDS*SECOND_BY_MILLI;
	
	
	/**
	 * ���t�A�����̕����񂩂�DB �ɕۑ����邽�߂̎���������ɕϊ�����
	 * @param date�@�ϊ����̓��t
	 * @param time�@�ϊ����Ƃ̎���
	 * @return�@RFC 3339�`���̓���������
	 */
	public static String toDBDateString(String date,String time){
		// �ǉ��\�ȕ�����N���XStringBuilder���쐬
		StringBuilder sb = new StringBuilder();
		sb.append(date);
		sb.append("T");
		sb.append(time);
		sb.append(":00.000");
		// TimeZone��������쐬���ǉ�
		sb.append(timeZoneToString(TimeZone.getDefault()));
		return sb.toString();
	}

	/**
	 * �^�C���]�[���̕�����𐶐�����
	 *  RFC 3339�Ŏg�p���邽�ߎ��A���̋�؂�ɁF������
	 * @param tz �^�C���]�[��
	 * @return �^�C���]�[��������
	 *         ��F�@+9���Ԃ̏ꍇ   +09:00
	 *             -3���Ԃ̏ꍇ  -03:00
	 *             0�̏ꍇ�@�@�@�@�@Z
	 */
	public static String timeZoneToString(TimeZone tz){
		// �J�����_�[�N���X�̃C���X�^���X���쐬
		Calendar cal = Calendar.getInstance();
		String dir=null;
		// TimeZone����~���b�P�ʂ�UTC����̂�����擾
		int offset = tz.getRawOffset();
		// �����ƒl�̕���
		if(offset<0){
			// offset���}�C�i�X�Ȃ畄����-
			// ����͐��ɂ��Ă���
			offset = -offset;
			dir = "-";
		}else if(offset>0){
			// �I�t�Z�b�g���v���X�Ȃ畄���́{
			dir = "+";
		}else if(offset == 0){
			// UTC�Ɉ�v����ꍇ��Z��Ԃ�
			return "Z";
		}
		// ���A�����v�Z��Calendar�ɃZ�b�g
		int offsetMin = offset/MINUTE_BY_MILLI;
		int offsetHour = offsetMin/HOUR_BY_MINUTES;
		offsetMin=offsetMin%60;
		cal.set(Calendar.HOUR_OF_DAY, offsetHour);
		cal.set(Calendar.MINUTE, offsetMin);
		//�@�����̕�����ǉ������������Ԃ�
		return dir+timeFormat.format(cal.getTime());
	}

	/**
	 * Calendar����DB�Ɋi�[���邽�߂̕�������쐬����
	 * @param cal �ϊ����Ƃ̒l
	 * @return ����������
	 */
	public static String toDBDateString(Calendar cal){
		// RFC 822�`���ŕ�����𐶐�
		String dateStr = RFC822MilliDateFormat.format(cal.getTime());
		// �^�C���]�[������������
		if(dateStr.matches(".+[+-][0-9]{4}$")){
			dateStr = dateStr.replaceAll("([+-][0-9]{2})([0-9]{2})","$1:$2");
		}
		return dateStr;
	}
	/**
	 * ���������񂩂�J�����_�[�ւ̕ϊ�
	 * @param startTime �ϊ����Ƃ̓���������
	 * @return Calendar 
	 */
	public static GregorianCalendar toCalendar(String startTime) {
		GregorianCalendar calendar = new GregorianCalendar();
		if(startTime == null){
			return calendar;
		}
		// ������𐔒l�ȊO�̕����ŕ������Đ؂蕪����
		String[] strs = startTime.split("[^0-9]");
		TimeZone timeZone = TimeZone.getDefault();
		if(startTime.matches("^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$")){
			// ���t�݂̂̕�����@�������O�O�F�O�O�ɐݒ�
			calendar.set(Calendar.YEAR, Integer.valueOf(strs[0]));
			calendar.set(Calendar.MONTH,Integer.valueOf(strs[1])-1);
			calendar.set(Calendar.DAY_OF_MONTH,Integer.valueOf(strs[2]));
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			calendar.set(Calendar.MILLISECOND,0);
			calendar.setTimeZone(timeZone);
		}else{
			// ����������@���l������𐔒l�ɕϊ����Đݒ�
			calendar.set(Calendar.YEAR, Integer.valueOf(strs[0]));
			calendar.set(Calendar.MONTH,Integer.valueOf(strs[1])-1);
			calendar.set(Calendar.DAY_OF_MONTH,Integer.valueOf(strs[2]));
			calendar.set(Calendar.HOUR_OF_DAY,Integer.valueOf(strs[3]));
			calendar.set(Calendar.MINUTE,Integer.valueOf(strs[4]));
			calendar.set(Calendar.SECOND,Integer.valueOf(strs[5]));
			calendar.set(Calendar.MILLISECOND,Integer.valueOf(strs[6]));
			// TimeZone�̃p�^�[���ɂ�鏈��
			if(startTime.matches(".+Z$")){
				// UTC
				timeZone.setRawOffset(0);
			}else if(startTime.matches(".+\\+[0-9][0-9]:[0-9][0-9]$")){
				// �I�t�Z�b�g���}�C�i�X
				timeZone.setRawOffset((Integer.valueOf(strs[7])*HOUR_BY_MINUTES+Integer.valueOf(strs[8]))*MINUTE_BY_MILLI);
			}else if(startTime.matches(".+-[0-9][0-9]:[0-9][0-9]$")){
				// �I�t�Z�b�g���v���X
				timeZone.setRawOffset(-(Integer.valueOf(strs[7])*HOUR_BY_MINUTES+Integer.valueOf(strs[8]))*MINUTE_BY_MILLI);
			}
			// TimeZone��ݒ�
			calendar.setTimeZone(timeZone);
		}
		return calendar;
	}

	/**
	 * �J�����_�[�����f�[�^��UTC�i���萢�E���j�ŕ\����������ɕϊ����܂��B
	 * ���{���ԂƂ�9���Ԃ̂��ꂪ����܂��B
	 * @param cal
	 * @return
	 */
	public static String toUTCString(Calendar cal){
		UTCDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return UTCDateFormat.format(cal.getTime())+"Z";
	}
}
