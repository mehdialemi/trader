package alemi.trading.bots;

import com.kucoin.sdk.rest.response.TickerResponse;

public interface BotOperation {

	void inform(String subject, TickerResponse tickerResponse);
}
