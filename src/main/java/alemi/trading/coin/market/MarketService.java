package alemi.trading.coin.market;

import alemi.trading.kucoin.KucoinConnector;
import com.kucoin.sdk.KucoinPublicWSClient;
import com.kucoin.sdk.exception.KucoinApiException;
import com.kucoin.sdk.rest.response.SymbolTickResponse;
import com.kucoin.sdk.rest.response.TickerResponse;
import com.kucoin.sdk.rest.response.TradeHistoryResponse;
import com.kucoin.sdk.websocket.KucoinAPICallback;
import com.kucoin.sdk.websocket.event.KucoinEvent;
import com.kucoin.sdk.websocket.event.Level2ChangeEvent;
import com.kucoin.sdk.websocket.event.Level3ChangeEvent;
import com.kucoin.sdk.websocket.event.TickerChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class MarketService {

	@Autowired
	private KucoinConnector connector;

	private KucoinPublicWSClient stream;

	@PostConstruct
	public void init() {
		stream = connector.getStream();
	}

	public void registerTicker(TickerEventHanlder handler, List <String> symbols) throws IOException {
		for (String symbol : symbols) {
			TickerResponse ticker = connector.getRest().symbolAPI().getTicker(symbol);
			handler.onNewTicker(symbol, ticker);
		}

		stream.onLevel2Data(response -> {
			log.info("got {}", response);
		}, symbols.toArray(new String[0]));

		stream.onLevel3Data(response -> {
			log.info("got 3d {}", response);
		}, symbols.toArray(new String[0]));

		stream.onTicker(response -> {
			log.trace("Receiving ticker event {}", response);
			TickerChangeEvent event = response.getData();
			handler.onNewTicker(response.getId(), event);
		}, symbols.toArray(new String[0]));
	}

	public List <TradeHistoryResponse> getHistory(String symbol) throws IOException {
		return connector.getHistoryApi().getTradeHistories(symbol);
	}

	public SymbolTickResponse get24hStats(String symbol) throws IOException {
		return connector.getRest().symbolAPI().get24hrStats(symbol);
	}

	public TickerResponse getTicker(String symbol) throws IOException {
		return connector.getRest().symbolAPI().getTicker(symbol);
	}
}
