package net.atos.ojas;

import net.atos.ojas.processor.CpuLoadProcessor;
import net.atos.ojas.processor.RamLoadProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.json.simple.JsonObject;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;

@Component
public class ApiRoute extends RouteBuilder {

  @Autowired
  CamelContext ctx;

  @Value("${ssl.hostname.verify}")
  String isSslHostnameValidation;

  @Override
  public void configure() throws Exception {
    // DEBUG
    //ctx.setTracing(true);
    // SSL Config
    configureSslForHttp4();

    // @formatter:off
    restConfiguration()
      .component("servlet")
      .contextPath("/")
      .bindingMode(RestBindingMode.off)
      .apiContextPath("/api-doc")
        .apiProperty("api.title", "User API").apiProperty("api.version", "1.0.0")
        .apiProperty("cors", "true");


    rest("/")
      .get("/")
      .consumes(MediaType.MEDIA_TYPE_WILDCARD)
      .produces(MediaType.APPLICATION_JSON)
        .to("direct:in")

      .post("/")
        .consumes(MediaType.MEDIA_TYPE_WILDCARD)
        .produces(MediaType.APPLICATION_JSON)
        .to("direct:in");

    from("direct:in").routeId("route-1")
      .log(LoggingLevel.INFO, "Message Trace Id: ${header.breadcrumbId}")
      .process((e) -> {
        e.getIn().setHeader("perf.startTimeMillis", System.currentTimeMillis());
      })
      .choice()
        .when(header(Exchange.HTTP_METHOD).isEqualTo("GET"))
          .log(LoggingLevel.INFO, "RAM Test")
          .process(new RamLoadProcessor())
          .endChoice()
        .when(header(Exchange.HTTP_METHOD).isEqualTo("POST"))
          .log(LoggingLevel.INFO, "CPU Test")
          .process(new CpuLoadProcessor())
          .endChoice()
        .otherwise()
          .log(LoggingLevel.WARN, "HTTP Method: ${header.CamelHttpMethod}")
          .endChoice()
      .end()
      .process((e) -> {
        long endTimeMillis = System.currentTimeMillis();
        long startTimeMillis = (long) e.getIn().getHeader("perf.startTimeMillis");
        long durationMillis = endTimeMillis - startTimeMillis;
        DateTime startDateTime = new DateTime(startTimeMillis);
        String t = startDateTime.toString("HH:mm:ss.SSS");
        e.getOut().setBody("{ \"start-time\": \""+t+"\",\n\"  duration\": "+durationMillis+" }\n");
       })
      .to("mock:out");
    // @formatter:on

  }

  private void configureSslForHttp4() {

    KeyStoreParameters trustStoreParameters = new KeyStoreParameters();
    trustStoreParameters.setResource("ssl/truststore.p12");
    trustStoreParameters.setPassword("password");

    TrustManagersParameters trustManagersParameters = new TrustManagersParameters();
    trustManagersParameters.setKeyStore(trustStoreParameters);

    SSLContextParameters sslContextParameters = new SSLContextParameters();
    sslContextParameters.setTrustManagers(trustManagersParameters);

    HttpComponent http4Component = getContext().getComponent("http4", HttpComponent.class);
    http4Component.setSslContextParameters(sslContextParameters);

    if ( isSslHostnameValidation != null && isSslHostnameValidation.equals("false")) {
      http4Component.setX509HostnameVerifier(new AllowAllHostnameVerifier());
    }
  }

}