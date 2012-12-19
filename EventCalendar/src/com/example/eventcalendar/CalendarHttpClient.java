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
	// HTTP�����������������ǂ�����ێ����郁���o�ϐ�
	public boolean mHttpSucceeded = false;
	
	/**
	 * Http Delete����
	 * @param String URL
	 */
	public void httpDelete(String url){
		httpPostXmlWithMethod(url,"","DELETE");
	}

	/**
	 * Http Post����
	 *
	 * @param String URL
	 * @param String XML
	 * @return InputStream �������ʂ�XML��Ԃ�InputStream
	 */
	public InputStream httpPost(String url,String xml){
		return httpPostXmlWithMethod(url,xml,null);
	}

	/**
	 * Http Put����
	 *
	 * @param String URL
	 * @param String XML
	 * @return InputStream �������ʂ�XML��Ԃ�InputStream
	 */
	public InputStream httpPut(String url,String xml){
		return httpPostXmlWithMethod(url,xml,"PUT");
	}

	/**
	 * XML�t�@�C����K���ȃ��\�b�h�ő��M����B
	 * 
	 * @param String URL
	 * @param String XML
	 * @param String ���\�b�h(PUT,DELETE)
	 * @return InputStream �������ʂ�Ԃ�InputStream
	 */
	public InputStream httpPostXmlWithMethod(String url,String xml,String method){
		mHttpSucceeded = false;
		try {
			while(url != null){
				URL u = new URL(url);
				// URL���w�肵�ăR�l�N�V�������J��
				HttpURLConnection httpConnection = (HttpURLConnection)u.openConnection();
				// ���\�b�h��POST���g�p����B
				httpConnection.setRequestMethod("POST");
				// GData-Version�͂Q���w�肷��
				httpConnection.setRequestProperty("GData-Version","2");
				if(method != null){
					// POST�ȊO�̃��\�b�h���w�肳�ꂽ���́A�w�b�_��If-Match:*��X-HTTP-Method-Override���w��
					httpConnection.setRequestProperty("If-Match","*");
					httpConnection.setRequestProperty("X-HTTP-Method-Override",method);
				}
				// �R���e���c���o�͂���ݒ�
				httpConnection.setDoOutput(true);
				// Content-Type�́@XML�t�@�C��
				httpConnection.setRequestProperty("Content-Type", "application/atom+xml");
				// OutputStreamWriter�Ɉ�����XML��ݒ肵�ďo��
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpConnection.getOutputStream(),"UTF-8");
				outputStreamWriter.write(xml);
				outputStreamWriter.close();
				// HTTP���X�|���X�R�[�h���擾
				int responseCode = httpConnection.getResponseCode();
				url = null;
				if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED){
					// ���X�|���X�R�[�h��OK�܂���CREATED�̏ꍇ�͏���������
					mHttpSucceeded = true;
					// ���͂�InputStream��Ԃ��ďI��
					return httpConnection.getInputStream();
				}else if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
					// ���X�|���X�R�[�h��MOVED_TEMP�������ꍇ�A���_�C���N�g�Ȃ̂ŁA
					// �w�b�_����Location�w�b�_�����o���āAURL�Ɏw�肵�čĎ��s(�擪��while�ɖ߂�j
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
	 * URL��n���ăf�[�^��InputStream�ŕԂ�
	 * @param String �A�N�Z�X����URL
	 * *return InputStream �T�[�o�����M�����f�[�^�ɃA�N�Z�X���邽�߂�InputStream
	 */
	public InputStream httpGet(String url){
		HttpGet httpGet = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();
		// GData Version 2�p��Header���Z�b�g
		httpGet.setHeader("GData-Version","2");
		HttpResponse response;
		try {
			// HTTP�A�N�Z�X�����s����
			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == HttpStatus.SC_OK){
				// �A�N�Z�X�ɐ���������response����InputStream���擾����
				return response.getEntity().getContent();
			}else if(statusCode == HttpStatus.SC_UNAUTHORIZED){
				return null;
			}
		} catch (Exception e) {
		}
		return null;
	}
}
