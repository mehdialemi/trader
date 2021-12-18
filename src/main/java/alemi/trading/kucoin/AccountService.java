package alemi.trading.kucoin;

import com.kucoin.sdk.rest.interfaces.AccountAPI;
import com.kucoin.sdk.rest.response.AccountBalancesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class AccountService {

	@Autowired
	private KucoinConnector kucoinConnector;

	private AccountAPI api;

	@PostConstruct
	public void init() throws IOException {
	    api = kucoinConnector.getRest().accountAPI();
		printAccounts("trade");
		printAccounts("main");
	}

	public void printAccounts(String type) throws IOException {
		List <AccountBalancesResponse> accounts = api.listAccounts("", type);
		log.info("There are {} accounts for type {}", accounts.size(), type);
		for (AccountBalancesResponse account : accounts) {
			log.info("Account: {}", account.toString());
		}
	}
}
