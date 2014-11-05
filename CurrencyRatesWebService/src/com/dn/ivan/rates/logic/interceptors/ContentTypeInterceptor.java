package com.dn.ivan.rates.logic.interceptors;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class ContentTypeInterceptor extends AbstractPhaseInterceptor<Message> {
	
	private static final String UTF_8 = "; charset=UTF-8";
	
	public ContentTypeInterceptor() {
		super(Phase.RECEIVE);
	}

	@Override
	public void handleMessage(Message message) {
		
		@SuppressWarnings("unchecked")
		Map<String, List<Object>> headers = (Map<String, List<Object>>) message.get(Message.PROTOCOL_HEADERS);

		if (headers == null) {
			headers = new TreeMap<String, List<Object>>();
			message.put(Message.PROTOCOL_HEADERS, headers);
		}

		boolean isValidContentTypes = isValidContentType(headers.get(Message.CONTENT_TYPE));
		boolean isValidAcceptContentTypes = isValidContentType(headers.get(Message.ACCEPT_CONTENT_TYPE));

		if (!isValidContentTypes && !isValidAcceptContentTypes) {
			
			headers.put(Message.CONTENT_TYPE, Collections.singletonList((Object) (MediaType.APPLICATION_XML + UTF_8)));
			headers.put(Message.ACCEPT_CONTENT_TYPE, Collections.singletonList((Object) (MediaType.APPLICATION_XML + UTF_8)));
		}
		else if (isValidContentTypes && !isValidAcceptContentTypes) {
			
			changeCharset(headers, Message.CONTENT_TYPE, Message.CONTENT_TYPE);
			changeCharset(headers, Message.CONTENT_TYPE, Message.ACCEPT_CONTENT_TYPE);
		}
		else if (isValidAcceptContentTypes && !isValidContentTypes) {
			
			changeCharset(headers, Message.ACCEPT_CONTENT_TYPE, Message.ACCEPT_CONTENT_TYPE);
			changeCharset(headers, Message.ACCEPT_CONTENT_TYPE, Message.CONTENT_TYPE);
		}
		else if (isValidAcceptContentTypes && isValidContentTypes) {
			
			changeCharset(headers, Message.CONTENT_TYPE, Message.CONTENT_TYPE);
			changeCharset(headers, Message.ACCEPT_CONTENT_TYPE, Message.ACCEPT_CONTENT_TYPE);
		}
	}

	private boolean isValidContentType(List<Object> contentTypes) {
		return contentTypes != null
				&& contentTypes.size() == 1
				&& contentTypes.get(0) instanceof String
				&& (((String) contentTypes.get(0)).toUpperCase().indexOf(MediaType.APPLICATION_XML.toUpperCase()) != -1 || ((String) contentTypes.get(0)).toUpperCase().indexOf(MediaType.TEXT_XML.toUpperCase()) != -1 || ((String) contentTypes.get(0)).toUpperCase().indexOf(MediaType.APPLICATION_JSON.toUpperCase()) != -1);
	}
	
	private void changeCharset(Map<String, List<Object>> headers, String from, String to) {
		
		String content = String.valueOf(headers.get(from).get(0));
		
		if (content.toUpperCase().indexOf(MediaType.APPLICATION_XML.toUpperCase()) != -1) {
			headers.put(to, Collections.singletonList((Object)(MediaType.APPLICATION_XML + UTF_8)));
		}
		else if (content.toUpperCase().indexOf(MediaType.TEXT_XML.toUpperCase()) != -1) {
			headers.put(to, Collections.singletonList((Object)(MediaType.TEXT_XML + UTF_8)));
		}
		else if (content.toUpperCase().indexOf(MediaType.APPLICATION_JSON.toUpperCase()) != -1) {
			headers.put(to, Collections.singletonList((Object)(MediaType.APPLICATION_JSON + UTF_8)));
		}
	}
}