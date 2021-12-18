package alemi.trading.bots;


import alemi.trading.coin.market.MarketService;
import alemi.trading.coin.market.TickerEventHanlder;
import alemi.trading.kucoin.OrderService;
import com.kucoin.sdk.rest.response.TickerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TradeEngine implements TickerEventHanlder {

	@Autowired
	private MarketService marketService;

	@Autowired
	private OrderService orderService;

	private Map <String, BigDecimal> lastPrices = new ConcurrentHashMap <>();
	private Map <String, Pair <BigDecimal, BigDecimal>> buyMap = new ConcurrentHashMap <>();
	private Map <String, Pair <BigDecimal, BigDecimal>> sellMap = new ConcurrentHashMap <>();
	private Map <String, BigDecimal> avgPrices = new ConcurrentHashMap <>();
	private BotOperation operation;
	private boolean simulate;
	private AtomicReference <BigDecimal> atomicBudget = new AtomicReference <>();

	public void setup(TradeBotConfig config) throws IOException {
		this.operation = config.getOperation();
		this.simulate = config.isSimulate();
		this.atomicBudget.set(config.getBudget());

		for (String symbol : config.getSymbols()) {
			lastPrices.put(symbol, BigDecimal.ZERO);
			buyMap.put(symbol, Pair.of(BigDecimal.ZERO, BigDecimal.ZERO));
			sellMap.put(symbol, Pair.of(BigDecimal.ZERO, BigDecimal.ZERO));
		}

		marketService.registerTicker(this::onNewTicker, config.getSymbols());
	}

	public synchronized void buy(String symbol, BigDecimal price, BigDecimal size) throws IOException {
		BigDecimal before = atomicBudget.get();
		BigDecimal subtract = before.subtract(price.multiply(size));

		if (subtract.compareTo(BigDecimal.ZERO) < 0 || size.equals(BigDecimal.ZERO))
			throw new RuntimeException();

		atomicBudget.compareAndSet(before, subtract);

		if (!simulate) {
			orderService.sell(symbol, price, size);
		}

		Pair <BigDecimal, BigDecimal> pair = buyMap.getOrDefault(symbol, Pair.of(BigDecimal.ZERO, BigDecimal.ZERO));
		BigDecimal sumCost = pair.getFirst().add(price);
		BigDecimal sumSize = pair.getSecond().add(size);
		buyMap.put(symbol, Pair.of(sumCost, sumSize));
		avgPrices.put(symbol, sumCost.divide(sumSize));
	}

	public synchronized void sell(String symbol, BigDecimal price, BigDecimal size) throws IOException {
		Pair <BigDecimal, BigDecimal> pairCostSize = buyMap.get(symbol);
		if (pairCostSize == null)
			throw new RuntimeException();

		BigDecimal coinCost = pairCostSize.getFirst();
		BigDecimal coinSize = pairCostSize.getSecond();

		BigDecimal subtract = coinSize.subtract(size);
		if (BigDecimal.ZERO.compareTo(subtract) < 0)
			throw new RuntimeException();

		buyMap.put(symbol, Pair.of(coinCost, subtract));

		if (!simulate) {
			orderService.sell(symbol, price, size);
		}

		Pair <BigDecimal, BigDecimal> ps = sellMap.get(symbol);
		sellMap.put(symbol, Pair.of(ps.getFirst().add(price), ps.getSecond().add(size)));
	}

	public BigDecimal getAvgPrice(String symbol) {
		return avgPrices.get(symbol);
	}

	public BigDecimal getLastPrice(String symbol) {
		return lastPrices.get(symbol);
	}

	public BigDecimal getRemainingBudget() {
		return atomicBudget.get();
	}

	public BigDecimal getProfit() {
		BigDecimal prof = BigDecimal.ZERO;
		for (Map.Entry <String, BigDecimal> entry : avgPrices.entrySet()) {
			Pair <BigDecimal, BigDecimal> pairCostSize = buyMap.get(entry.getKey());
			BigDecimal price = entry.getValue();
			BigDecimal cost = pairCostSize.getFirst();
			BigDecimal size = pairCostSize.getSecond();
			BigDecimal coinProfit = price.subtract(cost).multiply(size);
			prof.add(coinProfit);
		}
		return prof;
	}

	@Override
	public void onNewTicker(String symbol, TickerResponse ticker) {
		operation.inform(symbol, ticker);
		lastPrices.put(symbol, ticker.getPrice());
	}
}
