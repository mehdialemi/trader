package alemi.trading.coin.stats;

import lombok.Data;

import java.util.List;

@Data
public class CoinStatCandleSeq {
	private long slotTime;
	private List<CoinStatCandle> candles;
}
