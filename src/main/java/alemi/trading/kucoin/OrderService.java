package alemi.trading.kucoin;

import com.kucoin.sdk.rest.interfaces.OrderAPI;
import com.kucoin.sdk.rest.request.OrderCreateApiRequest;
import com.kucoin.sdk.rest.response.OrderCreateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;

@Service
public class OrderService {

	@Autowired
	private KucoinConnector kucoinConnector;

	private OrderAPI orderAPI;

	@PostConstruct
	public void init() {
		orderAPI = kucoinConnector.getRestClient().orderAPI();
	}

	public String buy(String symbol, BigDecimal price, BigDecimal size) throws IOException {
		return order("buy", symbol, price, size);
	}

	public String sell(String symbol, BigDecimal price, BigDecimal size) throws IOException {
		return order("sell", symbol, price, size);
	}

	public void cancel(String id) throws IOException {
		orderAPI.cancelOrder(id);
	}

	private String order(String side, String symbol, BigDecimal price, BigDecimal size) throws IOException {
		OrderCreateApiRequest request = OrderCreateApiRequest.builder()
				.symbol(symbol)
				.price(price)
				.size(size)
				.side(side)
				.build();
		OrderCreateResponse response = orderAPI.createOrder(request);
		String orderId = response.getOrderId();
		return orderId;
	}
}
