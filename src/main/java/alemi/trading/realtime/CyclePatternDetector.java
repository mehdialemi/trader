package alemi.trading.realtime;

import alemi.trading.coin.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service
public class CyclePatternDetector {

	@Autowired
	private CoinService coinService;

	public void history(String symbol) throws IOException {
		coinService.getHistory(symbol);
	}

	public void realtime(String symbol) {
		coinService.listen(coinData -> {

		}, Collections.singletonList(symbol));
	}

}
