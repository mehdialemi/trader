package alemi.trading.coin.market;

import com.kucoin.sdk.rest.response.TickerResponse;

public interface TickerEventHanlder {

	void onNewTicker(String symbol, TickerResponse ticker);
}
