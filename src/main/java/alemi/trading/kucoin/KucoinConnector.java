package alemi.trading.kucoin;

import com.kucoin.sdk.KucoinClientBuilder;
import com.kucoin.sdk.KucoinPublicWSClient;
import com.kucoin.sdk.KucoinRestClient;
import com.kucoin.sdk.rest.interfaces.HistoryAPI;
import com.kucoin.sdk.rest.response.CurrencyResponse;
import okhttp3.*;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

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

	private KucoinRestClient rest;

	private KucoinPublicWSClient stream;

	@PostConstruct
	public void init() throws IOException {
		rest = new KucoinClientBuilder()
				.withApiKey(apiKey, secretKey, passphrase)
				.buildRestClient();

		stream = new KucoinClientBuilder()
				.buildPublicWSClient();
	}

	public KucoinRestClient getRest() {
		return rest;
	}

	public KucoinPublicWSClient getStream() {
		return stream;
	}

	public HistoryAPI getHistoryApi() {
		return rest.historyAPI();
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
}
