package net.atos.ojas.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class DummyProcessor implements Processor {
  private final static String NO_VALUE = "hasNoValue";

  public final static String HEADER_DUMMY = "My-Dummy-Property";

  @Override
  public void process(Exchange exchange) throws Exception {
    String myStringProperty = exchange.getIn().getHeader(HEADER_DUMMY, NO_VALUE, String.class);
    if (myStringProperty.equals(NO_VALUE)) {
      // do stuff
      myStringProperty = "hasSomeValue";
    } else {
      // do other stuff
      myStringProperty = "hasOtherValue";
    }
    exchange.getIn().setHeader(HEADER_DUMMY, myStringProperty);
  }
}
