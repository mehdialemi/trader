package alemi.trading.market;

import lombok.Data;

import java.util.List;

@Data
public class CoinMarketDataSequence {
	private long slotTime;
	private List<CoinMarketData> slots;
}
