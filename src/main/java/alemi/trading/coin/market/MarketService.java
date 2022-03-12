package alemi.trading.coin.market;

import alemi.trading.kucoin.KucoinConnector;
import com.kucoin.sdk.KucoinPublicWSClient;
import com.kucoin.sdk.rest.response.SymbolResponse;
import com.kucoin.sdk.rest.response.SymbolTickResponse;
import com.kucoin.sdk.rest.response.TickerResponse;
import com.kucoin.sdk.rest.response.TradeHistoryResponse;
import com.kucoin.sdk.websocket.event.TickerChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MarketService {

	@Autowired
	private KucoinConnector connector;

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	@PostConstruct
	public void init() throws IOException {
		KucoinPublicWSClient stream = connector.getStream();
		List <String> symbolList = connector.getRest()
				.symbolAPI()
				.getSymbols()
				.stream()
				.filter(a -> a.getQuoteCurrency().equalsIgnoreCase("USDT"))
				.filter(SymbolResponse::isEnableTrading)
				.map(SymbolResponse::getSymbol)
				.collect(Collectors.toList());

		SortedSet <SymbolTickResponse> sortedSymbols =
				new TreeSet <>((o1, o2) -> o2.getVolValue().subtract(o1.getVolValue()).intValue());
		for (String symbol : symbolList) {
			SymbolTickResponse hrStats = connector.getRest().symbolAPI().get24hrStats(symbol);
			log.info("hrStats {}", hrStats);
			sortedSymbols.add(hrStats);
		}

		String[] monitorSymbols = sortedSymbols.stream()
				.limit(100)
				.map(s -> s.getSymbol())
				.collect(Collectors.toList())
				.toArray(new String[0]);

		stream.onTicker(response ->
				executorService.submit(() -> {
					log.trace("Receiving ticker event {}", response);
					TickerChangeEvent event = response.getData();
					String symbol = response.getTopic().split(":")[1].split("-")[0];
					for (TickerEventHanlder handler : handlers) {
						handler.onNewTicker(symbol, event);
					}
				}), monitorSymbols );
	}

	private final ConcurrentHashSet<TickerEventHanlder> handlers = new ConcurrentHashSet<>();

	public void addHandler(TickerEventHanlder handler) {
		handlers.add(handler);
	}

	public void start() {

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
