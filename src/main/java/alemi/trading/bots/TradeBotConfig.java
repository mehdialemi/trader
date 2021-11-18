package alemi.trading.bots;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TradeBotConfig {
	private List<String> symbols;
	private BigDecimal budget;
	private BotOperation operation;
	private boolean simulate;
}
