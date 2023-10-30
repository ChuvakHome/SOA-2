package ru.itmo.se.soa.lab2.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import ru.itmo.se.soa.lab2.dto.PatchBodyDTO;
import ru.itmo.se.soa.lab2.dto.PatchBodyDTO.FieldPatch;
import ru.itmo.se.soa.lab2.dto.VehicleDTO;

@Service
public class RestService {
	public static final String CALLEE_SERVICE_HOST_ENV_VAR = "SOA_LAB2_CALLEE_SERIVCE_HOST";
	
	public static final String CALLEE_SERVICE_HOST_NAME_ENV_VAR = "SOA_LAB2_CALLEE_SERIVCE_HOSTNAME";
	public static final String CALLEE_SERVICE_HOST_NAME_DEFAULT = "localhost";
	
	public static final String CALLEE_SERVICE_PORT_ENV_VAR = "SOA_LAB2_CALLEE_SERIVCE_PORT";
	public static final String CALLEE_SERVICE_PORT_DEFAULT = "8080";
	
	private final RestTemplate restTemplate;
	private final URL calleeServiceURL;

    public RestService() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        this.restTemplate = new RestTemplate();
        
        String catalinaBase = System.getProperty("catalina.base");
        String trustStoreFilepath = System.getProperty("javax.net.ssl.trustStore");
        String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
        
        File trustStoreFile = new File(catalinaBase, trustStoreFilepath);
        
        SSLContext sslContext = new SSLContextBuilder()
        		.loadTrustMaterial(trustStoreFile, trustStorePassword.toCharArray())
        		.build();
        
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(socketFactory)
                .build();
        
        HttpClient httpClient = HttpClients
        		.custom()
        		.setConnectionManager(connectionManager)
        		.build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        
        this.restTemplate.setRequestFactory(requestFactory);
    	this.restTemplate.setErrorHandler(new SilentErrorHandler());
        
        String calleeServiceURLString;
        
        if (System.getenv().containsKey(CALLEE_SERVICE_HOST_ENV_VAR))
        	calleeServiceURLString = System.getenv(CALLEE_SERVICE_HOST_ENV_VAR);
        else {
        	String calleeServiceHostname = Optional
        										.ofNullable(System.getenv(CALLEE_SERVICE_HOST_NAME_ENV_VAR))
        										.orElse(CALLEE_SERVICE_HOST_NAME_DEFAULT);
        	String calleeServicePort = Optional
											.ofNullable(System.getenv(CALLEE_SERVICE_PORT_ENV_VAR))
											.orElse(CALLEE_SERVICE_PORT_DEFAULT);
        	
        	calleeServiceURLString = String.format("https://%s:%s", calleeServiceHostname, calleeServicePort);
        }
        
        this.calleeServiceURL = new URL(calleeServiceURLString);
    }
    
    private ResponseEntity<Object> patch(Long id, PatchBodyDTO patchBodyDTO) {
    	String url = String.format("%s/api/v1/vehicles/{vehicle-id}", this.calleeServiceURL.toString());
    	
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	
    	return this.restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(patchBodyDTO, headers), Object.class, id);
    }
    
    public RestCallResult resetDistanceTravelled(Long id) {
    	try {
    		PatchBodyDTO patchBodyDTO = new PatchBodyDTO();
        	patchBodyDTO.setFields(List.of(
        		new FieldPatch("distanceTravelled", 0)
        	));
    		
    		patch(id, patchBodyDTO);
        	
        	return RestCallResult.SUCCESSFULL_OPERATION;
    	} catch (HttpBadRequestException e) {
    		return RestCallResult.BAD_REQUEST;
		} catch (HttpNotFoundException e) {
			return RestCallResult.NOT_FOUND;
		} catch (HttpServerErrorException e) {
			return RestCallResult.SERVER_ERROR;
		}
    }
    
    public RestCallResult addWheels(Long id, int wheelsCount) {
    	String url = String.format("%s/api/v1/vehicles/{vehicle-id}", this.calleeServiceURL.toString());
    	
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.TEXT_PLAIN);
    	headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    	
    	HttpEntity<Object> request = new HttpEntity<>(headers);
    	
    	try {
    		ResponseEntity<VehicleDTO> vehicleResponseEntity = this.restTemplate.exchange(url, HttpMethod.GET, request, VehicleDTO.class, id);
    		VehicleDTO vehicleDTO = vehicleResponseEntity.getBody();
    		
    		PatchBodyDTO patchBodyDTO = new PatchBodyDTO();
        	patchBodyDTO.setFields(List.of(
        		new FieldPatch("numberOfWheels", vehicleDTO.getNumberOfWheels() + wheelsCount)
        	));
        	
        	patch(id, patchBodyDTO);
        	
        	return RestCallResult.SUCCESSFULL_OPERATION;
    	} catch (HttpBadRequestException e) {
    		return RestCallResult.BAD_REQUEST;
		} catch (HttpNotFoundException e) {
			return RestCallResult.NOT_FOUND;
		} catch (HttpServerErrorException e) {
			return RestCallResult.SERVER_ERROR;
		}
    }
    
    private static class SilentErrorHandler extends DefaultResponseErrorHandler {
    	@Override
    	public boolean hasError(ClientHttpResponse response) throws IOException {
			return response.getStatusCode().isError();
		}

    	@Override
		public void handleError(ClientHttpResponse response) throws IOException {
			if (response.getStatusCode() == HttpStatus.BAD_REQUEST)
				throw new HttpBadRequestException();
			else if (response.getStatusCode() == HttpStatus.NOT_FOUND)
				throw new HttpNotFoundException();
			else if (response.getStatusCode().is5xxServerError())
				throw new HttpServerErrorException();
			else
				super.handleError(response);
		}
    }
}
