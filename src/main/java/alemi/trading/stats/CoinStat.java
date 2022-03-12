package alemi.trading.stats;

import com.kucoin.sdk.rest.response.TickerResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Data
@Slf4j
public class CoinStat {

	private BigDecimal avgPrice = BigDecimal.valueOf(0.0);
	private BigDecimal minPrice = BigDecimal.valueOf(Double.MAX_VALUE);
	private BigDecimal maxPrice = BigDecimal.valueOf(Double.MIN_VALUE);
	private BigDecimal numTickers = BigDecimal.valueOf(0.0);
	private BigDecimal maxDiff = BigDecimal.valueOf(0.0);
	private BigDecimal maxDiffPercent = BigDecimal.valueOf(0.0);

	public synchronized void reset() {
		avgPrice = BigDecimal.valueOf(0.0);
		minPrice = BigDecimal.valueOf(Double.MAX_VALUE);
		maxPrice = BigDecimal.valueOf(Double.MIN_VALUE);
		numTickers = BigDecimal.valueOf(0.0);
		maxDiff = BigDecimal.valueOf(0.0);
		maxDiffPercent = BigDecimal.valueOf(0.0);
	}

	public synchronized void update(TickerResponse ticker) {
		minPrice = minPrice.min(ticker.getPrice());
		maxPrice = maxPrice.max(ticker.getPrice());
		BigDecimal sum = numTickers.multiply(avgPrice).add(ticker.getPrice());
		numTickers = numTickers.add(BigDecimal.ONE);
		avgPrice = BigDecimal.valueOf(sum.doubleValue() / numTickers.intValue());
		maxDiff = maxPrice.subtract(minPrice);
		double percent = maxDiff.doubleValue() / minPrice.doubleValue();
		maxDiffPercent = BigDecimal.valueOf(percent);
	}



}
