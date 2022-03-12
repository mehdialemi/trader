package alemi.trading.stats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@ConditionalOnExpression("${stats.high-rate.enable}")
public class HighRateChangeCoins {

	@Autowired
	private MultiRangeStatService rangeStatService;

	private List <Pair<String, Double>> highRates = new ArrayList<>();

	@Scheduled(fixedRate = 40000)
	public void highestRates() {
		ConcurrentHashMap <String, List <CoinStat>> coinStats = rangeStatService.getCoinStats();
		for (Map.Entry <String, List <CoinStat>> entry : coinStats.entrySet()) {
			List <CoinStat> statList = entry.getValue();
			if (statList == null)
				continue;

			double v = maxRate(statList);

			if (Double.compare(v, 0.0) == 0.0)
				continue;

			highRates.add(Pair.of(entry.getKey(), v));
		}

		if (highRates.size() > 0) {
			highRates.stream().limit(10).forEach(s -> {
				List <CoinStat> stats = coinStats.get(s.getFirst());
				Collections.sort(stats, (o1, o2) -> o2.getMaxDiffPercent()
								.subtract(o1.getMaxDiffPercent().multiply(BigDecimal.valueOf(1000)))
								.intValue());
				CoinStat coinStat = stats.get(0);
				log.info("Highest rate {}", coinStat);
			});
		}
	}

	private double maxRate(List <CoinStat> coinStats) {
		return coinStats.stream().mapToDouble(a -> a.getMaxDiffPercent().doubleValue()).max().orElse(0);
	}
}
