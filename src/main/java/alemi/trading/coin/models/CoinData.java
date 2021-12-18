package alemi.trading.coin.models;

import com.kucoin.sdk.rest.response.SymbolResponse;
import com.kucoin.sdk.rest.response.SymbolTickResponse;
import com.kucoin.sdk.rest.response.TickerResponse;
import lombok.Data;

@Data
public class CoinData {
	private String symbol;
	private SymbolResponse basic;
	private TickerResponse ticker;
	private SymbolTickResponse hrStats;

	private CoinData() {}

	public static Builder newBuilder(String symbol) {
		return new Builder(symbol);
	}

	public static class Builder {
		private CoinData coin;


		Builder(String symbol) {
			coin = new CoinData();
			coin.symbol = symbol;
		}

		public Builder withSymbolInfo(SymbolResponse symbolInfo) {
			coin.basic = symbolInfo;
			return this;
		}

		public Builder withTickerInfo(TickerResponse tickerInfo) {
			coin.ticker = tickerInfo;
			return this;
		}

		public Builder withHrStats(SymbolTickResponse hrStat) {
			coin.hrStats = hrStat;
			return this;
		}

		public CoinData build() {
			return coin;
		}
	}
}
