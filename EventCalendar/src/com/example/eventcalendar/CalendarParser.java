package com.example.eventcalendar;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.util.Xml;

public class CalendarParser {
	// NameSpaceの定義
	public static final String NS_GD = "gd";
	public static final String NS_APP = "app";
	public static final String NS_XMLNS = "xmlns";
	public static final String NS_GCAL = "gCal";
	public static final String NS_OPEN_SEARCH = "openSearch";

	//feedで使用されるTAGの定義
	public static final String TAG_FEED = "feed";
	public static final String TAG_AUTHOR = "author";
	public static final String TAG_NAME = "name";
	public static final String TAG_GENERATOR = "generator";
	public static final String TAG_TOTAL_RESULT = "totalResult";
	public static final String TAG_START_INDEX = "startIndex";
	public static final String TAG_ITEM_PER_PAGE = "itemPerPage";
	public static final String TAG_TIMEZONE = "timezone";
	public static final String TAG_TIME_CLEANED = "timeCleaned";

	//entryで使用されるTAGの定義
	public static final String TAG_ENTRY = "entry";
	public static final String TAG_TEXT = "#text";
	public static final String TAG_ID = "id";
	public static final String TAG_EDITED = "edited";
	public static final String TAG_PUBLISHED = "published";
	public static final String TAG_UPDATED = "updated";
	public static final String TAG_CATEGORY = "category";
	public static final String TAG_TITLE = "title";
	public static final String TAG_CONTENT = "content";
	public static final String TAG_LINK = "link";
	public static final String TAG_WHERE = "where";
	public static final String TAG_WHO = "who";
	public static final String TAG_WHEN = "when";
	public static final String TAG_COMMENTS = "comments";
	public static final String TAG_EVENT_STATUS = "eventStatus";
	public static final String TAG_REMINDER = "reminder";
	public static final String TAG_TRANSPARENCY = "transparency";
	public static final String TAG_VISIBILITY = "visibility";
	public static final String TAG_ANYONE_CAN_ADD_SELF = "anyoneCanAddSelf";
	public static final String TAG_GUESTS_CAN_INVITE_OTHERS = "guestsCanInviteOthers";
	public static final String TAG_GUESTS_CAN_MODIFY = "guestsCanModify";
	public static final String TAG_GUESTS_CAN_SEE_GUESTS = "guestsCanSeeGuests";
	public static final String TAG_SEQUENCE = "sequence";
	public static final String TAG_UID = "uid";
	public static final String TAG_RECURRENCE = "recurrence";

	//NameSpace付きTAGの定義
	public static final String TAG_GD_WHERE = NS_GD+":"+TAG_WHERE;
	public static final String TAG_GD_WHO = NS_GD+":"+TAG_WHO;
	public static final String TAG_GD_WHEN = NS_GD+":"+TAG_WHEN;
	public static final String TAG_GD_REMINDER = NS_GD+":"+TAG_REMINDER;
	public static final String TAG_GD_COMMENTS = NS_GD+":"+TAG_COMMENTS;
	public static final String TAG_GD_EVENT_STATUS = NS_GD+":"+TAG_EVENT_STATUS;
	public static final String TAG_GD_TRANSPARENCY = NS_GD+":"+TAG_TRANSPARENCY;
	public static final String TAG_GD_VISIBILITY = NS_GD+":"+TAG_VISIBILITY;
	public static final String TAG_APP_EDITED = NS_APP+":"+TAG_EDITED;
	public static final String TAG_OPENSEARCH_TOTAL_RESULT = NS_OPEN_SEARCH+":"+TAG_TOTAL_RESULT;
	public static final String TAG_OPENSEARCH_START_INDEX = NS_OPEN_SEARCH+":"+TAG_START_INDEX;
	public static final String TAG_OPENSEARCH_ITEM_PER_PAGE = NS_OPEN_SEARCH+":"+TAG_ITEM_PER_PAGE;
	public static final String TAG_GCAL_TIMEZONE = NS_GCAL+":"+TAG_TIMEZONE;
	public static final String TAG_GCAL_TIME_CLEANED = NS_GCAL+":"+TAG_TIME_CLEANED;
	public static final String TAG_GCAL_ANYONE_CAN_ADD_SELF = NS_GCAL+":"+TAG_ANYONE_CAN_ADD_SELF;
	public static final String TAG_GCAL_GUESTS_CAN_INVITE_OTHERS = NS_GCAL+":"+TAG_GUESTS_CAN_INVITE_OTHERS;
	public static final String TAG_GCAL_GUESTS_CAN_MODIFY = NS_GCAL+":"+TAG_GUESTS_CAN_MODIFY;
	public static final String TAG_GCAL_GUESTS_CAN_SEE_GUESTS = NS_GCAL+":"+TAG_GUESTS_CAN_SEE_GUESTS;
	public static final String TAG_GCAL_SEQUENCE = NS_GCAL+":"+TAG_SEQUENCE;
	public static final String TAG_GCAL_UID = NS_GCAL+":"+TAG_UID;
	public static final String TAG_GD_RECURRENCE = NS_GD+":"+TAG_RECURRENCE;

