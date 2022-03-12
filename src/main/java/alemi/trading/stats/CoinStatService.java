package alemi.trading.stats;

import alemi.trading.coin.market.MarketService;
import com.kucoin.sdk.rest.response.TickerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
@Slf4j
public class CoinStatService {

	@Value("${stats.symbols}")
	private Set<String> symbols;

	@Autowired
	private MarketService marketService;

	private final Map<String, Set<TickerResponse>> tickers = new ConcurrentHashMap <>();

	private final List<CoinStatBase> statServices = new ArrayList<>();

	@PostConstruct
	public void init() {
		marketService.addHandler((symbol, ticker) -> add(symbol, ticker));
	}

	public void addStatService(CoinStatBase statService) {
		statServices.add(statService);
	}

	public Stream<TickerResponse> getLastTickers(String symbol, int size) {
		Set <TickerResponse> tickerSet = tickers.get(symbol);
		if (tickerSet == null) return null;

		int skipSize = tickerSet.size() - Math.min(size, tickerSet.size());
		return tickerSet.stream().skip(skipSize);
	}

	private void add(String symbol, TickerResponse ticker) {
		if (ticker== null) {
			return;
		}

		log.trace("Got ticker for symbol {} {}", symbol, ticker);
		for (CoinStatBase statService : statServices) {
			statService.addTicker(symbol, ticker);
		}
	}
}
