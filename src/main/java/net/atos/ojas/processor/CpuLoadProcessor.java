package net.atos.ojas.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.digest.Crypt;

public class CpuLoadProcessor implements Processor {
  /*********************************************
   *
   * This module attempts to generate a high CPU load
   * by repeatedly encrypting an arbitrary string
   *
   **********************************************/

  @Override
  public void process(Exchange exchange) throws Exception {
    String cipherText = Crypt.crypt("in Vino veritas!");
    for ( int i = 0; i < 100; i++) {
      cipherText = Crypt.crypt(cipherText);
    }
  }
}
