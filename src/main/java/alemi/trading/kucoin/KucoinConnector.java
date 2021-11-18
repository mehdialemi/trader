package alemi.trading.kucoin;

import com.kucoin.sdk.KucoinClientBuilder;
import com.kucoin.sdk.KucoinPublicWSClient;
import com.kucoin.sdk.KucoinRestClient;
import com.kucoin.sdk.rest.interfaces.HistoryAPI;
import okhttp3.*;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;

@Service
public class KucoinConnector {
	private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	@Value("${kucoin.api.url}")
	private String baseUrl;

	@Value("${kucoin.api.key}")
	private String apiKey;

	@Value("${kucoin.api.secret}")
	private String secretKey;

	@Value("${kucoin.api.passphrase}")
	private String passphrase;

	@Value("${kucoin.api.base-path}")
	private String apiBasePath;

	@Value("{kucoin.api.version}")
	private String apiVersion;

	private OkHttpClient client;

	private KucoinRestClient restClient;

	private KucoinPublicWSClient publicWSClient;

	@PostConstruct
	public void init() throws IOException {
		client = new OkHttpClient.Builder().build();
		restClient = new KucoinClientBuilder()
				.withBaseUrl(baseUrl)
				.withApiKey(apiKey, secretKey, passphrase)
				.buildRestClient();

		publicWSClient = new KucoinClientBuilder()
				.withBaseUrl(baseUrl)
				.withApiKey(apiKey, secretKey, passphrase)
				.buildPublicWSClient();
	}

	public KucoinRestClient getRestClient() {
		return restClient;
	}

	public HistoryAPI getHistoryApi() {
		return restClient.historyAPI();
	}

	public Response get(String path) throws IOException {
		Request.Builder builder = build( "GET", apiBasePath + path);
		Request request = builder.get().build();
		Response response = client.newCall(request).execute();
		return response;
	}

	public Response post(String path, String json) throws IOException {
		Request.Builder post = build("POST", apiBasePath + path);
		RequestBody body = RequestBody.create(JSON, json);
		Request build = post.post(body).build();
		Response response = client.newCall(build).execute();
		return response;
	}

	private Request.Builder build(String method , String path) {
		long now = Instant.now().toEpochMilli();
		String timestamp =  now + "";
		String signStr = timestamp + method + path;
		Base64Utils.encode(signStr.getBytes());
		byte[] bytes = HmacUtils.hmacSha256(secretKey, signStr);
		String signature = Base64Utils.encodeToString(bytes);

		bytes = HmacUtils.hmacSha256(secretKey, passphrase + "");
		String pass = Base64Utils.encodeToString(bytes);

		Request.Builder builder = new Request.Builder()
				.addHeader("KC-API-SIGN", signature)
				.addHeader("KC-API-TIMESTAMP", timestamp)
				.addHeader("KC-API-KEY", apiKey)
				.addHeader("KC-API-PASSPHRASE", pass)
				.addHeader("KC-API-KEY-VERSION", apiVersion)
				.addHeader("Content-Type","application/json")
				.url(baseUrl + path);

		return builder;
	}

	public KucoinPublicWSClient getPublicWSClient() {
		return publicWSClient;
	}
}
