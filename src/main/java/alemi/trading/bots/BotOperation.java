package alemi.trading.bots;

import alemi.trading.coin.CoinData;

public interface BotOperation {

	void inform(String subject, CoinData data);
}
