package alemi.trading.coin;

import alemi.trading.kucoin.KucoinConnector;
import com.kucoin.sdk.KucoinPublicWSClient;
import com.kucoin.sdk.rest.response.SymbolTickResponse;
import com.kucoin.sdk.rest.response.TickerResponse;
import com.kucoin.sdk.rest.response.TradeHistoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class CoinService {

	@Autowired
	private KucoinConnector connector;

	private KucoinPublicWSClient pwClient;

	@PostConstruct
	public void init() {
		pwClient = connector.getPublicWSClient();
	}

	public void listen(CoinEventHandler handler, List <String> symbols) {
		pwClient.onTicker(response -> {
			log.trace("Receiving ticker event {}", response);
			CoinData coinData = new CoinData();
			coinData.setSymbol(response.getSubject());
			coinData.setChange(response.getData());
			handler.handle(coinData);
		}, symbols.toArray(new String[0]));
	}

	public List <TradeHistoryResponse> getHistory(String symbol) throws IOException {
		return connector.getHistoryApi().getTradeHistories(symbol);
	}

	public SymbolTickResponse get24hStats(String symbol) throws IOException {
		return connector.getRestClient().symbolAPI().get24hrStats(symbol);
	}
}
