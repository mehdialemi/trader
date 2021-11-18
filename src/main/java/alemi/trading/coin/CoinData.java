package alemi.trading.coin;

import com.kucoin.sdk.rest.response.SymbolResponse;
import com.kucoin.sdk.websocket.event.TickerChangeEvent;
import lombok.Data;

@Data
public class CoinData {
	private String symbol;
	private TickerChangeEvent change;
	private SymbolResponse info;

	public static CoinData create(String symbol, SymbolResponse info) {
		CoinData coinData = new CoinData();
		coinData.setSymbol(symbol);
		coinData.setInfo(info);
		return coinData;
	}
}
