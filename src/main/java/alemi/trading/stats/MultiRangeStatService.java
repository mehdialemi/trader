package alemi.trading.stats;

import com.kucoin.sdk.rest.response.TickerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@ConditionalOnExpression("${stats.range.enable} || ${stats.high-rate.enable}")
public class MultiRangeStatService extends CoinStatBase {

	@Autowired
	private CoinStatService coinStatService;

	private RangeStats rangeStats;

	@PostConstruct
	public void init() {
		coinStatService.addStatService(this);
		rangeStats = new RangeStats(10);
	}

	public ConcurrentHashMap <String, List <CoinStat>> getCoinStats() {
		return rangeStats.map;
	}

	@Override
	public void addTicker(String symbol, TickerResponse ticker) {
		rangeStats.addTicker(symbol, ticker);
	}

	public static class RangeStats {

		private int rangeSize;
		private final ConcurrentHashMap<String, List<CoinStat>> map;
		private final Map<String, CoinStat> coinStats;

		public RangeStats(int rangeSize) {
			map = new ConcurrentHashMap<>();
			coinStats = new ConcurrentHashMap<>();
			this.rangeSize = rangeSize;
		}

		synchronized void addTicker(String symbol, TickerResponse ticker) {
			CoinStat coinStat = coinStats.get(symbol);
			if (coinStat == null) {
				coinStat = new CoinStat();
				coinStats.put(symbol, coinStat);
			}
			synchronized (coinStat) {
				if (coinStat.getNumTickers().intValue() >= rangeSize) {
					List <CoinStat> coinStats = map.computeIfAbsent(symbol, s -> new ArrayList<>());
					coinStats.add(coinStat);
					log.debug(
							"Coin stats for symbol {} is {}", symbol, coinStat);
					coinStat.reset();
				}
				coinStat.update(ticker);
			}
		}

	}
}
