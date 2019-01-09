package net.atos.ojas.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.digest.Crypt;

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class RamLoadProcessor implements Processor {
  /*********************************************
   *
   * This module attempts to generate a high RAM usage
   * by declaring an arbitrarily large byte array
   *
   *********************************************/

  @Override
  public void process(Exchange exchange) throws Exception {
    byte[] b = new byte[200*1024*1024];
    Arrays.fill(b,(byte) 0);
    //Thread.sleep(1000);
    b = null;
    System.gc();
  }
}
