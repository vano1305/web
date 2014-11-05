package com.dn.ivan.rates.logic.interceptors;

import javax.xml.bind.Marshaller;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class FormattedJAXBInterceptor extends AbstractPhaseInterceptor<Message> {
	
	  public FormattedJAXBInterceptor() {
		  super(Phase.PRE_STREAM);
	  }

	  @Override
	  public void handleMessage(Message message) {
		  message.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	  }

	  @Override
	  public void handleFault(Message message) {
		  message.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	  }
}