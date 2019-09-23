package org.acumos.deploymentclient.util;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.Crumb;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenkinsJobBuilder {
	
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public void buildJenkinsJob(String jenkinUrl,String jenkinUserName,String jenkinPassword,String jobName,String taskId)throws Exception {
		log.debug("buildJenkinsJob start");
		log.debug("jenkinUrl "+jenkinUrl);
		log.debug("jenkinUserName "+jenkinUserName);
		log.debug("jenkinPassword "+jenkinPassword);
		log.debug("jobName "+jobName);
		log.debug("taskId "+taskId);
		TrustManager[] trustAllCerts = new TrustManager[] { 
			    new X509TrustManager() {     
			        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
			            return new X509Certificate[0];
			        } 
			        public void checkClientTrusted( 
			            java.security.cert.X509Certificate[] certs, String authType) {
			            } 
			        public void checkServerTrusted( 
			            java.security.cert.X509Certificate[] certs, String authType) {
			        }
			    } 
			};
		final SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpClientBuilder aa=HttpClientBuilder.create().setSslcontext(sc);
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:67.0) Gecko/20100101 Firefox/67.0"));
		headers.add(new BasicHeader("requestBody", "user=admin&pass=admin"));
		aa.setDefaultHeaders(headers);
		JenkinsHttpClient jenkinHttpClient=new JenkinsHttpClient(URI.create(jenkinUrl),aa,jenkinUserName,jenkinPassword);
		JenkinsServer jenkins = new JenkinsServer(jenkinHttpClient);
		log.debug("jenkin Version : "+jenkins.getVersion());
		JobWithDetails x=jenkins.getJob(jobName);
		log.debug("LastBuild "+x.getLastBuild().getNumber()+" jenkin URL "+x.getUrl());
		Map<String,String> params=new HashMap<String,String>();
		params.put("taskId", taskId);
		x.build(params, true);
		jenkins.close();
		log.debug("buildJenkinsJob End");
	}

}
