package alemi.trading.stats;

import lombok.Data;

import java.util.List;

@Data
public class CoinStatCandleSeq {
	private long slotTime;
	private List<CoinStatCandle> candles;
}
