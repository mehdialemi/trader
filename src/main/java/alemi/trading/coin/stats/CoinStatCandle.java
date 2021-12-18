package alemi.trading.coin.stats;

import com.google.common.math.Stats;
import com.kucoin.sdk.rest.response.SymbolTickResponse;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoinStatCandle implements Comparable<CoinStatCandle> {
	private Stats price;
	private Stats volume;
	private Stats bestAskPrice;
	private Stats bestAskVolume;
	private Stats bestBidPrice;
	private Stats bestBidVolume;
	private SymbolTickResponse hrStats;

	@Override
	public int compareTo(CoinStatCandle other) {
		BigDecimal diff = BigDecimal.ZERO;

		BigDecimal rateDiff = changeRateDiff(other);
		diff.add(rateDiff.abs());

		BigDecimal priceDiff = priceDiff(other);
		diff.add(priceDiff.abs());

		BigDecimal volumeDiff = volumeDiff(other);
		diff.add(volumeDiff.abs());

		BigDecimal bestAskPriceDiff = bestAskPriceDiff(other);
		diff.add(bestAskPriceDiff.abs());

		BigDecimal bestAskVolumeDiff = bestAskVolumeDiff(other);
		diff.add(bestAskVolumeDiff.abs());

		BigDecimal bestBidPriceDiff = bestBidPriceDiff(other);
		diff.add(bestBidPriceDiff.abs());

		BigDecimal bestBidVolumeDiff = bestBidVolumeDiff(other);
		diff.add(bestBidVolumeDiff.abs());


		return diff.intValue();
	}

	private BigDecimal priceDiff(CoinStatCandle other) {
		return BigDecimal.valueOf(price.populationVariance()).subtract(BigDecimal.valueOf(other.price.populationVariance()));
	}

	private BigDecimal volumeDiff(CoinStatCandle other) {
		return BigDecimal.valueOf(volume.populationVariance()).subtract(BigDecimal.valueOf(other.volume.populationVariance()));
	}

	private BigDecimal bestAskPriceDiff(CoinStatCandle other) {
		return BigDecimal.valueOf(bestAskPrice.populationVariance()).subtract(BigDecimal.valueOf(other.bestAskPrice.populationVariance()));
	}

	private BigDecimal bestAskVolumeDiff(CoinStatCandle other) {
		return BigDecimal.valueOf(bestAskVolume.populationVariance()).subtract(BigDecimal.valueOf(other.bestAskVolume.populationVariance()));
	}

	private BigDecimal bestBidPriceDiff(CoinStatCandle other) {
		return BigDecimal.valueOf(bestBidPrice.populationVariance()).subtract(BigDecimal.valueOf(other.bestBidPrice.populationVariance()));
	}

	private BigDecimal bestBidVolumeDiff(CoinStatCandle other) {
		return BigDecimal.valueOf(bestBidVolume.populationVariance()).subtract(BigDecimal.valueOf(other.bestBidVolume.populationVariance()));
	}

	private BigDecimal changeRateDiff(CoinStatCandle other) {
		return hrStats.getChangeRate().subtract(other.hrStats.getChangeRate());
	}
}
