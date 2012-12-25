package com.example.eventcalendar;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


public class CalendarHttpClient {
	// HTTP処理が成功したかどうかを保持するメンバ変数
	private boolean mHttpSucceeded = false;
	
	/**
	 * Http Delete処理
	 * @param String URL
	 */
	public void httpDelete(String url){
		httpPostXmlWithMethod(url,"","DELETE");
	}

	/**
	 * Http Post処理
	 *
	 * @param String URL
	 * @param String XML
	 * @return InputStream 処理結果のXMLを返すInputStream
	 */
	public InputStream httpPost(String url,String xml){
		return httpPostXmlWithMethod(url,xml,null);
	}

	/**
	 * Http Put処理
	 *
	 * @param String URL
	 * @param String XML
	 * @return InputStream 処理結果のXMLを返すInputStream
	 */
	public InputStream httpPut(String url,String xml){
		return httpPostXmlWithMethod(url,xml,"PUT");
	}

	/**
	 * XMLファイルを適当なメソッドで送信する。
	 * 
	 * @param String URL
	 * @param String XML
	 * @param String メソッド(PUT,DELETE)
	 * @return InputStream 処理結果を返すInputStream
	 */
	public InputStream httpPostXmlWithMethod(String url,String xml,String method){
		mHttpSucceeded = false;
		try {
			while(url != null){
				URL u = new URL(url);
				// URLを指定してコネクションを開く
				HttpURLConnection httpConnection = (HttpURLConnection)u.openConnection();
				// メソッドはPOSTを使用する。
				httpConnection.setRequestMethod("POST");
				// GData-Versionは２を指定する
				httpConnection.setRequestProperty("GData-Version","2");
				if(method != null){
					// POST以外のメソッドを指定された時は、ヘッダにIf-Match:*とX-HTTP-Method-Overrideを指定
					httpConnection.setRequestProperty("If-Match","*");
					httpConnection.setRequestProperty("X-HTTP-Method-Override",method);
				}
				// コンテンツを出力する設定
				httpConnection.setDoOutput(true);
				// Content-Typeは　XMLファイル
				httpConnection.setRequestProperty("Content-Type", "application/atom+xml");
				// OutputStreamWriterに引数のXMLを設定して出力
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpConnection.getOutputStream(),"UTF-8");
				outputStreamWriter.write(xml);
				outputStreamWriter.close();
				// HTTPレスポンスコードを取得
				int responseCode = httpConnection.getResponseCode();
				url = null;
				if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED){
					// レスポンスコードがOKまたはCREATEDの場合は処理が完了
					mHttpSucceeded = true;
					// 入力のInputStreamを返して終了
					return httpConnection.getInputStream();
				}else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
					// レスポンスコードがMOVED_TEMPだった場合、リダイレクトなので、
					// ヘッダからLocationヘッダを取り出して、URLに指定して再実行(先頭のwhileに戻る）
					Map<String,List<String>> responseHeaders = httpConnection.getHeaderFields();
					if(responseHeaders.containsKey("Location")){
						url = responseHeaders.get("Location").get(0);
					}else if(responseHeaders.containsKey("location")){
						url = responseHeaders.get("location").get(0);
					}
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * URLを渡してデータをInputStreamで返す
	 * @param String アクセスするURL
	 * *return InputStream サーバから受信したデータにアクセスするためのInputStream
	 */
	public InputStream httpGet(String url){
		HttpGet httpGet = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();
		// GData Version 2用のHeaderをセット
		httpGet.setHeader("GData-Version","2");
		HttpResponse response;
		try {
			// HTTPアクセスを実行する
			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_OK){
				// アクセスに成功したらresponseからInputStreamを取得する
				return response.getEntity().getContent();
			}else if(statusCode == HttpStatus.SC_UNAUTHORIZED){
				return null;
			}
		} catch (Exception e) {
		}
		return null;
	}
}
