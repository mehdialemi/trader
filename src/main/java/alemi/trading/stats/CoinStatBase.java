package alemi.trading.stats;

import com.kucoin.sdk.rest.response.TickerResponse;

public abstract class CoinStatBase {

	public abstract void addTicker(String symbol, TickerResponse ticker);
}
