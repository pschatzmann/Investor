package ch.pschatzmann.stocks.data.universe;

import java.util.List;
import java.util.stream.Collectors;
import ch.pschatzmann.stocks.IStockID;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.errors.UniverseException;

/**
 * Provides the list of currently active stocks from the account
 * 
 * @author pschatzmann
 *
 */
public class AccountUniverse implements IUniverse {
	private List<IStockID> result;

	public AccountUniverse(IAccount account) {
		result = account.getStockIDs().
				stream().
				filter(id -> account.getQuantity(id).longValue() > 0L)
				.collect(Collectors.toList());
	}

	@Override
	public List<IStockID> list()  {
		return result;
	}

}
