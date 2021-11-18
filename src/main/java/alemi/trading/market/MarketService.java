package alemi.trading.market;

import alemi.trading.coin.CoinData;
import alemi.trading.coin.CoinService;
import alemi.trading.minig.CoinMiner;
import com.google.common.math.Stats;
import com.kucoin.sdk.rest.response.SymbolTickResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketService {

	@Autowired
	private CoinService coinService;

	@Autowired
	private CoinMiner coinMiner;

	@Value("${coin.market.data.sequence.slot}")
	private long slotTime;

	private final Map<String, CoinMarketDataSequence> coinSlots = new ConcurrentHashMap <>();
	private final Map<String, Pair<Instant, List<CoinData>>> coinSlot = new ConcurrentHashMap <>();

	@PostConstruct
	public void init() throws IOException {
		List <CoinData> coins = coinMiner.getCoins();

		for (CoinData coin : coins) {
			CoinMarketDataSequence sequence = new CoinMarketDataSequence();
			sequence.setSlotTime(slotTime);
			coinSlots.put(coin.getSymbol(), sequence);
		}

		List <String> symbols = coins.stream().map(CoinData::getSymbol).collect(Collectors.toList());
		coinService.listen(event -> reflect(event), symbols);
	}

	private void reflect(CoinData coinData) {
		Pair <Instant, List<CoinData>> slot = coinSlot.getOrDefault(coinData.getSymbol(),
				Pair.of(Instant.now(), new ArrayList <>()));
		Instant instant = slot.getFirst();
		if (instant.plusMillis(slotTime).isAfter(Instant.now())) {
			try {
				addSlot(coinData.getSymbol(), slot.getSecond());
			} catch (IOException e) {
				log.error("Unable to add slot for symbol {}", coinData.getSymbol());
			}
			coinSlot.remove(coinData.getSymbol());
		}
	}

	private void addSlot(String symbol, List<CoinData> list) throws IOException {
		Stats bestAskPrice = Stats.of(list.stream().mapToDouble(c -> c.getChange().getBestAsk().doubleValue()));
		Stats bestAskVolume = Stats.of(list.stream().mapToDouble(c -> c.getChange().getBestAskSize().doubleValue()));
		Stats bestBidPrice = Stats.of(list.stream().mapToDouble(c -> c.getChange().getBestBid().doubleValue()));
		Stats bestBidVolume = Stats.of(list.stream().mapToDouble(c -> c.getChange().getBestBidSize().doubleValue()));
		Stats price = Stats.of(list.stream().mapToDouble(c -> c.getChange().getPrice().doubleValue()));
		Stats volume = Stats.of(list.stream().mapToDouble(c -> c.getChange().getSize().doubleValue()));

		CoinMarketData slot = new CoinMarketData();
		slot.setPrice(price);
		slot.setVolume(volume);
		slot.setBestAskPrice(bestAskPrice);
		slot.setBestAskVolume(bestAskVolume);
		slot.setBestBidPrice(bestBidPrice);
		slot.setBestBidVolume(bestBidVolume);

		SymbolTickResponse hStats = coinService.get24hStats(symbol);
		slot.setHrStats(hStats);

		CoinMarketDataSequence slotSeq = coinSlots.get(symbol);
		slotSeq.getSlots().add(slot);
	}
}
