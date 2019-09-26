package org.acumos.deploymentclient.util;

import java.lang.invoke.MethodHandles;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class JenkinsJobBuilder {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void buildJenkinsJob(
      String jenkinUrl, String jenkinUserName, String jenkinPassword, String jobName, String taskId)
      throws Exception {
    log.debug("buildJenkinsJob start");
    log.debug("jenkinUrl " + jenkinUrl);
    log.debug("jenkinUserName " + jenkinUserName);
    log.debug("jenkinPassword " + jenkinPassword);
    log.debug("jobName " + jobName);
    log.debug("taskId " + taskId);
    TrustManager[] trustAllCerts =
        new TrustManager[] {
          new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }

            public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {}
          }
        };
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<String> entity = new HttpEntity<String>("");
    StringBuilder url = new StringBuilder();
    url.append(jenkinUrl);
    url.append("job/");
    url.append(jobName);
    url.append("/buildWithParameters?");
    url.append("taskId=");
    url.append(taskId);
    restTemplate.exchange(url.toString(), HttpMethod.POST, entity, String.class);

    // final SSLContext sc = SSLContext.getInstance("TLSv1.2");
    //    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    //    HttpClientBuilder aa=HttpClientBuilder.create().setSslcontext(sc);
    //    List<Header> headers = new ArrayList<>();
    //    headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:67.0)
    // Gecko/20100101 Firefox/67.0"));
    // headers.add(new BasicHeader("requestBody", "user=admin&pass=admin"));
    // aa.setDefaultHeaders(headers);
    // JenkinsHttpClient jenkinHttpClient=new
    // JenkinsHttpClient(URI.create(jenkinUrl),aa,jenkinUserName,jenkinPassword);
    // JenkinsServer jenkins = new JenkinsServer(jenkinHttpClient);
    // log.debug("jenkin Version : "+jenkins.getVersion());
    // JobWithDetails x=jenkins.getJob(jobName);
    // log.debug("LastBuild "+x.getLastBuild().getNumber()+" jenkin URL "+x.getUrl());
    // Map<String,String> params=new HashMap<String,String>();
    // params.put("taskId", taskId);
    // x.build(params, true);
    // jenkins.close();
    log.debug("buildJenkinsJob End");
  }
}
