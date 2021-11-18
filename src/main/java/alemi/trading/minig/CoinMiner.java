package alemi.trading.minig;

import alemi.trading.coin.CoinData;
import alemi.trading.kucoin.KucoinConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoinMiner {

	@Autowired
	private KucoinConnector kucoinConnector;

	public List<CoinData> getCoins() throws IOException {
		return kucoinConnector.getRestClient()
				.symbolAPI()
				.getSymbols()
				.stream()
				.map(a -> CoinData.create(a.getSymbol(), a))
				.collect(Collectors.toList());
	}

	public void categorize(List<CoinData> coins) {

	}
}
