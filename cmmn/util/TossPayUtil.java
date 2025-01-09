package cmmn.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import cmmn.vo.TossPayVO;

public class TossPayUtil{

	private static final String TOSS_API_URL = PropertiesUtil.getProperty("Globals.tosspayments.apiUrl");
	private static final String TOSS_SECRET_KEY = PropertiesUtil.getProperty("Globals.tosspayments.secretKey");

	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	protected static Logger LOG = LoggerFactory.getLogger(TossPayUtil.class);

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

		sb.append("_");
		// 1. 랜덤한 index를 추출하여
        // 2. 해당 값을 StringBuilder에 append
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
	public static Map<String, Object> getTossPayConfirm(TossPayVO paramVO) throws IOException {

    	UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(TOSS_API_URL).path("/v1/payments/confirm");

    	JSONObject requestJson = new JSONObject();
		requestJson.put("paymentKey", paramVO.getPaymentKey());
		requestJson.put("orderId", paramVO.getOrderId());
		requestJson.put("amount", paramVO.getAmount());

    	JSONObject response = sendRequest(requestJson, uriBuilder.build().toUriString());
    	boolean isSuccess = response.containsKey("error") ? false : true;

    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	if (isSuccess) {
    		resultMap.put("resultCode", "SUCCESS");
    		resultMap.put("paymentKey", response.get("paymentKey"));
    		resultMap.put("orderId", response.get("orderId"));
    		resultMap.put("method", response.get("method"));
    		resultMap.put("totalAmount", response.get("totalAmount"));
    		resultMap.put("status", response.get("status"));
    		resultMap.put("approvedAt", response.get("approvedAt"));

    		if ("카드".equals(response.get("method"))) {
    			resultMap.put("payType", "CARD");
    			resultMap.put("method", "신용/체크카드");
    		}else if ("간편결제".equals(response.get("method"))) {
    			resultMap.put("payType", "EASYPAY");
    		}else if ("휴대폰".equals(response.get("method"))) {
    			resultMap.put("payType", "MOBILE_PHONE");
    		}else if ("계좌이체".equals(response.get("method"))) {
    			resultMap.put("payType", "TRANSFER");
    		}
    	}else {
    		JSONObject responseFailure = (JSONObject)response.get("failure");
    		resultMap.put("resultCode", "FAIL");
    		resultMap.put("failureCode", responseFailure.get("code"));
    		resultMap.put("failureMessage", responseFailure.get("message"));
    	}

    	return resultMap;
    }

    @SuppressWarnings("unchecked")
	public static Map<String, Object> getTossPayCancel(TossPayVO paramVO) throws IOException {

    	String paymentKey = paramVO.getPaymentKey();

    	UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(TOSS_API_URL).path("/v1/payments/"+paymentKey+"/cancel");

    	JSONObject requestJson = new JSONObject();
		requestJson.put("paymentKey", paymentKey);
		requestJson.put("cancelReason", paramVO.getCancelReason());

    	JSONObject response = sendRequest(requestJson, uriBuilder.build().toUriString());

    	boolean isSuccess = response.containsKey("error") ? false : true;

    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	if (isSuccess) {
    		resultMap.put("resultCode", "SUCCESS");
    	}else {
    		JSONObject responseFailure = (JSONObject)response.get("failure");
    		resultMap.put("resultCode", "FAIL");
    		resultMap.put("failureCode", responseFailure.get("code"));
    		resultMap.put("failureMessage", responseFailure.get("message"));
    	}

    	return resultMap;
    }


	@SuppressWarnings("unchecked")
	public static JSONObject sendRequest(JSONObject requestData, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(urlString);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
            os.close();
        }

        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
            Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
        	LOG.error("Error reading response", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Error reading response");
            return errorResponse;
		}
    }

    public static HttpURLConnection createConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((TOSS_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }




}
