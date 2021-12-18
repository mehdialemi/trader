package alemi.trading.coin.stats;

import alemi.trading.coin.market.TickerEventHanlder;
import alemi.trading.coin.market.MarketService;
import com.google.common.math.Stats;
import com.kucoin.sdk.rest.response.TickerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketStatService implements TickerEventHanlder {

	@Value("${marketInfo.queue.aggregate.aggSize:1}")
	private int aggSize;

	@Value("${marketInfo.queue.sequence.aggSize:100}")
	private int seqSize;

	@Value("${market.ticker.symbols}")
	private List <String> symbols;

	@Autowired
	private MarketService marketService;

	private final ConcurrentHashMap<String, StatsCreator> stats = new ConcurrentHashMap <>();
	private final ConcurrentHashMap<String, CoinStats> seqs = new ConcurrentHashMap <>();

	@PostConstruct
	public void init() throws IOException {
		marketService.registerTicker(this::onNewTicker, symbols);
	}

	public ArrayBlockingQueue <CoinStat> getStats(String symbol) {
		return seqs.getOrDefault(symbol, new CoinStats()).queue;
	}

	@Override
	public void onNewTicker(String symbol, TickerResponse ticker) {
		stats.computeIfAbsent(symbol, s -> new StatsCreator(symbol)).addTicker(ticker);
	}

	private class StatsCreator {
		private final List<TickerResponse> list = new ArrayList <>();
		private final String symbol;


		StatsCreator(String symbol) {
			this.symbol = symbol;
		}

		synchronized void addTicker(TickerResponse ticker) {
			if (list.size() < aggSize) {
				list.add(ticker);
			} else {
				CoinStat coinStat = create();
				seqs.computeIfAbsent(symbol, s -> new CoinStats()).add(coinStat);
			}
		}

		private CoinStat create() {
			var pStats = Stats.of(list.stream().mapToDouble(a -> a.getPrice().doubleValue()));
			var vStats = Stats.of(list.stream().mapToDouble(a -> a.getSize().doubleValue()));

			Pair <Long, Long> minMaxTime = list.stream()
					.map(TickerResponse::getTime)
					.map(a -> Pair.of(a, a))
					.reduce(Pair.of(0L, 0L), (a, b) ->
							Pair.of(Math.min(a.getFirst(), b.getFirst()), Math.min(a.getSecond(), b.getSecond()))
					);

			return new CoinStat(symbol, minMaxTime.getFirst(), minMaxTime.getSecond(), pStats, vStats, list);
		}
	}

	private class CoinStats {
		private final ArrayBlockingQueue<CoinStat> queue = new ArrayBlockingQueue <>(seqSize);

		void add(CoinStat coinStat) {
			if (queue.remainingCapacity() == 0)
				queue.remove();
			queue.add(coinStat);
		}
	}

}