	// アトリビュート名の定義    
	public static final String ATT_XMLNS = "xmlns";
	public static final String ATT_KIND = "kind";
	public static final String ATT_TERM = "term";
	public static final String ATT_SCHEME = "scheme";
	public static final String ATT_REL = "rel";
	public static final String ATT_HREF = "href";
	public static final String ATT_VALUE = "value";
	public static final String ATT_VALUE_STRING = "valueString";
	public static final String ATT_EMAIL = "email";
	public static final String ATT_ENDTIME = "endTime";
	public static final String ATT_STARTTIME = "startTime";
	public static final String ATT_ETAG = "etag";
	public static final String ATT_FEED_LINK = "feedLink";
	public static final String ATT_METHOD = "method";
	public static final String ATT_MINUTES = "minutes";
	public static final String ATT_TYPE = "type";

	//NameSpace付きアトリビュート名の定義
	public static final String ATT_GD_ETAG = NS_GD+":"+ATT_ETAG;
	public static final String ATT_GD_KIND = NS_GD+":"+ATT_KIND;
	public static final String ATT_GD_FEED_LINK = NS_GD+":"+ATT_FEED_LINK;

	//Value名の定義
	public static final String VAL_NEXT = "next";
	public static final String VAL_EDIT = "edit";
	public static final String VAL_SELF = "self";
	public static final String VAL_TEXT = "text";
	public static final String VAL_ALTERNATE = "alternate";
	public static final String VAL_ALERT = "alert";
	private ContentResolver mContentResolver = null;

	/**
	 * コンストラクタ
	 */
	public CalendarParser(ContentResolver resolver){
		mContentResolver = resolver;
	}

