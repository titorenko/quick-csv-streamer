package uk.elementarysoftware.quickcsv.integration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import uk.elementarysoftware.quickcsv.api.CSVParser;
import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.sampledomain.City;

public class HttpStreamTest {
	
	@Rule
	public final FileServer httpServer = new FileServer();
	
	private final File testFile = IntegrationTest.inputUnix;
	private final CSVParser<City> parser = CSVParserBuilder.aParser(City.MAPPER).build();
	
	@Test
	public void testParseHttpResource() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(httpServer.getURI().resolve(testFile.getName()));
		CloseableHttpResponse response = httpclient.execute(httpGet);
		
		try(Stream<City> stream = parser.parse(response.getEntity().getContent())) {
			assertEquals(FileUtils.readLines(testFile, "UTF-8").size(), stream.count());
		}
	}

	static class FileServer extends ExternalResource {
		
		private Server server;

		@Override
		protected void before() throws Throwable {
			server = new Server(0);

			ResourceHandler rh = new ResourceHandler();
	        rh.setResourceBase("src/test/resources");
	        
	        HandlerList handlers = new HandlerList();
	        handlers.setHandlers(new Handler[] { rh, new DefaultHandler() });
	        server.setHandler(handlers);
	        
	        server.start();
		}
		

		@Override
		protected void after() {
			try {
				server.stop();
			} catch (Exception e) {
				//no-op
			}
		}
		
		public URI getURI() {
			return server.getURI();
		}	         
	}
}
