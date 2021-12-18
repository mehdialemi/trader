package alemi.trading.coin.stats;

import alemi.trading.coin.models.CoinMarketInfo;
import com.google.common.math.Stats;
import com.kucoin.sdk.rest.response.TickerResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoinStat {
	private String symbol;
	private long startTime;
	private long endTime;
	private Stats priceStats;
	private Stats volStats;
	private List<TickerResponse> tickers;
}