	/**
	 * パーサ本体
	 *
	 * @param InputStream XMLファイルを持っているInputStream
	 * @return feedタグに含まれるNextURL 最後のfeedの場合はnull
	 */
	public String parse(InputStream is){
		if(is==null){
			return null;
		}
		String nextUrl = null;
		String tagName = null;
		String parent = null;
		EventInfo eventInfo = null;

		// TAGの階層を保持しておくためのStackの定義
		Stack<String> tagStack = new Stack<String>();
		try {
			// 新しいPullParserのインスタンスを作成し処理の開始の準備
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(is,null);
			// 現在のEventTypeの取得
			int eventType = parser.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT){
				switch(eventType){
				// START_DOCUMENTの処理
				case XmlPullParser.START_DOCUMENT:
					break;
					// START_TAGの処理
				case XmlPullParser.START_TAG:
					// 現在のTAGを取得
					tagName = parser.getName();
					if(tagStack.empty() == false){
						// TAGスタックから一つ上位のTAGを取得
						parent = tagStack.peek();
					}
					// 現在のTAGをStackにPush
					tagStack.push(tagName);
					if(tagName.equalsIgnoreCase(TAG_ENTRY)){
						// entryタグの処理
						// 新しいEventInfoを作成
						eventInfo = new EventInfo(mContentResolver);
						// entry tagのアトリビュートの処理
						for(int i=0;i< parser.getAttributeCount();i++){
							if(parser.getAttributeName(i).equalsIgnoreCase(ATT_GD_ETAG)){
								eventInfo.setEtag(parser.getAttributeValue(i));
							}
						}
					}else if(tagName.equalsIgnoreCase(TAG_LINK)){
						// linkタグの処理
						if(parent.equalsIgnoreCase(TAG_ENTRY)){
							if(parser.getAttributeCount()>0){
								if(parser.getAttributeName(0).equalsIgnoreCase(ATT_REL) 
										&& parser.getAttributeValue(0).equalsIgnoreCase(VAL_EDIT)
										&& parser.getAttributeName(2).equalsIgnoreCase(ATT_HREF)){
									// 最初のアトリビュート名がrelで値がedit　かつ　3番目のat理ビューと名がhrefである場合
									// editURLなのでEventInfoに記録する
									eventInfo.setEditUrl(parser.getAttributeValue(2));
								}
							}
						}else if(parent.equalsIgnoreCase(TAG_FEED)){
							// feedタグの処理
							if(parser.getAttributeCount()>0){
								if(parser.getAttributeName(0).equalsIgnoreCase(ATT_REL) 
										&& parser.getAttributeValue(0).equalsIgnoreCase(VAL_NEXT)){
									// 最初のアトリビュート名がrelで値がnextならば、続きのfeedを取得するためのURLなので
									// nextUrlとして保存
									nextUrl = parser.getAttributeValue(2);
								}
							}
						}
					}else if(tagName.equalsIgnoreCase(TAG_CATEGORY)){
						// categoryタグの処理
						if(parent.equalsIgnoreCase(TAG_ENTRY)){
							for(int i=0;i<parser.getAttributeCount();i++){
								if(parser.getAttributeName(i).equalsIgnoreCase(ATT_TERM)){
									eventInfo.setCategory(parser.getAttributeValue(i));
								}
							}
						}
					}else if(tagName.equalsIgnoreCase(TAG_GD_WHERE)){
						// gd_whereタグの処理
						if(parser.getAttributeCount()>0){
							eventInfo.setWhere(parser.getAttributeValue(0));
						}
					}else if(tagName.equalsIgnoreCase(TAG_GD_WHEN)){
						// gd_whenタグの処理
						for(int i=0;i<parser.getAttributeCount();i++){
							if(parser.getAttributeName(i).equalsIgnoreCase(ATT_ENDTIME)){
								// アトリビュート名がgd_when_endtimeの場合、イベントの終了時刻
								eventInfo.setEnd(parser.getAttributeValue(i));
							}else if(parser.getAttributeName(i).equalsIgnoreCase(ATT_STARTTIME)){
								// アトリビュート名がgd_when_starttimeの場合、イベントの開始時刻
								eventInfo.setStart(parser.getAttributeValue(i));
							}
						}
					}else if(tagName.equalsIgnoreCase(TAG_GD_REMINDER)){
						if(parser.getAttributeCount() == 2){
							eventInfo.addToAlarmMap(parser.getAttributeValue(0),parser.getAttributeValue(1));
						}
					}else if(tagName.equalsIgnoreCase(TAG_GD_EVENT_STATUS)){
						// event_statusタグの処理
						if(parser.getAttributeCount()>0){
							eventInfo.setEventStatus(parser.getAttributeValue(0));
						}
					}
					break;
				case XmlPullParser.TEXT:
					//TAGのCONTET部分の処理
					if(eventInfo != null){
						if(tagName.equalsIgnoreCase(TAG_PUBLISHED)){
							// publishedタグの処理
							eventInfo.setPublished(parser.getText());
						}else if(tagName.equalsIgnoreCase(TAG_UPDATED)){
							// updatedタグの処理
							eventInfo.setUpdated(parser.getText());
						}else if(tagName.equalsIgnoreCase(TAG_TITLE)){
							// titleタグの処理
							eventInfo.setTitle(parser.getText());
						}else if(tagName.equalsIgnoreCase(TAG_CONTENT)){
							// contentタグの処理
							eventInfo.setContent(parser.getText());
						}else if(tagName.equalsIgnoreCase(TAG_ID)){
							// idタグの処理
							eventInfo.setEventId(parser.getText());
						}else if(tagName.equalsIgnoreCase(TAG_GD_RECURRENCE)){
							// recurrenceタグの処理
							eventInfo.setRecurrence(parser.getText());
						}
					}
					break;
				case XmlPullParser.END_TAG:
					// タグクローズの処理
					tagName = parser.getName();
					// TAG Stack からPOP
					tagStack.pop();
					if(tagName.equalsIgnoreCase(TAG_ENTRY)){
						// entryタグの終了の場合は、これまでに記録したEventInfoをデータベースに保存
						eventInfo.updateDB();
						eventInfo = null;
					}
					break;
				}
				// 次のeventに移動
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
		return nextUrl;
	}
	/**
	 * EventInfoの情報を元にinsert用のXMLを作成する。
	 *
	 * @param EventInfo
	 * @return String XML
	 */
	public String insertSerializer(EventInfo e){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			// 出力を設定
			serializer.setOutput(writer);
			// XML Documentを開始
			serializer.startDocument("UTF-8",false);
			serializer.startTag("", TAG_ENTRY);
			serializer.attribute("", NS_XMLNS, "http://www.w3.org/2005/Atom");
			serializer.attribute("", NS_XMLNS+":"+NS_GD, "http://schemas.google.com/g/2005");
			serializer.startTag("", TAG_CATEGORY);
			serializer.attribute("", ATT_SCHEME, "http://schemas.google.com/g/2005#kind");
			serializer.attribute("", ATT_TERM, "http://schemas.google.com/g/2005#event");
			serializer.endTag("", TAG_CATEGORY);
			serializer.startTag("",TAG_TITLE);
			serializer.attribute("", ATT_TYPE, VAL_TEXT);
			serializer.text(e.getTitle());
			serializer.endTag("", TAG_TITLE);
			serializer.startTag("", TAG_CONTENT);
			serializer.attribute("", ATT_TYPE, VAL_TEXT);
			serializer.text(e.getContent());
			serializer.endTag("", TAG_CONTENT);
			serializer.startTag("", TAG_GD_TRANSPARENCY);
			serializer.attribute("", ATT_VALUE, "http://schemas.google.com/g/2005#event.opaque");
			serializer.endTag("", TAG_GD_TRANSPARENCY);
			serializer.startTag("", TAG_GD_EVENT_STATUS);
			serializer.attribute("", ATT_VALUE, "http://schemas.google.com/g/2005#event.confirmed");
			serializer.endTag("", TAG_GD_EVENT_STATUS);
			serializer.startTag("", TAG_GD_WHERE);
			serializer.attribute("", ATT_VALUE_STRING, e.getWhere());
			serializer.endTag("", TAG_GD_WHERE);
			serializer.startTag("", TAG_GD_WHEN);
			serializer.attribute("", ATT_ENDTIME, e.getEndString());
			serializer.attribute("", ATT_STARTTIME, e.getStartString());
			serializer.endTag("", TAG_GD_WHEN);
			serializer.endTag("", TAG_ENTRY);
			// Documentを終了
			serializer.endDocument();
			// 結果のXMLをStringで返す
			return writer.toString();
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * 更新用のXMLを生成する
	 *
	 * @param InputStream 更新元のXMLを読み出すInputStream
	 * @param EventInfo 更新すべき内容を持ったEventInfo
	 * @return String 内容を書き換えたXML
	 */
	public String updateSerializer(InputStream is,EventInfo eventInfo){
		// XML作成用のSerializer
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		// 更新元を解釈するパーサー
		XmlPullParser parser;
		try {
			// 初期化
			parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(is,null);
			serializer.setOutput(writer);
			String tagName = null;
			Stack<String> tagStack = new Stack<String>();

			int eventType = parser.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT){
				switch(eventType){
				case XmlPullParser.START_DOCUMENT:
					// Document開始
					serializer.startDocument("UTF-8",false);
					break;
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					// TAG開始
					serializer.startTag("", tagName);
					tagStack.push(tagName);
					if(tagName.equalsIgnoreCase(TAG_ENTRY)){
						for(int i=0;i<parser.getAttributeCount();i++){
							if(parser.getAttributeName(i).equalsIgnoreCase(ATT_GD_ETAG)){
								serializer.attribute("",parser.getAttributeName(i), parser.getAttributeValue(i));
							}else{
								serializer.attribute("",parser.getAttributeName(i), parser.getAttributeValue(i));
							}
						}
					}else if(tagName.equalsIgnoreCase(TAG_GD_WHERE)){
						if(eventInfo.getWhere()!= null){
							serializer.attribute("", ATT_VALUE_STRING, eventInfo.getWhere());
						}
					}else if(tagName.equalsIgnoreCase(TAG_GD_WHEN)){
						for(int i=0;i<parser.getAttributeCount();i++){
							if(parser.getAttributeName(i).equalsIgnoreCase(ATT_ENDTIME)){
								serializer.attribute("", ATT_ENDTIME, eventInfo.getEndString());
							}else if(parser.getAttributeName(i).equalsIgnoreCase(ATT_STARTTIME)){
								serializer.attribute("", ATT_STARTTIME, eventInfo.getStartString());
							}
						}
					}else{
						for(int i=0;i<parser.getAttributeCount();i++){
							serializer.attribute("",parser.getAttributeName(i), parser.getAttributeValue(i));
						}
					}
					break;
				case XmlPullParser.TEXT:
					// TEXTでは、EventInfoに値を持っているものについては処理しない
					// END_TAGの所でTEXTの出力処理を行う。
					if(tagName.equalsIgnoreCase(TAG_ID)){
					}else if(tagName.equalsIgnoreCase(TAG_PUBLISHED)){
					}else if(tagName.equalsIgnoreCase(TAG_UPDATED)){
					}else if(tagName.equalsIgnoreCase(TAG_TITLE)){
					}else if(tagName.equalsIgnoreCase(TAG_CONTENT)){
					}else{
						serializer.text(parser.getText());
					}
					break;
				case XmlPullParser.END_TAG:
					// END_TAG処理
					tagName = parser.getName();
					// EventInfoで値を持っている場合はここでtext出力を行う
					if(tagName.equalsIgnoreCase(TAG_ID)){
						serializer.text(eventInfo.getEventId());
					}else if(tagName.equalsIgnoreCase(TAG_PUBLISHED)){
						serializer.text(eventInfo.getPublishedString());
					}else if(tagName.equalsIgnoreCase(TAG_UPDATED)){
						serializer.text(eventInfo.getUpdatedString());
					}else if(tagName.equalsIgnoreCase(TAG_TITLE)){
						serializer.text(eventInfo.getTitle());
					}else if(tagName.equalsIgnoreCase(TAG_CONTENT)){
						serializer.text(eventInfo.getContent());
					}
					tagStack.pop();
					serializer.endTag("", tagName);
					break;
				}
				eventType = parser.next();
			}
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
		}
		return null;
	}
}
