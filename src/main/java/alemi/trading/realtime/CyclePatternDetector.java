package alemi.trading.realtime;

import alemi.trading.coin.market.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service
public class CyclePatternDetector {

	@Autowired
	private MarketService marketService;

	public void history(String symbol) throws IOException {
		marketService.getHistory(symbol);
	}

	public void realtime(String symbol) throws IOException {
		marketService.registerTicker((symbol1, ticker) -> {

		}, Collections.singletonList(symbol));
	}

}
